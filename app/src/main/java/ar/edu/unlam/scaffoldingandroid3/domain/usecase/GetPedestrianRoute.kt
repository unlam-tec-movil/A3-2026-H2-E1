package ar.edu.unlam.scaffoldingandroid3.domain.usecase

import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationData
import ar.edu.unlam.scaffoldingandroid3.domain.model.PedestrianRoute
import ar.edu.unlam.scaffoldingandroid3.domain.repository.RouteRepository
import javax.inject.Inject

class GetPedestrianRoute
    @Inject
    constructor(
        private val routeRepository: RouteRepository,
    ) {
        suspend operator fun invoke(
            origin: LocationData,
            destination: LocationData,
        ): Result<PedestrianRoute> = routeRepository.getPedestrianRoute(origin, destination)
    }
