package ar.edu.unlam.scaffoldingandroid3.data.database.room.dao

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ar.edu.unlam.scaffoldingandroid3.data.database.room.entities.PhotoReportCacheEntity
import kotlinx.coroutines.flow.Flow

interface PhotoReportDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(entity: PhotoReportCacheEntity)

    @Query("SELECT * FROM photo_reports WHERE stopId = :stopId ORDER BY timestamp DESC")
    fun getReportsByStopId(stopId: Long): Flow<PhotoReportCacheEntity>
}
