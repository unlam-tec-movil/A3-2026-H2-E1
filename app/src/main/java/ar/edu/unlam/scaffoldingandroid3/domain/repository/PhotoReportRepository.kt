package ar.edu.unlam.scaffoldingandroid3.domain.repository

import ar.edu.unlam.scaffoldingandroid3.domain.model.PhotoReport
import kotlinx.coroutines.flow.Flow

interface PhotoReportRepository {
    suspend fun save(report: PhotoReport)

    fun observeByStopId(stopId: Long): Flow<List<PhotoReport>>

    fun observeByStopIds(stopIds: List<Long>): Flow<List<PhotoReport>>
}
