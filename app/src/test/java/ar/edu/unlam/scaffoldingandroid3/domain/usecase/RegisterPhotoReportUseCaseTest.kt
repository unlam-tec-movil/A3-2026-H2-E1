package ar.edu.unlam.scaffoldingandroid3.domain.usecase

import ar.edu.unlam.scaffoldingandroid3.domain.repository.PhotoReportRepository
import ar.edu.unlam.scaffoldingandroid3.factory.TestDataFactory.DataFactory.defaultPhotoReportInstance
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertThrows

class RegisterPhotoReportUseCaseTest {
    private val mockRepository = mockk<PhotoReportRepository>()
    private val useCase = RegisterPhotoReportUseCase(mockRepository)
    private var sampleReport = defaultPhotoReportInstance()

    @Test
    @DisplayName("Debería crear un reporte fotográfico con datos válidos")
    fun shouldSavePhotoReportWithValidData() =
        runTest {
            coEvery { mockRepository.saveReport(sampleReport) } just runs

            useCase(sampleReport)

            coVerify(exactly = 1) { mockRepository.saveReport(sampleReport) }
        }

    @Test
    @DisplayName("Debería propagar la excepción lanzada desde el repositorio al fallar")
    fun shouldPropagateExceptionThrownFromRepository() =
        runTest {
            coEvery { mockRepository.saveReport(sampleReport) } throws Exception("Database error")

            assertThrows<Exception> { useCase(sampleReport) }
        }
}
