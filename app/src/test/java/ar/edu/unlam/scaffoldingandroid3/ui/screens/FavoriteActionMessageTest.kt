package ar.edu.unlam.scaffoldingandroid3.ui.screens

import ar.edu.unlam.scaffoldingandroid3.R
import org.junit.Assert.assertEquals
import org.junit.Test

class FavoriteActionMessageTest {
    @Test
    fun `adding a favorite returns confirmation message`() {
        val result = favoriteActionMessageRes(wasFavorite = false)

        assertEquals(
            R.string.favorite_added_message,
            result,
        )
    }

    @Test
    fun `removing a favorite returns confirmation message`() {
        val result = favoriteActionMessageRes(wasFavorite = true)

        assertEquals(
            R.string.favorite_removed_message,
            result,
        )
    }

    @Test
    fun `adding and removing favorites return different messages`() {
        val addedMessage = favoriteActionMessageRes(wasFavorite = false)
        val removedMessage = favoriteActionMessageRes(wasFavorite = true)

        assertEquals(false, addedMessage == removedMessage)
    }
}
