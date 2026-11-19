package ar.edu.unlam.scaffoldingandroid3.data.datasources.demo

import ar.edu.unlam.scaffoldingandroid3.domain.model.Latitude
import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationData
import ar.edu.unlam.scaffoldingandroid3.domain.model.Longitude
import ar.edu.unlam.scaffoldingandroid3.domain.model.distanceMetersTo
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class DemoBusStopDataSourceImplTest {
    private val center = LocationData(Latitude(-34.6676), Longitude(-58.5606))
    private val dataSource = DemoBusStopDataSourceImpl()

    @Test
    @DisplayName("Las paradas de ejemplo deberían ordenarse de la más cercana a la más lejana")
    fun demoStopsAreSortedFromClosestToFarthest() =
        runTest {
            val stops = dataSource.getDemoStops(center, radiusMeters = 1_000.0)

            val distances = stops.map { center.distanceMetersTo(it.locationData) }

            assertEquals(distances.sorted(), distances)
        }

    @Test
    @DisplayName("El radio debería filtrar las paradas de ejemplo más lejanas")
    fun radiusFiltersOutFarDemoStops() =
        runTest {
            val radius = 250.0

            val stops = dataSource.getDemoStops(center, radiusMeters = radius)

            assertTrue(stops.isNotEmpty())
            assertTrue(stops.all { center.distanceMetersTo(it.locationData) <= radius })
        }

    @Test
    @DisplayName("Las paradas de ejemplo deberían generarse alrededor del punto recibido")
    fun demoStopsAreGeneratedAroundTheRequestedCenter() =
        runTest {
            val anotherCenter = LocationData(Latitude(-34.6037), Longitude(-58.3816))

            val stopsAtCenter = dataSource.getDemoStops(center, radiusMeters = 1_000.0)
            val stopsAtAnotherCenter = dataSource.getDemoStops(anotherCenter, radiusMeters = 1_000.0)

            assertEquals(stopsAtCenter.map { it.stopId }, stopsAtAnotherCenter.map { it.stopId })
            assertTrue(stopsAtAnotherCenter.all { it.locationData.distanceMetersTo(anotherCenter) < 1_000.0 })
        }

    @Test
    @DisplayName("Cada parada de ejemplo debería tener id único y al menos una línea")
    fun demoStopsHaveUniqueIdsAndAtLeastOneLine() =
        runTest {
            val stops = dataSource.getDemoStops(center, radiusMeters = 1_000.0)

            assertEquals(stops.map { it.stopId }.distinct().size, stops.size)
            assertTrue(stops.all { it.busLines.isNotEmpty() })
        }
}
