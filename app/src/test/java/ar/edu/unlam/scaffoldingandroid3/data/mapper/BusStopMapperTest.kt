package ar.edu.unlam.scaffoldingandroid3.data.mapper

import ar.edu.unlam.scaffoldingandroid3.data.mapper.BusStopMapper.toCacheEntity
import ar.edu.unlam.scaffoldingandroid3.data.mapper.BusStopMapper.toDomain
import ar.edu.unlam.scaffoldingandroid3.domain.enums.BusLineStatus
import ar.edu.unlam.scaffoldingandroid3.domain.enums.StopStatus
import ar.edu.unlam.scaffoldingandroid3.domain.model.BusLine
import ar.edu.unlam.scaffoldingandroid3.domain.model.BusStop
import ar.edu.unlam.scaffoldingandroid3.domain.model.Latitude
import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationData
import ar.edu.unlam.scaffoldingandroid3.domain.model.Longitude
import org.junit.Assert.assertEquals
import org.junit.Test

class BusStopMapperTest {
    @Test
    fun busStop_toCacheEntityAndToDomain_preservesAllData() {
        val original =
            BusStop(
                stopId = 123L,
                name = "Av. San Martín y Belgrano",
                locationData = LocationData(latitude = Latitude(-34.6705), longitude = Longitude(-58.5628)),
                markedAsFavorite = true,
                busLines =
                    listOf(
                        BusLine(
                            lineId = 96L,
                            number = 96,
                            routes = emptyList(),
                            currentStatus = BusLineStatus.NORMAL,
                            code = "96A",
                            branch = "A",
                            destination = "Plaza Once",
                        ),
                        BusLine(
                            lineId = 185L,
                            number = 185,
                            routes = emptyList(),
                            currentStatus = BusLineStatus.NORMAL,
                            code = "185",
                            branch = "",
                            destination = "Liniers",
                        ),
                    ),
                status = StopStatus.EN_OBRAS,
            )

        val entity = original.toCacheEntity()
        val restored = entity.toDomain()

        assertEquals(original.stopId, restored.stopId)
        assertEquals(original.name, restored.name)
        assertEquals(original.locationData.latitude.value, restored.locationData.latitude.value, 0.0001)
        assertEquals(original.locationData.longitude.value, restored.locationData.longitude.value, 0.0001)
        assertEquals(original.markedAsFavorite, restored.markedAsFavorite)
        assertEquals(original.status, restored.status)
        assertEquals(original.busLines.size, restored.busLines.size)
        assertEquals(original.busLines[0].number, restored.busLines[0].number)
        assertEquals(original.busLines[0].branch, restored.busLines[0].branch)
        assertEquals(original.busLines[0].destination, restored.busLines[0].destination)
        assertEquals(original.busLines[1].number, restored.busLines[1].number)
        assertEquals(original.busLines[1].destination, restored.busLines[1].destination)
    }

    @Test
    fun toDomain_recoversAnActiveStopWhenTheStoredStatusIsInvalid() {
        val entity =
            BusStop(
                stopId = 5L,
                name = "Ruta 3 y Zapiola",
                locationData = LocationData(latitude = Latitude(-34.7), longitude = Longitude(-58.6)),
                markedAsFavorite = false,
                busLines = emptyList(),
                status = StopStatus.ACTIVA,
            ).toCacheEntity().copy(stopStatus = "estado-desconocido")

        val restored = entity.toDomain()

        assertEquals(StopStatus.ACTIVA, restored.status)
    }
}
