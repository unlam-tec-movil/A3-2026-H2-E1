package ar.edu.unlam.scaffoldingandroid3.data.mapper

import ar.edu.unlam.scaffoldingandroid3.data.mapper.FirestoreBusStopMapper.toDomain
import ar.edu.unlam.scaffoldingandroid3.data.model.FirestoreBusLine
import ar.edu.unlam.scaffoldingandroid3.data.model.FirestoreBusStop
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import com.google.firebase.firestore.GeoPoint as FirestoreGeoPoint

class FirestoreBusStopMapperTest {
    @Test
    fun toDomain_mapsNameLocationAndLines() {
        val firestoreStop =
            FirestoreBusStop(
                name = "1524 DEFENSA",
                lines = listOf(FirestoreBusLine(code = "22"), FirestoreBusLine(code = "10A")),
                location = FirestoreGeoPoint(-34.62567626, -58.37100073),
                geohash = "69y7p8xdc4",
            )

        val stop = firestoreStop.toDomain("doc-id-123")

        assertNotNull(stop)
        assertEquals("1524 DEFENSA", stop!!.name)
        assertEquals(-34.62567626, stop.locationData.latitude.value, 0.00001)
        assertEquals(-58.37100073, stop.locationData.longitude.value, 0.00001)
        assertEquals(listOf(22, 10), stop.busLines.map { it.number })
    }

    @Test
    fun toDomain_producesTheSameStopIdForTheSameDocument() {
        val firestoreStop = FirestoreBusStop(name = "A", location = FirestoreGeoPoint(0.0, 0.0))

        assertEquals(firestoreStop.toDomain("doc")!!.stopId, firestoreStop.toDomain("doc")!!.stopId)
    }

    @Test
    fun toDomain_returnsNullForNullLocation() {
        assertNull(FirestoreBusStop(name = "Sin ubicacion").toDomain("doc"))
    }

    @Test
    fun toDomain_usesFallbackNameWhenBlank() {
        val stop = FirestoreBusStop(name = " ", location = FirestoreGeoPoint(0.0, 0.0)).toDomain("doc")

        assertEquals("Parada de Colectivo", stop!!.name)
    }

    @Test
    fun toDomain_dropsDuplicatedLines() {
        val stop =
            FirestoreBusStop(
                lines = listOf(FirestoreBusLine(code = "22"), FirestoreBusLine(code = "22")),
                location = FirestoreGeoPoint(0.0, 0.0),
            ).toDomain("doc")

        assertEquals(1, stop!!.busLines.size)
    }
}
