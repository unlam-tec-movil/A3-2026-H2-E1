package ar.edu.unlam.scaffoldingandroid3.data.datasources.local

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@Suppress("DEPRECATION")
class OrientationSensorDataSourceImpl
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : OrientationSensorDataSource,
        SensorEventListener {
        @Suppress("ktlint:standard:backing-property-naming")
        private val _orientationDegrees = MutableStateFlow<Float?>(null)
        override val orientationDegrees: StateFlow<Float?> = _orientationDegrees.asStateFlow()
        private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        override fun isOrientationSensorAvailable(): Boolean = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION) != null

        override fun startListening() {
            sensorManager.registerListener(
                this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION)!!,
                SensorManager.SENSOR_DELAY_GAME,
            )
        }

        override fun stopListening() {
            sensorManager.unregisterListener(this)
            _orientationDegrees.value = null
        }

        override fun onAccuracyChanged(
            sensor: Sensor?,
            accuracy: Int,
        ) {
        }

        override fun onSensorChanged(event: SensorEvent?) {
            event?.let {
                if (it.sensor.type == Sensor.TYPE_ORIENTATION) {
                    processOrientationValuesFromArray(it.values)
                }
            }
        }

        internal fun processOrientationValuesFromArray(values: FloatArray) {
            val azimuth = values[0]

            _orientationDegrees.value = azimuth
        }
    }
