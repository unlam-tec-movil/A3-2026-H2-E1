package ar.edu.unlam.scaffoldingandroid3.domain.usecase

import ar.edu.unlam.scaffoldingandroid3.domain.enums.BusLineStatus
import ar.edu.unlam.scaffoldingandroid3.domain.enums.StopStatus
import ar.edu.unlam.scaffoldingandroid3.domain.model.BusLine
import ar.edu.unlam.scaffoldingandroid3.domain.model.BusStop
import ar.edu.unlam.scaffoldingandroid3.domain.model.Latitude
import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationData
import ar.edu.unlam.scaffoldingandroid3.domain.model.Longitude
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class FilterBusStopsTest {
    private val useCase = FilterBusStops()

    private fun stop(
        stopId: Long,
        name: String,
        lineNumbers: List<Int>,
    ): BusStop =
        BusStop(
            stopId = stopId,
            name = name,
            locationData = LocationData(Latitude(-34.67 + stopId * 0.001), Longitude(-58.56 + stopId * 0.001)),
            markedAsFavorite = false,
            busLines =
                lineNumbers.map { number ->
                    BusLine(
                        lineId = number.toLong(),
                        number = number,
                        routes = emptyList(),
                        currentStatus = BusLineStatus.NORMAL,
                    )
                },
            status = StopStatus.ACTIVA,
        )

    private val sampleStops =
        listOf(
            stop(1, "Av. Perón y Arieta", listOf(96, 185)),
            stop(2, "Av. Perón y Salguero", listOf(205, 242)),
            stop(3, "Almafuerte y Arieta", listOf(406, 620)),
        )

    @Test
    @DisplayName("Sin texto de búsqueda debería devolver todas las paradas")
    fun shouldReturnAllStopsWhenQueryIsBlank() {
        assertEquals(sampleStops, useCase(sampleStops, ""))
        assertEquals(sampleStops, useCase(sampleStops, "   "))
    }

    @Test
    @DisplayName("Debería filtrar por nombre de parada ignorando mayúsculas")
    fun shouldFilterStopsByNameIgnoringCase() {
        val result = useCase(sampleStops, "almafuerte")

        assertEquals(listOf(sampleStops[2]), result)
    }

    @Test
    @DisplayName("Debería filtrar por número de línea")
    fun shouldFilterStopsByLineNumber() {
        val result = useCase(sampleStops, "185")

        assertEquals(listOf(sampleStops[0]), result)
    }

    @Test
    @DisplayName("Debería devolver una lista vacía cuando ninguna parada coincide")
    fun shouldReturnEmptyListWhenNoStopMatches() {
        val result = useCase(sampleStops, "Parada inexistente 999")

        assertEquals(emptyList<BusStop>(), result)
    }
}
