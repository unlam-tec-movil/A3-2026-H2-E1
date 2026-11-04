package ar.edu.unlam.scaffoldingandroid3.domain.usecase

import ar.edu.unlam.mobile.movil22026h2.domain.model.BusStop
import ar.edu.unlam.mobile.movil22026h2.domain.model.LocationData
import ar.edu.unlam.mobile.movil22026h2.domain.repository.BusStopRepository
import javax.inject.Inject

class GetNearbyStops
    @Inject
    constructor(
        private val busStopRepository: BusStopRepository,
    ) {
        operator fun invoke(currentLocation: LocationData): List<BusStop> =
            busStopRepository.getNearbyBusStopsByCurrentLocation(currentLocation)
    }
