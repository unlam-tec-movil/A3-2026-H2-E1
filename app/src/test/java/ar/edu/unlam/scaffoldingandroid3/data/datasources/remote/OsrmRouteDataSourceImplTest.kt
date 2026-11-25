package ar.edu.unlam.scaffoldingandroid3.data.datasources.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertThrows

class OsrmRouteDataSourceImplTest {
    private val dataSource = OsrmRouteDataSourceImpl()

    @Test
    @DisplayName("Debería parsear correctamente un JSON válido de OSRM con coordenadas y distancia")
    fun shouldParseValidOsrmJson() {
        val json =
            """
            {
              "code": "Ok",
              "routes": [
                {
                  "geometry": {
                    "coordinates": [
                      [-58.5630, -34.6705],
                      [-58.5635, -34.6710],
                      [-58.5640, -34.6720]
                    ],
                    "type": "LineString"
                  },
                  "distance": 250.5,
                  "duration": 180.0
                }
              ]
            }
            """.trimIndent()

        val route = dataSource.parseResponse(json)

        assertNotNull(route)
        assertEquals(3, route.points.size)
        assertEquals(-34.6705, route.points[0].latitude.value, 0.0001)
        assertEquals(-58.5630, route.points[0].longitude.value, 0.0001)
        assertEquals(-34.6720, route.points[2].latitude.value, 0.0001)
        assertEquals(-58.5640, route.points[2].longitude.value, 0.0001)
        assertEquals(250.5, route.distanceMeters, 0.01)
        assertEquals(180.0, route.durationSeconds, 0.01)
    }

    @Test
    @DisplayName("Debería lanzar excepción si el código no es Ok")
    fun shouldThrowExceptionWhenCodeIsNotOk() {
        val json =
            """
            {
              "code": "NoRoute",
              "routes": []
            }
            """.trimIndent()

        val exception =
            assertThrows<IllegalStateException> {
                dataSource.parseResponse(json)
            }
        assertTrue(exception.message?.contains("code=NoRoute") == true)
    }

    @Test
    @DisplayName("Debería lanzar excepción si la lista de rutas viene vacía")
    fun shouldThrowExceptionWhenRoutesIsEmpty() {
        val json =
            """
            {
              "code": "Ok",
              "routes": []
            }
            """.trimIndent()

        assertThrows<IllegalStateException> {
            dataSource.parseResponse(json)
        }
    }
}
