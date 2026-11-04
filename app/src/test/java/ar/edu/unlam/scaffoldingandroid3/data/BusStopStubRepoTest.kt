package ar.edu.unlam.scaffoldingandroid3.data

import ar.edu.unlam.scaffoldingandroid3.domain.GeoPoint
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BusStopStubRepoTest {
    private val center = GeoPoint(latitude = -34.6676, longitude = -58.5606)
    private val repository = BusStopStubRepo()

    @Test
    fun nearby_stops_are_sorted_from_closest_to_farthest() =
        runBlocking {
            val stops = repository.getNearbyStops(center, radiusMeters = 1_000.0)

            val distances = stops.map { center.distanceTo(it.location) }

            assertEquals(distances.sorted(), distances)
        }

    @Test
    fun radius_filters_out_far_stops() =
        runBlocking {
            val radius = 250.0

            val stops = repository.getNearbyStops(center, radiusMeters = radius)

            assertTrue(stops.isNotEmpty())
            assertTrue(stops.all { center.distanceTo(it.location) <= radius })
        }

    @Test
    fun every_stop_has_name_and_lines() =
        runBlocking {
            val stops = repository.getNearbyStops(center, radiusMeters = 1_000.0)

            assertTrue(stops.all { it.name.isNotBlank() && it.lines.isNotEmpty() })
        }
}
