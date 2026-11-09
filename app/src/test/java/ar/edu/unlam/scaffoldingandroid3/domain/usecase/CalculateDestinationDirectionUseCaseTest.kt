package ar.edu.unlam.scaffoldingandroid3.domain.usecase

import ar.edu.unlam.scaffoldingandroid3.domain.model.Latitude
import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationData
import ar.edu.unlam.scaffoldingandroid3.domain.model.Longitude
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

object TestConstants {
    const val ZERO = 0.0
    const val SMALL_ANGLE_DEGREE = 0.1
    const val NORTH_BEARING = 0.0
    const val EAST_BEARING = 90.0
    const val WEST_BEARING = 270.0
    const val PRECISION_TOLERANCE = 0.001
}

class CalculateDestinationDirectionUseCaseTest {
    private val useCase = CalculateDestinationDirection()

    @Test
    @DisplayName("Debería retornar 0.0 grados cuando el origen y el destino son la misma ubicación")
    fun shouldReturnZeroDegreesWhenOriginAndDestinationAreTheSame() =
        runTest {
            val origin = LocationData(Latitude(TestConstants.ZERO), Longitude(TestConstants.ZERO))
            val destination =
                LocationData(Latitude(TestConstants.ZERO), Longitude(TestConstants.ZERO))
            val expectedDegrees = TestConstants.NORTH_BEARING

            val result = useCase(origin, destination)

            assertEquals(expectedDegrees, result.bearing.value, TestConstants.PRECISION_TOLERANCE)
        }

    @Test
    @DisplayName("Debería retornar el bearing correcto cuando la diferencia de longitud es muy pequeña")
    fun shouldReturnCorrectBearingWhenLongitudeDifferenceIsVerySmall() =
        runTest {
            val origin = LocationData(Latitude(TestConstants.ZERO), Longitude(TestConstants.ZERO))
            val destination =
                LocationData(
                    Latitude(TestConstants.ZERO),
                    Longitude(TestConstants.SMALL_ANGLE_DEGREE),
                )
            val expectedDegrees = TestConstants.EAST_BEARING

            val result = useCase(origin, destination)

            assertEquals(expectedDegrees, result.bearing.value, TestConstants.PRECISION_TOLERANCE)
        }

    @Test
    @DisplayName("Debería retornar el bearing correcto cuando la diferencia de latitud es muy pequeña")
    fun shouldReturnCorrectBearingWhenLatitudeDifferenceIsVerySmall() =
        runTest {
            val origin = LocationData(Latitude(TestConstants.ZERO), Longitude(TestConstants.ZERO))
            val destination =
                LocationData(
                    Latitude(TestConstants.SMALL_ANGLE_DEGREE),
                    Longitude(TestConstants.ZERO),
                )
            val expectedDegrees = TestConstants.NORTH_BEARING

            val result = useCase(origin, destination)

            assertEquals(expectedDegrees, result.bearing.value, TestConstants.PRECISION_TOLERANCE)
        }

    @ParameterizedTest
    @CsvSource(
        value = [
            "0.0, 0.0, 1.0, 0.0, 0.0",
            "0.0, 0.0, 0.0, -1.0, ${TestConstants.WEST_BEARING}",
            "0.0, 0.0, 0.0, 1.0, 90.0",
            "40.0, -70.0, 40.0, -71.0, 270.3213985925103",
            "40.0, -70.0, 40.0, -69.0, 89.67860140748968",
            "35.0, -60.0, 35.0, -59.0, 89.71320689675753",
            "35.0, -60.0, 35.0, -61.0, 270.2867931032425",
        ],
        delimiter = ',',
    )
    @DisplayName("Debería retornar el bearing correcto para direcciones cardinales")
    fun shouldReturnCorrectCardinalDirection(
        originLat: Double,
        originLon: Double,
        destinationLat: Double,
        destinationLon: Double,
        expectedDegrees: Double,
    ) = runTest {
        val origin = LocationData(Latitude(originLat), Longitude(originLon))
        val destination = LocationData(Latitude(destinationLat), Longitude(destinationLon))

        val result = useCase(origin, destination)

        assertEquals(expectedDegrees, result.bearing.value, TestConstants.PRECISION_TOLERANCE)
    }

    @ParameterizedTest
    @CsvSource(
        value = [
            "0.0, 0.0, 45.0, 90.0, 45.0",
            "0.0, 0.0, 45.0, -90.0, 315.0",
            "0.0, 0.0, -45.0, 90.0, 135.0",
            "0.0, 0.0, -45.0, -90.0, 225.0",
        ],
        delimiter = ',',
    )
    @DisplayName("Debería retornar el bearing correcto para direcciones intermedias")
    fun shouldReturnCorrectIntermediateDirection(
        originLat: Double,
        originLon: Double,
        destinationLat: Double,
        destinationLon: Double,
        expectedDegrees: Double,
    ) = runTest {
        val origin = LocationData(Latitude(originLat), Longitude(originLon))
        val destination = LocationData(Latitude(destinationLat), Longitude(destinationLon))

        val result = useCase(origin, destination)

        assertEquals(expectedDegrees, result.bearing.value, TestConstants.PRECISION_TOLERANCE)
    }

    @ParameterizedTest
    @CsvSource(
        value = [
            "-90.0, -90.0, -45.0, -45.0, 45.0",
            "0.0, 0.0, 45.0, -90.0, 315.0",
        ],
        delimiter = ',',
    )
    @DisplayName("Debería manejar correctamente las coordenadas negativas")
    fun shouldHandleNegativeCoordinatesCorrectlyParametrized(
        originLat: Double,
        originLon: Double,
        destinationLat: Double,
        destinationLon: Double,
        expectedDegrees: Double,
    ) = runTest {
        val origin = LocationData(Latitude(originLat), Longitude(originLon))
        val destination = LocationData(Latitude(destinationLat), Longitude(destinationLon))

        val result = useCase(origin, destination)

        assertEquals(expectedDegrees, result.bearing.value, TestConstants.PRECISION_TOLERANCE)
    }
}
