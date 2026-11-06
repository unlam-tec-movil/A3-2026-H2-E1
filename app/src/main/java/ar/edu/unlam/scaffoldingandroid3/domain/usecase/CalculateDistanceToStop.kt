package ar.edu.unlam.scaffoldingandroid3.domain.usecase

import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationData
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class CalculateDistanceToStop {
    operator fun invoke(
        startingPoint: LocationData,
        destination: LocationData,
    ): Double = calculateDistanceByHaversineAlgorithm(startingPoint, destination)

    private fun calculateDistanceByHaversineAlgorithm(
        startingPoint: LocationData,
        destination: LocationData,
    ): Double {
        val lat1 = Math.toRadians(startingPoint.latitude.value)
        val lat2 = Math.toRadians(destination.latitude.value)
        val deltaLat = Math.toRadians(destination.latitude.value - startingPoint.latitude.value)
        val deltaLon = Math.toRadians(destination.longitude.value - startingPoint.longitude.value)

        val a =
            sin(deltaLat / 2) * sin(deltaLat / 2) +
                cos(lat1) * cos(lat2) *
                sin(deltaLon / 2) * sin(deltaLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return 6371.0 * c
    }
}
