package ar.edu.unlam.scaffoldingandroid3.data.mapper

import ar.edu.unlam.scaffoldingandroid3.domain.model.PhotoReport
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Instant

class PhotoReportMapperTest {
    data class TestScenario(
        val id: String,
        val photoUri: String,
        val observation: String,
        val stopId: Long,
    )

    private val pastTimestamp: Instant = Instant.parse("2020-01-01T00:00:00Z")
    private val pastScenario: TestScenario =
        TestScenario(
            id = "test-id-pasado",
            photoUri = "content://media/external/images/media/pasado",
            observation = "Observación pasado",
            stopId = 42,
        )

    private val oneHourTimestamp: Instant = Instant.parse("2025-11-14T10:00:00Z")
    private val oneHourScenario: TestScenario =
        TestScenario(
            id = "test-id-hora",
            photoUri = "content://media/external/images/media/hora",
            observation = "Observación hora",
            stopId = 99,
        )

    private val oneMinuteTimestamp: Instant = Instant.parse("2025-11-14T10:59:00Z")
    private val oneMinuteScenario: TestScenario =
        TestScenario(
            id = "test-id-minuto",
            photoUri = "content://media/external/images/media/minuto",
            observation = "Observación minuto",
            stopId = 1,
        )
    private val timestampActualId: String = "now-id"
    private val timestampActualPhotoUri: String = "content://media/external/images/media/now"
    private val timestampActualObservacion: String = "Observación actual"
    private val timestampActualStopId: Long = 123

    @Test
    @DisplayName("Round trip con timestamp pasado debe preservar todos los campos")
    fun roundTrip_shouldPreserveAllFieldsWithVariousTimestamps() {
        val pastDomain =
            PhotoReport(
                id = pastScenario.id,
                photoUri = pastScenario.photoUri,
                observation = pastScenario.observation,
                timestamp = pastTimestamp,
                stopId = pastScenario.stopId,
            )
        val pastEntity = pastDomain.toEntity()
        val pastRestored = pastEntity.toDomain()

        assertAll(
            { assertEquals(pastDomain.id, pastRestored.id) },
            { assertEquals(pastDomain.photoUri, pastRestored.photoUri) },
            { assertEquals(pastDomain.observation, pastRestored.observation) },
            { assertEquals(pastDomain.stopId, pastRestored.stopId) },
            { assertEquals(pastDomain.timestamp.toEpochMilli(), pastRestored.timestamp.toEpochMilli(), "timestamp") },
        )

        val oneHourDomain =
            PhotoReport(
                id = oneHourScenario.id,
                photoUri = oneHourScenario.photoUri,
                observation = oneHourScenario.observation,
                timestamp = oneHourTimestamp,
                stopId = oneHourScenario.stopId,
            )
        val oneHourEntity = oneHourDomain.toEntity()
        val oneHourRestored = oneHourEntity.toDomain()
        assertAll(
            { assertEquals(oneHourDomain.id, oneHourRestored.id) },
            { assertEquals(oneHourDomain.photoUri, oneHourRestored.photoUri) },
            { assertEquals(oneHourDomain.observation, oneHourRestored.observation) },
            { assertEquals(oneHourDomain.stopId, oneHourRestored.stopId) },
            { assertEquals(oneHourDomain.timestamp.toEpochMilli(), oneHourRestored.timestamp.toEpochMilli(), "timestamp") },
        )

        val oneMinuteDomain =
            PhotoReport(
                id = oneMinuteScenario.id,
                photoUri = oneMinuteScenario.photoUri,
                observation = oneMinuteScenario.observation,
                timestamp = oneMinuteTimestamp,
                stopId = oneMinuteScenario.stopId,
            )
        val oneMinuteEntity = oneMinuteDomain.toEntity()
        val oneMinuteRestored = oneMinuteEntity.toDomain()
        assertAll(
            { assertEquals(oneMinuteDomain.id, oneMinuteRestored.id) },
            { assertEquals(oneMinuteDomain.photoUri, oneMinuteRestored.photoUri) },
            { assertEquals(oneMinuteDomain.observation, oneMinuteRestored.observation) },
            { assertEquals(oneMinuteDomain.stopId, oneMinuteRestored.stopId) },
            { assertEquals(oneMinuteDomain.timestamp.toEpochMilli(), oneMinuteRestored.timestamp.toEpochMilli(), "timestamp") },
        )
    }

    @Test
    @DisplayName("Round trip con timestamp actual debe preservar todos los campos")
    fun roundTrip_shouldHandleCurrentTimestampWithoutDrift() {
        val now = Instant.now()
        val nowDomain =
            PhotoReport(
                id = timestampActualId,
                photoUri = timestampActualPhotoUri,
                observation = timestampActualObservacion,
                timestamp = now,
                stopId = timestampActualStopId,
            )
        val nowEntity = nowDomain.toEntity()
        val nowRestored = nowEntity.toDomain()
        assertAll(
            { assertEquals(nowDomain.id, nowRestored.id) },
            { assertEquals(nowDomain.photoUri, nowRestored.photoUri) },
            { assertEquals(nowDomain.observation, nowRestored.observation) },
            { assertEquals(nowDomain.stopId, nowRestored.stopId) },
            {
                assertEquals(
                    nowDomain.timestamp.toEpochMilli(),
                    nowRestored.timestamp.toEpochMilli(),
                    "Timestamp no preservado en round trip",
                )
            },
        )
    }
}
