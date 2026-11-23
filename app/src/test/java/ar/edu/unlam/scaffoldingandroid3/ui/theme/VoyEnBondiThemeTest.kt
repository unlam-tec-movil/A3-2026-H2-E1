package ar.edu.unlam.scaffoldingandroid3.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.v2.createComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class VoyEnBondiThemeTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private fun primaryColorFor(darkTheme: Boolean): Color {
        var primary = Color.Unspecified
        composeTestRule.setContent {
            VoyEnBondiTheme(darkTheme = darkTheme) { primary = MaterialTheme.colorScheme.primary }
        }
        composeTestRule.waitForIdle()
        return primary
    }

    @Test
    fun `given the light theme, when reading the primary color, then it is light primary`() {
        // Given / When
        val primary = primaryColorFor(darkTheme = false)

        // Then
        assertEquals(LightPrimary, primary)
    }

    @Test
    fun `given the dark theme, when reading the primary color, then it is dark primary`() {
        // Given / When
        val primary = primaryColorFor(darkTheme = true)

        // Then
        assertEquals(DarkPrimary, primary)
    }
}
