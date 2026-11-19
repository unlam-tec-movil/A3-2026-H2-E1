package ar.edu.unlam.scaffoldingandroid3.data.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ar.edu.unlam.scaffoldingandroid3.data.database.room.entities.BusStopCacheEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BusStopCacheDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStops(stops: List<BusStopCacheEntity>)

    @Query("DELETE FROM bus_stop_cache")
    suspend fun clearCache()

    @Query("SELECT * FROM bus_stop_cache ORDER BY cachedAt DESC")
    fun getAllCachedStops(): Flow<List<BusStopCacheEntity>>

    @Query("SELECT * FROM bus_stop_cache ORDER BY cachedAt DESC")
    suspend fun getCachedStopsList(): List<BusStopCacheEntity>
}
