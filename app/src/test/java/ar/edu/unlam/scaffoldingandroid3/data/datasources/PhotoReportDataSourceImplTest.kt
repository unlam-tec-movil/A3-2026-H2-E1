package ar.edu.unlam.scaffoldingandroid3.data.datasources

import ar.edu.unlam.scaffoldingandroid3.data.database.room.dao.PhotoReportDao
import ar.edu.unlam.scaffoldingandroid3.data.database.room.entities.PhotoReportCacheEntity
import ar.edu.unlam.scaffoldingandroid3.data.datasources.local.PhotoReportDataSourceImpl.PhotoReportDataSourceImpl
import ar.edu.unlam.scaffoldingandroid3.data.mapper.toEntity
import ar.edu.unlam.scaffoldingandroid3.factory.TestDataFactory
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class PhotoReportDataSourceImplTest {
    private val dao = mockk<PhotoReportDao>()
    private val datasource = PhotoReportDataSourceImpl(dao)
    private val sampleReport = TestDataFactory.defaultPhotoReportInstance()

    @Test
    @DisplayName("Debería persistir en el DAO la entidad mapeada desde el dominio")
    fun insertReport_shouldPersistMappedEntityInDao() =
        runTest {
            val expectedPhotoReportEntity =
                PhotoReportCacheEntity(
                    id = sampleReport.id,
                    photoUri = sampleReport.photoUri,
                    observation = sampleReport.observation,
                    timestamp = sampleReport.timestamp.toEpochMilli(),
                    stopId = sampleReport.stopId,
                )

            coEvery { dao.insertReport(any()) } just runs

            datasource.insertReport(sampleReport)

            coVerify(exactly = 1) { dao.insertReport(expectedPhotoReportEntity) }
        }

    @Test
    @DisplayName("Debería propagar la excepción lanzada por el DAO al insertar")
    fun insertReport_shouldPropagateDaoException() =
        runTest {
            coEvery { dao.insertReport(any()) } throws IllegalStateException("INSERT error on DB")

            assertThrows<IllegalStateException> { datasource.insertReport(sampleReport) }

            coVerify(exactly = 1) { dao.insertReport(any()) }
        }

    @Test
    @DisplayName("Debería consultar el DAO con el stopId solicitado")
    fun getReportsByStopId_shouldQueryDaoWithRequestedStopId() =
        runTest {
            val expectedEmission = flowOf(listOf(sampleReport.toEntity()))

            coEvery { dao.getReportsByStopId(sampleReport.stopId) } returns expectedEmission

            val result = datasource.getReportsByStopId(sampleReport.stopId).first()

            assertEquals(1, result.size)
            assertEquals(sampleReport.id, result.first().id)
            assertEquals(sampleReport.photoUri, result.first().photoUri)
            assertEquals(sampleReport.observation, result.first().observation)
            assertEquals(sampleReport.stopId, result.first().stopId)
            assertEquals(sampleReport.timestamp.toEpochMilli(), result.first().timestamp.toEpochMilli())
        }

    @Test
    @DisplayName("Debería emitir nuevamente cuando el DAO publica una actualización")
    fun getReportsByStopId_shouldEmitSubsequentDaoUpdates() {
    }

    @Test
    @DisplayName("Debería emitir una lista vacía cuando la parada no tiene reportes")
    fun getReportsByStopId_shouldEmitEmptyListWhenStopHasNoReports() =
        runTest {
            val emptyFlow: Flow<List<PhotoReportCacheEntity>> = flowOf(listOf())

            coEvery { dao.getReportsByStopId(sampleReport.stopId) } returns emptyFlow

            val result = datasource.getReportsByStopId(sampleReport.stopId).first()

            assertEquals(0, result.count())
        }
}
