package ar.edu.unlam.scaffoldingandroid3.domain.model

import ar.edu.unlam.scaffoldingandroid3.factory.TestDataFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import java.time.Instant

class PhotoReportTest {
    @Test
    @DisplayName("Reporte con todos los campos válidos debe crear instancia exitosamente")
    fun withValidFields_shouldCreateReport() {
        val photoReport = TestDataFactory.defaultPhotoReportInstance()

        assertAll(
            { assertNotNull(photoReport) },
            { assertEquals(REPORTE_ID, photoReport.id) },
            { assertEquals(REPORTE_PHOTO_URI, photoReport.photoUri) },
            { assertEquals(REPORTE_OBSERVACION, photoReport.observation) },
            { assertEquals(REPORTE_STOP_ID, photoReport.stopId) },
        )
    }

    @Test
    @DisplayName("Reporte con URI de foto vacío debe lanzar excepción")
    fun withEmptyPhotoUri_shouldThrowException() {
        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                PhotoReport(
                    id = REPORTE_ID,
                    photoUri = "",
                    observation = REPORTE_OBSERVACION,
                    timestamp = Instant.now(),
                    stopId = REPORTE_STOP_ID,
                )
            }

        assertTrue(exception.message?.contains("photoUri") == true)
    }

    @Test
    @DisplayName("Reporte con URI de foto solo espacios debe lanzar excepción")
    fun withWhitespacePhotoUri_shouldThrowException() {
        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                PhotoReport(
                    id = REPORTE_ID,
                    photoUri = "   ",
                    observation = REPORTE_OBSERVACION,
                    timestamp = Instant.now(),
                    stopId = REPORTE_STOP_ID,
                )
            }

        assertTrue(exception.message?.contains("photoUri") == true)
    }

    @Test
    @DisplayName("Reporte con ID vacío debe lanzar excepción")
    fun withEmptyId_shouldThrowException() {
        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                PhotoReport(
                    id = "",
                    photoUri = REPORTE_PHOTO_URI,
                    observation = REPORTE_OBSERVACION,
                    timestamp = Instant.now(),
                    stopId = -1L,
                )
            }

        assertTrue(exception.message?.contains("id") == true)
    }

    @Test
    @DisplayName("Reporte con stopId negativo debe lanzar excepción")
    fun withNegativeStopId_shouldThrowException() {
        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                PhotoReport(
                    id = REPORTE_ID,
                    photoUri = REPORTE_PHOTO_URI,
                    observation = REPORTE_OBSERVACION,
                    timestamp = Instant.now(),
                    stopId = -1L,
                )
            }

        assertTrue(exception.message?.contains("stopId") == true)
    }

    @Test
    @DisplayName("Reporte con stopId igual a cero debe lanzar excepción")
    fun withZeroStopId_shouldThrowException() {
        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                PhotoReport(
                    id = REPORTE_ID,
                    photoUri = REPORTE_PHOTO_URI,
                    observation = REPORTE_OBSERVACION,
                    timestamp = Instant.now(),
                    stopId = 0L,
                )
            }

        assertTrue(exception.message?.contains("stopId") == true)
    }

    @Test
    @DisplayName("Reporte con timestamp en el futuro debe lanzar excepción")
    fun withFutureTimestamp_shouldThrowException() {
        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                PhotoReport(
                    id = REPORTE_ID,
                    photoUri = REPORTE_PHOTO_URI,
                    observation = REPORTE_OBSERVACION,
                    timestamp = Instant.now().plusSeconds(FUTURO_SEGUNDOS),
                    stopId = REPORTE_STOP_ID,
                )
            }

        assertTrue(exception.message?.contains("timestamp") == true)
    }

    @Test
    @DisplayName("Reporte con observación vacía debe crear reporte exitosamente")
    fun withEmptyObservation_shouldCreateReport() {
        val photoReport =
            PhotoReport(
                id = REPORTE_ID,
                photoUri = REPORTE_PHOTO_URI,
                observation = "",
                timestamp = Instant.now(),
                stopId = REPORTE_STOP_ID,
            )

        assertAll(
            { assertNotNull(photoReport) },
            { assertEquals("", photoReport.observation) },
        )
    }
}

private const val REPORTE_ID = "rep_123"
private const val REPORTE_PHOTO_URI = "file://storage/emulated/0/Pictures/stop_photo.jpg"
private const val REPORTE_OBSERVACION = "Parada despejada"
private const val REPORTE_STOP_ID = 42L
private const val FUTURO_SEGUNDOS = 1000L
