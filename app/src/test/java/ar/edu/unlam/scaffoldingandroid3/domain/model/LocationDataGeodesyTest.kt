package ar.edu.unlam.scaffoldingandroid3.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class LocationDataGeodesyTest {
    @Nested
    @DisplayName("Distancia (Haversine)")
    inner class Distance {
        @Test
        @DisplayName("La distancia de un punto a sí mismo es cero")
        fun distanceBetweenSamePointIsZero() {
            val point = LocationData(Latitude(-34.6676), Longitude(-58.5606))

            assertEquals(0.0, point.distanceMetersTo(point), 0.001)
        }

        @Test
        @DisplayName("La distancia entre dos ciudades conocidas es razonable")
        fun distanceBetweenTwoCitiesIsReasonable() {
            val buenosAires = LocationData(Latitude(-34.6037), Longitude(-58.3816))
            val laPlata = LocationData(Latitude(-34.9215), Longitude(-57.9545))

            assertEquals(52_640.0, buenosAires.distanceMetersTo(laPlata), 500.0)
        }
    }

    @Nested
    @DisplayName("Rumbo (bearing)")
    inner class Bearing {
        @Test
        @DisplayName("El rumbo apunta a los cuatro puntos cardinales")
        fun bearingPointsToTheCardinalDirections() {
            val origin = LocationData(Latitude(0.0), Longitude(0.0))

            assertEquals(0.0, origin.bearingDegreesTo(LocationData(Latitude(1.0), Longitude(0.0))), 0.5)
            assertEquals(90.0, origin.bearingDegreesTo(LocationData(Latitude(0.0), Longitude(1.0))), 0.5)
            assertEquals(180.0, origin.bearingDegreesTo(LocationData(Latitude(-1.0), Longitude(0.0))), 0.5)
            assertEquals(270.0, origin.bearingDegreesTo(LocationData(Latitude(0.0), Longitude(-1.0))), 0.5)
        }

        @Test
        @DisplayName("El rumbo a un punto cercano hacia el este es 90 grados y está en rango")
        fun bearingToANearbyEasternPointIsEastAndInRange() {
            val origin = LocationData(Latitude(-34.6676), Longitude(-58.5606))
            val eastPoint = LocationData(Latitude(-34.6676), Longitude(-58.5596))

            val bearing = origin.bearingDegreesTo(eastPoint)

            assertEquals(90.0, bearing, 1.0)
            assertTrue(bearing in 0.0..360.0)
        }
    }

    @Nested
    @DisplayName("Proyección local en metros")
    inner class Projection {
        @Test
        @DisplayName("La proyección del mismo punto es cero")
        fun projectedMetersOfTheSamePointAreZero() {
            val origin = LocationData(Latitude(-34.6676), Longitude(-58.5606))

            val (north, east) = origin.projectedMetersFrom(origin)

            assertEquals(0.0, north, 0.001)
            assertEquals(0.0, east, 0.001)
        }
    }
}
