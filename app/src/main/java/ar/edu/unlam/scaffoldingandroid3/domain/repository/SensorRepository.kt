package ar.edu.unlam.scaffoldingandroid3.domain.repository

import kotlinx.coroutines.flow.StateFlow

interface SensorRepository {
    fun isOrientationSensorAvailable(): Boolean

    val orientationDegrees: StateFlow<Float?>

    fun startOrientationUpdates()

    fun stopOrientationUpdates()
}
