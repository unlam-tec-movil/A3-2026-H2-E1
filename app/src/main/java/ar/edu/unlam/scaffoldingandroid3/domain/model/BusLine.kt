package ar.edu.unlam.scaffoldingandroid3.domain.model

/**
 * Representa una línea de colectivo en el sistema de transporte público.
 *
 * Un [BusLine] contiene información básica sobre la línea (identificador, número)
 * junto con sus rutas asociadas y su estado actual.
 *
 * @param lineId el identificador único de la línea de colectivo.
 * @param number el número visible de la línea de colectivo (ej: "10", "42", etc.).
 * @param routes la lista de rutas que componen esta línea de colectivo.
 * @param currentStatus el estado actual de la línea de colectivo (activa, suspendida, etc.).
 * @since 1.0
 */
data class BusLine(
    val lineId: Long,
    val number: Int,
    val routes: List<BusRoute>,
    val currentStatus: BusLineStatus,
)
