package ar.edu.unlam.scaffoldingandroid3.domain.repository

import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationData

interface LocationRepository {
    suspend fun getLocationData(): LocationData

    suspend fun isLocationEnabled(): Boolean
}
