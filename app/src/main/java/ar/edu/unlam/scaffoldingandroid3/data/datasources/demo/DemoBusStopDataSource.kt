package ar.edu.unlam.scaffoldingandroid3.data.datasources.demo

import ar.edu.unlam.scaffoldingandroid3.domain.model.BusStop
import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationData

interface DemoBusStopDataSource {
    suspend fun getDemoStops(
        center: LocationData,
        radiusMeters: Double,
    ): List<BusStop>
}
