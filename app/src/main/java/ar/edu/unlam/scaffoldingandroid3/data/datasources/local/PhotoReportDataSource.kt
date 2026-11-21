package ar.edu.unlam.scaffoldingandroid3.data.datasources.local

import ar.edu.unlam.scaffoldingandroid3.data.database.room.entities.PhotoReportEntity
import kotlinx.coroutines.flow.Flow

interface PhotoReportDataSource {
    suspend fun insert(report: PhotoReportEntity)

    fun observeByStopId(stopId: Long): Flow<List<PhotoReportEntity>>

    fun observeByStopIds(stopIds: List<Long>): Flow<List<PhotoReportEntity>>
}
