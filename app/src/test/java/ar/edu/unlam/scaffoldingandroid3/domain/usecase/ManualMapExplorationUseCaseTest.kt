package ar.edu.unlam.mobile.movil22026h2.domain.usecase

import ar.edu.unlam.mobile.movil22026h2.domain.model.Latitude
import ar.edu.unlam.mobile.movil22026h2.domain.model.LocationData
import ar.edu.unlam.mobile.movil22026h2.domain.model.Longitude
import ar.edu.unlam.mobile.movil22026h2.domain.repository.LocationRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

class ManualMapExplorationUseCaseTest {
    private val mockRepository: LocationRepository = mockk<LocationRepository>()
    private val useCase =
        ExploreMapManually(mockRepository)

    @Test
    @DisplayName("Retornar ubicación de fallback al explorar mapa manualmente")
    fun returnFallbackLocationWhenExploringMapManually() =
        runTest {
            assertDoesNotThrow {
                val expectedLocation =
                    LocationData(
                        Latitude(-34.6037),
                        Longitude(-58.3816),
                    )

                coEvery { mockRepository.getLocationData() } returns expectedLocation

                val result = useCase()

                Assertions.assertEquals(expectedLocation, result)
            }
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
