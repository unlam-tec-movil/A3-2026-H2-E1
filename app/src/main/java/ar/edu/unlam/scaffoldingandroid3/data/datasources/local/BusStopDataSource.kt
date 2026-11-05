package ar.edu.unlam.scaffoldingandroid3.data.datasources.local

import ar.edu.unlam.scaffoldingandroid3.domain.model.BusStop
import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationData

interface BusStopDataSource {
    suspend fun getBusStops(location: LocationData): List<BusStop>
}
