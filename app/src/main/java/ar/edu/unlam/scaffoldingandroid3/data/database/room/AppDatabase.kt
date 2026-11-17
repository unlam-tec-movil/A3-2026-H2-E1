package ar.edu.unlam.scaffoldingandroid3.data.database.room

import androidx.room.Database
import androidx.room.RoomDatabase
import ar.edu.unlam.scaffoldingandroid3.data.database.room.dao.BusStopCacheDao
import ar.edu.unlam.scaffoldingandroid3.data.database.room.dao.PhotoReportDao
import ar.edu.unlam.scaffoldingandroid3.data.database.room.dao.SearchHistoryDao
import ar.edu.unlam.scaffoldingandroid3.data.database.room.entities.BusStopCacheEntity
import ar.edu.unlam.scaffoldingandroid3.data.database.room.entities.PhotoReportCacheEntity
import ar.edu.unlam.scaffoldingandroid3.data.database.room.entities.SearchHistoryEntity

@Database(
    entities = [BusStopCacheEntity::class, SearchHistoryEntity::class, PhotoReportCacheEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun busStopCacheDao(): BusStopCacheDao

    abstract fun searchHistoryDao(): SearchHistoryDao

    abstract fun photoReportDao(): PhotoReportDao
}
