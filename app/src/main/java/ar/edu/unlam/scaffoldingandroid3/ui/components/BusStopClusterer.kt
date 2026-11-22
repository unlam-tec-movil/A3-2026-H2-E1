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

    val clusters = ArrayList<ClusterItem>()

    selectedStops.forEach { selected ->
        clusters.add(ClusterItem.Single(selected))
    }

    val visitedIndices = mutableSetOf<Int>()
    val screenPoints =
        unselectedStops.map { stop ->
            toScreenPixels(
                stop.locationData.latitude.value,
                stop.locationData.longitude.value,
            )
        }

    for (index in unselectedStops.indices) {
        if (visitedIndices.contains(index)) continue
        visitedIndices.add(index)

        val clusterMembers =
            ArrayList<BusStop>().apply {
                add(unselectedStops[index])
            }
        val currentPoint = screenPoints[index]

        for (otherIndex in index + 1 until unselectedStops.size) {
            if (visitedIndices.contains(otherIndex)) continue
            val otherPoint = screenPoints[otherIndex]
            val distancePixels = hypot(currentPoint.x - otherPoint.x, currentPoint.y - otherPoint.y)
            if (distancePixels <= radiusPixels) {
                visitedIndices.add(otherIndex)
                clusterMembers.add(unselectedStops[otherIndex])
            }
        }

        if (clusterMembers.size == 1) {
            clusters.add(ClusterItem.Single(clusterMembers.first()))
        } else {
            val avgLat = clusterMembers.map { it.locationData.latitude.value }.average()
            val avgLon = clusterMembers.map { it.locationData.longitude.value }.average()
            clusters.add(
                ClusterItem.Group(
                    stops = clusterMembers,
                    centerLat = avgLat,
                    centerLon = avgLon,
                ),
            )
        }
    }

    return clusters
}
