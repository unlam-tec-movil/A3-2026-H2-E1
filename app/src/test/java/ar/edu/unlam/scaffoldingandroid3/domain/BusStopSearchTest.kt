package ar.edu.unlam.scaffoldingandroid3.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BusStopSearchTest {
    private val stops =
        listOf(
            BusStop(
                id = "1",
                name = "Av. Perón y Arieta",
                lines = listOf(BusLine(code = "96", destination = "Plaza Once")),
                location = GeoPoint(latitude = -34.6676, longitude = -58.5606),
            ),
            BusStop(
                id = "2",
                name = "Av. Illia y Tucumán",
                lines = listOf(BusLine(code = "306", destination = "Morón")),
                location = GeoPoint(latitude = -34.6686, longitude = -58.5606),
            ),
            BusStop(
                id = "3",
                name = "Parada Lejana",
                lines = listOf(BusLine(code = "100", destination = "Retiro")),
                location = GeoPoint(latitude = -34.6037, longitude = -58.3816), // Obelisco (~17 km)
            ),
        )

    private val searchRadiusMeters = 1_000.0

    @Test
    fun empty_query_returns_every_stop() {
        assertEquals(stops, filterBusStops(stops, query = "  "))
    }

    @Test
    fun query_matches_stop_name() {
        val result = filterBusStops(stops, query = "perón")

        assertEquals(listOf("1"), result.map { it.id })
    }

    @Test
    fun query_matches_line_code() {
        val result = filterBusStops(stops, query = "306")

        assertEquals(listOf("2"), result.map { it.id })
    }

    @Test
    fun query_without_matches_returns_empty_list() {
        assertEquals(emptyList<BusStop>(), filterBusStops(stops, query = "línea 999"))
    }

    @Test
    fun nearby_search_returns_stops_within_radius() {
        // Coordenada exacta cercana a las paradas 1 y 2
        val userLocation = GeoPoint(latitude = -34.6680, longitude = -58.5606)

        val nearby = stops.filter { stop ->
            userLocation.distanceTo(stop.location) <= searchRadiusMeters
        }

        assertEquals(listOf("1", "2"), nearby.map { it.id })
        assertFalse(nearby.any { it.id == "3" })
    }

    @Test
    fun approximate_location_calculates_stops_from_estimated_coordinates() {
        // Coordenada con leve desplazamiento (~200m)
        val approximateLocation = GeoPoint(latitude = -34.6690, longitude = -58.5610)

        val nearby = stops.filter { stop ->
            approximateLocation.distanceTo(stop.location) <= searchRadiusMeters
        }

        assertTrue(nearby.isNotEmpty())
        assertTrue(nearby.any { it.id == "1" || it.id == "2" })
    }

    @Test
    fun manual_exploration_calculates_stops_from_default_location() {
        // Coordenada de exploración manual (San Justo)
        val defaultManualLocation = GeoPoint(latitude = -34.6676, longitude = -58.5606)

        val manualStops = stops.filter { stop ->
            defaultManualLocation.distanceTo(stop.location) <= searchRadiusMeters
        }

        assertTrue("Debe encontrar paradas al explorar manualmente en la coordenada base", manualStops.isNotEmpty())
    }
}