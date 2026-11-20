package ar.edu.unlam.scaffoldingandroid3.data.repositories

import ar.edu.unlam.scaffoldingandroid3.data.database.room.entities.PhotoReportEntity
import ar.edu.unlam.scaffoldingandroid3.data.datasources.local.PhotoReportDataSource
import ar.edu.unlam.scaffoldingandroid3.domain.model.PhotoReport
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PhotoReportRepositoryImplTest {
    private val dataSource = mockk<PhotoReportDataSource>(relaxed = true)
    private val repository = PhotoReportRepositoryImpl(dataSource)

    @Test
    fun `saves the report as an entity`() =
        runTest {
            val report = report()

            repository.save(report)

            coVerify {
                dataSource.insert(
                    PhotoReportEntity(
                        id = report.id,
                        photoUri = report.photoUri,
                        observation = report.observation,
                        timestamp = report.timestamp,
                        stopId = report.stopId,
                    ),
                )
            }
        }

    @Test
    fun `observes reports mapped to domain`() =
        runTest {
            val entity =
                PhotoReportEntity(
                    id = "report-1",
                    photoUri = "content://photo/1",
                    observation = "Sin cartel",
                    timestamp = 1_700_000_000_000L,
                    stopId = 42L,
                )
            every { dataSource.observeByStopId(42L) } returns flowOf(listOf(entity))

            assertEquals(listOf(report("Sin cartel")), repository.observeByStopId(42L).first())
        }

    private fun report(observation: String = "Sin cartel") =
        PhotoReport(
            id = "report-1",
            photoUri = "content://photo/1",
            observation = observation,
            timestamp = 1_700_000_000_000L,
            stopId = 42L,
        )
}
