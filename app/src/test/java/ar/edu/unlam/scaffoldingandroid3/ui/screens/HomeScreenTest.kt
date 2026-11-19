package ar.edu.unlam.scaffoldingandroid3.ui.screens

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
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
class HomeScreenTest {
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
                HomeScreen(navigateToFavoriteBusStop = {}, viewModel = viewModel)
            }
        }
    }

    @Test
    fun `given the home screen, when it is shown, then lists the nearby stops`() {
        // Given / When
        showScreen()

        // Then
        composeTestRule.onNodeWithText("Paradas cercanas").assertIsDisplayed()
        composeTestRule.onNodeWithText("Av. San Martín y Arieta").assertIsDisplayed()
        composeTestRule.onNodeWithText("Florencio Varela y Villegas").assertIsDisplayed()
    }

    @Test
    fun `given no favorites, when it is shown, then every stop offers to be added`() {
        // Given / When
        showScreen()

        // Then
        composeTestRule.onAllNodesWithContentDescription("Agregar a favoritos").assertCountEquals(2)
    }

    @Test
    fun `given a favorite stop, when it is shown, then that stop offers to be removed`() {
        // Given
        favoriteIds.value = setOf(1L)

        // When
        showScreen()

        // Then
        composeTestRule.onAllNodesWithContentDescription("Quitar de favoritos").assertCountEquals(1)
        composeTestRule.onAllNodesWithContentDescription("Agregar a favoritos").assertCountEquals(1)
    }

    @Test
    fun `given the stops, when tapping the heart of the first one, then toggles that favorite`() {
        // Given
        showScreen()

        // When
        composeTestRule.onAllNodesWithContentDescription("Agregar a favoritos")[0].performClick()

        // Then
        verify(exactly = 1) { viewModel.toggleFavorite(1L) }
    }
}
