package ar.edu.unlam.scaffoldingandroid3.data

import android.util.Log
import ar.edu.unlam.scaffoldingandroid3.data.database.MongoDataSource
import ar.edu.unlam.scaffoldingandroid3.data.database.room.dao.BusStopCacheDao
import ar.edu.unlam.scaffoldingandroid3.data.database.room.dao.SearchHistoryDao
import ar.edu.unlam.scaffoldingandroid3.data.database.room.entities.SearchHistoryEntity
import ar.edu.unlam.scaffoldingandroid3.data.mapper.BusStopMapper.fromBsonDocument
import ar.edu.unlam.scaffoldingandroid3.data.mapper.BusStopMapper.toCacheEntity
import ar.edu.unlam.scaffoldingandroid3.data.mapper.BusStopMapper.toDomain
import ar.edu.unlam.scaffoldingandroid3.domain.BusStop
import ar.edu.unlam.scaffoldingandroid3.domain.BusStopRepository
import ar.edu.unlam.scaffoldingandroid3.domain.GeoPoint
import ar.edu.unlam.scaffoldingandroid3.domain.SearchHistory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.bson.Document
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BusStopRepositoryImpl
    @Inject
    constructor(
        private val busStopCacheDao: BusStopCacheDao,
        private val searchHistoryDao: SearchHistoryDao,
    ) : BusStopRepository {
        private val database = MongoDataSource.getDatabase("voy_en_bondi")
        private val collection = database.getCollection<Document>("paradas")

        override suspend fun getNearbyStops(
            center: GeoPoint,
            radiusMeters: Double,
        ): List<BusStop> =
            try {
                val query =
                    Document(
                        "geometry",
                        Document(
                            "\$nearSphere",
                            Document(
                                "\$geometry",
                                Document("type", "Point").append("coordinates", listOf(center.longitude, center.latitude)),
                            ).append("\$maxDistance", radiusMeters),
                        ),
                    )

                val documents = collection.find(query).toList()
                val stops =
                    documents
                        .map { doc -> fromBsonDocument(doc) }
                        .sortedBy { stop -> center.distanceTo(stop.location) }

                if (stops.isNotEmpty()) {
                    busStopCacheDao.clearCache()
                    busStopCacheDao.insertStops(stops.map { it.toCacheEntity() })
                }

                stops
            } catch (e: Exception) {
                val cachedEntities = busStopCacheDao.getCachedStopsList()
                Log.e("MONGO_DEBUG", "Error conectando con MongoDB", e)
                if (cachedEntities.isNotEmpty()) {
                    Log.e("MONGO_DEBUG", "Error conectando con MongoDB", e)
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
