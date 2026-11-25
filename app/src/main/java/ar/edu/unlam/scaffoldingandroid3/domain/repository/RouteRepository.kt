package ar.edu.unlam.scaffoldingandroid3.domain.repository

import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationData
import ar.edu.unlam.scaffoldingandroid3.domain.model.PedestrianRoute

interface RouteRepository {
    suspend fun getPedestrianRoute(
        origin: LocationData,
        destination: LocationData,
    ): Result<PedestrianRoute>
}
