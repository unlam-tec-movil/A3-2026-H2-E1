package ar.edu.unlam.scaffoldingandroid3.ui.viewmodel

import app.cash.turbine.test
import ar.edu.unlam.scaffoldingandroid3.dispatcher.MainDispatcherRule
import ar.edu.unlam.scaffoldingandroid3.domain.enums.NearbyStopsOrigin
import ar.edu.unlam.scaffoldingandroid3.domain.model.NearbyStopsResult
import ar.edu.unlam.scaffoldingandroid3.domain.repository.BusStopRepository
import ar.edu.unlam.scaffoldingandroid3.domain.repository.LocationRepository
import ar.edu.unlam.scaffoldingandroid3.domain.usecase.ExploreMapManually
import ar.edu.unlam.scaffoldingandroid3.domain.usecase.FilterBusStops
import ar.edu.unlam.scaffoldingandroid3.domain.usecase.GetNearbyStops
import ar.edu.unlam.scaffoldingandroid3.factory.TestDataFactory
import ar.edu.unlam.scaffoldingandroid3.ui.uistate.MapUiState
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName

class NearbyStopsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val exploreMapUseCase = mockk<ExploreMapManually>()
    private val getNearbyStops = mockk<GetNearbyStops>()
    private val filterBusStops = FilterBusStops()
    private val busStopRepository = mockk<BusStopRepository>()
    private val repository = mockk<LocationRepository>()
    private lateinit var viewModel: NearbyStopsScreenViewModel

    private fun createViewModel(): NearbyStopsScreenViewModel {
        every { busStopRepository.getRecentSearches() } returns flowOf(emptyList())
        return NearbyStopsScreenViewModel(exploreMapUseCase, getNearbyStops, filterBusStops, busStopRepository, repository)
    }

    @Test
    @DisplayName("Debería iniciar en Loading")
    fun shouldStartInLoadingState() =
        runTest {
            viewModel = createViewModel()

            assertEquals(MapUiState.Loading, viewModel.uiState.value)
        }

    @Test
    @DisplayName("Debería emitir Success con la ubicación cuando se carga correctamente")
    fun shouldEmitSuccessWithLocationWhenLoaded() =
        runTest {
            val expectedLocation = TestDataFactory.locationDataInstance()
            val expectedStops = TestDataFactory.busStopList()

            coEvery { repository.isLocationEnabled() } returns true
            coEvery { exploreMapUseCase() } returns expectedLocation
            coEvery { getNearbyStops(expectedLocation) } returns
                NearbyStopsResult(stops = expectedStops, origin = NearbyStopsOrigin.REMOTE)

            viewModel = createViewModel()

            viewModel.uiState.test {
                assertEquals(MapUiState.Loading, awaitItem())

                viewModel.loadUserLocationAndStops()

                val successState = awaitItem() as MapUiState.Success
                assertEquals(expectedLocation, successState.center)
                assertEquals(expectedStops, successState.busStops)
                assertEquals(expectedStops, successState.filteredStops)
                assertFalse(successState.isFromCache)
                assertFalse(successState.isDemo)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    @DisplayName("Debería marcar el estado cuando el origen de las paradas es la caché")
    fun shouldFlagStateWhenStopsComeFromCache() =
        runTest {
            val expectedLocation = TestDataFactory.locationDataInstance()

            coEvery { repository.isLocationEnabled() } returns true
            coEvery { exploreMapUseCase() } returns expectedLocation
            coEvery { getNearbyStops(expectedLocation) } returns
                NearbyStopsResult(stops = TestDataFactory.busStopList(), origin = NearbyStopsOrigin.CACHE)

            viewModel = createViewModel()

            viewModel.uiState.test {
                assertEquals(MapUiState.Loading, awaitItem())

                viewModel.loadUserLocationAndStops()

                val successState = awaitItem() as MapUiState.Success
                assertTrue(successState.isFromCache)
                assertFalse(successState.isDemo)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    @DisplayName("Debería informar ubicación aproximada cuando solo se otorga el permiso aproximado")
    fun shouldFlagApproximateLocationWhenOnlyCoarsePermissionIsGranted() =
        runTest {
            val expectedLocation = TestDataFactory.locationDataInstance()

            coEvery { repository.isLocationEnabled() } returns true
            coEvery { exploreMapUseCase() } returns expectedLocation
            coEvery { getNearbyStops(expectedLocation) } returns
                NearbyStopsResult(stops = TestDataFactory.busStopList(), origin = NearbyStopsOrigin.REMOTE)

            viewModel = createViewModel()

            viewModel.uiState.test {
                assertEquals(MapUiState.Loading, awaitItem())

                viewModel.onPermissionResult(fineGranted = false, coarseGranted = true)

                val successState = awaitItem() as MapUiState.Success
                assertTrue(successState.isApproximate)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    @DisplayName("Debería emitir PermissionDenied cuando no se otorga ningún permiso")
    fun shouldEmitPermissionDeniedWhenNoPermissionIsGranted() =
        runTest {
            viewModel = createViewModel()

            viewModel.uiState.test {
                assertEquals(MapUiState.Loading, awaitItem())

                viewModel.onPermissionResult(fineGranted = false, coarseGranted = false)

                assertEquals(MapUiState.PermissionDenied, awaitItem())

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    @DisplayName("Debería emitir LocationDisabled cuando el servicio de ubicación está desactivado")
    fun shouldEmitLocationDisabledWhenLocationServiceIsDisabled() =
        runTest {
            coEvery { repository.isLocationEnabled() } returns false

            viewModel = createViewModel()

            viewModel.uiState.test {
                assertEquals(MapUiState.Loading, awaitItem())

                viewModel.loadUserLocationAndStops()

                assertEquals(MapUiState.LocationDisabled, awaitItem())

                coVerify(exactly = 0) { exploreMapUseCase() }
                coVerify(exactly = 0) { getNearbyStops(any()) }

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    @DisplayName("Debería emitir LocationUnavailable cuando no hay una ubicación conocida")
    fun shouldEmitLocationUnavailableWhenThereIsNoKnownLocation() =
        runTest {
            coEvery { repository.isLocationEnabled() } returns true
            coEvery { exploreMapUseCase() } returns null

            viewModel = createViewModel()

            viewModel.uiState.test {
                assertEquals(MapUiState.Loading, awaitItem())

                viewModel.loadUserLocationAndStops()

                assertEquals(MapUiState.LocationUnavailable, awaitItem())

                coVerify(exactly = 0) { getNearbyStops(any()) }

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    @DisplayName("Debería emitir Error cuando hay SecurityException")
    fun shouldEmitErrorOnSecurityException() =
        runTest {
            coEvery { repository.isLocationEnabled() } returns true
            coEvery { exploreMapUseCase() } throws SecurityException("No location access")

            viewModel = createViewModel()

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
            coEvery { repository.isLocationEnabled() } returns true
            coEvery { exploreMapUseCase() } throws Exception()

            viewModel = createViewModel()

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
    @DisplayName("Debería actualizar la parada seleccionada por id y poder deseleccionarla")
    fun shouldUpdateSelectedBusStopWhenStopIsClicked() =
        runTest {
            val expectedLocation = TestDataFactory.locationDataInstance()
            val expectedStops = TestDataFactory.busStopList()
            val stopToSelect = expectedStops.first()

            coEvery { repository.isLocationEnabled() } returns true
            coEvery { exploreMapUseCase() } returns expectedLocation
            coEvery { getNearbyStops(expectedLocation) } returns
                NearbyStopsResult(stops = expectedStops, origin = NearbyStopsOrigin.REMOTE)

            viewModel = createViewModel()

            viewModel.uiState.test {
                assertEquals(MapUiState.Loading, awaitItem())

                viewModel.loadUserLocationAndStops()

                val initialSuccess = awaitItem() as MapUiState.Success

                assertNull(initialSuccess.selectedStopId)

                viewModel.selectBusStop(stopToSelect.stopId)

                val updatedSuccess = awaitItem() as MapUiState.Success

                assertEquals(stopToSelect.stopId, updatedSuccess.selectedStopId)

                viewModel.selectBusStop(null)

                val clearedSuccess = awaitItem() as MapUiState.Success

                assertNull(clearedSuccess.selectedStopId)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    @DisplayName("Debería filtrar las paradas cuando cambia el texto de búsqueda")
    fun shouldFilterStopsWhenSearchQueryChanges() =
        runTest {
            val expectedLocation = TestDataFactory.locationDataInstance()
            val expectedStops = TestDataFactory.busStopList()

            coEvery { repository.isLocationEnabled() } returns true
            coEvery { exploreMapUseCase() } returns expectedLocation
            coEvery { getNearbyStops(expectedLocation) } returns
                NearbyStopsResult(stops = expectedStops, origin = NearbyStopsOrigin.REMOTE)

            viewModel = createViewModel()

            viewModel.uiState.test {
                assertEquals(MapUiState.Loading, awaitItem())

                viewModel.loadUserLocationAndStops()

                awaitItem() as MapUiState.Success

                viewModel.onSearchQueryChange("Centro")

                val filteredState = awaitItem() as MapUiState.Success

                assertEquals(1, filteredState.filteredStops.size)
                assertEquals("Parada Centro", filteredState.filteredStops.first().name)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    @DisplayName("Debería guardar la búsqueda enviada recortada en el historial")
    fun shouldSaveTrimmedQueryWhenSearchIsSubmitted() =
        runTest {
            coEvery { busStopRepository.saveSearchQuery(any()) } just Runs

            viewModel = createViewModel()

            viewModel.submitSearch(" 96 ")

            coVerify(exactly = 1) { busStopRepository.saveSearchQuery("96") }
        }
}
