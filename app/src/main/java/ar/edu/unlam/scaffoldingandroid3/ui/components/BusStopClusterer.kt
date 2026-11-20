package ar.edu.unlam.scaffoldingandroid3.ui.components

import ar.edu.unlam.scaffoldingandroid3.domain.model.BusStop
import kotlin.math.hypot

/**
 * Coordenadas bidimensionales de pantalla en píxeles.
 */
data class ScreenPoint(
    val x: Float,
    val y: Float,
)

/**
 * Elemento resultante del agrupamiento de marcadores de paradas.
 */
sealed interface ClusterItem {
    val stops: List<BusStop>

    data class Single(
        val stop: BusStop,
    ) : ClusterItem {
        override val stops: List<BusStop> get() = listOf(stop)
    }

    data class Group(
        override val stops: List<BusStop>,
        val centerLat: Double,
        val centerLon: Double,
    ) : ClusterItem
}

/**
 * Agrupa una lista de paradas por cercanía en coordenadas proyectadas de pantalla.
 *
 * @param stops Lista de paradas a evaluar.
 * @param toScreenPixels Función que proyecta coordenadas geográficas (lat, lon) a [ScreenPoint] en píxeles.
 * @param radiusPixels Radio máximo de proximidad en píxeles para agrupar paradas en un mismo cluster.
 * @return Lista de [ClusterItem] conteniendo elementos individuales o agrupados.
 */
fun clusterBusStops(
    stops: List<BusStop>,
    toScreenPixels: (Double, Double) -> ScreenPoint,
    radiusPixels: Float = 70f,
): List<ClusterItem> {
    if (stops.isEmpty()) return emptyList()

    val visited = BooleanArray(stops.size)
    val screenPoints =
        stops.map { stop ->
            toScreenPixels(
                stop.locationData.latitude.value,
                stop.locationData.longitude.value,
            )
        }

    val result = mutableListOf<ClusterItem>()

    for (i in stops.indices) {
        if (visited[i]) continue
        visited[i] = true

        val clusterMembers = mutableListOf(stops[i])
        val p1 = screenPoints[i]

        for (j in i + 1 until stops.size) {
            if (visited[j]) continue
            val p2 = screenPoints[j]
            val distance = hypot(p1.x - p2.x, p1.y - p2.y)
            if (distance <= radiusPixels) {
                visited[j] = true
                clusterMembers.add(stops[j])
            }
        }

        if (clusterMembers.size == 1) {
            result.add(ClusterItem.Single(clusterMembers.first()))
        } else {
            val avgLat = clusterMembers.map { it.locationData.latitude.value }.average()
            val avgLon = clusterMembers.map { it.locationData.longitude.value }.average()
            result.add(
                ClusterItem.Group(
                    stops = clusterMembers,
                    centerLat = avgLat,
                    centerLon = avgLon,
                ),
            )
        }
    }

    return result
}
