package ar.edu.unlam.scaffoldingandroid3.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.edu.unlam.scaffoldingandroid3.domain.model.distanceMetersTo
import ar.edu.unlam.scaffoldingandroid3.domain.repository.LocationRepository
import ar.edu.unlam.scaffoldingandroid3.domain.repository.PhotoReportRepository
import ar.edu.unlam.scaffoldingandroid3.domain.usecase.ExploreMapManually
import ar.edu.unlam.scaffoldingandroid3.domain.usecase.GetNearbyStops
import ar.edu.unlam.scaffoldingandroid3.ui.uistate.NoticeFilter
import ar.edu.unlam.scaffoldingandroid3.ui.uistate.NoticeItem
import ar.edu.unlam.scaffoldingandroid3.ui.uistate.NoticeSortOrder
import ar.edu.unlam.scaffoldingandroid3.ui.uistate.NoticeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.cancellation.CancellationException

private const val LOCATION_TIMEOUT_MILLIS = 10_000L
private const val RADIUS_METERS = 1000.0

@HiltViewModel
class NoticeViewModel
    @Inject
    constructor(
        private val exploreMapUseCase: ExploreMapManually,
        private val getNearbyStops: GetNearbyStops,
        private val locationRepository: LocationRepository,
        private val photoReportRepository: PhotoReportRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<NoticeUiState>(NoticeUiState.Loading)
        val uiState: StateFlow<NoticeUiState> = _uiState.asStateFlow()

        private var allNotices: List<NoticeItem> = emptyList()
        private var currentFilter: NoticeFilter = NoticeFilter.ALL
        private var currentSortOrder: NoticeSortOrder = NoticeSortOrder.CLOSEST

        private var loadJob: Job? = null

        init {
            load()
        }

        fun setFilter(filter: NoticeFilter) {
            currentFilter = filter
            updateSuccessState()
        }

        fun setSortOrder(sortOrder: NoticeSortOrder) {
            currentSortOrder = sortOrder
            updateSuccessState()
        }

        private fun updateSuccessState() {
            val filtered =
                when (currentFilter) {
                    NoticeFilter.ALL -> allNotices
                    NoticeFilter.WORKS ->
                        allNotices.filter {
                            it.report.observation.contains("obra", ignoreCase = true)
                        }
                    NoticeFilter.HARD_TO_FIND ->
                        allNotices.filter {
                            it.report.observation.contains("difícil", ignoreCase = true) ||
                                it.report.observation.contains("encontrar", ignoreCase = true)
                        }
                }

            val sorted =
                when (currentSortOrder) {
                    NoticeSortOrder.CLOSEST -> filtered.sortedBy { it.distanceMeters }
                    NoticeSortOrder.MOST_RECENT -> filtered.sortedByDescending { it.report.timestamp }
                }

            _uiState.value = NoticeUiState.Success(sorted, currentFilter, currentSortOrder)
        }

        fun load() {
            loadJob?.cancel()
            loadJob =
                viewModelScope.launch {
                    _uiState.value = NoticeUiState.Loading
                    try {
                        if (!locationRepository.isLocationEnabled()) {
                            _uiState.value = NoticeUiState.LocationDisabled
                            return@launch
                        }

                        val location = withTimeoutOrNull(LOCATION_TIMEOUT_MILLIS) { exploreMapUseCase() }
                        if (location == null) {
                            _uiState.value = NoticeUiState.LocationUnavailable
                            return@launch
                        }

                        val nearbyResult = getNearbyStops(location)
                        val stopMap = nearbyResult.stops.associateBy { it.stopId }
                        val distanceByStop =
                            nearbyResult
                                .stops
                                .associate { it.stopId to location.distanceMetersTo(it.locationData) }
                                .filterValues { it <= RADIUS_METERS }

                        photoReportRepository
                            .observeByStopIds(distanceByStop.keys.toList())
                            .map { reports ->
                                reports
                                    .mapNotNull { report ->
                                        val distance = distanceByStop[report.stopId]
                                        val stop = stopMap[report.stopId]
                                        if (distance != null && stop != null) {
                                            NoticeItem(
                                                report = report,
                                                distanceMeters = distance,
                                                stopName = stop.name,
                                                latitude = stop.locationData.latitude.value,
                                                longitude = stop.locationData.longitude.value,
                                            )
                                        } else {
                                            null
                                        }
                                    }.sortedBy { it.distanceMeters }
                            }.collect { notices ->
                                allNotices = notices
                                updateSuccessState()
                            }
                    } catch (e: CancellationException) {
                        throw e
                    } catch (_: SecurityException) {
                        _uiState.value = NoticeUiState.PermissionDenied
                    } catch (_: Exception) {
                        _uiState.value = NoticeUiState.Error
                    }
                }
        }
    }
