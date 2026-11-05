package ar.edu.unlam.scaffoldingandroid3.domain.repository

import ar.edu.unlam.scaffoldingandroid3.domain.model.BusStop
import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationData

interface BusStopRepository {
    suspend fun getNearbyBusStopsByCurrentLocation(currentLocation: LocationData): List<BusStop>
}
