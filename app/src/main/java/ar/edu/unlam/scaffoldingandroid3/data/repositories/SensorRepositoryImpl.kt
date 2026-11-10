package ar.edu.unlam.scaffoldingandroid3.data.repositories

import ar.edu.unlam.scaffoldingandroid3.data.datasources.local.OrientationSensorDataSource
import ar.edu.unlam.scaffoldingandroid3.domain.repository.SensorRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class SensorRepositoryImpl
    @Inject
    constructor(
        private val dataSource: OrientationSensorDataSource,
    ) : SensorRepository {
        override fun isOrientationSensorAvailable(): Boolean = dataSource.isOrientationSensorAvailable()

        override val orientationDegrees: StateFlow<Float?> = dataSource.orientationDegrees

        override fun startOrientationUpdates() = dataSource.startListening()

        override fun stopOrientationUpdates() = dataSource.stopListening()
    }
