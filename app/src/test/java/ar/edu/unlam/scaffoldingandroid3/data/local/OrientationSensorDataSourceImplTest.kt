package ar.edu.unlam.scaffoldingandroid3.data.local

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import ar.edu.unlam.scaffoldingandroid3.data.datasources.local.OrientationSensorDataSourceImpl
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertNull
import javax.inject.Inject

@Suppress("DEPRECATION")
@HiltAndroidTest
class OrientationSensorDataSourceImplTest {
    @Inject
    lateinit var dataSource: OrientationSensorDataSourceImpl

    val mockContext = mockk<Context>()
    val mockSensorManager = mockk<SensorManager>(relaxed = true)
    val mockSensor = mockk<Sensor>()

    @Test
    @DisplayName("Debería retornar false cuando el sensor de orientación no está disponible")
    fun shouldReturnFalseWhenOrientationSensorIsNotAvailable() {
        every { mockSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION) } returns null
        every { mockContext.getSystemService(Context.SENSOR_SERVICE) } returns mockSensorManager

        val dataSource = OrientationSensorDataSourceImpl(mockContext)
        val result = dataSource.isOrientationSensorAvailable()
        assertFalse(result)
    }

    @Test
    @DisplayName("Debería retornar true cuando el sensor de orientación está disponible")
    fun shouldReturnTrueWhenOrientationSensorIsAvailable() {
        every { mockSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION) } returns mockk<Sensor>()
        every { mockContext.getSystemService(Context.SENSOR_SERVICE) } returns mockSensorManager

        val dataSource = OrientationSensorDataSourceImpl(mockContext)
        val result = dataSource.isOrientationSensorAvailable()
        assertTrue(result)
    }

    @Test
    @DisplayName("Debería iniciar la escucha cuando el sensor está disponible")
    fun shouldStartListeningWhenSensorIsAvailable() {
        every { mockSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION) } returns mockSensor
        every { mockContext.getSystemService(Context.SENSOR_SERVICE) } returns mockSensorManager

        val dataSource = OrientationSensorDataSourceImpl(mockContext)
        dataSource.startListening()

        verify { mockSensorManager.registerListener(any(), mockSensor, any()) }
    }

    @Test
    @DisplayName("Debería detener la escucha cuando se solicita")
    fun shouldStopListeningWhenRequested() {
        every { mockSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION) } returns mockSensor
        every { mockContext.getSystemService(Context.SENSOR_SERVICE) } returns mockSensorManager

        val dataSource = OrientationSensorDataSourceImpl(mockContext)
        dataSource.startListening()
        dataSource.stopListening()

        verify(exactly = 1) { mockSensorManager.unregisterListener(any<SensorEventListener>()) }
    }

    @Test
    @DisplayName("Debería actualizar orientationDegrees cuando onSensorChanged recibe un evento del sensor de orientación")
    fun shouldUpdateOrientationDegreesWhenSensorChanged() {
        every { mockSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION) } returns mockSensor
        every { mockContext.getSystemService(Context.SENSOR_SERVICE) } returns mockSensorManager

        dataSource = OrientationSensorDataSourceImpl(mockContext)

        val expectedAzimuth = 45.0f

        val sampleValues = floatArrayOf(expectedAzimuth, 0.0f, 0.0f)

        dataSource.processOrientationValuesFromArray(sampleValues)

        assertEquals(expectedAzimuth, dataSource.orientationDegrees.value)
    }

    @Test
    @DisplayName("Debería reiniciar orientationDegrees a null al llamar a stopListening")
    fun shouldResetOrientationDegreesToNullWhenStopListening() {
        every { mockSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION) } returns mockSensor
        every { mockContext.getSystemService(Context.SENSOR_SERVICE) } returns mockSensorManager

        val dataSource = OrientationSensorDataSourceImpl(mockContext)
        dataSource.processOrientationValuesFromArray(floatArrayOf(90.0f, 0.0f, 0.0f))

        dataSource.stopListening()

        assertNull(dataSource.orientationDegrees.value)
    }
}
