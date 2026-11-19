package ar.edu.unlam.scaffoldingandroid3.domain.usecase

import ar.edu.unlam.scaffoldingandroid3.domain.model.Latitude
import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationData
import ar.edu.unlam.scaffoldingandroid3.domain.model.Longitude
import ar.edu.unlam.scaffoldingandroid3.domain.repository.LocationRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ManualMapExplorationUseCaseTest {
    private val mockRepository: LocationRepository = mockk<LocationRepository>()
    private val useCase =
        ExploreMapManually(mockRepository)

    @Test
    @DisplayName("Debería devolver la última ubicación conocida del dispositivo")
    fun returnLocationWhenExploringMapManually() =
        runTest {
            val expectedLocation =
                LocationData(
                    Latitude(-34.6037),
                    Longitude(-58.3816),
                )

            coEvery { mockRepository.getLocationData() } returns expectedLocation

            assertEquals(expectedLocation, useCase())
        }

    @Test
    @DisplayName("Debería devolver null cuando no hay ubicación conocida, sin fallback silencioso")
    fun shouldReturnNullWhenThereIsNoKnownLocation() =
        runTest {
            coEvery { mockRepository.getLocationData() } returns null

            assertNull(useCase())
        }

    @Test
    @DisplayName("Debería arrojar SecurityException si no se permite acceder a lecturas de GPS")
    fun shouldThrowSecurityExceptionIfGpsAccessIsDenied() =
        runTest {
            coEvery { mockRepository.getLocationData() } throws SecurityException("Permiso de GPS denegado")

            assertThrows<SecurityException> {
                useCase()
            }
        }
}
