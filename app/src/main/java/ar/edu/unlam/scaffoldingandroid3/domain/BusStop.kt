package ar.edu.unlam.scaffoldingandroid3.domain

/** Línea de colectivo que pasa por una parada. */
data class BusLine(
    val code: String,
    val destination: String,
)

/** Parada de colectivo con las líneas que pasan por ella. */
data class BusStop(
    val id: String,
    val name: String,
    val lines: List<BusLine>,
    val location: GeoPoint,
)
