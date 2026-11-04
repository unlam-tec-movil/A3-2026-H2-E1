package ar.edu.unlam.scaffoldingandroid3.domain.usecase

import ar.edu.unlam.mobile.movil22026h2.domain.model.BusStop
import ar.edu.unlam.mobile.movil22026h2.domain.repository.BusStopRepository
import ar.edu.unlam.scaffoldingandroid3.factory.TestDataFactory.DataFactory.busStopList
import ar.edu.unlam.scaffoldingandroid3.factory.TestDataFactory.DataFactory.locationDataInstance
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

class GetNearbyStopsUseCaseTest {
    private val mockRepository: BusStopRepository = mockk<BusStopRepository>()
    private val useCase: GetNearbyStops = GetNearbyStops(mockRepository)
    private val location = locationDataInstance()

    @Test
    @DisplayName("Debería obtener una lista de paradas de colectivo cercanas a partir de una ubicación en el mapa")
    fun shouldGetListOfNearbyBusStopsFromRepository() {
        assertDoesNotThrow {
            val expectedBusStops = busStopList()

            every { mockRepository.getNearbyBusStopsByCurrentLocation(any()) } returns expectedBusStops

            val result = useCase(location)

            assertEquals(expectedBusStops, result)
        }
    }

    @Test
    @DisplayName("No debería fallar si no se encuentran paradas cercanas a la ubicación del usuario")
    fun shouldNotThrowExceptionIfBusStopsListIsEmpty() {
        assertDoesNotThrow {
            val emptyList = emptyList<BusStop>()

            every { mockRepository.getNearbyBusStopsByCurrentLocation(any()) } returns emptyList

            val result = useCase(location)

            assertEquals(emptyList, result)
        }
    }

    @Test
    @DisplayName("Debería propagar la excepción si no se asignan permisos")
    fun shouldThrowSecurityExceptionIfPermissionIsDenied() {
        every { mockRepository.getNearbyBusStopsByCurrentLocation(any()) } throws SecurityException()

        assertThrows<SecurityException> { useCase(location) }
    }
}
