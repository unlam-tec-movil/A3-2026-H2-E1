package ar.edu.unlam.scaffoldingandroid3.domain.usecase

import ar.edu.unlam.scaffoldingandroid3.factory.TestDataFactory
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class CalculateDistanceToStopUseCaseTest {
    private val useCase = CalculateDistanceToStop()
    private val startLocation = TestDataFactory.locationDataInstance()
    private val endLocation = TestDataFactory.anotherLocationDataInstance()

    @Test
    @DisplayName("Debería calcular la distancia en metros hasta el punto de destino")
    fun shouldInvokeFromStartingPointToEndingPointInMeters() =
        runTest {
            val expectedDistance = 49.5

            val calculatedDistance = useCase.invoke(startLocation, endLocation)

            assertEquals(expectedDistance, calculatedDistance, 0.1)
        }
}
