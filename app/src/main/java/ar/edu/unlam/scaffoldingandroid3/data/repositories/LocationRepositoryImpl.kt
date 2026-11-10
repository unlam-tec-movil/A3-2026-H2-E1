package ar.edu.unlam.scaffoldingandroid3.data.repositories

import ar.edu.unlam.scaffoldingandroid3.data.datasources.local.LocationDataSource
import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationData
import ar.edu.unlam.scaffoldingandroid3.domain.repository.LocationRepository
import javax.inject.Inject

class LocationRepositoryImpl
    @Inject
    constructor(
        private val dataSource: LocationDataSource,
    ) : LocationRepository {
        override suspend fun getLocationData(): LocationData? = dataSource.getLastKnowLocation()

        override suspend fun isLocationEnabled(): Boolean = dataSource.isLocationEnabled()
    }
