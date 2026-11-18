package ar.edu.unlam.scaffoldingandroid3.data.datasources.local

import ar.edu.unlam.scaffoldingandroid3.domain.model.BusLineStatus
import ar.edu.unlam.scaffoldingandroid3.domain.model.StopStatus
import ar.edu.unlam.scaffoldingandroid3.factory.TestDataFactory.DataFactory.locationDataInstance
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BusStopDataSourceImplTest {
    private val dataSource = BusStopDataSourceImpl()

    @Test
    fun `given any location, when getting bus stops, then returns the two known stops`() =
        runTest {
            // Given
            val location = locationDataInstance()

            // When
            val stops = dataSource.getBusStops(location)

            // Then
            assertEquals(listOf(1L, 2L), stops.map { it.stopId })
        }

    @Test
    fun `given any location, when getting bus stops, then every stop is active`() =
        runTest {
            // Given
            val location = locationDataInstance()

            // When
            val stops = dataSource.getBusStops(location)

            // Then
            assertTrue(stops.all { it.status == StopStatus.ACTIVA })
        }

    @Test
    fun `given the stops, when reading favorites, then only the second stop is marked as favorite`() =
        runTest {
            // Given
            val location = locationDataInstance()

            // When
            val stops = dataSource.getBusStops(location)

            // Then
            assertFalse(stops.first().markedAsFavorite)
            assertTrue(stops.last().markedAsFavorite)
        }

    @Test
    fun `given the stops, when reading bus lines, then a delayed line is reported`() =
        runTest {
            // Given
            val location = locationDataInstance()

            // When
            val lines = dataSource.getBusStops(location).flatMap { it.busLines }

            // Then
            assertEquals(listOf(96, 620, 180), lines.map { it.number })
            assertEquals(1, lines.count { it.currentStatus == BusLineStatus.DEMORADO })
        }
}
