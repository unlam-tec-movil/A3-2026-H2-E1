package ar.edu.unlam.scaffoldingandroid3.domain.usecase

import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationData
import ar.edu.unlam.scaffoldingandroid3.domain.repository.LocationRepository
import javax.inject.Inject

class ExploreMapManually
    @Inject
    constructor(
        private val locationRepository: LocationRepository,
    ) {
        suspend operator fun invoke(): LocationData = locationRepository.getLocationData()
    }
