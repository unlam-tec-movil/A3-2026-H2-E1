package ar.edu.unlam.scaffoldingandroid3.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class GeoPointTest {
    @Test
    fun distance_between_same_point_is_zero() {
        val point = GeoPoint(latitude = -34.6676, longitude = -58.5606)

        assertEquals(0.0, point.distanceTo(point), 0.001)
    }

    @Test
    fun distance_between_two_cities_is_reasonable() {
        val buenosAires = GeoPoint(latitude = -34.6037, longitude = -58.3816)
        val laPlata = GeoPoint(latitude = -34.9215, longitude = -57.9545)

        assertEquals(52_640.0, buenosAires.distanceTo(laPlata), 500.0)
    }

    @Test
    fun offset_of_one_hundred_meters_is_one_hundred_meters_away() {
        val origin = GeoPoint(latitude = -34.6676, longitude = -58.5606)

        val moved = origin.offset(northMeters = 100.0, eastMeters = 0.0)

        assertEquals(100.0, origin.distanceTo(moved), 0.5)
    }

    @Test
    fun projected_meters_of_the_same_point_are_zero() {
        val origin = GeoPoint(latitude = -34.6676, longitude = -58.5606)

        val (north, east) = origin.projectedMetersFrom(origin)

        assertEquals(0.0, north, 0.001)
        assertEquals(0.0, east, 0.001)
    }
}
