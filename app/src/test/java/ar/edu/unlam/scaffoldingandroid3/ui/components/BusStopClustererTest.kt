package ar.edu.unlam.scaffoldingandroid3.ui.components

import ar.edu.unlam.scaffoldingandroid3.domain.model.BusStop
import ar.edu.unlam.scaffoldingandroid3.domain.model.Latitude
import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationData
import ar.edu.unlam.scaffoldingandroid3.domain.model.Longitude
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName

class BusStopClustererTest {
    private fun createStop(
        id: Long,
        lat: Double,
        lon: Double,
        name: String = "Parada $id",
    ): BusStop =
        BusStop(
            stopId = id,
            name = name,
            locationData =
                LocationData(
                    latitude = Latitude(lat),
                    longitude = Longitude(lon),
                ),
            markedAsFavorite = false,
            busLines = emptyList(),
        )

    @Test
    @DisplayName("Debería retornar lista vacía cuando no hay paradas")
    fun shouldReturnEmptyWhenStopsListIsEmpty() {
        val result =
            clusterBusStops(
                stops = emptyList(),
                toScreenPixels = { lat, lon -> ScreenPoint(lat.toFloat(), lon.toFloat()) },
                radiusPixels = 50f,
            )

        assertTrue(result.isEmpty())
    }

    @Test
    @DisplayName("Debería retornar ClusterItem.Single cuando hay una única parada")
    fun shouldReturnSingleWhenOnlyOneStopExists() {
        val stop = createStop(1L, -34.6700, -58.5600)

        val result =
            clusterBusStops(
                stops = listOf(stop),
                toScreenPixels = { _, _ -> ScreenPoint(100f, 100f) },
                radiusPixels = 50f,
            )

        assertEquals(1, result.size)
        val single = result.first() as ClusterItem.Single
        assertEquals(stop.stopId, single.stop.stopId)
        assertEquals(1, single.stops.size)
    }

    @Test
    @DisplayName("Debería retornar múltiples ClusterItem.Single cuando las paradas están lejanas en pantalla")
    fun shouldReturnMultipleSinglesWhenStopsAreDistant() {
        val stop1 = createStop(1L, -34.6700, -58.5600)
        val stop2 = createStop(2L, -34.6800, -58.5700)

        val result =
            clusterBusStops(
                stops = listOf(stop1, stop2),
                toScreenPixels = { lat, _ ->
                    if (lat == stop1.locationData.latitude.value) {
                        ScreenPoint(100f, 100f)
                    } else {
                        ScreenPoint(300f, 300f)
                    }
                },
                radiusPixels = 50f,
            )

        assertEquals(2, result.size)
        assertTrue(result.all { it is ClusterItem.Single })
    }

    @Test
    @DisplayName("Debería agrupar paradas en ClusterItem.Group cuando están dentro del radio")
    fun shouldGroupStopsIntoClusterWhenWithinRadius() {
        val stop1 = createStop(1L, -34.6700, -58.5600)
        val stop2 = createStop(2L, -34.6702, -58.5602)
        val stop3 = createStop(3L, -34.6704, -58.5604)

        val result =
            clusterBusStops(
                stops = listOf(stop1, stop2, stop3),
                toScreenPixels = { lat, _ ->
                    when (lat) {
                        stop1.locationData.latitude.value -> ScreenPoint(100f, 100f)
                        stop2.locationData.latitude.value -> ScreenPoint(105f, 105f)
                        else -> ScreenPoint(110f, 110f)
                    }
                },
                radiusPixels = 50f,
            )

        assertEquals(1, result.size)
        val group = result.first() as ClusterItem.Group
        assertEquals(3, group.stops.size)
        assertEquals((-34.6700 + -34.6702 + -34.6704) / 3, group.centerLat, 0.0001)
        assertEquals((-58.5600 + -58.5602 + -58.5604) / 3, group.centerLon, 0.0001)
    }

    @Test
    @DisplayName("Debería agrupar paradas cercanas y mantener parada lejana como Single")
    fun shouldHandleMixedClusteredAndSingleStops() {
        val stop1 = createStop(1L, -34.6700, -58.5600)
        val stop2 = createStop(2L, -34.6701, -58.5601)
        val stop3 = createStop(3L, -34.6900, -58.5900)

        val result =
            clusterBusStops(
                stops = listOf(stop1, stop2, stop3),
                toScreenPixels = { lat, _ ->
                    when (lat) {
                        stop1.locationData.latitude.value -> ScreenPoint(100f, 100f)
                        stop2.locationData.latitude.value -> ScreenPoint(110f, 100f)
                        else -> ScreenPoint(500f, 500f)
                    }
                },
                radiusPixels = 50f,
            )

        assertEquals(2, result.size)
        val groups = result.filterIsInstance<ClusterItem.Group>()
        val singles = result.filterIsInstance<ClusterItem.Single>()

        assertEquals(1, groups.size)
        assertEquals(2, groups.first().stops.size)
        assertEquals(1, singles.size)
        assertEquals(stop3.stopId, singles.first().stop.stopId)
    }

    @Test
    @DisplayName("Debería mantener la parada seleccionada como Single independientemente de la cercanía")
    fun shouldKeepSelectedStopAsSingleEvenIfCloseToOthers() {
        val stop1 = createStop(1L, -34.6700, -58.5600)
        val stop2 = createStop(2L, -34.6701, -58.5601)

        val result =
            clusterBusStops(
                stops = listOf(stop1, stop2),
                toScreenPixels = { _, _ -> ScreenPoint(100f, 100f) },
                radiusPixels = 50f,
                selectedStopId = 1L,
            )

        val singles = result.filterIsInstance<ClusterItem.Single>()
        assertTrue(singles.any { it.stop.stopId == 1L })
    }
}
