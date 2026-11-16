package ar.edu.unlam.scaffoldingandroid3.data.datasources.local

import ar.edu.unlam.scaffoldingandroid3.domain.model.PhotoReport
import kotlinx.coroutines.flow.Flow

interface PhotoReportDataSource {
    suspend fun insertReport(report: PhotoReport)

    suspend fun getReportsByStopId(stopId: Long): Flow<List<PhotoReport>>
}
