package ar.edu.unlam.scaffoldingandroid3.domain.model

/**
 * Representa una ruta de autobús dentro de una línea de servicio.
 *
 * Una [BusRoute] define el trayecto completo de una línea de autobús, desde el punto
 * de partida hasta el destino final, incluyendo todas las paradas intermedias.
 *
 * @param routeId el identificador único de la ruta.
 * @param startingPoint la ubicación de origen de la ruta (punto de partida).
 * @param destination la ubicación de destino de la ruta.
 * @since 1.0
 */
data class BusRoute(
    val routeId: Long,
    val startingPoint: LocationData,
    val destination: LocationData,
)
