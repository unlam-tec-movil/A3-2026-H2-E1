package ar.edu.unlam.scaffoldingandroid3.domain.usecase

import ar.edu.unlam.scaffoldingandroid3.domain.model.PhotoReport
import ar.edu.unlam.scaffoldingandroid3.domain.repository.PhotoReportRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPhotoReportsForStopUseCase
    @Inject
    constructor(
        private val repository: PhotoReportRepository,
    ) {
        suspend operator fun invoke(stopId: Long): Flow<List<PhotoReport>> = repository.getReportsByStopId(stopId)
    }
