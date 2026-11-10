package ar.edu.unlam.scaffoldingandroid3.data.mapper

import ar.edu.unlam.scaffoldingandroid3.data.mapper.BusStopMapper.fromBsonDocument
import ar.edu.unlam.scaffoldingandroid3.data.mapper.BusStopMapper.toCacheEntity
import ar.edu.unlam.scaffoldingandroid3.data.mapper.BusStopMapper.toDomain
import ar.edu.unlam.scaffoldingandroid3.domain.BusLine
import ar.edu.unlam.scaffoldingandroid3.domain.BusStop
import ar.edu.unlam.scaffoldingandroid3.domain.GeoPoint
import org.bson.Document
import org.bson.types.ObjectId
import org.junit.Assert.assertEquals
import org.junit.Test

class BusStopMapperTest {
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
                .append("L2", "46")
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

        assertEquals(objectId.toString(), busStop.id)
        assertEquals("435 REGIMIENTO DE PATRICIOS AV.", busStop.name)
        assertEquals(-34.6340118, busStop.location.latitude, 0.0001)
        assertEquals(-58.3702771, busStop.location.longitude, 0.0001)
        assertEquals(2, busStop.lines.size)
        assertEquals("24", busStop.lines[0].code)
        assertEquals("Sentido I", busStop.lines[0].destination)
        assertEquals("46", busStop.lines[1].code)
        assertEquals("Sentido V", busStop.lines[1].destination)
    }

    @Test
    fun busStop_toCacheEntityAndToDomain_preservesAllData() {
        val original =
            BusStop(
                id = "stop-123",
                name = "Av. San Martín y Belgrano",
                lines = listOf(BusLine("96", "Plaza Once"), BusLine("185", "Liniers")),
                location = GeoPoint(latitude = -34.6705, longitude = -58.5628),
            )

        val entity = original.toCacheEntity()
        val restored = entity.toDomain()

        assertEquals(original.id, restored.id)
        assertEquals(original.name, restored.name)
        assertEquals(original.location.latitude, restored.location.latitude, 0.0001)
        assertEquals(original.location.longitude, restored.location.longitude, 0.0001)
        assertEquals(original.lines.size, restored.lines.size)
        assertEquals(original.lines[0].code, restored.lines[0].code)
        assertEquals(original.lines[0].destination, restored.lines[0].destination)
    }
}
