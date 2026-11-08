package ar.edu.unlam.scaffoldingandroid3.data

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.Surface
import android.view.WindowManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/** Brújula del dispositivo: convierte el sensor de rotación en un ángulo respecto del norte. */
class OrientationRepo(
    context: Context,
) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val rotationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager

    /** Verifica si el celular tiene el sensor de rotación. */
    fun hasSensor(): Boolean = rotationSensor != null

    /** Emite hacia dónde apunta el teléfono en tiempo real, en grados (0° a 360°). */
    fun getAzimuth(): Flow<Float> =
        callbackFlow {
            val manager = sensorManager
            val sensor = rotationSensor
            if (manager == null || sensor == null) {
                close()
                return@callbackFlow
            }

            val listener =
                object : SensorEventListener {
                    override fun onSensorChanged(event: SensorEvent) {
                        val matrix = FloatArray(9)
                        SensorManager.getRotationMatrixFromVector(matrix, event.values)
                        val orientation = FloatArray(3)
                        SensorManager.getOrientation(matrix, orientation)
                        val azimuth = Math.toDegrees(orientation[0].toDouble()) + screenRotationDegrees()
                        trySend(((azimuth % 360) + 360).toFloat() % 360f)
                    }

                    override fun onAccuracyChanged(
                        sensor: Sensor?,
                        accuracy: Int,
                    ) = Unit
                }

            manager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
            awaitClose { manager.unregisterListener(listener) }
        }

    /** El sensor mide respecto del eje natural del equipo; si la pantalla está rotada hay que compensarlo. */
    private fun screenRotationDegrees(): Double =
        when (windowManager?.defaultDisplay?.rotation) {
            Surface.ROTATION_90 -> 90.0
            Surface.ROTATION_180 -> 180.0
            Surface.ROTATION_270 -> 270.0
            else -> 0.0
        }
}
