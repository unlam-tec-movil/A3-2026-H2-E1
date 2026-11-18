package ar.edu.unlam.scaffoldingandroid3.data.mapper

import ar.edu.unlam.scaffoldingandroid3.data.mapper.BusStopMapper.toCacheEntity
import ar.edu.unlam.scaffoldingandroid3.data.mapper.BusStopMapper.toDomain
import ar.edu.unlam.scaffoldingandroid3.data.mapper.FirestoreBusStopMapper.toDomain
import ar.edu.unlam.scaffoldingandroid3.data.model.FirestoreBusLine
import ar.edu.unlam.scaffoldingandroid3.data.model.FirestoreBusStop
import ar.edu.unlam.scaffoldingandroid3.domain.BusLine
import ar.edu.unlam.scaffoldingandroid3.domain.BusStop
import ar.edu.unlam.scaffoldingandroid3.domain.GeoPoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import com.google.firebase.firestore.GeoPoint as FirestoreGeoPoint

class FirestoreBusStopMapperTest {
    @Test
    fun toDomain_correctlyMapsFirestoreBusStopAndDestinations() {
        val firestoreStop =
            FirestoreBusStop(
                name = "1524 DEFENSA",
                lines =
                    listOf(
                        FirestoreBusLine(code = "22", destination = "v"),
                        FirestoreBusLine(code = "53", destination = "i"),
                        FirestoreBusLine(code = "152", destination = "otro"),
                        FirestoreBusLine(code = "60", destination = null),
                    ),
                location = FirestoreGeoPoint(-34.62567626, -58.37100073),
                geohash = "69y7p8xdc4",
            )

        val domainStop = firestoreStop.toDomain("doc-id-123")
        assertNotNull(domainStop)

        assertEquals("doc-id-123", domainStop!!.id)
        assertEquals("1524 DEFENSA", domainStop.name)
        assertEquals(-34.62567626, domainStop.location.latitude, 0.00001)
        assertEquals(-58.37100073, domainStop.location.longitude, 0.00001)
        assertEquals(4, domainStop.lines.size)

        assertEquals("22", domainStop.lines[0].code)
        assertEquals("Vuelta", domainStop.lines[0].destination)

        assertEquals("53", domainStop.lines[1].code)
        assertEquals("Ida", domainStop.lines[1].destination)

        assertEquals("152", domainStop.lines[2].code)
        assertEquals("otro", domainStop.lines[2].destination)

        assertEquals("60", domainStop.lines[3].code)
        assertEquals("Sentido no especificado", domainStop.lines[3].destination)
    }

    @Test
    fun toDomain_returnsNullForNullLocation() {
        val firestoreStop =
            FirestoreBusStop(
                name = "Parada Sin Ubicacion",
                lines = emptyList(),
                location = null,
                geohash = "",
            )

        val domainStop = firestoreStop.toDomain("doc-id-999")
        assertNull(domainStop)
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
