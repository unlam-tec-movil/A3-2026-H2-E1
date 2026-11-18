package ar.edu.unlam.scaffoldingandroid3.data.repositories

import ar.edu.unlam.scaffoldingandroid3.data.datasources.local.BusStopDataSource
import ar.edu.unlam.scaffoldingandroid3.domain.model.BusStop
import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationData
import ar.edu.unlam.scaffoldingandroid3.domain.repository.BusStopRepository
import javax.inject.Inject
class BusStopRepositoryImpl
    @Inject
    constructor(
        private val dataSource: BusStopDataSource,
    ) : BusStopRepository {
        override suspend fun getNearbyBusStopsByCurrentLocation(currentLocation: LocationData): List<BusStop> =
            dataSource.getBusStops(currentLocation)
    }
