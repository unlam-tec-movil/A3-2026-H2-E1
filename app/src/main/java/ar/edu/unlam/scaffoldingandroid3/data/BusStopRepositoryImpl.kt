package ar.edu.unlam.scaffoldingandroid3.data

import android.util.Log
import ar.edu.unlam.scaffoldingandroid3.data.database.room.dao.BusStopCacheDao
import ar.edu.unlam.scaffoldingandroid3.data.database.room.dao.SearchHistoryDao
import ar.edu.unlam.scaffoldingandroid3.data.database.room.entities.SearchHistoryEntity
import ar.edu.unlam.scaffoldingandroid3.data.mapper.BusStopMapper.toCacheEntity
import ar.edu.unlam.scaffoldingandroid3.data.mapper.BusStopMapper.toDomain
import ar.edu.unlam.scaffoldingandroid3.data.mapper.FirestoreBusStopMapper.toDomain
import ar.edu.unlam.scaffoldingandroid3.data.model.FirestoreBusStop
import ar.edu.unlam.scaffoldingandroid3.domain.BusStop
import ar.edu.unlam.scaffoldingandroid3.domain.BusStopRepository
import ar.edu.unlam.scaffoldingandroid3.domain.GeoPoint
import ar.edu.unlam.scaffoldingandroid3.domain.SearchHistory
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BusStopRepositoryImpl
    @Inject
    constructor(
        private val firestore: FirebaseFirestore,
        private val busStopCacheDao: BusStopCacheDao,
        private val searchHistoryDao: SearchHistoryDao,
    ) : BusStopRepository {
        override suspend fun getNearbyStops(
            center: GeoPoint,
            radiusMeters: Double,
        ): List<BusStop> =
            try {
                val centerGeo = GeoLocation(center.latitude, center.longitude)
                val bounds = GeoFireUtils.getGeoHashQueryBounds(centerGeo, radiusMeters)

                val matchingStops =
                    coroutineScope {
                        bounds
                            .map { bound ->
                                async {
                                    val snap =
                                        firestore
                                            .collection("gtfs_stops")
                                            .orderBy("geohash")
                                            .startAt(bound.startHash)
                                            .endAt(bound.endHash)
                                            .get()
                                            .await()

                                    snap.documents.mapNotNull { doc ->
                                        val firestoreStop = doc.toObject(FirestoreBusStop::class.java)
                                        firestoreStop?.toDomain(doc.id)
                                    }
                                }
                            }.awaitAll()
                            .flatten()
                    }

                val validStops =
                    matchingStops
                        .distinctBy { it.id }
                        .filter { center.distanceTo(it.location) <= radiusMeters }
                        .sortedBy { center.distanceTo(it.location) }

                busStopCacheDao.clearCache()
                if (validStops.isNotEmpty()) {
                    busStopCacheDao.insertStops(validStops.map { it.toCacheEntity() })
                }

                validStops
            } catch (e: Exception) {
                if (e is CancellationException) {
                    throw e
                }
                Log.e("FIRESTORE_DEBUG", "Error consultando paradas en Firestore", e)
                val cachedEntities = busStopCacheDao.getCachedStopsList()
                if (cachedEntities.isNotEmpty()) {
                    cachedEntities
                        .map { it.toDomain() }
                        .sortedBy { center.distanceTo(it.location) }
                } else {
                    throw e
                }
            }

        override fun getCachedStops(): Flow<List<BusStop>> =
            busStopCacheDao.getAllCachedStops().map { entities ->
                entities.map { it.toDomain() }
            }

        override suspend fun saveSearchQuery(query: String) {
            val trimmed = query.trim()
            if (trimmed.isNotBlank()) {
                searchHistoryDao.deleteSearch(trimmed)
                searchHistoryDao.insertSearch(SearchHistoryEntity(query = trimmed))
            }
        }

        override fun getRecentSearches(): Flow<List<SearchHistory>> =
            searchHistoryDao.getRecentSearches().map { entities ->
                entities.map { SearchHistory(id = it.id, query = it.query, timestamp = it.timestamp) }
            }
    }
