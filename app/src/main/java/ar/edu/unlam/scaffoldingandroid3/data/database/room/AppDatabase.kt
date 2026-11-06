package ar.edu.unlam.scaffoldingandroid3.data.database.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ar.edu.unlam.scaffoldingandroid3.data.database.room.dao.BusStopCacheDao
import ar.edu.unlam.scaffoldingandroid3.data.database.room.dao.SearchHistoryDao
import ar.edu.unlam.scaffoldingandroid3.data.database.room.entities.BusStopCacheEntity
import ar.edu.unlam.scaffoldingandroid3.data.database.room.entities.SearchHistoryEntity

@Database(
    entities = [BusStopCacheEntity::class, SearchHistoryEntity::class],
    version = 1,
    exportSchema = false,
)
@TypeConverters(RoomConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun busStopCacheDao(): BusStopCacheDao

    abstract fun searchHistoryDao(): SearchHistoryDao
}
