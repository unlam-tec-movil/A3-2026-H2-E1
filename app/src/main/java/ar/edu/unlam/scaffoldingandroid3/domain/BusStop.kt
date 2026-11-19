package ar.edu.unlam.scaffoldingandroid3.domain

import kotlinx.serialization.Serializable

/** Línea de colectivo que pasa por una parada. */
@Serializable
data class BusLine(
    val code: String,
    val destination: String,
)

/** Parada de colectivo con las líneas que pasan por ella. */
@Serializable
data class BusStop(
    val id: String,
    val name: String,
    val lines: List<BusLine>,
    val location: GeoPoint,
)
