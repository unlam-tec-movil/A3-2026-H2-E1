package ar.edu.unlam.scaffoldingandroid3.data.repositories

import ar.edu.unlam.scaffoldingandroid3.data.datasources.local.OrientationSensorDataSource
import ar.edu.unlam.scaffoldingandroid3.domain.repository.SensorRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * Implementación de la interfaz de repositorio para el sensor de orientación.
 *
 * Proporciona acceso al sensor de orientación del dispositivo (giroscopio) a través de
 * la fuente de datos [OrientationSensorDataSource].
 *
 * **Funcionalidad**:
 * - Verifica la disponibilidad del sensor de orientación
 * - Proporciona los grados de orientación como flujo de estado
 * - Inicia y detiene las actualizaciones del sensor
 *
 * **Dependencia**: [OrientationSensorDataSource] - Fuente de datos para el sensor de orientación.
 *
 * @see SensorRepository
 * @see OrientationSensorDataSource
 */
class SensorRepositoryImpl
    @Inject
    constructor(
        private val dataSource: OrientationSensorDataSource,
    ) : SensorRepository {
        /**
         * Verifica si el sensor de orientación está disponible en el dispositivo.
         *
         * Esta función delega la verificación a la fuente de datos, que consulta
         * al hardware del dispositivo para determinar si el giroscopio está presente.
         *
         * @return true si el sensor de orientación está disponible, false en caso contrario.
         * @see OrientationSensorDataSource.isOrientationSensorAvailable()
         */
        override fun isOrientationSensorAvailable(): Boolean = dataSource.isOrientationSensorAvailable()

        /**
         * Proporciona el flujo de estado con los grados de orientación actuales.
         *
         * Este flujo emite los valores de orientación en grados cada vez que el sensor
         * reporta un cambio. El valor es null cuando el sensor no está disponible.
         *
         * @return un [StateFlow] que emite los grados de orientación (o null si no disponible).
         * @see StateFlow
         */
        override val orientationDegrees: StateFlow<Float?> = dataSource.orientationDegrees

        /**
         * Inicia las actualizaciones del sensor de orientación.
         *
         * Esta función inicia el proceso de escucha del sensor, comenzando a recibir
         * actualizaciones en tiempo real de los valores de orientación.
         *
         * @see OrientationSensorDataSource.startListening()
         */
        override fun startOrientationUpdates() = dataSource.startListening()

        /**
         * Detiene las actualizaciones del sensor de orientación.
         *
         * Esta función detiene el proceso de escucha del sensor, deteniendo la recepción
         * de actualizaciones en tiempo real.
         *
         * @see OrientationSensorDataSource.stopListening()
         */
        override fun stopOrientationUpdates() = dataSource.stopListening()
    }
