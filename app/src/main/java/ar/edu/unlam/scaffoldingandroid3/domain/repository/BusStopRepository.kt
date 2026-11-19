package ar.edu.unlam.scaffoldingandroid3.domain.repository

import ar.edu.unlam.scaffoldingandroid3.domain.model.BusStop
import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationData
import ar.edu.unlam.scaffoldingandroid3.domain.model.NearbyStopsResult
import ar.edu.unlam.scaffoldingandroid3.domain.model.SearchHistory
import kotlinx.coroutines.flow.Flow

interface BusStopRepository {
    suspend fun getNearbyStops(
        center: LocationData,
        radiusMeters: Double = DEFAULT_RADIUS_METERS,
    ): NearbyStopsResult

    fun getCachedStops(): Flow<List<BusStop>>

    suspend fun saveSearchQuery(query: String)

    fun getRecentSearches(): Flow<List<SearchHistory>>
}

private const val DEFAULT_RADIUS_METERS = 1_000.0
