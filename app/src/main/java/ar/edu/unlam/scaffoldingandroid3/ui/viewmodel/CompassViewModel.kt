package ar.edu.unlam.scaffoldingandroid3.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import ar.edu.unlam.scaffoldingandroid3.domain.model.Latitude
import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationData
import ar.edu.unlam.scaffoldingandroid3.domain.model.Longitude
import ar.edu.unlam.scaffoldingandroid3.domain.repository.LocationRepository
import ar.edu.unlam.scaffoldingandroid3.domain.repository.SensorRepository
import ar.edu.unlam.scaffoldingandroid3.domain.usecase.CalculateDestinationDirection
import ar.edu.unlam.scaffoldingandroid3.domain.usecase.CalculateDistanceToStop
import ar.edu.unlam.scaffoldingandroid3.domain.usecase.CheckOrientationSensorAvailability
import ar.edu.unlam.scaffoldingandroid3.ui.navigation.Compass
import ar.edu.unlam.scaffoldingandroid3.ui.uistate.CompassUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

private const val LOCATION_TIMEOUT_MILLIS = 10_000L

/**
 * ViewModel de la brújula: orienta al pasajero hacia la parada elegida.
 *
 * La parada de destino llega como argumento de navegación (se lee del [SavedStateHandle]);
 * la ubicación y el azimut del dispositivo provienen de los repositorios. La flecha se
 * recalcula cada vez que el teléfono gira.
 */
@HiltViewModel
class CompassViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        checkOrientationSensorAvailability: CheckOrientationSensorAvailability,
        private val calculateDistanceToStop: CalculateDistanceToStop,
        private val calculateDestinationDirection: CalculateDestinationDirection,
        private val locationRepository: LocationRepository,
        private val sensorRepository: SensorRepository,
    ) : ViewModel() {
        private val route = savedStateHandle.toRoute<Compass>()
        private val stopLocation = LocationData(latitude = Latitude(route.latitude), longitude = Longitude(route.longitude))

        val stopName: String = route.stopName

        @Suppress("ktlint:standard:backing-property-naming")
        private val _uiState = MutableStateFlow<CompassUiState>(CompassUiState.Locating)
        val uiState: StateFlow<CompassUiState> = _uiState.asStateFlow()

        private var currentLocation: LocationData? = null

        init {
            if (checkOrientationSensorAvailability()) {
                sensorRepository.startOrientationUpdates()
                viewModelScope.launch {
                    sensorRepository.orientationDegrees.collect { azimuth ->
                        if (azimuth != null && _uiState.value is CompassUiState.Ready) {
                            refreshReadyState(azimuth)
                        }
                    }
                }
            } else {
                _uiState.value = CompassUiState.SensorUnavailable
            }
        }

        override fun onCleared() {
            sensorRepository.stopOrientationUpdates()
        }

        /** Registra el resultado del pedido de permiso de ubicación. */
        fun onPermissionResult(granted: Boolean) {
            if (!granted) {
                _uiState.value = CompassUiState.PermissionDenied
                return
            }
            locateUser()
        }

        /** Obtiene la ubicación del pasajero (con timeout) y arma el estado de la brújula. */
        fun locateUser() {
            viewModelScope.launch {
                _uiState.value = CompassUiState.Locating
                try {
                    currentLocation =
                        withTimeoutOrNull(LOCATION_TIMEOUT_MILLIS.milliseconds) { locationRepository.getLocationData() }
                    if (currentLocation == null) {
                        _uiState.value = CompassUiState.LocationUnavailable
                    } else {
                        refreshReadyState(sensorRepository.orientationDegrees.value ?: 0f)
                    }
                } catch (_: Exception) {
                    _uiState.value = CompassUiState.LocationUnavailable
                }
            }
        }

        private fun refreshReadyState(deviceAzimuthDegrees: Float) {
            val location = currentLocation ?: return
            _uiState.value =
                CompassUiState.Ready(
                    distanceMeters = calculateDistanceToStop(location, stopLocation),
                    targetBearingDegrees = calculateDestinationDirection(location, stopLocation).bearing.value,
                    deviceAzimuthDegrees = deviceAzimuthDegrees,
                )
        }
    }
