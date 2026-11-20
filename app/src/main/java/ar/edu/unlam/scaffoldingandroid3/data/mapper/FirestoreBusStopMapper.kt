package ar.edu.unlam.scaffoldingandroid3.data.mapper

import ar.edu.unlam.scaffoldingandroid3.data.mapper.BusStopMapper.toLineNumber
import ar.edu.unlam.scaffoldingandroid3.data.mapper.BusStopMapper.toStableLong
import ar.edu.unlam.scaffoldingandroid3.data.model.FirestoreBusLine
import ar.edu.unlam.scaffoldingandroid3.data.model.FirestoreBusStop
import ar.edu.unlam.scaffoldingandroid3.domain.enums.BusLineStatus
import ar.edu.unlam.scaffoldingandroid3.domain.enums.StopStatus
import ar.edu.unlam.scaffoldingandroid3.domain.model.BusLine
import ar.edu.unlam.scaffoldingandroid3.domain.model.BusStop
import ar.edu.unlam.scaffoldingandroid3.domain.model.Latitude
import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationData
import ar.edu.unlam.scaffoldingandroid3.domain.model.Longitude

private const val FALLBACK_STOP_NAME = "Parada de Colectivo"

object FirestoreBusStopMapper {
    fun FirestoreBusStop.toDomain(documentId: String): BusStop? {
        val loc = location ?: return null

        return BusStop(
            stopId = documentId.toStableLong(),
            name = name.ifBlank { FALLBACK_STOP_NAME },
            locationData = LocationData(latitude = Latitude(loc.latitude), longitude = Longitude(loc.longitude)),
            markedAsFavorite = false,
            busLines = lines.map { it.toDomain() }.distinctBy { it.lineId },
            status = StopStatus.ACTIVA,
        )
    }

    fun FirestoreBusLine.toDomain(): BusLine {
        val lineCode = code.trim().ifBlank { "-" }
        val lineNumber = lineCode.toLineNumber()
        val digits = lineNumber.toString()
        val extractedBranch =
            if (lineCode.startsWith(digits) && lineCode.length > digits.length) {
                lineCode.removePrefix(digits).trim()
            } else {
                ""
            }
        val finalBranch =
            ramal?.trim()?.ifBlank { null }
                ?: branch?.trim()?.ifBlank { null }
                ?: extractedBranch

        val dest = destination?.trim() ?: ""
        val formattedDestination =
            when (dest.uppercase()) {
                "I" -> "Ida"
                "V" -> "Vuelta"
                else -> if (dest.isNotBlank()) dest else ""
            }

        val stableLineKey = "$lineCode-$finalBranch-$formattedDestination"

        return BusLine(
            lineId = stableLineKey.toStableLong(),
            number = lineNumber,
            routes = emptyList(),
            currentStatus = BusLineStatus.NORMAL,
            code = lineCode,
            branch = finalBranch,
            destination = formattedDestination,
        )
    }
}
