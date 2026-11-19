package ar.edu.unlam.scaffoldingandroid3.domain.model

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class PhotoReportTest {
    @Test
    fun `accepts a complete report`() {
        assertDoesNotThrow { report() }
    }

    @Test
    fun `rejects a blank observation`() {
        assertThrows(IllegalArgumentException::class.java) { report(observation = "") }
    }

    @Test
    fun `rejects an invalid stop id`() {
        assertThrows(IllegalArgumentException::class.java) { report(stopId = 0) }
    }

    private fun report(
        observation: String = "Parada sin cartel",
        stopId: Long = 42L,
    ) = PhotoReport(
        id = "report-1",
        photoUri = "content://photo/1",
        observation = observation,
        timestamp = 1_700_000_000_000L,
        stopId = stopId,
    )
}
