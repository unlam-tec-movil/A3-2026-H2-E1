package ar.edu.unlam.scaffoldingandroid3.data.repositories

import ar.edu.unlam.scaffoldingandroid3.data.datasources.local.PhotoReportDataSource
import ar.edu.unlam.scaffoldingandroid3.domain.model.PhotoReport
import ar.edu.unlam.scaffoldingandroid3.domain.repository.PhotoReportRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PhotoReportRepositoryImpl
    @Inject
    constructor(
        private val dataSource: PhotoReportDataSource,
    ) : PhotoReportRepository {
        override suspend fun saveReport(report: PhotoReport) {
            dataSource.insertReport(report)
        }

        override suspend fun getReportsByStopId(stopId: Long): Flow<List<PhotoReport>> = dataSource.getReportsByStopId(stopId)
    }
