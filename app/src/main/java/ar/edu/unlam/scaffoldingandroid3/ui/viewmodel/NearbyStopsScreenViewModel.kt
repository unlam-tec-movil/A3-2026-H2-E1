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
import kotlin.time.Duration.Companion.milliseconds

private const val LOCATION_TIMEOUT_MILLIS = 10_000L
private const val LOCATION_ERROR = "Error loading user location"
private const val UNKNOWN_ERROR = "Unknown error occurred while loading the user location"
private val DEFAULT_MANUAL_LOCATION = LocationData(Latitude(-34.6705), Longitude(-58.5628))

/**
 * ViewModel para la pantalla de detección de paradas de autobús cercanas.
 *
 * Gestiona la lógica de carga de ubicación del usuario, obtención de paradas cercanas,
 * filtrado de resultados y manejo de la interfaz de usuario mediante [MapUiState].
 * También soporta búsquedas manuales y exploración del mapa.
 *
 * @see ExploreMapManually
 * @see GetNearbyStops
 * @see FilterBusStops
 */
@HiltViewModel
class NearbyStopsScreenViewModel
    @Inject
    constructor(
        private val exploreMapUseCase: ExploreMapManually,
        private val getNearbyStops: GetNearbyStops,
        private val filterBusStops: FilterBusStops,
        private val busStopRepository: BusStopRepository,
        private val locationRepository: LocationRepository,
    ) : ViewModel() {
        /**
         * Estado de la interfaz de usuario del ViewModel.
         *
         * Emite el estado actual de la pantalla, incluyendo carga, éxito, errores,
         * permisos y resultados de búsqueda.
         *
         * @see MapUiState
         */
        @Suppress("ktlint:standard:backing-property-naming")
        private val _uiState = MutableStateFlow<MapUiState>(MapUiState.Loading)
        val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

        /**
         * Historial de búsquedas recientes del usuario.
         *
         * Emite una lista de consultas de búsqueda guardadas en el repositorio.
         *
         * @see SearchHistory
         */
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
         * Maneja el resultado de la solicitud de permisos de ubicación.
         *
         * Si no se tienen permisos finos ni gruesos, muestra un estado de
         * permiso denegado. Si solo se tiene permiso grueso, carga la ubicación
         * y las paradas con aproximación.
         *
         * @param fineGranted Indica si el permiso de ubicación fina fue concedido.
         * @param coarseGranted Indica si el permiso de ubicación gruesa fue concedido.
         * @see MapUiState.PermissionDenied
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
         * Carga la ubicación del usuario y las paradas de autobús cercanas.
         *
         * Verifica si el GPS está habilitado, obtiene la ubicación con un timeout de
         * 10 segundos, y luego obtiene las paradas cercanas. Maneja excepciones de
         * seguridad y errores genéricos actualizando el estado del UI.
         *
         * @param isApproximate Indica si la ubicación es aproximada (solo permiso grueso).
         * @see MapUiState.LocationDisabled
         * @see MapUiState.LocationUnavailable
         */
        fun loadUserLocationAndStops(isApproximate: Boolean = false) {
            viewModelScope.launch {
                _uiState.value = MapUiState.Loading

                try {
                    if (!locationRepository.isLocationEnabled()) {
                        _uiState.value = MapUiState.LocationDisabled
                        return@launch
                    }

                    val location = withTimeoutOrNull(LOCATION_TIMEOUT_MILLIS.milliseconds) { exploreMapUseCase() }
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

        /**
         * Explora el mapa manualmente usando una ubicación predeterminada.
         *
         * Utiliza una ubicación fija en Buenos Aires (-34.6705, -58.5628) para
         * mostrar las paradas cercanas sin acceder a la ubicación del usuario.
         *
         * @see DEFAULT_MANUAL_LOCATION
         */
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

        /**
         * Actualiza la consulta de búsqueda y filtra las paradas cercanas.
         *
         * Solo filtra las paradas si el estado actual es [MapUiState.Success].
         * Guarda la consulta en [currentQuery] para uso posterior.
         *
         * @param query La consulta de búsqueda del usuario.
         * @see FilterBusStops
         * @see MapUiState.Success
         */
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

        /**
         * Envía la consulta de búsqueda al repositorio para guardar en el historial.
         *
         * Solo guarda la consulta si no está vacía después de eliminar espacios en blanco.
         *
         * @param query La consulta de búsqueda del usuario.
         * @see BusStopRepository
         */
        fun submitSearch(query: String) {
            val trimmed = query.trim()
            if (trimmed.isBlank()) return
            viewModelScope.launch {
                busStopRepository.saveSearchQuery(trimmed)
            }
        }

        /**
         * Selecciona una parada de autobús específica.
         *
         * Solo actualiza el estado si el estado actual es [MapUiState.Success].
         *
         * @param stopId El identificador de la parada de autobús seleccionada.
         * @see MapUiState.Success
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
