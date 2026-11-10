package ar.edu.unlam.scaffoldingandroid3.domain.usecase

import ar.edu.unlam.scaffoldingandroid3.domain.enums.NearbyStopsOrigin
import ar.edu.unlam.scaffoldingandroid3.domain.model.NearbyStopsResult
import ar.edu.unlam.scaffoldingandroid3.domain.repository.BusStopRepository
import ar.edu.unlam.scaffoldingandroid3.factory.TestDataFactory.DataFactory.busStopList
import ar.edu.unlam.scaffoldingandroid3.factory.TestDataFactory.DataFactory.locationDataInstance
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

class GetNearbyStopsUseCaseTest {
    private val mockRepository: BusStopRepository = mockk<BusStopRepository>()
    private val useCase: GetNearbyStops = GetNearbyStops(mockRepository)
    private val location = locationDataInstance()

    @Test
    @DisplayName("Debería obtener las paradas cercanas y el origen de los datos desde el repositorio")
    fun shouldGetNearbyStopsWithOriginFromRepository() =
        runTest {
            assertDoesNotThrow {
                val expectedResult =
                    NearbyStopsResult(
                        stops = busStopList(),
                        origin = NearbyStopsOrigin.REMOTE,
                    )

                coEvery { mockRepository.getNearbyStops(any(), any()) } returns expectedResult

                val result = useCase(location)

                assertEquals(expectedResult, result)

                coVerify(exactly = 1) { mockRepository.getNearbyStops(location, any()) }
            }
        }

    @Test
    @DisplayName("No debería fallar si no se encuentran paradas cercanas a la ubicación del usuario")
    fun shouldNotThrowExceptionIfBusStopsListIsEmpty() =
        runTest {
            assertDoesNotThrow {
                val expectedResult =
                    NearbyStopsResult(
                        stops = emptyList(),
                        origin = NearbyStopsOrigin.DEMO,
                    )

                coEvery { mockRepository.getNearbyStops(any(), any()) } returns expectedResult

                val result = useCase(location)

                assertEquals(expectedResult, result)
            }
        }

    @Test
    @DisplayName("Debería propagar la excepción del repositorio")
    fun shouldThrowSecurityExceptionIfRepositoryFails() =
        runTest {
            coEvery { mockRepository.getNearbyStops(any(), any()) } throws SecurityException()

            assertThrows<SecurityException> { useCase(location) }
        }
}
