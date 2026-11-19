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
        return BusLine(
            lineId = lineCode.toStableLong(),
            number = lineCode.toLineNumber(),
            routes = emptyList(),
            currentStatus = BusLineStatus.NORMAL,
        )
    }
}
