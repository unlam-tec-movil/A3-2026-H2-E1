package ar.edu.unlam.scaffoldingandroid3.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.edu.unlam.scaffoldingandroid3.domain.model.distanceMetersTo
import ar.edu.unlam.scaffoldingandroid3.domain.repository.LocationRepository
import ar.edu.unlam.scaffoldingandroid3.domain.repository.PhotoReportRepository
import ar.edu.unlam.scaffoldingandroid3.domain.usecase.ExploreMapManually
import ar.edu.unlam.scaffoldingandroid3.domain.usecase.GetNearbyStops
import ar.edu.unlam.scaffoldingandroid3.ui.uistate.NoticeItem
import ar.edu.unlam.scaffoldingandroid3.ui.uistate.NoticeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
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

        private var loadJob: Job? = null

        init {
            load()
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

                        val distanceByStop =
                            getNearbyStops(location)
                                .stops
                                .associate { it.stopId to location.distanceMetersTo(it.locationData) }
                                .filterValues { it <= RADIUS_METERS }

                        photoReportRepository
                            .observeByStopIds(distanceByStop.keys.toList())
                            .map { reports ->
                                reports
                                    .mapNotNull { report ->
                                        distanceByStop[report.stopId]?.let { NoticeItem(report, it) }
                                    }.sortedBy { it.distanceMeters }
                            }.collect { _uiState.value = NoticeUiState.Success(it) }
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
