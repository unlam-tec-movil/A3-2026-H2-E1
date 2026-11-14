@file:Suppress("PackageName")

package ar.edu.unlam.scaffoldingandroid3.data.datasources.local.PhotoReportDataSourceImpl

import ar.edu.unlam.scaffoldingandroid3.data.database.room.dao.PhotoReportDao
import ar.edu.unlam.scaffoldingandroid3.data.datasources.local.PhotoReportDataSource
import ar.edu.unlam.scaffoldingandroid3.data.mapper.toEntity
import ar.edu.unlam.scaffoldingandroid3.domain.model.PhotoReport
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class PhotoReportDataSourceImpl
    @Inject
    constructor(
        private val dao: PhotoReportDao,
    ) : PhotoReportDataSource {
        override suspend fun insertReport(report: PhotoReport) {
            dao.insertReport(report.toEntity())
        }

        override suspend fun getReportsByStopId(stopId: Long): StateFlow<List<PhotoReport>> {
            TODO("Not yet implemented")
        }
    }
