package ar.edu.unlam.scaffoldingandroid3.data.datasources.local

import kotlinx.coroutines.flow.StateFlow

interface OrientationSensorDataSource {
    val orientationDegrees: StateFlow<Float?>

    fun isOrientationSensorAvailable(): Boolean

    fun startListening()

    fun stopListening()
}
