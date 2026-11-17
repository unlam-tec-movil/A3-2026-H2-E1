package ar.edu.unlam.scaffoldingandroid3.data.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ar.edu.unlam.scaffoldingandroid3.data.database.room.entities.PhotoReportCacheEntity
import kotlinx.coroutines.flow.Flow

/**
 * Interface DAO para operaciones de acceso a datos de [PhotoReportCacheEntity].
 *
 * Proporciona métodos para insertar reportes y obtener reportes por ID de parada.
 *
 * @see PhotoReportCacheEntity
 */
@Dao
interface PhotoReportDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(entity: PhotoReportCacheEntity)

    @Query("SELECT * FROM photo_reports WHERE stopId = :stopId ORDER BY timestamp DESC")
    fun getReportsByStopId(stopId: Long): Flow<List<PhotoReportCacheEntity>>
}
