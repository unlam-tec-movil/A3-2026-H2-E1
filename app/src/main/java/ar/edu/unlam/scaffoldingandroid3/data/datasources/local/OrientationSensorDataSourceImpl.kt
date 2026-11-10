package ar.edu.unlam.scaffoldingandroid3.data.datasources.local

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.Surface
import android.view.WindowManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

private const val ROTATION_MATRIX_SIZE = 9
private const val ORIENTATION_VECTOR_SIZE = 3
private const val FULL_CIRCLE_DEGREES = 360f

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
        private val rotationVectorSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        override fun isOrientationSensorAvailable(): Boolean = rotationVectorSensor != null

        override fun startListening() {
            rotationVectorSensor?.let { sensor ->
                sensorManager.registerListener(
                    this,
                    sensor,
                    SensorManager.SENSOR_DELAY_GAME,
                )
            }
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
            if (event?.sensor?.type == Sensor.TYPE_ROTATION_VECTOR) {
                processRotationVectorValues(event.values)
            }
        }

        internal fun processRotationVectorValues(values: FloatArray) {
            val rotationMatrix = FloatArray(ROTATION_MATRIX_SIZE)
            SensorManager.getRotationMatrixFromVector(rotationMatrix, values)
            val remappedMatrix = remapForDisplayRotation(rotationMatrix)

            val orientationAngles = FloatArray(ORIENTATION_VECTOR_SIZE)
            SensorManager.getOrientation(remappedMatrix, orientationAngles)
            val azimuthDegrees = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()

            _orientationDegrees.value = (azimuthDegrees + FULL_CIRCLE_DEGREES) % FULL_CIRCLE_DEGREES
        }

        private fun remapForDisplayRotation(rotationMatrix: FloatArray): FloatArray {
            val displayRotation = currentDisplayRotation()
            if (displayRotation == Surface.ROTATION_0) return rotationMatrix

            val (axisX, axisY) =
                when (displayRotation) {
                    Surface.ROTATION_90 -> SensorManager.AXIS_Y to SensorManager.AXIS_MINUS_X
                    Surface.ROTATION_180 -> SensorManager.AXIS_MINUS_X to SensorManager.AXIS_MINUS_Y
                    Surface.ROTATION_270 -> SensorManager.AXIS_MINUS_Y to SensorManager.AXIS_X
                    else -> SensorManager.AXIS_X to SensorManager.AXIS_Y
                }

            val remappedMatrix = FloatArray(ROTATION_MATRIX_SIZE)
            SensorManager.remapCoordinateSystem(rotationMatrix, axisX, axisY, remappedMatrix)
            return remappedMatrix
        }

        @Suppress("DEPRECATION")
        private fun currentDisplayRotation(): Int =
            (context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager)
                ?.defaultDisplay
                ?.rotation
                ?: Surface.ROTATION_0
    }
