package ar.edu.unlam.scaffoldingandroid3.ui.screens

import ar.edu.unlam.scaffoldingandroid3.data.LocationRepo
import ar.edu.unlam.scaffoldingandroid3.domain.BusLine
import ar.edu.unlam.scaffoldingandroid3.domain.BusStop
import ar.edu.unlam.scaffoldingandroid3.domain.BusStopRepository
import ar.edu.unlam.scaffoldingandroid3.domain.GeoPoint
import ar.edu.unlam.scaffoldingandroid3.domain.SearchHistory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NearbyStopsViewModelTest {
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
    private val fakeRepository = FakeBusStopRepository()
    private val fakeLocationRepo = LocationRepo(context = FakeContext())

    private lateinit var viewModel: NearbyStopsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = NearbyStopsViewModel(fakeRepository, fakeLocationRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_isLocating() {
        assertEquals(NearbyStopsUiState.Locating, viewModel.uiState.value)
    }

    @Test
    fun onPermissionDenied_updatesUiStateToPermissionDenied() {
        viewModel.onPermissionDenied()
        assertEquals(NearbyStopsUiState.PermissionDenied, viewModel.uiState.value)
    }

    @Test
    fun onLocationDisabled_updatesUiStateToLocationDisabled() {
        viewModel.onLocationDisabled()
        assertEquals(NearbyStopsUiState.LocationDisabled, viewModel.uiState.value)
    }

    @Test
    fun onLocationUnavailable_updatesUiStateToLocationUnavailable() {
        viewModel.onLocationUnavailable()
        assertEquals(NearbyStopsUiState.LocationUnavailable, viewModel.uiState.value)
    }

    @Test
    fun setLocating_updatesUiStateToLocating() {
        viewModel.onPermissionDenied()
        viewModel.setLocating()
        assertEquals(NearbyStopsUiState.Locating, viewModel.uiState.value)
    }

    @Test
    fun loadStopsForLocation_success_updatesUiStateToReady() =
        runTest {
            val center = GeoPoint(-34.6705, -58.5628)
            val sampleStops =
                listOf(
                    BusStop("1", "Parada 1", listOf(BusLine("96", "Plaza Once")), center),
                )
            fakeRepository.stubStops = sampleStops

            viewModel.loadStopsForLocation(center, isApproximate = false)

            val state = viewModel.uiState.value
            assertTrue(state is NearbyStopsUiState.Ready)
            val readyState = state as NearbyStopsUiState.Ready
            assertEquals(center, readyState.center)
            assertEquals(1, readyState.stops.size)
            assertEquals("Parada 1", readyState.stops[0].name)
        }

    @Test
    fun loadStopsForLocation_failure_updatesUiStateToError() =
        runTest {
            val center = GeoPoint(-34.6705, -58.5628)
            fakeRepository.shouldThrowError = true

            viewModel.loadStopsForLocation(center, isApproximate = false)

            val state = viewModel.uiState.value
            assertTrue(state is NearbyStopsUiState.Error)
            val errorState = state as NearbyStopsUiState.Error
            assertEquals("Error simulado de red", errorState.message)
        }

    @Test
    fun onSearchSubmitted_validQuery_savesSearchToRepository() =
        runTest {
            viewModel.onSearchSubmitted(" 96 ")
            assertEquals("96", fakeRepository.lastSavedSearchQuery)
        }
}

private class FakeContext : android.content.ContextWrapper(null)

private class FakeBusStopRepository : BusStopRepository {
    var stubStops: List<BusStop> = emptyList()
    var shouldThrowError = false
    var lastSavedSearchQuery: String? = null
    private val searchesFlow = MutableStateFlow<List<SearchHistory>>(emptyList())

    override suspend fun getNearbyStops(
        center: GeoPoint,
        radiusMeters: Double,
    ): List<BusStop> {
        if (shouldThrowError) {
            throw RuntimeException("Error simulado de red")
        }
        return stubStops
    }

    override fun getCachedStops(): Flow<List<BusStop>> = flowOf(emptyList())

    override suspend fun saveSearchQuery(query: String) {
        lastSavedSearchQuery = query
    }

    override fun getRecentSearches(): Flow<List<SearchHistory>> = searchesFlow
}
