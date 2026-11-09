package ar.edu.unlam.scaffoldingandroid3.domain.model

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class BearingDataTest {
    @Test
    @DisplayName("Debería crear un BearingData válido con bearing 0.0 (borde inferior)")
    fun shouldSucceed_whenCreatedWithBearingZero() {
        val bearingData = assertDoesNotThrow<BearingData> { BearingData(Bearing(0.0)) }

        assertEquals(0.0, bearingData.bearing.value, 0.0)
    }

    @Test
    @DisplayName("Debería crear un BearingData válido con bearing 360.0 (borde superior)")
    fun shouldSucceed_whenCreatedWithBearingThreeSixty() {
        val bearingData = assertDoesNotThrow<BearingData> { BearingData(Bearing(360.0)) }

        assertEquals(360.0, bearingData.bearing.value, 0.0)
    }

    @Test
    @DisplayName("Debería crear un BearingData válido con bearing 180.0 (valor medio)")
    fun shouldSucceed_whenCreatedWithBearingOneEighty() {
        val bearingData = assertDoesNotThrow<BearingData> { BearingData(Bearing(180.0)) }

        assertEquals(180.0, bearingData.bearing.value, 0.0)
    }

    @Test
    @DisplayName("Debería fallar con bearing negativo")
    fun shouldFail_whenCreatedWithNegativeBearing() {
        val exception =
            assertThrows<IllegalArgumentException> {
                BearingData(Bearing(-10.0))
            }

        assertEquals("El valor de bearing debe ubicarse entre 0.0 y 360.0", exception.message)
    }

    @Test
    @DisplayName("Debería fallar con bearing mayor a 360.0")
    fun shouldFail_whenCreatedWithBearingGreaterThanThreeSixty() {
        val exception =
            assertThrows<IllegalArgumentException> {
                BearingData(Bearing(361.0))
            }

        assertEquals("El valor de bearing debe ubicarse entre 0.0 y 360.0", exception.message)
    }

    @Test
    @DisplayName("Debería actualizar el bearing usando withBearing")
    fun shouldUpdateBearing_usingWithBearing() {
        val originalBearingData = assertDoesNotThrow<BearingData> { BearingData(Bearing(45.0)) }
        val updatedBearingData = originalBearingData.withBearing(Bearing(90.0))

        assertEquals(45.0, originalBearingData.bearing.value, 0.0)
        assertEquals(90.0, updatedBearingData.bearing.value, 0.0)
    }

    @Test
    @DisplayName("Debería fallar al actualizar con bearing inválido usando withBearing")
    fun shouldFail_whenUpdatingWithInvalidBearing_usingWithBearing() {
        val originalBearingData = assertDoesNotThrow<BearingData> { BearingData(Bearing(45.0)) }
        val exception =
            assertThrows<IllegalArgumentException> {
                originalBearingData.withBearing(Bearing(-10.0))
            }

        assertEquals("El valor de bearing debe ubicarse entre 0.0 y 360.0", exception.message)
    }

    @Test
    @DisplayName("Debería crear una copia con copy")
    fun shouldCreateACopy_usingCopy() {
        val originalBearingData = assertDoesNotThrow<BearingData> { BearingData(Bearing(45.0)) }
        val copy = originalBearingData.copy(bearing = Bearing(90.0))

        assertEquals(45.0, originalBearingData.bearing.value, 0.0)
        assertEquals(90.0, copy.bearing.value, 0.0)
    }

    @Test
    @DisplayName("Debería fallar al crear copia con bearing inválido")
    fun shouldFail_whenCreatingCopyWithInvalidBearing() {
        var originalBearingData = assertDoesNotThrow<BearingData> { BearingData(Bearing(45.0)) }
        val exception =
            assertThrows<IllegalArgumentException> {
                originalBearingData = originalBearingData.copy(bearing = Bearing(361.0))
            }

        assertEquals("El valor de bearing debe ubicarse entre 0.0 y 360.0", exception.message)
    }

    @Test
    @DisplayName("Debería tener equals, hashCode y toString correctos")
    fun shouldHaveEqualsAndHashCodeCorrect() {
        val bearingData1 = assertDoesNotThrow<BearingData> { BearingData(Bearing(45.0)) }
        val bearingData2 = assertDoesNotThrow<BearingData> { BearingData(Bearing(45.0)) }
        val bearingData3 = assertDoesNotThrow<BearingData> { BearingData(Bearing(90.0)) }
        val bearingData4 = assertDoesNotThrow<BearingData> { BearingData(Bearing(90.0)) }

        assertTrue(bearingData1 == bearingData2)
        assertFalse(bearingData1 == bearingData3)

        assertTrue(bearingData1.hashCode() == bearingData2.hashCode())
        assertFalse(bearingData1.hashCode() == bearingData3.hashCode())

        assertEquals("BearingData(bearing=Bearing(value=45.0))", bearingData1.toString())
        assertEquals("BearingData(bearing=Bearing(value=90.0))", bearingData4.toString())
    }
}
