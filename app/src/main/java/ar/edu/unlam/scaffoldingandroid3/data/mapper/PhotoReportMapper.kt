package ar.edu.unlam.scaffoldingandroid3.data.mapper

import android.os.Build
import androidx.annotation.RequiresApi
import ar.edu.unlam.scaffoldingandroid3.data.database.room.entities.PhotoReportCacheEntity
import ar.edu.unlam.scaffoldingandroid3.domain.model.PhotoReport
import kotlin.time.Instant
import kotlin.time.toJavaInstant

/**
 * Convierte un modelo de dominio [PhotoReport] a una entidad de caché [PhotoReportCacheEntity].
 *
 * Esta función mapea los datos del reporte de fotografía desde el modelo de dominio
 * hacia la entidad de persistencia, transformando el timestamp a milisegundos Unix.
 *
 * **Transformaciones**:
 * - [id]: Se mantiene igual
 * - [photoUri]: Se mantiene igual
 * - [observation]: Se mantiene igual
 * - [timestamp]: Se convierte de [Instant] a milisegundos usando [toEpochMilli()]
 * - [stopId]: Se mantiene igual
 *
 * @param photoReport El modelo de dominio a convertir a entidad.
 * @return Una nueva instancia de [PhotoReportCacheEntity] con los datos mapeados.
 * @see PhotoReport
 * @see PhotoReportCacheEntity
 * @see Instant
 */
@RequiresApi(Build.VERSION_CODES.O)
fun PhotoReport.toEntity(): PhotoReportCacheEntity =
    PhotoReportCacheEntity(
        id = this.id,
        photoUri = this.photoUri,
        observation = this.observation,
        timestamp = this.timestamp.toEpochMilli(),
        stopId = this.stopId,
    )

/**
 * Convierte una entidad de caché [PhotoReportCacheEntity] a un modelo de dominio [PhotoReport].
 *
 * Esta función mapea los datos desde la entidad de persistencia hacia el modelo de dominio,
 * transformando el timestamp de milisegundos Unix a [Instant].
 *
 * **Transformaciones**:
 * - [id]: Se mantiene igual
 * - [photoUri]: Se mantiene igual
 * - [observation]: Se mantiene igual
 * - [timestamp]: Se convierte de milisegundos a [Instant] usando [Instant.fromEpochMilliseconds()]
 * - [stopId]: Se mantiene igual
 *
 * @param photoReportCacheEntity La entidad de caché a convertir a modelo de dominio.
 * @return Un nuevo modelo de dominio [PhotoReport] con los datos mapeados.
 * @see PhotoReportCacheEntity
 * @see PhotoReport
 * @see Instant
 */
@RequiresApi(Build.VERSION_CODES.O)
fun PhotoReportCacheEntity.toDomain(): PhotoReport =
    PhotoReport(
        id = this.id,
        photoUri = this.photoUri,
        observation = this.observation,
        timestamp = Instant.fromEpochMilliseconds(this.timestamp).toJavaInstant(),
        stopId = this.stopId,
    )
