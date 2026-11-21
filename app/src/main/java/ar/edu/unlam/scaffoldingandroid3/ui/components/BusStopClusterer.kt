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
 * Las paradas seleccionadas (`selectedStopId`) se eximen del agrupamiento para ser
 * representadas siempre como marcadores individuales.
 *
 * @param stops Lista de paradas a evaluar.
 * @param toScreenPixels Función que proyecta coordenadas geográficas (lat, lon) a [ScreenPoint] en píxeles.
 * @param radiusPixels Radio máximo de proximidad en píxeles para agrupar paradas en un mismo cluster.
 * @param selectedStopId ID opcional de la parada seleccionada a mantener desacoplada del cluster.
 * @return Lista de [ClusterItem] conteniendo elementos individuales o agrupados.
 */
fun clusterBusStops(
    stops: List<BusStop>,
    toScreenPixels: (Double, Double) -> ScreenPoint,
    radiusPixels: Float = 70f,
    selectedStopId: Long? = null,
): List<ClusterItem> {
    if (stops.isEmpty()) return emptyList()

    val (selectedStops, unselectedStops) =
        if (selectedStopId != null) {
            stops.partition { it.stopId == selectedStopId }
        } else {
            Pair(emptyList(), stops)
        }

    val result = mutableListOf<ClusterItem>()

    selectedStops.forEach { selected ->
        result.add(ClusterItem.Single(selected))
    }

    if (unselectedStops.isEmpty()) return result

    val visited = BooleanArray(unselectedStops.size)
    val screenPoints =
        unselectedStops.map { stop ->
            toScreenPixels(
                stop.locationData.latitude.value,
                stop.locationData.longitude.value,
            )
        }

    for (i in unselectedStops.indices) {
        if (visited[i]) continue
        visited[i] = true

        val clusterMembers = mutableListOf(unselectedStops[i])
        val p1 = screenPoints[i]

        for (j in i + 1 until unselectedStops.size) {
            if (visited[j]) continue
            val p2 = screenPoints[j]
            val distance = hypot(p1.x - p2.x, p1.y - p2.y)
            if (distance <= radiusPixels) {
                visited[j] = true
                clusterMembers.add(unselectedStops[j])
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
