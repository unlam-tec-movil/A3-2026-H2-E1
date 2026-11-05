package ar.edu.unlam.scaffoldingandroid3.data.local

import ar.edu.unlam.scaffoldingandroid3.data.datasources.local.LocationDataSource
import ar.edu.unlam.scaffoldingandroid3.data.repositories.LocationRepositoryImpl
import ar.edu.unlam.scaffoldingandroid3.domain.model.Latitude
import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationData
import ar.edu.unlam.scaffoldingandroid3.domain.model.Longitude
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class LocationRepositoryImplTest {
    private val mockDataSource: LocationDataSource = mockk<LocationDataSource>()
    private val repository = LocationRepositoryImpl(mockDataSource)

    @Test
    @DisplayName("Debería retornar LocationData correctamente cuando el data source provee una ubicación válida")
    fun shouldReturnLocationDataWhenDataSourceProvidesValidLocation() =
        runTest {
            val expectedLatitude = -34.6037
            val expectedLongitude = -58.3816

            coEvery { mockDataSource.getLastKnowLocation() } returns
                LocationData(
                    Latitude(expectedLatitude),
                    Longitude(expectedLongitude),
                )

            val result = repository.getLocationData()

            assertEquals(Latitude(expectedLatitude), result.latitude)
            assertEquals(Longitude(expectedLongitude), result.longitude)
        }

    @Test
    @DisplayName("Debería retornar la ubicación por defecto (fallback) cuando el data source devuelve null")
    fun shouldReturnFallbackLocationWhenDataSourceReturnsNull() =
        runTest {
            coEvery { mockDataSource.getLastKnowLocation() } returns null

            val expectedFallback =
                LocationData(
                    latitude = Latitude(-34.6037),
                    longitude = Longitude(-58.3816),
                )

            val result = repository.getLocationData()

            assertEquals(expectedFallback, result)
        }

    @Test
    @DisplayName("Debería devolverse la ubicación de fallback si la lectura arroja una SecurityException")
    fun shouldReturnFallbackLocationIfSecurityExceptionWasThrown() =
        runTest {
            coEvery { mockDataSource.getLastKnowLocation() } throws SecurityException()

            val expectedFallback =
                LocationData(
                    latitude = Latitude(-34.6037),
                    longitude = Longitude(-58.3816),
                )

            val result = repository.getLocationData()

            assertEquals(expectedFallback, result)
        }
}
