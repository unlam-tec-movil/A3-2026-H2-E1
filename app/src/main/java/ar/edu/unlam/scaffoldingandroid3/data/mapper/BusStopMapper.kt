package ar.edu.unlam.scaffoldingandroid3.data.mapper

import ar.edu.unlam.scaffoldingandroid3.data.database.room.entities.BusStopCacheEntity
import ar.edu.unlam.scaffoldingandroid3.domain.BusLine
import ar.edu.unlam.scaffoldingandroid3.domain.BusStop
import ar.edu.unlam.scaffoldingandroid3.domain.GeoPoint
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bson.Document

object BusStopMapper {
    fun fromBsonDocument(doc: Document): BusStop {
        val id = doc.get("_id")?.toString() ?: ""
        val properties = doc.get("properties", Document::class.java) ?: Document()
        val name =
            properties.getString("DIRECCION")
                ?: properties.getString("CALLE")
                ?: "Parada de Colectivo"

        val location = parseLocation(doc, properties)
        val lines = parseLines(properties)

        return BusStop(
            id = id,
            name = name,
            lines = lines,
            location = location,
        )
    }

    private fun parseLocation(
        doc: Document,
        properties: Document,
    ): GeoPoint {
        val geometry = doc.get("geometry", Document::class.java)
        if (geometry != null) {
            val coordinates = geometry.get("coordinates") as? List<*>
            if (coordinates != null && coordinates.size >= 2) {
                val lng = (coordinates[0] as? Number)?.toDouble() ?: 0.0
                val lat = (coordinates[1] as? Number)?.toDouble() ?: 0.0
                return GeoPoint(latitude = lat, longitude = lng)
            }
        }

        val lat =
            properties.getDouble("coord_Y")
                ?: properties.get("coord_Y")?.toString()?.toDoubleOrNull()
                ?: 0.0
        val lng =
            properties.getDouble("coord_X")
                ?: properties.get("coord_X")?.toString()?.toDoubleOrNull()
                ?: 0.0

        return GeoPoint(latitude = lat, longitude = lng)
    }

    private fun parseLines(properties: Document): List<BusLine> {
        val linesList = mutableListOf<BusLine>()

        properties.keys
            .filter { key -> key.startsWith("L", ignoreCase = true) && !key.contains("sen", ignoreCase = true) }
            .sortedBy { key -> key.substring(1).toIntOrNull() ?: 99 }
            .forEach { key ->
                val code = properties.getString(key)?.trim()
                if (!code.isNullOrBlank() && !code.equals("null", ignoreCase = true)) {
                    val senseKey = "${key.lowercase()}_sen"
                    val sense =
                        properties.getString(senseKey)
                            ?: properties.getString(senseKey.uppercase())
                            ?: ""
                    val destination = if (sense.isNotBlank()) "Sentido $sense" else "Recorrido habitual"
                    linesList.add(BusLine(code = code, destination = destination))
                }
            }

        return linesList.distinctBy { it.code }
    }

    fun BusStop.toCacheEntity(): BusStopCacheEntity =
        BusStopCacheEntity(
            id = id,
            name = name,
            latitude = location.latitude,
            longitude = location.longitude,
            linesJson = Json.encodeToString(lines),
        )

    fun BusStopCacheEntity.toDomain(): BusStop =
        BusStop(
            id = id,
            name = name,
            lines = if (linesJson.isBlank()) emptyList() else Json.decodeFromString(linesJson),
            location = GeoPoint(latitude = latitude, longitude = longitude),
        )
}
