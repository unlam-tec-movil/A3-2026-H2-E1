package ar.edu.unlam.scaffoldingandroid3.domain.model

import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationDataTest.Companion.BoundaryCoordinates.LATITUDE_HIGH
import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationDataTest.Companion.BoundaryCoordinates.LATITUDE_LOW
import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationDataTest.Companion.BoundaryCoordinates.LATITUDE_NEAR_MAX
import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationDataTest.Companion.BoundaryCoordinates.LATITUDE_NEAR_MIN
import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationDataTest.Companion.BoundaryCoordinates.LONGITUDE_HIGH
import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationDataTest.Companion.BoundaryCoordinates.LONGITUDE_LOW
import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationDataTest.Companion.BoundaryCoordinates.LONGITUDE_NEAR_MAX
import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationDataTest.Companion.BoundaryCoordinates.LONGITUDE_NEAR_MIN
import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationDataTest.Companion.BoundaryCoordinates.NEAR_ZERO
import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationDataTest.Companion.InvalidCoordinates.LATITUDE_ABOVE_MAX
import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationDataTest.Companion.InvalidCoordinates.LATITUDE_BELOW_MIN
import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationDataTest.Companion.InvalidCoordinates.LONGITUDE_ABOVE_MAX
import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationDataTest.Companion.InvalidCoordinates.LONGITUDE_BELOW_MIN
import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationDataTest.Companion.ValidCoordinates.LATITUDE_MAX
import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationDataTest.Companion.ValidCoordinates.LATITUDE_MIN
import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationDataTest.Companion.ValidCoordinates.LONGITUDE_MAX
import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationDataTest.Companion.ValidCoordinates.LONGITUDE_MIN
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class LocationDataTest {
    @Nested
    inner class HappyPaths {
        @Test
        @DisplayName("Crear LocationData con datos válidos")
        fun createLocationDataWithValidLatitude() {
            assertDoesNotThrow {
                defaultLocationData()
            }
        }

        @Test
        @DisplayName("Crear LocationData con latitud 0 (ecuador)")
        fun createLocationDataWithLatitudeZero() {
            assertDoesNotThrow {
                defaultLocationData().withLatitude(0.0)
            }
        }

        @Test
        @DisplayName("Crear LocationData con longitud 0 (línea de cambio de fecha)")
        fun createLocationDataWithLongitudeZero() {
            assertDoesNotThrow {
                defaultLocationData().withLongitude(0.0)
            }
        }

        @Test
        @DisplayName("Crear LocationData con longitud 180")
        fun createLocationDataWithLongitude180() {
            assertDoesNotThrow {
                defaultLocationData().withLongitude(180.0)
            }
        }

        @Test
        @DisplayName("Crear LocationData con longitud -180")
        fun createLocationDataWithLongitudeMinus180() {
            assertDoesNotThrow {
                defaultLocationData().withLongitude(-180.0)
            }
        }

        @Test
        @DisplayName("Crear LocationData con valor 0 en latitud y longitud")
        fun createLocationDataWithZeroCoordinates() {
            assertDoesNotThrow { LocationData(Latitude(0.0), Longitude(0.0)) }
        }
    }

    @Nested
    inner class SadPaths {
        @Test
        @DisplayName("No crear LocationData con latitud mayor a 90")
        fun throwExceptionWhenLatitudeGreaterThanNinety() {
            assertThrows<IllegalArgumentException> {
                defaultLocationData().withLatitude(
                    LATITUDE_ABOVE_MAX,
                )
            }
        }

        @Test
        @DisplayName("No crear LocationData con latitud menor a -90")
        fun throwExceptionWhenLatitudeLessThanMinusNinety() {
            assertThrows<IllegalArgumentException> {
                defaultLocationData().withLatitude(
                    LATITUDE_BELOW_MIN,
                )
            }
        }

        @Test
        @DisplayName("No crear LocationData con longitud mayor a 180")
        fun throwExceptionWhenLongitudeGreaterThanOneEighty() {
            assertThrows<IllegalArgumentException> {
                defaultLocationData().withLongitude(
                    LONGITUDE_ABOVE_MAX,
                )
            }
        }

        @Test
        @DisplayName("No crear LocationData con longitud menor a -180")
        fun throwExceptionWhenLongitudeLessThanMinusOneEighty() {
            assertThrows<IllegalArgumentException> {
                defaultLocationData().withLongitude(
                    LONGITUDE_BELOW_MIN,
                )
            }
        }

        @Test
        @DisplayName("No crear LocationData con latitud 90.00001")
        fun throwExceptionWhenLatitudeExceedsNinetyBySmallAmount() {
            assertThrows<IllegalArgumentException> { defaultLocationData().withLatitude(90.00001) }
        }

        @Test
        @DisplayName("No crear LocationData con latitud -90.00001")
        fun throwExceptionWhenLatitudeExceedsMinusNinetyBySmallAmount() {
            assertThrows<IllegalArgumentException> { defaultLocationData().withLatitude(-90.00001) }
        }

        @Test
        @DisplayName("No crear LocationData con longitud 180.00001")
        fun throwExceptionWhenLongitudeExceedsOneEightyBySmallAmount() {
            assertThrows<IllegalArgumentException> { defaultLocationData().withLongitude(180.00001) }
        }

        @Test
        @DisplayName("No crear LocationData con longitud -180.00001")
        fun throwExceptionWhenLongitudeExceedsMinusOneEightyBySmallAmount() {
            assertThrows<IllegalArgumentException> { defaultLocationData().withLongitude(-180.00001) }
        }
    }

    @Nested
    inner class EdgeCases {
        @Test
        @DisplayName("Crear LocationData con latitud exactamente 90.0")
        fun createLocationDataWithLatitudeExactlyNinety() {
            assertDoesNotThrow { defaultLocationData().withLatitude(LATITUDE_MAX) }
        }

        @Test
        @DisplayName("Crear LocationData con latitud exactamente -90.0")
        fun createLocationDataWithLatitudeExactlyMinusNinety() {
            assertDoesNotThrow { defaultLocationData().withLatitude(LATITUDE_MIN) }
        }

        @Test
        @DisplayName("Crear LocationData con longitud exactamente 180.0")
        fun createLocationDataWithLongitudeExactlyOneEighty() {
            assertDoesNotThrow { defaultLocationData().withLongitude(LONGITUDE_MAX) }
        }

        @Test
        @DisplayName("Crear LocationData con longitud exactamente -180.0")
        fun createLocationDataWithLongitudeExactlyMinusOneEighty() {
            assertDoesNotThrow { defaultLocationData().withLongitude(LONGITUDE_MIN) }
        }

        @Test
        @DisplayName("Crear LocationData con latitud 89.99999999999999")
        fun createLocationDataWithLatitudeNearMaximum() {
            assertDoesNotThrow { defaultLocationData().withLatitude(LATITUDE_NEAR_MAX) }
        }

        @Test
        @DisplayName("Crear LocationData con longitud 179.99999999999999")
        fun createLocationDataWithLongitudeNearMaximum() {
            assertDoesNotThrow { defaultLocationData().withLongitude(LONGITUDE_NEAR_MAX) }
        }

        @Test
        @DisplayName("Crear LocationData con latitud -89.99999999999999")
        fun createLocationDataWithLatitudeNearMinimum() {
            assertDoesNotThrow { defaultLocationData().withLatitude(LATITUDE_NEAR_MIN) }
        }

        @Test
        @DisplayName("Crear LocationData con longitud -179.99999999999999")
        fun createLocationDataWithLongitudeNearMinimum() {
            assertDoesNotThrow { defaultLocationData().withLongitude(LONGITUDE_NEAR_MIN) }
        }

        @Test
        @DisplayName("Crear LocationData con latitud 0.0000001")
        fun createLocationDataWithLatitudeNearZero() {
            assertDoesNotThrow { defaultLocationData().withLatitude(NEAR_ZERO) }
        }

        @Test
        @DisplayName("Crear LocationData con longitud 0.0000001")
        fun createLocationDataWithLongitudeNearZero() {
            assertDoesNotThrow { defaultLocationData().withLongitude(NEAR_ZERO) }
        }

        @Test
        @DisplayName("Crear LocationData con latitud 89.9")
        fun createLocationDataWithHighLatitude() {
            assertDoesNotThrow { defaultLocationData().withLatitude(LATITUDE_HIGH) }
        }

        @Test
        @DisplayName("Crear LocationData con longitud 179.9")
        fun createLocationDataWithHighLongitude() {
            assertDoesNotThrow { defaultLocationData().withLongitude(LONGITUDE_HIGH) }
        }

        @Test
        @DisplayName("Crear LocationData con latitud -89.9")
        fun createLocationDataWithLowLatitude() {
            assertDoesNotThrow { defaultLocationData().withLatitude(LATITUDE_LOW) }
        }

        @Test
        @DisplayName("Crear LocationData con longitud -179.9")
        fun createLocationDataWithLowLongitude() {
            assertDoesNotThrow { defaultLocationData().withLongitude(LONGITUDE_LOW) }
        }
    }

    companion object {
        object ValidCoordinates {
            const val LATITUDE_MIN: Double = -90.0
            const val LATITUDE_MAX: Double = 90.0
            const val LONGITUDE_MIN: Double = -180.0
            const val LONGITUDE_MAX: Double = 180.0
            const val DEFAULT_LATITUDE: Double = 45.0
            const val DEFAULT_LONGITUDE: Double = -75.0
        }

        object InvalidCoordinates {
            const val LATITUDE_BELOW_MIN: Double = -90.1
            const val LATITUDE_ABOVE_MAX: Double = 90.1
            const val LONGITUDE_BELOW_MIN: Double = -180.1
            const val LONGITUDE_ABOVE_MAX: Double = 180.1
        }

        object BoundaryCoordinates {
            const val LATITUDE_NEAR_MAX: Double = 89.99999999999999
            const val LATITUDE_NEAR_MIN: Double = -89.99999999999999
            const val LONGITUDE_NEAR_MAX: Double = 179.99999999
            const val LONGITUDE_NEAR_MIN: Double = -179.999999
            const val LATITUDE_HIGH: Double = 89.9
            const val LATITUDE_LOW: Double = -89.9
            const val LONGITUDE_HIGH: Double = 179.9
            const val LONGITUDE_LOW: Double = -179.9
            const val NEAR_ZERO: Double = 0.0000001
        }

        private fun defaultLocationData(): LocationData =
            LocationData(
                Latitude(ValidCoordinates.DEFAULT_LATITUDE),
                Longitude(ValidCoordinates.DEFAULT_LONGITUDE),
            )
    }
}
