package ar.edu.unlam.scaffoldingandroid3.ui.uistate

import ar.edu.unlam.scaffoldingandroid3.domain.model.BusStop
import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationData

sealed interface MapUiState {
    data object Loading : MapUiState

    data object PermissionDenied : MapUiState

    data object LocationDisabled : MapUiState

    data object LocationUnavailable : MapUiState

    data class Error(
        val message: String,
    ) : MapUiState

    data class Success(
        val center: LocationData,
        val busStops: List<BusStop>,
        val filteredStops: List<BusStop> = busStops,
        val selectedStopId: Long? = null,
        val clusterFilterStopIds: Set<Long>? = null,
        val isApproximate: Boolean = false,
        val isFromCache: Boolean = false,
        val isDemo: Boolean = false,
        val isNavigationMode: Boolean = false,
        val navigationRoutePoints: List<LocationData>? = null,
        val navigationBearing: Double? = null,
        val deviceAzimuth: Float? = null,
        val isSensorAvailable: Boolean = true,
    ) : MapUiState
}
