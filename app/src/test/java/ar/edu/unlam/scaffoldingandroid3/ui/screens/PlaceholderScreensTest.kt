package ar.edu.unlam.scaffoldingandroid3.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import ar.edu.unlam.scaffoldingandroid3.ui.theme.VoyEnBondiTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PlaceholderScreensTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `given the map screen, when it is shown, then displays the coming soon message`() {
        // Given / When
        composeTestRule.setContent { VoyEnBondiTheme { MapScreen() } }

        // Then
        composeTestRule.onNodeWithText("Explorá el mapa").assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Próximamente vas a poder explorar las paradas de colectivo en el mapa.")
            .assertIsDisplayed()
    }

    @Test
    fun `given the routes screen, when it is shown, then displays the trip planning message`() {
        // Given / When
        composeTestRule.setContent { VoyEnBondiTheme { RoutesScreen() } }

        // Then
        composeTestRule.onNodeWithText("Planificá tu viaje").assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Acá vas a poder buscar un destino y consultar recorridos y combinaciones.")
            .assertIsDisplayed()
    }
}
