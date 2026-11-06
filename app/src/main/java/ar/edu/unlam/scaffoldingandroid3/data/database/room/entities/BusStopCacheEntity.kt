package ar.edu.unlam.scaffoldingandroid3.data.database.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bus_stop_cache")
data class BusStopCacheEntity(
    @PrimaryKey val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val linesJson: String,
    val cachedAt: Long = System.currentTimeMillis(),
)
