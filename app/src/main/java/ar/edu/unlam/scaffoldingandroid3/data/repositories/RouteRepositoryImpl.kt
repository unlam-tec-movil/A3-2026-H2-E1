package ar.edu.unlam.scaffoldingandroid3.data.repositories

import ar.edu.unlam.scaffoldingandroid3.data.datasources.remote.OsrmRouteDataSource
import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationData
import ar.edu.unlam.scaffoldingandroid3.domain.model.PedestrianRoute
import ar.edu.unlam.scaffoldingandroid3.domain.repository.RouteRepository
import javax.inject.Inject

class RouteRepositoryImpl
    @Inject
    constructor(
        private val routeDataSource: OsrmRouteDataSource,
    ) : RouteRepository {
        override suspend fun getPedestrianRoute(
            origin: LocationData,
            destination: LocationData,
        ): Result<PedestrianRoute> =
            runCatching {
                routeDataSource.getPedestrianRoute(origin, destination)
            }
    }
