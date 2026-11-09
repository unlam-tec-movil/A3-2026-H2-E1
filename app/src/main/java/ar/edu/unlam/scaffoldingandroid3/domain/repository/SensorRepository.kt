package ar.edu.unlam.scaffoldingandroid3.domain.repository

interface SensorRepository {
    fun isOrientationSensorAvailable(): Boolean
}
