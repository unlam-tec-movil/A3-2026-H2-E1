package ar.edu.unlam.scaffoldingandroid3.data.local

import ar.edu.unlam.scaffoldingandroid3.data.datasources.local.LocationDataSource
import ar.edu.unlam.scaffoldingandroid3.data.repositories.LocationRepositoryImpl
import ar.edu.unlam.scaffoldingandroid3.domain.model.Latitude
import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationData
import ar.edu.unlam.scaffoldingandroid3.domain.model.Longitude
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class LocationRepositoryImplTest {
    private val mockDataSource: LocationDataSource = mockk<LocationDataSource>()
    private val repository = LocationRepositoryImpl(mockDataSource)

    @Test
    @DisplayName("Debería retornar LocationData correctamente cuando el data source provee una ubicación válida")
    fun shouldReturnLocationDataWhenDataSourceProvidesValidLocation() =
        runTest {
            val expected =
                LocationData(
                    Latitude(-34.6037),
                    Longitude(-58.3816),
                )

            coEvery { mockDataSource.getLastKnowLocation() } returns expected

            assertEquals(expected, repository.getLocationData())
        }

    @Test
    @DisplayName("Debería retornar null cuando no hay última ubicación conocida, sin fallback silencioso")
    fun shouldReturnNullWhenDataSourceReturnsNull() =
        runTest {
            coEvery { mockDataSource.getLastKnowLocation() } returns null

            assertNull(repository.getLocationData())
        }

    @Test
    @DisplayName("Debería propagar la excepción del data source en lugar de enmascararla con una ubicación por defecto")
    fun shouldPropagateExceptionFromDataSource() =
        runTest {
            coEvery { mockDataSource.getLastKnowLocation() } throws SecurityException()

            assertThrows<SecurityException> { repository.getLocationData() }
        }

    @Test
    @DisplayName("Debería retornar false si el GPS o los servicios de ubicación están desactivados")
    fun shouldReturnFalseIfGpsOrLocationServicesAreDisabled() =
        runTest {
            coEvery { mockDataSource.isLocationEnabled() } returns false

            assertFalse(repository.isLocationEnabled())
        }

    @Test
    @DisplayName("Debería delegar la obtención de flujo de ubicaciones en tiempo real al data source")
    fun shouldDelegateGetLocationUpdatesToDataSource() =
        runTest {
            val expectedLocation = LocationData(Latitude(-34.6037), Longitude(-58.3816))
            every { mockDataSource.getLocationUpdates(any(), any()) } returns flowOf(expectedLocation)

            val flow = repository.getLocationUpdates()
            flow.collect { location ->
                assertEquals(expectedLocation, location)
            }
        }
}
