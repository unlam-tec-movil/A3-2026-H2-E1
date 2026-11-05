package ar.edu.unlam.scaffoldingandroid3.ui.viewmodel

import app.cash.turbine.test
import ar.edu.unlam.scaffoldingandroid3.domain.repository.LocationRepository
import ar.edu.unlam.scaffoldingandroid3.domain.usecase.ExploreMapManually
import ar.edu.unlam.scaffoldingandroid3.domain.usecase.GetNearbyStops
import ar.edu.unlam.scaffoldingandroid3.factory.TestDataFactory
import ar.edu.unlam.scaffoldingandroid3.ui.uistate.MapUiState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertNull

class MapExploreViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val exploreMapUseCase = mockk<ExploreMapManually>()
    private val getNearbyStops = mockk<GetNearbyStops>()
    private val repository = mockk<LocationRepository>()
    private lateinit var viewModel: MapViewModel

    @Test
    @DisplayName("Debería iniciar en Loading")
    fun shouldStartInLoadingState() =
        runTest {
            val expectedUiState = MapUiState.Loading

            viewModel = MapViewModel(exploreMapUseCase, getNearbyStops, repository)

            val actualUiState = viewModel.uiState.value

            assertEquals(expectedUiState, actualUiState)
        }

    @Test
    @DisplayName("Debería emitir Success con la ubicación cuando se carga correctamente")
    fun shouldEmitSuccessWithLocationWhenLoaded() =
        runTest {
            val expectedLocation = TestDataFactory.locationDataInstance()
            val expectedStops = TestDataFactory.busStopList()

            coEvery { exploreMapUseCase() } returns expectedLocation
            coEvery { getNearbyStops(expectedLocation) } returns expectedStops
            coEvery { repository.isLocationEnabled() } returns true

            viewModel = MapViewModel(exploreMapUseCase, getNearbyStops, repository)

            viewModel.uiState.test {
                assertEquals(MapUiState.Loading, awaitItem())

                viewModel.loadUserLocationAndStops()

                val succesState = awaitItem() as MapUiState.Success
                assertEquals(expectedLocation, succesState.location)
                assertEquals(expectedStops, succesState.busStops)
            }
        }

    @Test
    @DisplayName("Debería emitir Error cuando hay SecurityException")
    fun shouldEmitErrorOnSecurityException() =
        runTest {
            coEvery { exploreMapUseCase() } throws SecurityException("No location access")
            coEvery { repository.isLocationEnabled() } returns true

            viewModel = MapViewModel(exploreMapUseCase, getNearbyStops, repository)

            viewModel.uiState.test {
                assertEquals(MapUiState.Loading, awaitItem())

                viewModel.loadUserLocationAndStops()

                val errorState = awaitItem() as MapUiState.Error
                assertEquals("No location access", errorState.message)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    @DisplayName("Debería emitir Error cuando ocurre una excepción genérica")
    fun shouldEmitErrorOnGenericException() =
        runTest {
            coEvery { exploreMapUseCase() } throws Exception()
            coEvery { repository.isLocationEnabled() } returns true

            viewModel = MapViewModel(exploreMapUseCase, getNearbyStops, repository)

            viewModel.uiState.test {
                assertEquals(MapUiState.Loading, awaitItem())

                viewModel.loadUserLocationAndStops()

                val errorState = awaitItem() as MapUiState.Error
                assertEquals(
                    "Unknown error occurred while loading the user location",
                    errorState.message,
                )

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    @DisplayName("Debería actualizar la información de la parada al seleccionarla en el mapa")
    fun shouldUpdateSelectedBusStopWhenStopIsClicked() =
        runTest {
            val expectedLocation = TestDataFactory.locationDataInstance()
            val expectedStops = TestDataFactory.busStopList()
            val stopToSelect = expectedStops.first()

            coEvery { exploreMapUseCase() } returns expectedLocation
            coEvery { getNearbyStops(expectedLocation) } returns expectedStops
            coEvery { repository.isLocationEnabled() } returns true

            viewModel = MapViewModel(exploreMapUseCase, getNearbyStops, repository)

            viewModel.uiState.test {
                assertEquals(MapUiState.Loading, awaitItem())

                viewModel.loadUserLocationAndStops()

                val initialSuccess = awaitItem() as MapUiState.Success

                assertNull(initialSuccess.selectedStop)

                viewModel.selectBusStop(stopToSelect)

                val updatedSuccess = awaitItem() as MapUiState.Success

                assertEquals(expectedLocation, updatedSuccess.location)
                assertEquals(expectedStops, updatedSuccess.busStops)
                assertEquals(stopToSelect, updatedSuccess.selectedStop)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    @DisplayName("Debería limpiar la información de la parada al dejar de seleccionarla")
    fun shouldClearSelectedBusStopWhenDeselected() =
        runTest {
            val expectedLocation = TestDataFactory.locationDataInstance()
            val expectedStops = TestDataFactory.busStopList()
            val stopToSelect = expectedStops.first()

            coEvery { exploreMapUseCase() } returns expectedLocation
            coEvery { getNearbyStops(expectedLocation) } returns expectedStops
            coEvery { repository.isLocationEnabled() } returns true

            viewModel = MapViewModel(exploreMapUseCase, getNearbyStops, repository)

            viewModel.uiState.test {
                assertEquals(MapUiState.Loading, awaitItem())

                viewModel.loadUserLocationAndStops()

                val initialSuccess = awaitItem() as MapUiState.Success

                assertNull(initialSuccess.selectedStop)

                viewModel.selectBusStop(stopToSelect)

                var updatedSuccess = awaitItem() as MapUiState.Success

                assertEquals(stopToSelect, updatedSuccess.selectedStop)

                viewModel.clearSelectedBusStop()

                updatedSuccess = awaitItem() as MapUiState.Success

                assertNull(updatedSuccess.selectedStop)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    @DisplayName("Debería emitir un error cuando el servicio de ubicación esté desactivado")
    fun shouldEmitErrorWhenLocationServiceIsDisabled() =
        runTest {
            coEvery { repository.isLocationEnabled() } returns false

            viewModel = MapViewModel(exploreMapUseCase, getNearbyStops, repository)

            viewModel.uiState.test {
                assertEquals(MapUiState.Loading, awaitItem())

                viewModel.loadUserLocationAndStops()

                val errorState = awaitItem() as MapUiState.Error
                assertEquals(
                    "Location service disabled",
                    errorState.message,
                )

                coVerify(exactly = 0) { exploreMapUseCase() }
                coVerify(exactly = 0) { getNearbyStops(any()) }

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    @DisplayName("Debería emitir un éxito cuando la ubicación de servicio esté activado")
    fun shouldEmitSuccessWhenLocationServiceIsEnabled() =
        runTest {
            val expectedLocation = TestDataFactory.locationDataInstance()
            val expectedStops = TestDataFactory.busStopList()

            coEvery { exploreMapUseCase() } returns expectedLocation
            coEvery { getNearbyStops(expectedLocation) } returns expectedStops
            coEvery { repository.isLocationEnabled() } returns true

            viewModel = MapViewModel(exploreMapUseCase, getNearbyStops, repository)

            viewModel.uiState.test {
                assertEquals(MapUiState.Loading, awaitItem())

                viewModel.loadUserLocationAndStops()

                val successState = awaitItem() as MapUiState.Success
                assertEquals(expectedLocation, successState.location)
                assertEquals(expectedStops, successState.busStops)

                coVerify(exactly = 1) { exploreMapUseCase() }
                coVerify(exactly = 1) { getNearbyStops(expectedLocation) }

                cancelAndIgnoreRemainingEvents()
            }
        }
}
