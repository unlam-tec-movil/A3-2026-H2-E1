package ar.edu.unlam.scaffoldingandroid3.ui.navigation

import kotlinx.serialization.Serializable

// Por cada Screen que va a tener comportamiento de navegación se crea un serializable
@Serializable
object Home

@Serializable
object FavoriteBusStops

@Serializable
object NearbyStops

// H02: brújula hacia la parada elegida.
@Serializable
data class Compass(
    val stopName: String,
    val latitude: Double,
    val longitude: Double,
)
