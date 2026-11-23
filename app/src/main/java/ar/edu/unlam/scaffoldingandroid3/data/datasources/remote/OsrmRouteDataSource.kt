package ar.edu.unlam.scaffoldingandroid3.data.datasources.remote

import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationData
import ar.edu.unlam.scaffoldingandroid3.domain.model.PedestrianRoute

interface OsrmRouteDataSource {
    suspend fun getPedestrianRoute(
        origin: LocationData,
        destination: LocationData,
    ): PedestrianRoute
}
