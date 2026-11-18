package ar.edu.unlam.scaffoldingandroid3.data.mapper

import ar.edu.unlam.scaffoldingandroid3.data.model.FirestoreBusLine
import ar.edu.unlam.scaffoldingandroid3.data.model.FirestoreBusStop
import ar.edu.unlam.scaffoldingandroid3.domain.BusLine
import ar.edu.unlam.scaffoldingandroid3.domain.BusStop
import ar.edu.unlam.scaffoldingandroid3.domain.GeoPoint

object FirestoreBusStopMapper {
    fun FirestoreBusStop.toDomain(documentId: String): BusStop? {
        val loc = location ?: return null

        return BusStop(
            id = documentId,
            name = name.ifBlank { "Parada de Colectivo" },
            lines = lines.map { it.toDomain() },
            location = GeoPoint(latitude = loc.latitude, longitude = loc.longitude),
        )
    }

    fun FirestoreBusLine.toDomain(): BusLine {
        val dest = destination?.trim() ?: ""
        val formattedDestination =
            when (dest.uppercase()) {
                "I" -> "Ida"
                "V" -> "Vuelta"
                else -> if (dest.isNotBlank()) dest else "Sentido no especificado"
            }

        return BusLine(
            code = code.ifBlank { "-" },
            destination = formattedDestination,
        )
    }
}
