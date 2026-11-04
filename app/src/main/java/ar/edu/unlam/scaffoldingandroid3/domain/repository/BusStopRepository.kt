package ar.edu.unlam.mobile.movil22026h2.domain.repository

import ar.edu.unlam.mobile.movil22026h2.domain.model.BusStop
import ar.edu.unlam.mobile.movil22026h2.domain.model.LocationData

interface BusStopRepository {
    fun getNearbyBusStopsByCurrentLocation(currentLocation: LocationData): List<BusStop>
}
