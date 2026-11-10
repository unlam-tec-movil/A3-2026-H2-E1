package ar.edu.unlam.scaffoldingandroid3.data.mongodb

import ar.edu.unlam.scaffoldingandroid3.domain.GeoPoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BusStopDocumentTest {
    @Test
    fun `convierte coordenadas GeoJSON lng-lat a latitud-longitud`() {
        val stop =
            BusStopDocument(
                id = "1",
                name = "Perón y Arieta",
                lines = listOf(BusLineDocument("96", "Plaza Once")),
                location = GeoJsonPointDocument(coordinates = listOf(-58.56, -34.67)),
            ).toDomain()

        assertEquals(GeoPoint(latitude = -34.67, longitude = -58.56), stop?.location)
        assertEquals("96", stop?.lines?.single()?.code)
    }

    @Test
    fun `descarta paradas sin coordenadas`() {
        assertNull(BusStopDocument(id = "1").toDomain())
    }
}
