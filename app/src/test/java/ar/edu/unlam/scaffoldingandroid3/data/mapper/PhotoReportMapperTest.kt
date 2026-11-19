package ar.edu.unlam.scaffoldingandroid3.data.mapper

import ar.edu.unlam.scaffoldingandroid3.domain.model.PhotoReport
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PhotoReportMapperTest {
    @Test
    fun `maps domain to entity and back without losing data`() {
        val report =
            PhotoReport(
                id = "report-1",
                photoUri = "content://photo/1",
                observation = "Parada sin cartel",
                timestamp = 1_700_000_000_000L,
                stopId = 42L,
            )

        val entity = with(PhotoReportMapper) { report.toEntity() }
        val result = with(PhotoReportMapper) { entity.toDomain() }

        assertEquals(report, result)
    }
}
