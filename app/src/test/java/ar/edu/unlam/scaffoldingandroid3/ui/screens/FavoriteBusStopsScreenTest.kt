package ar.edu.unlam.scaffoldingandroid3.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import ar.edu.unlam.scaffoldingandroid3.ui.theme.VoyEnBondiTheme
import ar.edu.unlam.scaffoldingandroid3.ui.viewmodel.FavoriteBusStopsViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FavoriteBusStopsScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val favoriteIds = MutableStateFlow(emptySet<Long>())
    private val viewModel =
        mockk<FavoriteBusStopsViewModel>(relaxed = true) {
            every { favoriteStopIds } returns favoriteIds
        }

    private fun showScreen() {
        composeTestRule.setContent {
            VoyEnBondiTheme {
                FavoriteBusStopsScreen(navigateToHome = {}, viewModel = viewModel)
            }
        }
    }

    @Test
    fun `given no favorites, when it is shown, then displays the empty state`() {
        // Given / When
        showScreen()

        // Then
        composeTestRule.onNodeWithText("Mis paradas favoritas").assertIsDisplayed()
        composeTestRule.onNodeWithText("Todavía no tenés paradas favoritas.").assertIsDisplayed()
    }

    @Test
    fun `given a favorite stop, when it is shown, then lists only that stop`() {
        // Given
        favoriteIds.value = setOf(2L)

        // When
        showScreen()

        // Then
        composeTestRule.onNodeWithText("Florencio Varela y Villegas").assertIsDisplayed()
        composeTestRule.onNodeWithText("Av. San Martín y Arieta").assertDoesNotExist()
    }

    @Test
    fun `given a favorite stop, when tapping its heart, then removes it from favorites`() {
        // Given
        favoriteIds.value = setOf(2L)
        showScreen()

        // When
        composeTestRule.onNodeWithContentDescription("Quitar de favoritos").performClick()

        // Then
        verify(exactly = 1) { viewModel.removeFavorite(2L) }
    }
}
