package ar.edu.unlam.scaffoldingandroid3.data.datasources.remote

import ar.edu.unlam.scaffoldingandroid3.domain.model.BusStop
import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationData

interface RemoteBusStopDataSource {
    suspend fun findByLocation(
        center: LocationData,
        radiusMeters: Double,
    ): List<BusStop>
}
