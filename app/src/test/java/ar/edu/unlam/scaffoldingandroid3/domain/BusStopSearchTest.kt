package ar.edu.unlam.scaffoldingandroid3.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class BusStopSearchTest {
    private val sampleStops =
        listOf(
            BusStop(
                id = "1",
                name = "Av. Perón y Arieta",
                lines = listOf(BusLine("96", "Plaza Once"), BusLine("185", "Liniers")),
                location = GeoPoint(-34.67, -58.56),
            ),
            BusStop(
                id = "2",
                name = "Av. Perón y Salguero",
                lines = listOf(BusLine("205", "La Matanza"), BusLine("242", "San Justo")),
                location = GeoPoint(-34.68, -58.57),
            ),
            BusStop(
                id = "3",
                name = "Almafuerte y Arieta",
                lines = listOf(BusLine("406", "Ramos Mejía")),
                location = GeoPoint(-34.69, -58.58),
            ),
        )

    @Test
    fun filterBusStops_emptyQuery_returnsAllStops() {
        val result = filterBusStops(sampleStops, "")
        assertEquals(3, result.size)
    }

    @Test
    fun filterBusStops_filterByName_returnsMatchingStops() {
        val result = filterBusStops(sampleStops, "Almafuerte")
        assertEquals(1, result.size)
        assertEquals("Almafuerte y Arieta", result[0].name)
    }

    @Test
    fun filterBusStops_filterByLineCode_returnsMatchingStops() {
        val result = filterBusStops(sampleStops, "205")
        assertEquals(1, result.size)
        assertEquals("Av. Perón y Salguero", result[0].name)
    }

    @Test
    fun filterBusStops_filterByDestination_returnsMatchingStops() {
        val result = filterBusStops(sampleStops, "Liniers")
        assertEquals(1, result.size)
        assertEquals("Av. Perón y Arieta", result[0].name)
    }
}
