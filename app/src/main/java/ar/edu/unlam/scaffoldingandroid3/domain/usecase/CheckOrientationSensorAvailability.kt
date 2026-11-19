package ar.edu.unlam.scaffoldingandroid3.domain.usecase

import ar.edu.unlam.scaffoldingandroid3.domain.repository.SensorRepository
import javax.inject.Inject

class CheckOrientationSensorAvailability
    @Inject
    constructor(
        private val sensorRepository: SensorRepository,
    ) {
        operator fun invoke(): Boolean = sensorRepository.isOrientationSensorAvailable()
    }
