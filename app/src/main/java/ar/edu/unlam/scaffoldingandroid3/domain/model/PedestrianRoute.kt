package ar.edu.unlam.scaffoldingandroid3.domain.model

data class PedestrianRoute(
    val points: List<LocationData>,
    val distanceMeters: Double = 0.0,
    val durationSeconds: Double = 0.0,
)
