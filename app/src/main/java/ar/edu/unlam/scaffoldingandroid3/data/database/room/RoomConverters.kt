package ar.edu.unlam.scaffoldingandroid3.data.database.room

import androidx.room.TypeConverter
import ar.edu.unlam.scaffoldingandroid3.domain.BusLine
import kotlinx.serialization.json.Json

class RoomConverters {
    @TypeConverter
    fun fromBusLineList(lines: List<BusLine>): String = Json.encodeToString(lines)

    @TypeConverter
    fun toBusLineList(linesJson: String): List<BusLine> = if (linesJson.isBlank()) emptyList() else Json.decodeFromString(linesJson)
}
