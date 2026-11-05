package ar.edu.unlam.scaffoldingandroid3.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.edu.unlam.scaffoldingandroid3.domain.model.BusStop
import ar.edu.unlam.scaffoldingandroid3.domain.usecase.ExploreMapManually
import ar.edu.unlam.scaffoldingandroid3.domain.usecase.GetNearbyStops
import ar.edu.unlam.scaffoldingandroid3.ui.uistate.MapUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel que maneja el estado y la lógica de la vista del mapa.
 *
 * Esta ViewModel:
 * - Carga la ubicación del usuario y las paradas de autobús cercanas
 * - Gestiona la selección de una parada de autobús específica
 * - Maneja errores relacionados con permisos y excepciones de seguridad
 *
 * @param exploreMapUseCase el caso de uso para explorar el mapa y cargar la ubicación del
 *                           usuario junto con las paradas cercanas.
 * @param getNearbyStops el caso de uso para obtener las paradas de autobús cercanas a la
 *                        ubicación del usuario.
 */
@HiltViewModel
class MapViewModel
    @Inject
    constructor(
        private val exploreMapUseCase: ExploreMapManually,
        private val getNearbyStops: GetNearbyStops,
    ) : ViewModel() {
        @Suppress("ktlint:standard:backing-property-naming")
        private val _uiState = MutableStateFlow<MapUiState>(MapUiState.Loading)
        val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

        /**
         * Carga la ubicación del usuario y las paradas de colectivo cercanas.
         *
         * Esta función:
         * 1. Establece el estado inicial de carga mediante [MapUiState.Loading]
         * 2. Obtiene la ubicación del usuario a través de [exploreMapUseCase]
         * 3. Obtiene las paradas cercanas a través de [getNearbyStops]
         * 4. Actualiza el estado a éxito con la ubicación y las paradas
         * 5. Maneja errores de seguridad (SecurityException) mostrando un mensaje de error
         * 6. Captura excepciones genéricas mostrando un mensaje de error genérico
         *
         * **Manejo de errores**:
         * - [SecurityException]: Se muestra el mensaje de error o "Error loading user location"
         * - [Exception] genérico: Se muestra "Unknown error occurred while loading the user location"
         *
         * @see LOCATION_ERROR
         * @see UNKNOWN_ERROR
         */
        fun loadUserLocationAndStops() {
            viewModelScope.launch {
                _uiState.value = MapUiState.Loading

                try {
                    val location = exploreMapUseCase()
                    val stops = getNearbyStops(location)

                    _uiState.value = MapUiState.Success(location, stops)
                } catch (e: SecurityException) {
                    _uiState.value = MapUiState.Error(e.message ?: LOCATION_ERROR)
                } catch (_: Exception) {
                    _uiState.value =
                        MapUiState.Error(UNKNOWN_ERROR)
                }
            }
        }

        /**
         * Selecciona una parada de colectivo específica en el mapa.
         *
         * Esta función:
         * 1. Obtiene el estado actual a través de [_uiState]
         * 2. Solo permite seleccionar una parada si el estado es [MapUiState.Success]
         * 3. Establece la parada seleccionada en el estado de la UI
         *
         * @param stopToSelect la parada de colectivo a seleccionar.
         * @see clearSelectedBusStop
         */
        fun selectBusStop(stopToSelect: BusStop) {
            val currentState = _uiState.value

            if (currentState is MapUiState.Success) {
                _uiState.value = currentState.copy(selectedStop = stopToSelect)
            }
        }

        /**
         * Elimina la selección de la parada de colectivo actual.
         *
         * Esta función:
         * 1. Obtiene el estado actual a través de [_uiState]
         * 2. Solo permite limpiar la selección si el estado es [MapUiState.Success]
         * 3. Establece la parada seleccionada a `null` en el estado de la UI
         *
         * @see selectBusStop
         */
        fun clearSelectedBusStop() {
            val currentState = _uiState.value

            if (currentState is MapUiState.Success) {
                _uiState.value = currentState.copy(selectedStop = null)
            }
        }
    }

private const val LOCATION_ERROR = "Error loading user location"
private const val UNKNOWN_ERROR = "Unknown error occurred while loading the user location"
