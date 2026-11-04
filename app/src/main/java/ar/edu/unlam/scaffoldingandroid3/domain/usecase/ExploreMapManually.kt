package ar.edu.unlam.mobile.movil22026h2.domain.usecase

import ar.edu.unlam.mobile.movil22026h2.domain.model.LocationData
import ar.edu.unlam.mobile.movil22026h2.domain.repository.LocationRepository
import javax.inject.Inject

class ExploreMapManually
    @Inject
    constructor(
        private val locationRepository: LocationRepository,
    ) {
        suspend operator fun invoke(): LocationData = locationRepository.getLocationData()
    }
