package ar.edu.unlam.scaffoldingandroid3.data.mapper

import android.os.Build
import androidx.annotation.RequiresApi
import ar.edu.unlam.scaffoldingandroid3.data.database.room.entities.PhotoReportCacheEntity
import ar.edu.unlam.scaffoldingandroid3.domain.model.PhotoReport
import kotlin.time.Instant
import kotlin.time.toJavaInstant

@RequiresApi(Build.VERSION_CODES.O)
fun PhotoReport.toEntity(): PhotoReportCacheEntity =
    PhotoReportCacheEntity(
        id = this.id,
        photoUri = this.photoUri,
        observation = this.observation,
        timestamp = this.timestamp.toEpochMilli(),
        stopId = this.stopId,
    )

@RequiresApi(Build.VERSION_CODES.O)
fun PhotoReportCacheEntity.toDomain(): PhotoReport =
    PhotoReport(
        id = this.id,
        photoUri = this.photoUri,
        observation = this.observation,
        timestamp = Instant.fromEpochMilliseconds(this.timestamp).toJavaInstant(),
        stopId = this.stopId,
    )
