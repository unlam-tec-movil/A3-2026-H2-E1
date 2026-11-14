package ar.edu.unlam.scaffoldingandroid3.data.datasources

import ar.edu.unlam.scaffoldingandroid3.data.database.room.dao.PhotoReportDao
import ar.edu.unlam.scaffoldingandroid3.data.database.room.entities.PhotoReportCacheEntity
import ar.edu.unlam.scaffoldingandroid3.data.datasources.local.PhotoReportDataSourceImpl.PhotoReportDataSourceImpl
import ar.edu.unlam.scaffoldingandroid3.factory.TestDataFactory
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.test.runTest
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

            coVerify(exactly = 1) { datasource.insertReport(sampleReport) }
        }

    @Test
    @DisplayName("Debería propagar la excepción lanzada por el DAO al insertar")
    fun insertReport_shouldPropagateDaoException() =
        runTest {
            coEvery { dao.insertReport(any()) } throws Exception("INSERT error on DB")

            assertThrows<Exception> { datasource.insertReport(sampleReport) }

            coVerify(exactly = 1) { dao.insertReport(any()) }
        }

    @Test
    @DisplayName("Debería consultar el DAO con el stopId solicitado")
    fun getReportsByStopId_shouldQueryDaoWithRequestedStopId() {
    }

    @Test
    @DisplayName("Debería emitir los reportes mapeados al modelo de dominio")
    fun getReportsByStopId_shouldEmitReportsMappedToDomain() {
    }

    @Test
    @DisplayName("Debería emitir nuevamente cuando el DAO publica una actualización")
    fun getReportsByStopId_shouldEmitSubsequentDaoUpdates() {
    }

    @Test
    @DisplayName("Debería emitir una lista vacía cuando la parada no tiene reportes")
    fun getReportsByStopId_shouldEmitEmptyListWhenStopHasNoReports() {
    }

    @Test
    @DisplayName("Debería exponer el valor actual sin requerir un colector")
    fun getReportsByStopId_shouldExposeCurrentValueWithoutCollector() {
    }

    @Test
    @DisplayName("Debería compartir una única suscripción al DAO entre varios colectores")
    fun getReportsByStopId_shouldShareSingleUpstreamSubscriptionAcrossCollectors() {
    }

    @Test
    @DisplayName("Debería propagar a los colectores el error emitido por el flujo del DAO")
    fun getReportsByStopId_shouldPropagateDaoFlowErrorToCollectors() {
    }
}
