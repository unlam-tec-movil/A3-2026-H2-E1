package ar.edu.unlam.mobile.movil22026h2.domain.repository

import ar.edu.unlam.mobile.movil22026h2.domain.model.LocationData

interface LocationRepository {
    suspend fun getLocationData(): LocationData
}
