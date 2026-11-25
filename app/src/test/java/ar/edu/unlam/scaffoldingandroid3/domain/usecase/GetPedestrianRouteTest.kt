package ar.edu.unlam.scaffoldingandroid3.domain.usecase

import ar.edu.unlam.scaffoldingandroid3.domain.model.Latitude
import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationData
import ar.edu.unlam.scaffoldingandroid3.domain.model.Longitude
import ar.edu.unlam.scaffoldingandroid3.domain.model.PedestrianRoute
import ar.edu.unlam.scaffoldingandroid3.domain.repository.RouteRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.jupiter.api.DisplayName

class GetPedestrianRouteTest {
    private val repository: RouteRepository = mockk()
    private val useCase = GetPedestrianRoute(repository)

    private val origin = LocationData(Latitude(-34.6705), Longitude(-58.5630))
    private val destination = LocationData(Latitude(-34.6720), Longitude(-58.5640))

    @Test
    @DisplayName("Debería delegar la obtención de la ruta al repositorio")
    fun shouldDelegateRouteFetchingToRepository() =
        runTest {
            val expectedRoute =
                PedestrianRoute(
                    points = listOf(origin, destination),
                    distanceMeters = 300.0,
                    durationSeconds = 240.0,
                )

            coEvery { repository.getPedestrianRoute(origin, destination) } returns Result.success(expectedRoute)

            val result = useCase(origin, destination)

            assertTrue(result.isSuccess)
            assertEquals(expectedRoute, result.getOrNull())
            coVerify(exactly = 1) { repository.getPedestrianRoute(origin, destination) }
        }

    @Test
    @DisplayName("Debería retornar failure cuando el repositorio falla")
    fun shouldReturnFailureWhenRepositoryFails() =
        runTest {
            val exception = RuntimeException("Service unavailable")
            coEvery { repository.getPedestrianRoute(origin, destination) } returns Result.failure(exception)

            val result = useCase(origin, destination)

            assertTrue(result.isFailure)
            assertEquals(exception, result.exceptionOrNull())
        }
}
