
package ar.edu.unlam.scaffoldingandroid3.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import ar.edu.unlam.scaffoldingandroid3.domain.model.FavoriteStopSnapshot
import ar.edu.unlam.scaffoldingandroid3.ui.theme.VoyEnBondiTheme
import ar.edu.unlam.scaffoldingandroid3.ui.viewmodel.FavoriteBusStopsViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FavoriteBusStopsScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val favoriteIds = MutableStateFlow(emptySet<Long>())

    private val favoriteStops =
        MutableStateFlow(emptyList<FavoriteStopSnapshot>())

    private val viewModel =
        mockk<FavoriteBusStopsViewModel>(relaxed = true) {
            every { favoriteStopIds } returns favoriteIds
            every { favoriteStops } returns this@FavoriteBusStopsScreenTest.favoriteStops
        }

    private val testStop =
        FavoriteStopSnapshot(
            stopId = 2L,
            name = "Florencio Varela y Villegas",
            latitude = -34.65,
            longitude = -58.62,
            lines = "Línea 96",
        )

    private var selectedStop: FavoriteStopSnapshot? = null
    private var exploreMapClicked = false

    private fun showScreen() {
        composeTestRule.setContent {
            VoyEnBondiTheme {
                FavoriteBusStopsScreen(
                    onStopSelected = { stop ->
                        selectedStop = stop
                    },
                    onExploreMap = {
                        exploreMapClicked = true
                    },
                    viewModel = viewModel,
                )
            }
        }
    }

    private fun setFavorite() {
        favoriteIds.value = setOf(testStop.stopId)
        favoriteStops.value = listOf(testStop)
    }

    @Test
    fun `given no favorites, when it is shown, then displays the empty state`() {
        showScreen()

        composeTestRule
            .onNodeWithText("Mis paradas favoritas")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Todavía no agregaste paradas favoritas")
            .assertIsDisplayed()
    }

    @Test
    fun `given a favorite stop, when it is shown, then lists only that stop`() {
        setFavorite()
        showScreen()

        composeTestRule
            .onNodeWithText("Florencio Varela y Villegas")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Av. San Martín y Arieta")
            .assertDoesNotExist()
    }

    @Test
    fun `given a favorite stop, when tapping its heart, then removes it from favorites`() {
        setFavorite()
        showScreen()

        composeTestRule
            .onNodeWithContentDescription("Quitar de favoritos")
            .performClick()

        verify(exactly = 1) {
            viewModel.removeFavorite(testStop.stopId)
        }
    }

    @Test
    fun `given a favorite stop, when selecting it, then navigates to the map`() {
        setFavorite()
        showScreen()

        composeTestRule
            .onNodeWithText("Florencio Varela y Villegas")
            .performClick()

        assertEquals(testStop, selectedStop)
    }

    @Test
    fun `search filters favorite stops by name`() {
        setFavorite()
        showScreen()

        composeTestRule
            .onNodeWithText("Buscar favoritas")
            .performTextInput("Florencio")

        composeTestRule
            .onNodeWithText("Florencio Varela y Villegas")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("1 parada encontrada")
            .assertIsDisplayed()
    }

    @Test
    fun `search displays empty results message`() {
        setFavorite()
        showScreen()

        composeTestRule
            .onNodeWithText("Buscar favoritas")
            .performTextInput("Parada inexistente")

        composeTestRule
            .onNodeWithText("No encontramos favoritas con esa búsqueda")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Florencio Varela y Villegas")
            .assertDoesNotExist()
    }

    @Test
    fun `clearing search restores favorite stops`() {
        setFavorite()
        showScreen()

        composeTestRule
            .onNodeWithText("Buscar favoritas")
            .performTextInput("Inexistente")

        composeTestRule
            .onNodeWithContentDescription("Limpiar búsqueda")
            .performClick()

        composeTestRule
            .onNodeWithText("Florencio Varela y Villegas")
            .assertIsDisplayed()
    }

    @Test
    fun `empty state displays explore map button`() {
        showScreen()

        composeTestRule
            .onNodeWithText("Explorar mapa")
            .assertIsDisplayed()
    }

    @Test
    fun `explore map button invokes navigation callback`() {
        showScreen()

        composeTestRule
            .onNodeWithText("Explorar mapa")
            .performClick()

        assertTrue(exploreMapClicked)
    }

    @Test
    fun `removing favorite displays confirmation with undo action`() {
        setFavorite()
        showScreen()

        composeTestRule
            .onNodeWithContentDescription("Quitar de favoritos")
            .performClick()

        composeTestRule.mainClock.advanceTimeBy(1000)
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText("Parada eliminada de favoritas")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Deshacer")
            .assertIsDisplayed()
    }

    @Test
    fun `undo restores removed favorite with its data`() {
        setFavorite()
        showScreen()

        composeTestRule
            .onNodeWithContentDescription("Quitar de favoritos")
            .performClick()

        composeTestRule
            .onNodeWithText("Deshacer")
            .performClick()

        composeTestRule.waitForIdle()

        verify(exactly = 1) {
            viewModel.restoreFavorite(testStop)
        }
    }

    @Test
    fun `removing last favorite displays empty state`() {
        setFavorite()
        showScreen()

        composeTestRule
            .onNodeWithContentDescription("Quitar de favoritos")
            .performClick()

        composeTestRule.runOnIdle {
            favoriteStops.value = emptyList()
            favoriteIds.value = emptySet()
        }

        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText("Todavía no agregaste paradas favoritas")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Explorar mapa")
            .assertIsDisplayed()
    }
}
