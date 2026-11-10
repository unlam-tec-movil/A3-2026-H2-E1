package ar.edu.unlam.scaffoldingandroid3.data.datasources.local

import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationData

interface LocationDataSource {
    suspend fun getLastKnowLocation(): LocationData?

    suspend fun isLocationEnabled(): Boolean
}
