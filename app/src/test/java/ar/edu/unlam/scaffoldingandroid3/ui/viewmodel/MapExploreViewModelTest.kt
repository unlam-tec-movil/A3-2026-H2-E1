package ar.edu.unlam.scaffoldingandroid3.ui.viewmodel

import app.cash.turbine.test
import ar.edu.unlam.scaffoldingandroid3.domain.usecase.ExploreMapManually
import ar.edu.unlam.scaffoldingandroid3.domain.usecase.GetNearbyStops
import ar.edu.unlam.scaffoldingandroid3.factory.TestDataFactory
import ar.edu.unlam.scaffoldingandroid3.ui.uistate.MapUiState
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName

class MapExploreViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val exploreMapUseCase = mockk<ExploreMapManually>()
    private val getNearbyStops = mockk<GetNearbyStops>()
    private lateinit var viewModel: MapViewModel

    @Test
    @DisplayName("Debería iniciar en Loading")
    fun shouldStartInLoadingState() =
        runTest {
            val expectedUiState = MapUiState.Loading

            viewModel = MapViewModel(exploreMapUseCase, getNearbyStops)

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

            viewModel = MapViewModel(exploreMapUseCase, getNearbyStops)

            viewModel.uiState.test {
                assertEquals(MapUiState.Loading, awaitItem())

                viewModel.loadUserLocation()

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

            viewModel = MapViewModel(exploreMapUseCase, getNearbyStops)

            viewModel.uiState.test {
                assertEquals(MapUiState.Loading, awaitItem())

                viewModel.loadUserLocation()

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

            viewModel = MapViewModel(exploreMapUseCase, getNearbyStops)

            viewModel.uiState.test {
                assertEquals(MapUiState.Loading, awaitItem())

                viewModel.loadUserLocation()

                val errorState = awaitItem() as MapUiState.Error
                assertEquals(
                    "Unknown error occurred while loading the user location",
                    errorState.message,
                )

                cancelAndIgnoreRemainingEvents()
            }
        }
}
