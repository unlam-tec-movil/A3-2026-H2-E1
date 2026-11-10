package ar.edu.unlam.scaffoldingandroid3.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.edu.unlam.scaffoldingandroid3.data.BusStopStubRepo
import ar.edu.unlam.scaffoldingandroid3.data.LocationRepo
import ar.edu.unlam.scaffoldingandroid3.domain.BusStop
import ar.edu.unlam.scaffoldingandroid3.domain.BusStopRepository
import ar.edu.unlam.scaffoldingandroid3.domain.GeoPoint
import ar.edu.unlam.scaffoldingandroid3.domain.SearchHistory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val SEARCH_RADIUS_METERS = 1_000.0

sealed interface NearbyStopsUiState {
    data object Locating : NearbyStopsUiState

    data object PermissionDenied : NearbyStopsUiState

    data object LocationDisabled : NearbyStopsUiState

    data object LocationUnavailable : NearbyStopsUiState

    data class Error(
        val message: String,
    ) : NearbyStopsUiState

    data class Ready(
        val center: GeoPoint,
        val stops: List<BusStop>,
        val isApproximate: Boolean = false,
        val isFromCache: Boolean = false,
        val isDemo: Boolean = false,
    ) : NearbyStopsUiState
}

@HiltViewModel
class NearbyStopsViewModel
    @Inject
    constructor(
        private val busStopRepository: BusStopRepository,
        val locationRepo: LocationRepo,
    ) : ViewModel() {
        private val demoRepository: BusStopRepository = BusStopStubRepo()

        val recentSearches: StateFlow<List<SearchHistory>> =
            busStopRepository
                .getRecentSearches()
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = emptyList(),
                )

        fun loadStopsForLocation(
            center: GeoPoint,
            isApproximate: Boolean,
            onStateUpdated: (NearbyStopsUiState) -> Unit,
        ) {
            viewModelScope.launch {
                val stops =
                    try {
                        busStopRepository.getNearbyStops(center, SEARCH_RADIUS_METERS)
                    } catch (e: Exception) {
                        emptyList()
                    }
                if (stops.isNotEmpty()) {
                    onStateUpdated(
                        NearbyStopsUiState.Ready(
                            center = center,
                            stops = stops,
                            isApproximate = isApproximate,
                            isFromCache = false,
                        ),
                    )
                } else {
                    // Sin servidor o sin datos en la zona: mostramos paradas de ejemplo para no dejar la pantalla vacía.
                    onStateUpdated(
                        NearbyStopsUiState.Ready(
                            center = center,
                            stops = demoRepository.getNearbyStops(center, SEARCH_RADIUS_METERS),
                            isApproximate = isApproximate,
                            isDemo = true,
                        ),
                    )
                }
            }
        }

        fun onSearchSubmitted(query: String) {
            val trimmed = query.trim()
            if (trimmed.isNotBlank()) {
                viewModelScope.launch {
                    busStopRepository.saveSearchQuery(trimmed)
                }
            }
        }
    }
