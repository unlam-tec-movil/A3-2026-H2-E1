package ar.edu.unlam.scaffoldingandroid3.domain.usecase

import ar.edu.unlam.scaffoldingandroid3.domain.model.PhotoReport
import ar.edu.unlam.scaffoldingandroid3.domain.repository.PhotoReportRepository
import javax.inject.Inject

class RegisterPhotoReportUseCase
    @Inject
    constructor(
        private val repository: PhotoReportRepository,
    ) {
        suspend operator fun invoke(report: PhotoReport) = repository.save(report)
    }
