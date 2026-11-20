package ar.edu.unlam.scaffoldingandroid3.data.repositories

import ar.edu.unlam.scaffoldingandroid3.data.datasources.local.PhotoReportDataSource
import ar.edu.unlam.scaffoldingandroid3.data.mapper.PhotoReportMapper.toDomain
import ar.edu.unlam.scaffoldingandroid3.data.mapper.PhotoReportMapper.toEntity
import ar.edu.unlam.scaffoldingandroid3.domain.model.PhotoReport
import ar.edu.unlam.scaffoldingandroid3.domain.repository.PhotoReportRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PhotoReportRepositoryImpl
    @Inject
    constructor(
        private val dataSource: PhotoReportDataSource,
    ) : PhotoReportRepository {
        override suspend fun save(report: PhotoReport) = dataSource.insert(report.toEntity())

        override fun observeByStopId(stopId: Long): Flow<List<PhotoReport>> =
            dataSource.observeByStopId(stopId).map { reports -> reports.map { it.toDomain() } }
    }
