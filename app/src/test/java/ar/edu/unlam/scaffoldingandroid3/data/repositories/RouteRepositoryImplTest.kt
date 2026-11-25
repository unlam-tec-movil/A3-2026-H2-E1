package ar.edu.unlam.scaffoldingandroid3.data.repositories

import ar.edu.unlam.scaffoldingandroid3.data.datasources.remote.OsrmRouteDataSource
import ar.edu.unlam.scaffoldingandroid3.domain.model.Latitude
import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationData
import ar.edu.unlam.scaffoldingandroid3.domain.model.Longitude
import ar.edu.unlam.scaffoldingandroid3.domain.model.PedestrianRoute
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.jupiter.api.DisplayName
import java.io.IOException

class RouteRepositoryImplTest {
    private val dataSource: OsrmRouteDataSource = mockk()
    private val repository = RouteRepositoryImpl(dataSource)

    private val origin = LocationData(Latitude(-34.6705), Longitude(-58.5630))
    private val destination = LocationData(Latitude(-34.6720), Longitude(-58.5640))

    @Test
    @DisplayName("Debería retornar Result.success cuando el DataSource responde exitosamente")
    fun shouldReturnSuccessWhenDataSourceSucceeds() =
        runTest {
            val expectedRoute =
                PedestrianRoute(
                    points = listOf(origin, destination),
                    distanceMeters = 300.0,
                    durationSeconds = 240.0,
                )

            coEvery { dataSource.getPedestrianRoute(origin, destination) } returns expectedRoute

            val result = repository.getPedestrianRoute(origin, destination)

            assertTrue(result.isSuccess)
            assertEquals(expectedRoute, result.getOrNull())
        }

    @Test
    @DisplayName("Debería retornar Result.failure cuando el DataSource lanza excepción por error de red")
    fun shouldReturnFailureWhenDataSourceThrowsIOException() =
        runTest {
            coEvery { dataSource.getPedestrianRoute(origin, destination) } throws IOException("No connection")

            val result = repository.getPedestrianRoute(origin, destination)

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is IOException)
        }
}
