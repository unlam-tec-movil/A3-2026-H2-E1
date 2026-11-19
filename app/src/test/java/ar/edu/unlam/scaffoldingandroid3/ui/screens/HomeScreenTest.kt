
package ar.edu.unlam.scaffoldingandroid3.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import ar.edu.unlam.scaffoldingandroid3.ui.theme.VoyEnBondiTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class HomeScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private var mapClicks = 0
    private var favoriteClicks = 0

    private fun showScreen() {
        composeTestRule.setContent {
            VoyEnBondiTheme {
                HomeScreen(
                    navigateToFavoriteBusStop = { favoriteClicks++ },
                    navigateToMap = { mapClicks++ },
                )
            }
        }
    }

    @Test
    fun `given home screen, when shown, then displays navigation options`() {
        showScreen()

        composeTestRule.onNodeWithText("Voy en Bondi").assertIsDisplayed()
        composeTestRule.onNodeWithText("Ver paradas cercanas").assertIsDisplayed()
        composeTestRule.onNodeWithText("Mis favoritas").assertIsDisplayed()
    }

    @Test
    fun `given home screen, when tapping map, then navigates to map`() {
        showScreen()

        composeTestRule.onNodeWithText("Ver paradas cercanas").performClick()

        assertEquals(1, mapClicks)
        assertEquals(0, favoriteClicks)
    }

    @Test
    fun `given home screen, when tapping favorites, then navigates to favorites`() {
        showScreen()

        composeTestRule.onNodeWithText("Mis favoritas").performClick()

        assertEquals(1, favoriteClicks)
        assertEquals(0, mapClicks)
    }
}
