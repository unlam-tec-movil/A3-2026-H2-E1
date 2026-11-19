package ar.edu.unlam.scaffoldingandroid3.domain.usecase

import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationData
import ar.edu.unlam.scaffoldingandroid3.domain.model.distanceMetersTo
import javax.inject.Inject

class CalculateDistanceToStop
    @Inject
    constructor() {
        operator fun invoke(
            startingPoint: LocationData,
            destination: LocationData,
        ): Double = startingPoint.distanceMetersTo(destination)
    }
