package ar.edu.unlam.scaffoldingandroid3.ui.uistate

sealed interface CompassUiState {
    /** El dispositivo no tiene sensor de orientación: no se puede mostrar la brújula. */
    data object SensorUnavailable : CompassUiState

    data object PermissionDenied : CompassUiState

    data object Locating : CompassUiState

    data object LocationUnavailable : CompassUiState

    data class Ready(
        val distanceMeters: Double,
        val targetBearingDegrees: Double,
        val deviceAzimuthDegrees: Float,
    ) : CompassUiState
}
