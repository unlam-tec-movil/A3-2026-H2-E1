package ar.edu.unlam.scaffoldingandroid3.data.mapper

import ar.edu.unlam.scaffoldingandroid3.data.mapper.BusStopMapper.fromBsonDocument
import ar.edu.unlam.scaffoldingandroid3.data.mapper.BusStopMapper.toCacheEntity
import ar.edu.unlam.scaffoldingandroid3.data.mapper.BusStopMapper.toDomain
import ar.edu.unlam.scaffoldingandroid3.domain.enums.BusLineStatus
import ar.edu.unlam.scaffoldingandroid3.domain.enums.StopStatus
import ar.edu.unlam.scaffoldingandroid3.domain.model.BusLine
import ar.edu.unlam.scaffoldingandroid3.domain.model.BusStop
import ar.edu.unlam.scaffoldingandroid3.domain.model.Latitude
import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationData
import ar.edu.unlam.scaffoldingandroid3.domain.model.Longitude
import org.bson.Document
import org.bson.types.ObjectId
import org.junit.Assert.assertEquals
import org.junit.Test

class BusStopMapperTest {
    /**
     * Hash FNV-1a de 64 bits, igual al que usa el mapper internamente: se replica acá para
     * validar que el mismo documento siempre produce el mismo id, sin exponer la función.
     */
    private fun stableLong(value: String): Long =
        value.fold(0xcbf29ce484222325uL) { acc, char -> (acc xor char.code.toULong()) * 0x100000001b3uL }.toLong()

    @Test
    fun fromBsonDocument_correctlyParsesGeoJsonFeature() {
        val propertiesDoc =
            Document()
                .append("DIRECCION", "435 REGIMIENTO DE PATRICIOS AV.")
                .append("CALLE", "REGIMIENTO DE PATRICIOS AV.")
                .append("coord_X", -58.3702771)
                .append("coord_Y", -34.6340118)
                .append("L1", "24")
                .append("l1_sen", "I")
                .append("L2", "10A")
                .append("l2_sen", "V")

        val geometryDoc =
            Document()
                .append("type", "Point")
                .append("coordinates", listOf(-58.3702771, -34.6340118))

        val objectId = ObjectId()
        val bsonDoc =
            Document()
                .append("_id", objectId)
                .append("type", "Feature")
                .append("properties", propertiesDoc)
                .append("geometry", geometryDoc)

        val busStop = fromBsonDocument(bsonDoc)

        assertEquals(stableLong(objectId.toHexString()), busStop.stopId)
        assertEquals("435 REGIMIENTO DE PATRICIOS AV.", busStop.name)
        assertEquals(-34.6340118, busStop.locationData.latitude.value, 0.0001)
        assertEquals(-58.3702771, busStop.locationData.longitude.value, 0.0001)
        assertEquals(2, busStop.busLines.size)
        assertEquals(24, busStop.busLines[0].number)
        assertEquals(10, busStop.busLines[1].number)
    }

    @Test
    fun fromBsonDocument_fallsBackToCoordPropertiesWhenThereIsNoGeometry() {
        val propertiesDoc =
            Document()
                .append("CALLE", "AV. RIVADAVIA")
                .append("coord_X", -58.4)
                .append("coord_Y", -34.6)
        val bsonDoc =
            Document()
                .append("_id", ObjectId())
                .append("properties", propertiesDoc)

        val busStop = fromBsonDocument(bsonDoc)

        assertEquals("AV. RIVADAVIA", busStop.name)
        assertEquals(-34.6, busStop.locationData.latitude.value, 0.0001)
        assertEquals(-58.4, busStop.locationData.longitude.value, 0.0001)
        assertEquals(0, busStop.busLines.size)
    }

    @Test
    fun fromBsonDocument_producesTheSameStopIdForTheSameDocument() {
        val objectId = ObjectId()
        val bsonDoc =
            Document()
                .append("_id", objectId)
                .append("properties", Document().append("CALLE", "AV. RIVADAVIA"))

        val firstStop = fromBsonDocument(bsonDoc)
        val secondStop = fromBsonDocument(bsonDoc)

        assertEquals(firstStop.stopId, secondStop.stopId)
    }

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
                        BusLine(lineId = 96L, number = 96, routes = emptyList(), currentStatus = BusLineStatus.NORMAL),
                        BusLine(lineId = 185L, number = 185, routes = emptyList(), currentStatus = BusLineStatus.NORMAL),
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
        assertEquals(original.busLines[1].number, restored.busLines[1].number)
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
