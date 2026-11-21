package ar.edu.unlam.scaffoldingandroid3.data.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ar.edu.unlam.scaffoldingandroid3.data.database.room.entities.PhotoReportEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoReportDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(report: PhotoReportEntity)

    @Query("SELECT * FROM photo_reports WHERE stopId = :stopId ORDER BY timestamp DESC")
    fun observeByStopId(stopId: Long): Flow<List<PhotoReportEntity>>

    @Query("SELECT * FROM photo_reports WHERE stopId IN (:stopIds) ORDER BY timestamp DESC")
    fun observeByStopIds(stopIds: List<Long>): Flow<List<PhotoReportEntity>>
}
