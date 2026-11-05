package ar.edu.unlam.scaffoldingandroid3.data.local

import ar.edu.unlam.scaffoldingandroid3.data.datasources.local.BusStopDataSource
import ar.edu.unlam.scaffoldingandroid3.data.repositories.BusStopRepositoryImpl
import ar.edu.unlam.scaffoldingandroid3.domain.repository.BusStopRepository
import ar.edu.unlam.scaffoldingandroid3.factory.TestDataFactory
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class BusStopRepositoryImplTest {
    private val mockDataSource = mockk<BusStopDataSource>()
    private lateinit var repository: BusStopRepository

    @BeforeEach
    fun setUp() {
        repository = BusStopRepositoryImpl(mockDataSource)
    }

    @Test
    @DisplayName("Debería solicitar las paradas de colectivo cercanas al Datasource con la ubicación y retornar la lista mapeada")
    fun shouldGetAndMapBusStopsFromDataSource() =
        runTest {
            val currentLocation = TestDataFactory.locationDataInstance()
            val expectedBusStopList = TestDataFactory.busStopList()

            coEvery {
                mockDataSource.getBusStops(currentLocation)
            } returns expectedBusStopList

            val result = repository.getNearbyBusStopsByCurrentLocation(currentLocation)

            assertEquals(expectedBusStopList, result)
            coVerify(exactly = 1) { mockDataSource.getBusStops(currentLocation) }
        }

    @Test
    @DisplayName("Debería propagarse la excepción si el Datasource falla")
    fun shouldhandleDataSourceFailure() =
        runTest {
            val currentLocation = TestDataFactory.locationDataInstance()
            val expectedException = Exception("Error al conectar con la fuente de datos")

            coEvery {
                mockDataSource.getBusStops(currentLocation)
            } throws expectedException

            val actualException =
                assertThrows<Exception> {
                    repository.getNearbyBusStopsByCurrentLocation(currentLocation)
                }

            assertEquals(expectedException.message, actualException.message)
            coVerify(exactly = 1) { mockDataSource.getBusStops(currentLocation) }
        }
}
