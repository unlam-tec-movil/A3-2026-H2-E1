package ar.edu.unlam.scaffoldingandroid3.data.database.room

import ar.edu.unlam.scaffoldingandroid3.domain.BusLine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RoomConvertersTest {
    private val converters = RoomConverters()

    @Test
    fun fromBusLineList_encodesListToJsonString() {
        val lines = listOf(BusLine("96", "Plaza Once"), BusLine("185", "Liniers"))
        val json = converters.fromBusLineList(lines)

        assertTrue(json.contains("96"))
        assertTrue(json.contains("Plaza Once"))
    }

    @Test
    fun toBusLineList_decodesJsonStringToList() {
        val json = """[{"code":"96","destination":"Plaza Once"},{"code":"185","destination":"Liniers"}]"""
        val lines = converters.toBusLineList(json)

        assertEquals(2, lines.size)
        assertEquals("96", lines[0].code)
        assertEquals("Plaza Once", lines[0].destination)
        assertEquals("185", lines[1].code)
        assertEquals("Liniers", lines[1].destination)
    }

    @Test
    fun toBusLineList_emptyString_returnsEmptyList() {
        val lines = converters.toBusLineList("")
        assertTrue(lines.isEmpty())
    }
}
