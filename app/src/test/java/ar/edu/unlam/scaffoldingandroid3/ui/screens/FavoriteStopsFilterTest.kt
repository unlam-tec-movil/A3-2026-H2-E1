package ar.edu.unlam.scaffoldingandroid3.ui.screens

import ar.edu.unlam.scaffoldingandroid3.domain.model.FavoriteStopSnapshot
import org.junit.Assert.assertEquals
import org.junit.Test

class FavoriteStopsFilterTest {
    private val stops =
        listOf(
            FavoriteStopSnapshot(
                stopId = 1L,
                name = "Zapiola y Rivadavia",
                latitude = -34.65,
                longitude = -58.62,
                lines = "Línea 136",
            ),
            FavoriteStopSnapshot(
                stopId = 2L,
                name = "Arieta y San Martín",
                latitude = -34.64,
                longitude = -58.61,
                lines = "Línea 96",
            ),
            FavoriteStopSnapshot(
                stopId = 3L,
                name = "Belgrano y Mitre",
                latitude = -34.63,
                longitude = -58.60,
                lines = "Línea 182",
            ),
        )

    @Test
    fun `favorites are sorted alphabetically`() {
        val result = filterAndSortFavoriteStops(stops, "")

        assertEquals(
            listOf(
                "Arieta y San Martín",
                "Belgrano y Mitre",
                "Zapiola y Rivadavia",
            ),
            result.map { it.name },
        )
    }

    @Test
    fun `favorites can be filtered by stop name`() {
        val result = filterAndSortFavoriteStops(stops, "Arieta")

        assertEquals(1, result.size)
        assertEquals(2L, result.first().stopId)
    }

    @Test
    fun `favorites can be filtered by bus line`() {
        val result = filterAndSortFavoriteStops(stops, "136")

        assertEquals(1, result.size)
        assertEquals(1L, result.first().stopId)
    }

    @Test
    fun `search ignores uppercase and lowercase`() {
        val result = filterAndSortFavoriteStops(stops, "BELGRANO")

        assertEquals(1, result.size)
        assertEquals(3L, result.first().stopId)
    }

    @Test
    fun `search ignores surrounding spaces`() {
        val result = filterAndSortFavoriteStops(stops, " 96 ")

        assertEquals(1, result.size)
        assertEquals(2L, result.first().stopId)
    }

    @Test
    fun `search returns empty list when there are no matches`() {
        val result = filterAndSortFavoriteStops(stops, "999")

        assertEquals(emptyList<FavoriteStopSnapshot>(), result)
    }
}
