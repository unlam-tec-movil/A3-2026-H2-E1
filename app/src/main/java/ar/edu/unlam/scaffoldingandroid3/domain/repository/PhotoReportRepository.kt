package ar.edu.unlam.scaffoldingandroid3.domain.repository

import ar.edu.unlam.scaffoldingandroid3.domain.model.PhotoReport
import kotlinx.coroutines.flow.StateFlow

interface PhotoReportRepository {
    suspend fun saveReport(report: PhotoReport)

    suspend fun getReportsByStopId(stopId: Long): StateFlow<List<PhotoReport>>
}
