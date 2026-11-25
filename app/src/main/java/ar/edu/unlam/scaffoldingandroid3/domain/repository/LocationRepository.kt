package ar.edu.unlam.scaffoldingandroid3.domain.repository

import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationData
import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    suspend fun getLocationData(): LocationData?

    fun getLocationUpdates(
        intervalMillis: Long = 3_000L,
        minDistanceMeters: Float = 3f,
    ): Flow<LocationData>

    suspend fun isLocationEnabled(): Boolean
}
