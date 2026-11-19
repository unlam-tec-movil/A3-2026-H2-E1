package ar.edu.unlam.scaffoldingandroid3.domain.usecase

import ar.edu.unlam.scaffoldingandroid3.domain.repository.SensorRepository
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.jupiter.api.DisplayName

class CheckOrientationSensorAvailabilityUseCaseTest {
    private val sensorRepository = mockk<SensorRepository>()
    private val useCase = CheckOrientationSensorAvailability(sensorRepository)

    @Test
    @DisplayName("Debería retornar true cuando el repositorio indica que los sensores están disponibles")
    fun shouldReturnTrueWhenOrientationSensorsAreAvailable() =
        runTest {
            every { sensorRepository.isOrientationSensorAvailable() } returns true

            val result = useCase()

            assertTrue(result)
        }

    @Test
    @DisplayName("Debería retornar false cuando el repositorio indica que los sensores no están disponibles")
    fun shouldReturnFalseWhenOrientationSensorsAreNotAvailable() =
        runTest {
            every { sensorRepository.isOrientationSensorAvailable() } returns false

            val result = useCase()

            assertFalse(result)
        }
}
