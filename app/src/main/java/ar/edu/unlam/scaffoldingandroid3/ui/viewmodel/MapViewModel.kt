package ar.edu.unlam.scaffoldingandroid3.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.edu.unlam.scaffoldingandroid3.domain.enums.NearbyStopsOrigin
import ar.edu.unlam.scaffoldingandroid3.domain.model.Latitude
import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationData
import ar.edu.unlam.scaffoldingandroid3.domain.model.Longitude
import ar.edu.unlam.scaffoldingandroid3.domain.model.NearbyStopsResult
import ar.edu.unlam.scaffoldingandroid3.domain.model.SearchHistory
import ar.edu.unlam.scaffoldingandroid3.domain.repository.BusStopRepository
import ar.edu.unlam.scaffoldingandroid3.domain.repository.LocationRepository
import ar.edu.unlam.scaffoldingandroid3.domain.usecase.ExploreMapManually
import ar.edu.unlam.scaffoldingandroid3.domain.usecase.FilterBusStops
import ar.edu.unlam.scaffoldingandroid3.domain.usecase.GetNearbyStops
import ar.edu.unlam.scaffoldingandroid3.ui.uistate.MapUiState
import dagger.hilt.android.lifecycle.HiltViewModel
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

/** Punto de partida cuando el pasajero prefiere explorar el mapa sin usar su ubicación. */
private val DEFAULT_MANUAL_LOCATION = LocationData(Latitude(-34.6705), Longitude(-58.5628))

/**
 * ViewModel unificado de la pantalla principal: mapa y lista de paradas cercanas.
 *
 * Absorbe lo mejor de las dos arquitecturas que se fusionaron:
 * - Carga la ubicación con timeout y las paradas cercanas con uso de casos.
 * - Comunica por qué no hay ubicación (permiso, servicio deshabilitado o sin fix) en lugar
 *   de caer silenciosamente a una ubicación por defecto.
 * - Reporta el origen de las paradas (remoto, caché o demo) para que la pantalla informe.
 * - Filtra las paradas por texto de búsqueda y persiste el historial de búsquedas.
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
            viewModelScope.launch {
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
                } catch (e: SecurityException) {
                    _uiState.value = MapUiState.Error(e.message ?: LOCATION_ERROR)
                } catch (_: Exception) {
                    _uiState.value = MapUiState.Error(UNKNOWN_ERROR)
                }
            }
        }

        /** Carga las paradas alrededor de un punto fijo, sin usar la ubicación del pasajero. */
        fun exploreManually() {
            viewModelScope.launch {
                _uiState.value = MapUiState.Loading
                try {
                    updateWithStops(DEFAULT_MANUAL_LOCATION, getNearbyStops(DEFAULT_MANUAL_LOCATION), isApproximate = false)
                } catch (_: Exception) {
                    _uiState.value = MapUiState.Error(UNKNOWN_ERROR)
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
            _uiState.value = current.copy(selectedStopId = stopId)
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
