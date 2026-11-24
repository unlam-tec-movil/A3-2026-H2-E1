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
import org.junit.jupiter.api.DisplayName
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
    @DisplayName("Dado que no hay favoritos, cuando se muestra la pantalla, entonces muestra el estado vacío")
    fun givenNoFavoritesThenEmptyStateIsDisplayed() {
        showScreen()

        composeTestRule
            .onNodeWithText("Mis paradas favoritas")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Todavía no agregaste paradas favoritas")
            .assertIsDisplayed()
    }

    @Test
    @DisplayName("Dado que hay una parada favorita, cuando se muestra la pantalla, entonces lista solo esa parada")
    fun givenOneFavoriteStopThenOnlyThatStopIsListed() {
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
    @DisplayName("Dado que hay una parada favorita, cuando se hace clic en su corazón, entonces la elimina de los favoritos")
    fun givenOneFavoriteStopThenTappingHeartRemovesItFromFavorites() {
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
    @DisplayName("Dado que hay una parada favorita, cuando se selecciona, entonces navega al mapa")
    fun givenOneFavoriteStopThenSelectingItNavigatesToMap() {
        setFavorite()
        showScreen()

        composeTestRule
            .onNodeWithText("Florencio Varela y Villegas")
            .performClick()

        assertEquals(testStop, selectedStop)
    }

    @Test
    @DisplayName("Cuando se busca, los favoritos se filtran por nombre")
    fun searchFiltersFavoritesByStopName() {
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
    @DisplayName("La búsqueda muestra un mensaje de resultados vacíos")
    fun searchDisplaysEmptyResultsMessage() {
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
    @DisplayName("Limpiar la búsqueda restaura las paradas favoritas")
    fun clearingSearchRestoresFavoriteStops() {
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
    @DisplayName("El estado vacío muestra el botón de explorar mapa")
    fun emptyStateDisplaysExploreMapButton() {
        showScreen()

        composeTestRule
            .onNodeWithText("Explorar mapa")
            .assertIsDisplayed()
    }

    @Test
    @DisplayName("El botón de explorar mapa invoca el callback de navegación")
    fun exploreMapButtonInvokesNavigationCallback() {
        showScreen()

        composeTestRule
            .onNodeWithText("Explorar mapa")
            .performClick()

        assertTrue(exploreMapClicked)
    }

    @Test
    @DisplayName("Eliminar un favorito muestra una confirmación con acción de deshacer")
    fun removingFavoriteDisplaysConfirmationWithUndoAction() {
        setFavorite()
        showScreen()

        composeTestRule
            .onNodeWithContentDescription("Quitar de favoritos")
            .performClick()

        composeTestRule.mainClock.advanceTimeBy(MAGIC_TIME_MS)
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText("Parada eliminada de favoritas")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Deshacer")
            .assertIsDisplayed()
    }

    @Test
    @DisplayName("Deshacer restaura el favorito eliminado con sus datos")
    fun undoRestoresRemovedFavoriteWithItsData() {
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
    @DisplayName("Eliminar el último favorito muestra el estado vacío")
    fun removingLastFavoriteDisplaysEmptyState() {
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

    companion object Constants {
        private const val MAGIC_TIME_MS = 1000L
    }
}
