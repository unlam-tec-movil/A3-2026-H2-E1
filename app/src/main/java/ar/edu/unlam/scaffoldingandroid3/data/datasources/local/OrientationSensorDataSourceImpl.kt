@file:Suppress("ktlint:standard:kdoc")

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

/**
 * Tamaño de la matriz de rotación.
 *
 * La matriz de rotación es una matriz 3x3 que representa la orientación del dispositivo.
 * Se utiliza para convertir los vectores de rotación en ángulos de orientación.
 */
private const val ROTATION_MATRIX_SIZE = 9

/**
 * Tamaño del vector de orientación.
 *
 * El vector de orientación contiene tres valores:
 * - orientationAngles[0]: Ángulo de azimut (dirección hacia el norte)
 * - orientationAngles[1]: Ángulo de elevación
 * - orientationAngles[2]: Ángulo de cabeceo
 */
private const val ORIENTATION_VECTOR_SIZE = 3

/**
 * Grados en un círculo completo.
 *
 * Se utiliza para normalizar los valores de orientación entre 0 y 360 grados.
 */
private const val FULL_CIRCLE_DEGREES = 360f

/**
 * Implementación de la fuente de datos para el sensor de orientación del dispositivo.
 *
 * Esta clase implementa [OrientationSensorDataSource] para proporcionar acceso a los datos
 * de orientación del dispositivo, procesando los datos del sensor de vectores de rotación y
 * ajustando la orientación según la rotación de la pantalla del dispositivo.
 *
 * **Funcionalidad**:
 * - Proporciona un flujo de estado con los grados de orientación del dispositivo
 * - Verifica la disponibilidad del sensor de vectores de rotación
 * - Inicia y detiene la escucha de datos de orientación
 * - Procesa los vectores de rotación para calcular la orientación en grados
 * - Ajusta la orientación según la rotación actual de la pantalla
 *
 * **Procesamiento de datos**:
 * - Lee datos del sensor de vectores de rotación ([Sensor.TYPE_ROTATION_VECTOR])
 * - Convierte los vectores de rotación en una matriz de rotación
 * - Remapea la matriz según la rotación de la pantalla para visualización correcta
 * - Calcula la orientación en grados usando la matriz remapeada
 *
 * **Rotación de pantalla**:
 * - [Surface.ROTATION_0]: No se requiere remapeo
 * - [Surface.ROTATION_90]: Remapea Y a -X
 * - [Surface.ROTATION_180]: Remapea -X a -Y
 * - [Surface.ROTATION_270]: Remapea -Y a X
 *
 * @see OrientationSensorDataSource
 * @see SensorEventListener
 */

/**
 * Crea una instancia de la fuente de datos de orientación del dispositivo.
 *
 * @param context Contexto de la aplicación para acceder al [SensorManager] y al
 *                [WindowManager] para obtener la rotación de la pantalla.
 * @see OrientationSensorDataSource
 */
@Suppress("ktlint:standard:no-consecutive-comments")
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
        private val rotationVectorSensor: Sensor? =
            sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        /**
         * Verifica si el sensor de vectores de rotación está disponible en el dispositivo.
         *
         * Retorna true si el sensor de vectores de rotación está disponible y reconocido
         * por el sistema, false si no lo está.
         *
         * @return true si el sensor de vectores de rotación está disponible, false si no lo está.
         */
        override fun isOrientationSensorAvailable(): Boolean = rotationVectorSensor != null

        /**
         * Inicia la escucha de datos de orientación del dispositivo.
         *
         * Registra un escuchador de eventos del sensor de vectores de rotación con
         * la frecuencia de muestreo para juegos ([SensorManager.SENSOR_DELAY_GAME]).
         *
         * **Comportamiento**:
         * - Registra el escuchador de eventos del sensor de vectores de rotación
         * - Comienza a recibir actualizaciones en tiempo real de la orientación
         * - Los nuevos valores se procesan en [onSensorChanged()]
         *
         * @see OrientationSensorDataSource
         * @see SensorManager.SENSOR_DELAY_GAME
         */
        override fun startListening() {
            rotationVectorSensor?.let { sensor ->
                sensorManager.registerListener(
                    this,
                    sensor,
                    SensorManager.SENSOR_DELAY_GAME,
                )
            }
        }

        /**
         * Detiene la escucha de datos de orientación del dispositivo.
         *
         * Desregistra el escuchador de eventos del sensor y establece el valor de
         * orientación en null para indicar que no hay datos disponibles.
         *
         * **Comportamiento**:
         * - Desregistra el escuchador de eventos del sensor de vectores de rotación
         * - Libera los recursos asociados con la escucha activa
         * - Establece el valor de orientación en null
         *
         * @see OrientationSensorDataSource
         */
        override fun stopListening() {
            sensorManager.unregisterListener(this)
            _orientationDegrees.value = null
        }

        /**
         * Recibe notificaciones de cambios en la precisión del sensor.
         *
         * Este metodo se llama cuando cambia la precisión del sensor. En esta implementación,
         * no se realiza ningún procesamiento adicional ya que solo nos interesa el sensor
         * de vectores de rotación.
         *
         * @param sensor El sensor del que ha cambiado la precisión.
         * @param accuracy La nueva precisión del sensor.
         * @see SensorEventListener
         */
        override fun onAccuracyChanged(
            sensor: Sensor?,
            accuracy: Int,
        ) {
        }

        /**
         * Recibe notificaciones de cambios en los datos del sensor.
         *
         * Este metodo se llama cada vez que hay una actualización de datos del sensor.
         * Solo procesa los eventos del sensor de vectores de rotación, ignorando
         * otros tipos de sensores.
         *
         * **Procesamiento**:
         * - Verifica que el sensor sea de vectores de rotación ([Sensor.TYPE_ROTATION_VECTOR])
         * - Convierte los vectores de rotación en una matriz de rotación
         * - Remapea la matriz según la rotación de la pantalla
         * - Calcula los ángulos de orientación
         * - Convierte el ángulo de azimut a grados y normaliza entre 0-360
         *
         * @param event El evento del sensor que contiene los nuevos datos.
         * @see SensorEventListener
         * @see processRotationVectorValues
         */
        override fun onSensorChanged(event: SensorEvent?) {
            if (event?.sensor?.type == Sensor.TYPE_ROTATION_VECTOR) {
                processRotationVectorValues(event.values)
            }
        }

        /**
         * Procesa los valores del vector de rotación para calcular la orientación.
         *
         * Convierte los vectores de rotación en una matriz de rotación, remapea la matriz
         * según la rotación de la pantalla, y calcula los ángulos de orientación.
         *
         * **Pasos de procesamiento**:
         * 1. Convierte los vectores de rotación en una matriz de rotación 3x3
         * 2. Remapea la matriz para compensar la rotación de la pantalla
         * 3. Extrae los ángulos de orientación de la matriz remapeada
         * 4. Convierte el ángulo de azimut de radianes a grados
         * 5. Normaliza el valor entre 0 y 360 grados
         *
         * @param values Array de valores del vector de rotación de 4 elementos:
         *               [w, x, y, z] donde w es la componente temporal y x, y, z son espaciales.
         * @see ROTATION_MATRIX_SIZE
         * @see ORIENTATION_VECTOR_SIZE
         * @see remapForDisplayRotation
         * @see SensorManager.getOrientation
         */
        internal fun processRotationVectorValues(values: FloatArray) {
            val rotationMatrix = FloatArray(ROTATION_MATRIX_SIZE)
            SensorManager.getRotationMatrixFromVector(rotationMatrix, values)
            val remappedMatrix = remapForDisplayRotation(rotationMatrix)

            val orientationAngles = FloatArray(ORIENTATION_VECTOR_SIZE)
            SensorManager.getOrientation(remappedMatrix, orientationAngles)
            val azimuthDegrees = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()

            _orientationDegrees.value = (azimuthDegrees + FULL_CIRCLE_DEGREES) % FULL_CIRCLE_DEGREES
        }

        /**
         * Remapea la matriz de rotación según la rotación de la pantalla.
         *
         * Ajusta el sistema de coordenadas de la matriz de rotación para compensar
         * la rotación de la pantalla del dispositivo, asegurando que la orientación
         * se muestre correctamente independientemente de la posición del dispositivo.
         *
         * **Remapeos por rotación de pantalla**:
         * - [Surface.ROTATION_0]: No se requiere remapeo (eje X, eje Y)
         * - [Surface.ROTATION_90]: Remapea Y a -X
         * - [Surface.ROTATION_180]: Remapea -X a -Y
         * - [Surface.ROTATION_270]: Remapea -Y a X
         *
         * **Sistema de coordenadas**:
         * - X: Eje horizontal (derecha/izquierda)
         * - Y: Eje vertical (arriba/abajo)
         * - -X: Eje negativo de X
         * - -Y: Eje negativo de Y
         *
         * @param rotationMatrix Matriz de rotación original 3x3 de 9 elementos.
         * @return Matriz de rotación remapeada lista para el cálculo de orientación.
         * @see Surface.ROTATION_0
         * @see Surface.ROTATION_90
         * @see Surface.ROTATION_180
         * @see Surface.ROTATION_270
         * @see SensorManager.remapCoordinateSystem
         */
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

        /**
         * Obtiene la rotación actual de la pantalla del dispositivo.
         *
         * Utiliza el [WindowManager] para obtener la rotación actual de la pantalla
         * y la utiliza para ajustar el procesamiento de los datos del sensor.
         *
         * **Retorno**:
         * - [Surface.ROTATION_0]: Pantalla en posición vertical (0 grados)
         * - [Surface.ROTATION_90]: Pantalla rotada 90 grados a la derecha
         * - [Surface.ROTATION_180]: Pantalla rotada 180 grados
         * - [Surface.ROTATION_270]: Pantalla rotada 270 grados a la derecha
         *
         * @return La rotación actual de la pantalla, o [Surface.ROTATION_0] si no se puede obtener.
         * @see WindowManager
         * @see Surface.ROTATION_0
         * @see Surface.ROTATION_90
         * @see Surface.ROTATION_180
         * @see Surface.ROTATION_270
         */
        @Suppress("DEPRECATION")
        private fun currentDisplayRotation(): Int =
            (context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager)
                ?.defaultDisplay
                ?.rotation
                ?: Surface.ROTATION_0
    }
