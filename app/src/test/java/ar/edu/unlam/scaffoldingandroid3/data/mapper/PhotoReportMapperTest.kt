package ar.edu.unlam.scaffoldingandroid3.data.mapper

import ar.edu.unlam.scaffoldingandroid3.domain.model.PhotoReport
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Instant

class PhotoReportMapperTest {
    @Test
    @DisplayName("Round trip con timestamps variados debe preservar todos los campos")
    fun roundTrip_shouldPreserveAllFieldsWithVariousTimestamps() {
        val pastTimestamp = Instant.now().minusSeconds(365 * 24 * 60 * 60 * 3)
        val domain1 =
            PhotoReport(
                id = TIMESTAMP_PASADO_ID,
                photoUri = TIMESTAMP_PASADO_PHOTO_URI,
                observation = TIMESTAMP_PASADO_OBSERVACION,
                timestamp = pastTimestamp,
                stopId = TIMESTAMP_PASADO_STOP_ID,
            )
        val entity1 = domain1.toEntity()
        val domain1Restored = entity1.toDomain()

        assertEquals(domain1.id, domain1Restored.id)
        assertEquals(domain1.photoUri, domain1Restored.photoUri)
        assertEquals(domain1.observation, domain1Restored.observation)
        assertEquals(domain1.stopId, domain1Restored.stopId)
        assertEquals(
            domain1.timestamp.toEpochMilli(),
            domain1Restored.timestamp.toEpochMilli(),
            "Timestamp pasado lejano debe preservarse",
        )

        val oneHourAgo = Instant.now().minusSeconds(60 * 60)
        val domain2 =
            PhotoReport(
                id = TIMESTAMP_UNA_HORA_ID,
                photoUri = TIMESTAMP_UNA_HORA_PHOTO_URI,
                observation = TIMESTAMP_UNA_HORA_OBSERVACION,
                timestamp = oneHourAgo,
                stopId = TIMESTAMP_UNA_HORA_STOP_ID,
            )
        val entity2 = domain2.toEntity()
        val domain2Restored = entity2.toDomain()
        assertEquals(domain2.id, domain2Restored.id)
        assertEquals(domain2.photoUri, domain2Restored.photoUri)
        assertEquals(domain2.observation, domain2Restored.observation)
        assertEquals(domain2.stopId, domain2Restored.stopId)
        assertEquals(
            domain2.timestamp.toEpochMilli(),
            domain2Restored.timestamp.toEpochMilli(),
            "Timestamp 1 hora atrás debe preservarse",
        )

        val oneMinuteAgo = Instant.now().minusSeconds(60)
        val domain3 =
            PhotoReport(
                id = TIMESTAMP_UNA_MINUTO_ID,
                photoUri = TIMESTAMP_UNA_MINUTO_PHOTO_URI,
                observation = TIMESTAMP_UNA_MINUTO_OBSERVACION,
                timestamp = oneMinuteAgo,
                stopId = TIMESTAMP_UNA_MINUTO_STOP_ID,
            )
        val entity3 = domain3.toEntity()
        val domain3Restored = entity3.toDomain()
        assertEquals(domain3.id, domain3Restored.id)
        assertEquals(domain3.photoUri, domain3Restored.photoUri)
        assertEquals(domain3.observation, domain3Restored.observation)
        assertEquals(domain3.stopId, domain3Restored.stopId)
        assertEquals(
            domain3.timestamp.toEpochMilli(),
            domain3Restored.timestamp.toEpochMilli(),
            "Timestamp 1 minuto atrás debe preservarse",
        )
    }

    @Test
    @DisplayName("Con timestamp igual al actual debe convertir correctamente sin drift")
    fun withTimestampEqualToNow_shouldConvertCorrectly() {
        val now = Instant.now()
        val domain =
            PhotoReport(
                id = TIMESTAMP_ACTUAL_ID,
                photoUri = TIMESTAMP_ACTUAL_PHOTO_URI,
                observation = TIMESTAMP_ACTUAL_OBSERVACION,
                timestamp = now,
                stopId = TIMESTAMP_ACTUAL_STOP_ID,
            )
        val entity = domain.toEntity()
        val domainRestored = entity.toDomain()

        assertEquals(domain.id, domainRestored.id)
        assertEquals(domain.photoUri, domainRestored.photoUri)
        assertEquals(domain.observation, domainRestored.observation)
        assertEquals(domain.stopId, domainRestored.stopId)
        assertEquals(
            domain.timestamp.toEpochMilli(),
            domainRestored.timestamp.toEpochMilli(),
            "Timestamp actual debe preservarse",
        )
    }
}

private const val TIMESTAMP_PASADO_ID: String = "test-id-pasado"
private const val TIMESTAMP_PASADO_PHOTO_URI: String = "content://media/external/images/media/pasado"
private const val TIMESTAMP_PASADO_OBSERVACION: String = "Observación pasado"
private const val TIMESTAMP_PASADO_STOP_ID: Long = 42
private const val TIMESTAMP_UNA_HORA_ID: String = "test-id-hora"
private const val TIMESTAMP_UNA_HORA_PHOTO_URI: String = "content://media/external/images/media/hora"
private const val TIMESTAMP_UNA_HORA_OBSERVACION: String = "Observación hora"
private const val TIMESTAMP_UNA_HORA_STOP_ID: Long = 99
private const val TIMESTAMP_UNA_MINUTO_ID: String = "test-id-minuto"
private const val TIMESTAMP_UNA_MINUTO_PHOTO_URI: String = "content://media/external/images/media/minuto"
private const val TIMESTAMP_UNA_MINUTO_OBSERVACION: String = "Observación minuto"
private const val TIMESTAMP_UNA_MINUTO_STOP_ID: Long = 1
private const val TIMESTAMP_ACTUAL_ID: String = "now-id"
private const val TIMESTAMP_ACTUAL_PHOTO_URI: String = "content://media/external/images/media/now"
private const val TIMESTAMP_ACTUAL_OBSERVACION: String = "Observación actual"
private const val TIMESTAMP_ACTUAL_STOP_ID: Long = 123
