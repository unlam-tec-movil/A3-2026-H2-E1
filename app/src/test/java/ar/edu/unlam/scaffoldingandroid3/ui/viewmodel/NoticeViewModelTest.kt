package ar.edu.unlam.scaffoldingandroid3.ui.viewmodel

import app.cash.turbine.test
import ar.edu.unlam.scaffoldingandroid3.dispatcher.MainDispatcherRule
import ar.edu.unlam.scaffoldingandroid3.domain.enums.NearbyStopsOrigin
import ar.edu.unlam.scaffoldingandroid3.domain.model.NearbyStopsResult
import ar.edu.unlam.scaffoldingandroid3.domain.model.PhotoReport
import ar.edu.unlam.scaffoldingandroid3.domain.repository.LocationRepository
import ar.edu.unlam.scaffoldingandroid3.domain.repository.PhotoReportRepository
import ar.edu.unlam.scaffoldingandroid3.domain.usecase.ExploreMapManually
import ar.edu.unlam.scaffoldingandroid3.domain.usecase.GetNearbyStops
import ar.edu.unlam.scaffoldingandroid3.factory.TestDataFactory
import ar.edu.unlam.scaffoldingandroid3.ui.uistate.NoticeFilter
import ar.edu.unlam.scaffoldingandroid3.ui.uistate.NoticeUiState
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class NoticeViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val exploreMapUseCase = mockk<ExploreMapManually>()
    private val getNearbyStops = mockk<GetNearbyStops>()
    private val locationRepository = mockk<LocationRepository>()
    private val photoReportRepository = mockk<PhotoReportRepository>()

    private fun createViewModel() =
        NoticeViewModel(
            exploreMapUseCase = exploreMapUseCase,
            getNearbyStops = getNearbyStops,
            locationRepository = locationRepository,
            photoReportRepository = photoReportRepository,
        )

    @Test
    fun `given location disabled, when loading, then state is LocationDisabled`() =
        runTest {
            coEvery { locationRepository.isLocationEnabled() } returns false

            val viewModel = createViewModel()

            viewModel.uiState.test {
                assertEquals(NoticeUiState.LocationDisabled, awaitItem())
            }
        }

    @Test
    fun `given location unavailable, when loading, then state is LocationUnavailable`() =
        runTest {
            coEvery { locationRepository.isLocationEnabled() } returns true
            coEvery { exploreMapUseCase() } returns null

            val viewModel = createViewModel()

            viewModel.uiState.test {
                assertEquals(NoticeUiState.LocationUnavailable, awaitItem())
            }
        }

    @Test
    fun `given security exception, when loading, then state is PermissionDenied`() =
        runTest {
            coEvery { locationRepository.isLocationEnabled() } throws SecurityException("Denied")

            val viewModel = createViewModel()

            viewModel.uiState.test {
                assertEquals(NoticeUiState.PermissionDenied, awaitItem())
            }
        }

    @Test
    fun `given generic exception, when loading, then state is Error`() =
        runTest {
            coEvery { locationRepository.isLocationEnabled() } throws RuntimeException("Error")

            val viewModel = createViewModel()

            viewModel.uiState.test {
                assertEquals(NoticeUiState.Error, awaitItem())
            }
        }

    @Test
    fun `given location and nearby stops with reports, when loading, then state is Success with sorted notices and stop name`() =
        runTest {
            val location = TestDataFactory.locationDataInstance()
            val stops = TestDataFactory.busStopList()
            val report =
                PhotoReport(
                    id = "rep-1",
                    photoUri = "content://photo/1",
                    observation = "Sin cartel",
                    timestamp = 1000L,
                    stopId = 1L,
                )

            coEvery { locationRepository.isLocationEnabled() } returns true
            coEvery { exploreMapUseCase() } returns location
            coEvery { getNearbyStops(location) } returns NearbyStopsResult(stops, NearbyStopsOrigin.REMOTE)
            every { photoReportRepository.observeByStopIds(any()) } returns flowOf(listOf(report))

            val viewModel = createViewModel()

            viewModel.uiState.test {
                val state = awaitItem() as NoticeUiState.Success
                assertEquals(1, state.notices.size)
                assertEquals("rep-1", state.notices[0].report.id)
                assertEquals("Parada Plaza", state.notices[0].stopName)
            }
        }

    @Test
    fun `given notices, when setting works filter, then emits filtered notices`() =
        runTest {
            val location = TestDataFactory.locationDataInstance()
            val stops = TestDataFactory.busStopList()
            val report1 = PhotoReport("1", "content://1", "Obras en la calle", 1000L, 1L)
            val report2 = PhotoReport("2", "content://2", "Parada normal", 1000L, 2L)

            coEvery { locationRepository.isLocationEnabled() } returns true
            coEvery { exploreMapUseCase() } returns location
            coEvery { getNearbyStops(location) } returns NearbyStopsResult(stops, NearbyStopsOrigin.REMOTE)
            every { photoReportRepository.observeByStopIds(any()) } returns flowOf(listOf(report1, report2))

            val viewModel = createViewModel()

            viewModel.uiState.test {
                val state = awaitItem() as NoticeUiState.Success
                assertEquals(2, state.notices.size)

                viewModel.setFilter(NoticeFilter.WORKS)
                val filteredState = awaitItem() as NoticeUiState.Success
                assertEquals(1, filteredState.notices.size)
                assertEquals("Obras en la calle", filteredState.notices[0].report.observation)
            }
        }
}
