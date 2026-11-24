package ar.edu.unlam.scaffoldingandroid3.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.edu.unlam.scaffoldingandroid3.domain.enums.NearbyStopsOrigin
import ar.edu.unlam.scaffoldingandroid3.domain.model.BusStop
import ar.edu.unlam.scaffoldingandroid3.domain.model.Latitude
import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationData
import ar.edu.unlam.scaffoldingandroid3.domain.model.Longitude
import ar.edu.unlam.scaffoldingandroid3.domain.model.NearbyStopsResult
import ar.edu.unlam.scaffoldingandroid3.domain.model.SearchHistory
import ar.edu.unlam.scaffoldingandroid3.domain.model.distanceMetersTo
import ar.edu.unlam.scaffoldingandroid3.domain.repository.BusStopRepository
import ar.edu.unlam.scaffoldingandroid3.domain.repository.LocationRepository
import ar.edu.unlam.scaffoldingandroid3.domain.repository.SensorRepository
import ar.edu.unlam.scaffoldingandroid3.domain.usecase.CalculateDestinationDirection
import ar.edu.unlam.scaffoldingandroid3.domain.usecase.CheckOrientationSensorAvailability
import ar.edu.unlam.scaffoldingandroid3.domain.usecase.ExploreMapManually
import ar.edu.unlam.scaffoldingandroid3.domain.usecase.FilterBusStops
import ar.edu.unlam.scaffoldingandroid3.domain.usecase.GetNearbyStops
import ar.edu.unlam.scaffoldingandroid3.domain.usecase.GetPedestrianRoute
import ar.edu.unlam.scaffoldingandroid3.ui.uistate.MapUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

private const val LOCATION_TIMEOUT_MILLIS = 10_000L
private const val LOCATION_ERROR = "Error loading user location"
private const val UNKNOWN_ERROR = "Unknown error occurred while loading the user location"
private const val REROUTE_THRESHOLD_METERS = 15.0
private const val REROUTE_MIN_INTERVAL_MILLIS = 5_000L

/** Punto de partida cuando el pasajero prefiere explorar el mapa sin usar su ubicación. */
private val DEFAULT_MANUAL_LOCATION = LocationData(Latitude(-34.6705), Longitude(-58.5628))

/**
 * ViewModel unificado de la pantalla principal: mapa, lista de paradas cercanas y navegación.
 */
@HiltViewModel
class MapViewModel
    @Inject
    constructor(
        private val exploreMapUseCase: ExploreMapManually,
        private val getNearbyStops: GetNearbyStops,
        private val filterBusStops: FilterBusStops,
        private val busStopRepository: BusStopRepository,
        private val locationRepository: LocationRepository,
        private val checkOrientationSensorAvailability: CheckOrientationSensorAvailability,
        private val calculateDestinationDirection: CalculateDestinationDirection,
        private val sensorRepository: SensorRepository,
        private val getPedestrianRoute: GetPedestrianRoute,
    ) : ViewModel() {
        @Suppress("ktlint:standard:backing-property-naming")
        private val _uiState = MutableStateFlow<MapUiState>(MapUiState.Loading)
        val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

        val recentSearches: StateFlow<List<SearchHistory>> =
            busStopRepository
                .getRecentSearches()
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = emptyList(),
                )

        private var currentQuery = ""
        private var clusterFilterStopIds: Set<Long>? = null
        private var orientationJob: Job? = null
        private var locationUpdatesJob: Job? = null
        private var isRerouting = false
        private var lastRerouteTimeMillis = 0L

        override fun onCleared() {
            stopNavigationSensors()
            stopLocationTracking()
        }

        /**
         * Registra el resultado del pedido de permisos y, si hubo alguno otorgado, arranca
         * la carga. Si solo se otorgó el permiso aproximado, se informa en el estado para
         * que la pantalla muestre el banner correspondiente.
         */
        fun onPermissionResult(
            fineGranted: Boolean,
            coarseGranted: Boolean,
        ) {
            if (!fineGranted && !coarseGranted) {
                _uiState.value = MapUiState.PermissionDenied
                return
            }
            loadUserLocationAndStops(isApproximate = coarseGranted && !fineGranted)
        }

        /**
         * Carga la ubicación del pasajero y las paradas cercanas.
         *
         * Si el servicio de ubicación está apagado → [MapUiState.LocationDisabled].
         * Si no hay una ubicación conocida dentro del timeout → [MapUiState.LocationUnavailable].
         */
        fun loadUserLocationAndStops(isApproximate: Boolean = false) {
            stopLocationTracking()
            viewModelScope.launch {
                stopNavigationSensors()
                _uiState.value = MapUiState.Loading

                try {
                    if (!locationRepository.isLocationEnabled()) {
                        _uiState.value = MapUiState.LocationDisabled
                        return@launch
                    }

                    val location = withTimeoutOrNull(LOCATION_TIMEOUT_MILLIS) { exploreMapUseCase() }
                    if (location == null) {
                        _uiState.value = MapUiState.LocationUnavailable
                        return@launch
                    }

                    updateWithStops(location, getNearbyStops(location), isApproximate)

                    startLocationTracking()
                } catch (e: SecurityException) {
                    _uiState.value = MapUiState.Error(e.message ?: LOCATION_ERROR)
                } catch (_: Exception) {
                    _uiState.value = MapUiState.Error(UNKNOWN_ERROR)
                }
            }
        }

        /** Carga las paradas alrededor de un punto fijo, sin usar la ubicación del pasajero. */
        fun exploreManually() {
            stopLocationTracking()
            viewModelScope.launch {
                stopNavigationSensors()
                _uiState.value = MapUiState.Loading
                try {
                    updateWithStops(
                        DEFAULT_MANUAL_LOCATION,
                        getNearbyStops(DEFAULT_MANUAL_LOCATION),
                        isApproximate = false,
                    )
                } catch (_: Exception) {
                    _uiState.value = MapUiState.Error(UNKNOWN_ERROR)
                }
            }
        }

        private fun startLocationTracking() {
            stopLocationTracking()
            locationUpdatesJob =
                viewModelScope.launch {
                    try {
                        locationRepository.getLocationUpdates().collect { newLocation ->
                            onLocationUpdated(newLocation)
                        }
                    } catch (_: Exception) {
                        // Ignorar fallos temporales del sensor GPS
                    }
                }
        }

        private fun stopLocationTracking() {
            locationUpdatesJob?.cancel()
            locationUpdatesJob = null
        }

        private fun onLocationUpdated(newLocation: LocationData) {
            val current = _uiState.value as? MapUiState.Success ?: return

            var updatedRoutePoints = current.navigationRoutePoints
            var updatedBearing = current.navigationBearing

            if (current.isNavigationMode && current.selectedStopId != null) {
                val targetStop =
                    current.busStops.firstOrNull { it.stopId == current.selectedStopId }
                        ?: current.filteredStops.firstOrNull { it.stopId == current.selectedStopId }

                if (targetStop != null) {
                    updatedBearing = calculateDestinationDirection(newLocation, targetStop.locationData).bearing.value

                    val routePoints = current.navigationRoutePoints
                    if (!routePoints.isNullOrEmpty()) {
                        var closestIndex = 0
                        var minDistance = Double.MAX_VALUE

                        routePoints.forEachIndexed { index, point ->
                            val dist = newLocation.distanceMetersTo(point)
                            if (dist < minDistance) {
                                minDistance = dist
                                closestIndex = index
                            }
                        }

                        val currentTime = System.currentTimeMillis()
                        if (minDistance > REROUTE_THRESHOLD_METERS &&
                            !isRerouting &&
                            currentTime - lastRerouteTimeMillis > REROUTE_MIN_INTERVAL_MILLIS
                        ) {
                            recalculateRoute(newLocation, targetStop)
                        } else if (minDistance <= REROUTE_THRESHOLD_METERS) {
                            updatedRoutePoints = listOf(newLocation) + routePoints.subList(closestIndex, routePoints.size)
                        }
                    }
                }
            }

            _uiState.value =
                current.copy(
                    center = newLocation,
                    navigationBearing = updatedBearing,
                    navigationRoutePoints = updatedRoutePoints,
                )
        }

        private fun recalculateRoute(
            userLocation: LocationData,
            targetStop: BusStop,
        ) {
            isRerouting = true
            lastRerouteTimeMillis = System.currentTimeMillis()
            viewModelScope.launch {
                try {
                    val routeResult = getPedestrianRoute(userLocation, targetStop.locationData)
                    val updatedCurrent = _uiState.value as? MapUiState.Success ?: return@launch
                    if (updatedCurrent.isNavigationMode && updatedCurrent.selectedStopId == targetStop.stopId) {
                        val points = routeResult.getOrNull()?.points
                        if (!points.isNullOrEmpty()) {
                            _uiState.value = updatedCurrent.copy(navigationRoutePoints = points)
                        }
                    }
                } finally {
                    isRerouting = false
                }
            }
        }

        /** Filtra las paradas del estado actual mientras el pasajero escribe. */
        fun onSearchQueryChange(query: String) {
            currentQuery = query
            clusterFilterStopIds = null
            val current = _uiState.value as? MapUiState.Success ?: return
            _uiState.value =
                current.copy(
                    clusterFilterStopIds = null,
                    filteredStops = filterBusStops(current.busStops, query),
                )
        }

        /** Filtra las paradas de la lista para mostrar únicamente las pertenecientes al cluster seleccionado. */
        fun filterByCluster(stopIds: Set<Long>) {
            val current = _uiState.value as? MapUiState.Success ?: return
            clusterFilterStopIds = stopIds
            val baseStops = filterBusStops(current.busStops, currentQuery)
            val clusteredStops = baseStops.filter { it.stopId in stopIds }
            _uiState.value =
                current.copy(
                    clusterFilterStopIds = stopIds,
                    filteredStops = clusteredStops,
                )
        }

        /** Restablece el filtrado por cluster volviendo a mostrar las paradas de la búsqueda actual. */
        fun clearClusterFilter() {
            val current = _uiState.value as? MapUiState.Success ?: return
            clusterFilterStopIds = null
            val baseStops = filterBusStops(current.busStops, currentQuery)
            _uiState.value =
                current.copy(
                    clusterFilterStopIds = null,
                    filteredStops = baseStops,
                )
        }

        /** Persiste una búsqueda enviada en el historial (Enter o chip de historial). */
        fun submitSearch(query: String) {
            val trimmed = query.trim()
            if (trimmed.isBlank()) return
            viewModelScope.launch {
                busStopRepository.saveSearchQuery(trimmed)
            }
        }

        /**
         * Selecciona la parada identificada por [stopId], o deselecciona si es `null`.
         */
        fun selectBusStop(stopId: Long?) {
            val current = _uiState.value as? MapUiState.Success ?: return
            if (stopId == null) {
                if (current.isNavigationMode) {
                    stopNavigation()
                }
                _uiState.value = (_uiState.value as? MapUiState.Success)?.copy(selectedStopId = null) ?: return
            } else {
                if (current.isNavigationMode && stopId != current.selectedStopId) {
                    val newTarget =
                        current.busStops.firstOrNull { it.stopId == stopId }
                            ?: current.filteredStops.firstOrNull { it.stopId == stopId }
                    if (newTarget != null) {
                        _uiState.value = current.copy(selectedStopId = stopId)
                        startNavigation(newTarget)
                    } else {
                        stopNavigation()
                        _uiState.value = (_uiState.value as? MapUiState.Success)?.copy(selectedStopId = stopId) ?: return
                    }
                } else {
                    _uiState.value = current.copy(selectedStopId = stopId)
                }
            }
        }

        /**
         * Alterna el modo de navegación hacia la parada seleccionada.
         */
        fun toggleNavigationMode(targetBusStop: BusStop? = null) {
            val current = _uiState.value as? MapUiState.Success ?: return
            if (current.isNavigationMode) {
                stopNavigation()
            } else {
                val target =
                    targetBusStop
                        ?: current.selectedStopId?.let { id ->
                            current.busStops.firstOrNull { it.stopId == id }
                                ?: current.filteredStops.firstOrNull { it.stopId == id }
                        }
                        ?: return
                startNavigation(target)
            }
        }

        fun pauseNavigationSensors() {
            val current = _uiState.value as? MapUiState.Success ?: return
            if (current.isNavigationMode) {
                sensorRepository.stopOrientationUpdates()
            }
        }

        fun resumeNavigationSensors() {
            val current = _uiState.value as? MapUiState.Success ?: return
            if (current.isNavigationMode && current.isSensorAvailable) {
                sensorRepository.startOrientationUpdates()
            }
        }

        private fun startNavigation(targetStop: BusStop) {
            val current = _uiState.value as? MapUiState.Success ?: return
            val bearing = calculateDestinationDirection(current.center, targetStop.locationData).bearing.value
            val sensorAvailable = checkOrientationSensorAvailability()

            _uiState.value =
                current.copy(
                    isNavigationMode = true,
                    selectedStopId = targetStop.stopId,
                    navigationBearing = bearing,
                    isSensorAvailable = sensorAvailable,
                    deviceAzimuth = sensorRepository.orientationDegrees.value ?: 0f,
                    navigationRoutePoints = null,
                )

            if (sensorAvailable) {
                startNavigationSensors()
            }

            viewModelScope.launch {
                val routeResult = getPedestrianRoute(current.center, targetStop.locationData)
                val updatedCurrent = _uiState.value as? MapUiState.Success ?: return@launch
                if (updatedCurrent.isNavigationMode && updatedCurrent.selectedStopId == targetStop.stopId) {
                    _uiState.value =
                        updatedCurrent.copy(
                            navigationRoutePoints = routeResult.getOrNull()?.points,
                        )
                }
            }
        }

        private fun stopNavigation() {
            stopNavigationSensors()
            val current = _uiState.value as? MapUiState.Success ?: return
            _uiState.value =
                current.copy(
                    isNavigationMode = false,
                    navigationRoutePoints = null,
                    navigationBearing = null,
                    deviceAzimuth = null,
                )
        }

        private fun startNavigationSensors() {
            sensorRepository.startOrientationUpdates()
            orientationJob?.cancel()
            orientationJob =
                viewModelScope.launch {
                    sensorRepository.orientationDegrees.collect { azimuth ->
                        if (azimuth != null) {
                            val current = _uiState.value as? MapUiState.Success ?: return@collect
                            if (current.isNavigationMode) {
                                _uiState.value = current.copy(deviceAzimuth = azimuth)
                            }
                        }
                    }
                }
        }

        private fun stopNavigationSensors() {
            orientationJob?.cancel()
            orientationJob = null
            sensorRepository.stopOrientationUpdates()
        }

        private fun updateWithStops(
            center: LocationData,
            result: NearbyStopsResult,
            isApproximate: Boolean,
        ) {
            val baseStops = filterBusStops(result.stops, currentQuery)
            val finalStops =
                clusterFilterStopIds?.let { ids ->
                    baseStops.filter { it.stopId in ids }
                } ?: baseStops

            _uiState.value =
                MapUiState.Success(
                    center = center,
                    busStops = result.stops,
                    filteredStops = finalStops,
                    clusterFilterStopIds = clusterFilterStopIds,
                    isApproximate = isApproximate,
                    isFromCache = result.origin == NearbyStopsOrigin.CACHE,
                    isDemo = result.origin == NearbyStopsOrigin.DEMO,
                )
        }
    }
