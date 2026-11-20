package ar.edu.unlam.scaffoldingandroid3.data.mapper

import ar.edu.unlam.scaffoldingandroid3.data.database.room.entities.PhotoReportEntity
import ar.edu.unlam.scaffoldingandroid3.domain.model.PhotoReport

object PhotoReportMapper {
    fun PhotoReport.toEntity(): PhotoReportEntity =
        PhotoReportEntity(
            id = id,
            photoUri = photoUri,
            observation = observation,
            timestamp = timestamp,
            stopId = stopId,
        )

    fun PhotoReportEntity.toDomain(): PhotoReport =
        PhotoReport(
            id = id,
            photoUri = photoUri,
            observation = observation,
            timestamp = timestamp,
            stopId = stopId,
        )
}
