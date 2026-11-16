@file:Suppress("PackageName")

package ar.edu.unlam.scaffoldingandroid3.data.datasources.local.PhotoReportDataSourceImpl

import ar.edu.unlam.scaffoldingandroid3.data.database.room.dao.PhotoReportDao
import ar.edu.unlam.scaffoldingandroid3.data.datasources.local.PhotoReportDataSource
import ar.edu.unlam.scaffoldingandroid3.data.mapper.toDomain
import ar.edu.unlam.scaffoldingandroid3.data.mapper.toEntity
import ar.edu.unlam.scaffoldingandroid3.domain.model.PhotoReport
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PhotoReportDataSourceImpl
    @Inject
    constructor(
        private val dao: PhotoReportDao,
    ) : PhotoReportDataSource {
        override suspend fun insertReport(report: PhotoReport) {
            dao.insertReport(report.toEntity())
        }

        override suspend fun getReportsByStopId(stopId: Long): Flow<List<PhotoReport>> =
            dao.getReportsByStopId(stopId).map { entityList ->
                entityList.map {
                    it.toDomain()
                }
            }
    }
