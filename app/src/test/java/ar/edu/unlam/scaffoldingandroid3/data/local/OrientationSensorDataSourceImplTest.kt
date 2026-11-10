package ar.edu.unlam.scaffoldingandroid3.data.local

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import ar.edu.unlam.scaffoldingandroid3.data.datasources.local.OrientationSensorDataSourceImpl
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class OrientationSensorDataSourceImplTest {
    private val mockContext = mockk<Context>()
    private val mockSensorManager = mockk<SensorManager>(relaxed = true)
    private val mockSensor = mockk<Sensor>()

    @BeforeEach
    fun setUp() {
        mockkStatic(SensorManager::class)
        every { mockSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) } returns mockSensor
        every { mockContext.getSystemService(Context.SENSOR_SERVICE) } returns mockSensorManager
        // Sin WindowManager el datasource usa la rotación por defecto (ROTATION_0).
        every { mockContext.getSystemService(Context.WINDOW_SERVICE) } returns null
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(SensorManager::class)
    }

    private fun dataSource() = OrientationSensorDataSourceImpl(mockContext)

    @Test
    @DisplayName("Debería retornar false cuando el sensor de rotación no está disponible")
    fun shouldReturnFalseWhenRotationVectorSensorIsNotAvailable() {
        every { mockSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) } returns null

        assertFalse(dataSource().isOrientationSensorAvailable())
    }

    @Test
    @DisplayName("Debería retornar true cuando el sensor de rotación está disponible")
    fun shouldReturnTrueWhenRotationVectorSensorIsAvailable() {
        assertTrue(dataSource().isOrientationSensorAvailable())
    }

    @Test
    @DisplayName("Debería iniciar la escucha del sensor de rotación cuando está disponible")
    fun shouldStartListeningWhenSensorIsAvailable() {
        dataSource().startListening()

        verify { mockSensorManager.registerListener(any(), mockSensor, any()) }
    }

    @Test
    @DisplayName("Debería detener la escucha cuando se solicita")
    fun shouldStopListeningWhenRequested() {
        val dataSource = dataSource()
        dataSource.startListening()
        dataSource.stopListening()

        verify(exactly = 1) { mockSensorManager.unregisterListener(any<SensorEventListener>()) }
    }

    @Test
    @DisplayName("Debería actualizar orientationDegrees con el azimut calculado del vector de rotación")
    fun shouldUpdateOrientationDegreesFromRotationVector() {
        stubOrientationMath(azimuthDegrees = 45f)

        val dataSource = dataSource()

        dataSource.processRotationVectorValues(floatArrayOf(0f, 0f, 0f, 0f, 0f))

        assertEquals(45f, dataSource.orientationDegrees.value)
    }

    @Test
    @DisplayName("Debería normalizar azimuts negativos al rango [0, 360)")
    fun shouldNormalizeNegativeAzimuths() {
        stubOrientationMath(azimuthDegrees = -90f)

        val dataSource = dataSource()

        dataSource.processRotationVectorValues(floatArrayOf(0f))

        assertEquals(270f, dataSource.orientationDegrees.value)
    }

    @Test
    @DisplayName("Debería reiniciar orientationDegrees a null al llamar a stopListening")
    fun shouldResetOrientationDegreesToNullWhenStopListening() {
        stubOrientationMath(azimuthDegrees = 90f)
        val dataSource = dataSource()
        dataSource.processRotationVectorValues(floatArrayOf(0f))

        dataSource.stopListening()

        assertNull(dataSource.orientationDegrees.value)
    }

    /** Simula la matemática de SensorManager: matriz identidad + azimut dado en grados. */
    private fun stubOrientationMath(azimuthDegrees: Float) {
        every { SensorManager.getRotationMatrixFromVector(any(), any()) } answers {
            val matrix = firstArg<FloatArray>()
            matrix[0] = 1f
            matrix[4] = 1f
            matrix[8] = 1f
            true
        }
        every { SensorManager.getOrientation(any(), any()) } answers {
            val angles = secondArg<FloatArray>()
            angles[0] = Math.toRadians(azimuthDegrees.toDouble()).toFloat()
            angles
        }
    }
}
