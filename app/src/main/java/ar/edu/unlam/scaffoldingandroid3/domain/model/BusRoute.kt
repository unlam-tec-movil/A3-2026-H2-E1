package ar.edu.unlam.scaffoldingandroid3.domain.model

data class BusRoute(
    val routeId: Long,
    val startingPoint: LocationData,
    val destination: LocationData,
)
