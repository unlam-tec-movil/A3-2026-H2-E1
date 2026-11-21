package ar.edu.unlam.scaffoldingandroid3.data.datasources.local

import ar.edu.unlam.scaffoldingandroid3.data.database.room.dao.PhotoReportDao
import ar.edu.unlam.scaffoldingandroid3.data.database.room.entities.PhotoReportEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PhotoReportDataSourceImpl
    @Inject
    constructor(
        private val dao: PhotoReportDao,
    ) : PhotoReportDataSource {
        override suspend fun insert(report: PhotoReportEntity) = dao.insert(report)

        override fun observeByStopId(stopId: Long): Flow<List<PhotoReportEntity>> = dao.observeByStopId(stopId)

        override fun observeByStopIds(stopIds: List<Long>): Flow<List<PhotoReportEntity>> = dao.observeByStopIds(stopIds)
    }
