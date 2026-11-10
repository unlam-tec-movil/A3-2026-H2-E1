package ar.edu.unlam.scaffoldingandroid3.domain.usecase

import ar.edu.unlam.scaffoldingandroid3.domain.model.Bearing
import ar.edu.unlam.scaffoldingandroid3.domain.model.BearingData
import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationData
import ar.edu.unlam.scaffoldingandroid3.domain.model.bearingDegreesTo
import javax.inject.Inject

class CalculateDestinationDirection
    @Inject
    constructor() {
        operator fun invoke(
            userLocation: LocationData,
            busStopLocation: LocationData,
        ): BearingData = BearingData(Bearing(userLocation.bearingDegreesTo(busStopLocation)))
    }
