package ar.edu.unlam.scaffoldingandroid3.domain.usecase

import ar.edu.unlam.scaffoldingandroid3.domain.model.PhotoReport
import ar.edu.unlam.scaffoldingandroid3.domain.repository.PhotoReportRepository
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PhotoReportUseCasesTest {
    private val repository = mockk<PhotoReportRepository>(relaxed = true)

    @Test
    fun `register delegates to the repository`() =
        runTest {
            val report = report()

            RegisterPhotoReportUseCase(repository)(report)

            coVerify { repository.save(report) }
        }

    @Test
    fun `get reports returns the reports of the stop`() =
        runTest {
            val report = report()
            every { repository.observeByStopId(42L) } returns flowOf(listOf(report))

            assertEquals(listOf(report), GetPhotoReportsForStopUseCase(repository)(42L).first())
        }

    private fun report() =
        PhotoReport(
            id = "report-1",
            photoUri = "content://photo/1",
            observation = "Sin cartel",
            timestamp = 1_700_000_000_000L,
            stopId = 42L,
        )
}
