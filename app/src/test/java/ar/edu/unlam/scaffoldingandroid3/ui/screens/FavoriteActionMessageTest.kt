
package ar.edu.unlam.scaffoldingandroid3.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Test

class FavoriteActionMessageTest {
    @Test
    fun `adding a favorite returns confirmation message`() {
        val result = favoriteActionMessage(wasFavorite = false)

        assertEquals(
            "Parada agregada a favoritas",
            result,
        )
    }

    @Test
    fun `removing a favorite returns confirmation message`() {
        val result = favoriteActionMessage(wasFavorite = true)

        assertEquals(
            "Parada eliminada de favoritas",
            result,
        )
    }

    @Test
    fun `adding and removing favorites return different messages`() {
        val addedMessage = favoriteActionMessage(wasFavorite = false)
        val removedMessage = favoriteActionMessage(wasFavorite = true)

        assertEquals(false, addedMessage == removedMessage)
    }
}
