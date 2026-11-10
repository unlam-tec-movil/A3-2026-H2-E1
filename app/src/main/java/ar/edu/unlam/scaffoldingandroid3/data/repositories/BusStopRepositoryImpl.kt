package ar.edu.unlam.scaffoldingandroid3.data.repositories

import android.util.Log
import ar.edu.unlam.scaffoldingandroid3.data.database.room.dao.BusStopCacheDao
import ar.edu.unlam.scaffoldingandroid3.data.database.room.dao.SearchHistoryDao
import ar.edu.unlam.scaffoldingandroid3.data.database.room.entities.SearchHistoryEntity
import ar.edu.unlam.scaffoldingandroid3.data.datasources.demo.DemoBusStopDataSource
import ar.edu.unlam.scaffoldingandroid3.data.datasources.remote.RemoteBusStopDataSource
import ar.edu.unlam.scaffoldingandroid3.data.mapper.BusStopMapper.toCacheEntity
import ar.edu.unlam.scaffoldingandroid3.data.mapper.BusStopMapper.toDomain
import ar.edu.unlam.scaffoldingandroid3.domain.enums.NearbyStopsOrigin
import ar.edu.unlam.scaffoldingandroid3.domain.model.BusStop
import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationData
import ar.edu.unlam.scaffoldingandroid3.domain.model.NearbyStopsResult
import ar.edu.unlam.scaffoldingandroid3.domain.model.SearchHistory
import ar.edu.unlam.scaffoldingandroid3.domain.model.distanceMetersTo
import ar.edu.unlam.scaffoldingandroid3.domain.repository.BusStopRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BusStopRepositoryImpl
    @Inject
    constructor(
        private val remoteDataSource: RemoteBusStopDataSource,
        private val busStopCacheDao: BusStopCacheDao,
        private val searchHistoryDao: SearchHistoryDao,
        private val demoDataSource: DemoBusStopDataSource,
    ) : BusStopRepository {
        override suspend fun getNearbyStops(
            center: LocationData,
            radiusMeters: Double,
        ): NearbyStopsResult {
            val remoteStops =
                try {
                    remoteDataSource.findByLocation(center, radiusMeters)
                } catch (e: Exception) {
                    Log.w(LOG_TAG, "Error al consultar las paradas remotas; se degrada la fuente", e)
                    null
                }

            if (!remoteStops.isNullOrEmpty()) {
                busStopCacheDao.clearCache()
                busStopCacheDao.insertStops(remoteStops.map { it.toCacheEntity() })
                return NearbyStopsResult(
                    stops = remoteStops.sortedByNearestTo(center),
                    origin = NearbyStopsOrigin.REMOTE,
                )
            }

            if (remoteStops == null) {
                val cachedStops = busStopCacheDao.getCachedStopsList().map { it.toDomain() }
                if (cachedStops.isNotEmpty()) {
                    return NearbyStopsResult(
                        stops = cachedStops.sortedByNearestTo(center),
                        origin = NearbyStopsOrigin.CACHE,
                    )
                }
            }

            return NearbyStopsResult(
                stops = demoDataSource.getDemoStops(center, radiusMeters),
                origin = NearbyStopsOrigin.DEMO,
            )
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
                entities.map { entity -> SearchHistory(id = entity.id, query = entity.query, timestamp = entity.timestamp) }
            }

        private fun List<BusStop>.sortedByNearestTo(center: LocationData): List<BusStop> =
            sortedBy { stop -> center.distanceMetersTo(stop.locationData) }

        private companion object {
            const val LOG_TAG = "BusStopRepository"
        }
    }
