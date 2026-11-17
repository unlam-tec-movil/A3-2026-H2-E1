package ar.edu.unlam.scaffoldingandroid3.domain.usecase

import ar.edu.unlam.scaffoldingandroid3.domain.repository.PhotoReportRepository
import ar.edu.unlam.scaffoldingandroid3.factory.TestDataFactory
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class GetPhotoReportUseCaseTest {
    private val mockRepository = mockk<PhotoReportRepository>()
    private val useCase = GetPhotoReportsForStopUseCase(mockRepository)
    private val sampleReport = TestDataFactory.defaultPhotoReportInstance()

    @Test
    @DisplayName("Debería delegar al datasource para obtener los reportes de una parada")
    fun getPhotoReportsForStop_shouldDelegateToDataSource() =
        runTest {
            coEvery { mockRepository.getReportsByStopId(sampleReport.stopId) } returns
                flowOf(listOf(sampleReport))

            val result = useCase(sampleReport.stopId)

            assertEquals(listOf(sampleReport), result.first())

            coVerify(exactly = 1) {
                @Suppress("UnusedFlow")
                mockRepository.getReportsByStopId(sampleReport.stopId)
            }
        }

    @Test
    @DisplayName("Debería emitir una lista vacía cuando la parada no tiene reportes")
    fun getPhotoReportsForStop_shouldEmitEmptyListWhenNoReports() =
        runTest {
            coEvery { mockRepository.getReportsByStopId(sampleReport.stopId) } returns
                flowOf(
                    emptyList(),
                )

            val result = useCase(sampleReport.stopId).first()

            val resultAsList = result.toList()

            assertTrue(resultAsList.isEmpty())
        }

    @Test
    @DisplayName("Debería propagar las excepciones lanzadas por el datasource")
    fun getPhotoReportsForStop_shouldPropagateDataSourceExceptions() =
        runTest {
            coEvery { mockRepository.getReportsByStopId(sampleReport.stopId) } throws Exception("Database error")

            @Suppress("UnusedFlow")
            assertThrows<Exception> { useCase(sampleReport.stopId) }
        }
}
