package ar.edu.unlam.scaffoldingandroid3.data.database.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import ar.edu.unlam.scaffoldingandroid3.domain.enums.StopStatus

@Entity(tableName = "bus_stop_cache")
data class BusStopCacheEntity(
    @PrimaryKey val stopId: Long,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val linesJson: String,
    val markedAsFavorite: Boolean = false,
    val stopStatus: String = StopStatus.ACTIVA.name,
    val cachedAt: Long = System.currentTimeMillis(),
)
