package ar.edu.unlam.scaffoldingandroid3.data.local

import ar.edu.unlam.scaffoldingandroid3.data.datasources.local.PhotoReportDataSource
import ar.edu.unlam.scaffoldingandroid3.data.repositories.PhotoReportRepositoryImpl
import ar.edu.unlam.scaffoldingandroid3.domain.repository.PhotoReportRepository
import ar.edu.unlam.scaffoldingandroid3.factory.TestDataFactory.DataFactory.defaultPhotoReportInstance
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName

class PhotoReportRepositoryImplTest {
    private val dataSource = mockk<PhotoReportDataSource>()
    private val repository: PhotoReportRepository = PhotoReportRepositoryImpl(dataSource)
    val sampleReport = defaultPhotoReportInstance()

    @Test
    @DisplayName("Debería llamar al data source local para guardar un reporte")
    fun saveReport_shouldCallLocalDataSourceToPersistReport() =
        runTest {
            coEvery { dataSource.insertReport(any()) } just runs
            coEvery { repository.saveReport(sampleReport) } just runs
            repository.saveReport(sampleReport)

            coVerify(exactly = 1) { dataSource.insertReport(any()) }
        }

    @Test
    @DisplayName("Debería obtener un reporte fotográfico existente desde el repositorio mediante su ID")
    fun shouldGetPhotoReportByIdIfExists() =
        runTest {
            val stateFlow = MutableStateFlow(listOf(sampleReport))

            coEvery { dataSource.getReportsByStopId(sampleReport.stopId) } returns stateFlow

            val result = repository.getReportsByStopId(sampleReport.stopId)

            assertEquals(stateFlow, result)
            coVerify(exactly = 1) { dataSource.getReportsByStopId(sampleReport.stopId) }
        }
}
