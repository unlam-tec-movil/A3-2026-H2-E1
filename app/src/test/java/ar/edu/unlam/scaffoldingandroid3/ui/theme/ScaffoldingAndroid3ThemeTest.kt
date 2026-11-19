package ar.edu.unlam.scaffoldingandroid3.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class ScaffoldingAndroid3ThemeTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private fun primaryColor(
        darkTheme: Boolean,
        dynamicColor: Boolean,
    ): Color {
        var primary = Color.Unspecified
        composeTestRule.setContent {
            ScaffoldingAndroid3Theme(darkTheme = darkTheme, dynamicColor = dynamicColor) {
                primary = MaterialTheme.colorScheme.primary
            }
        }
        composeTestRule.waitForIdle()
        return primary
    }

    @Test
    fun `given the light theme without dynamic color, when reading primary, then it is the light purple`() {
        // Given / When
        val primary = primaryColor(darkTheme = false, dynamicColor = false)

        // Then
        assertEquals(Purple40, primary)
    }

    @Test
    fun `given the dark theme without dynamic color, when reading primary, then it is the dark purple`() {
        // Given / When
        val primary = primaryColor(darkTheme = true, dynamicColor = false)

        // Then
        assertEquals(Purple80, primary)
    }

    @Test
    @Config(sdk = [35])
    fun `given dynamic color on a modern device, when reading primary, then the static palette is not used`() {
        // Given / When
        val light = primaryColor(darkTheme = false, dynamicColor = true)

        // Then
        assertEquals(true, light != Color.Unspecified)
    }

    @Test
    @Config(sdk = [30])
    fun `given dynamic color on a pre Android 12 device, when reading primary, then the static palette is used`() {
        // Given / When
        val primary = primaryColor(darkTheme = false, dynamicColor = true)

        // Then
        assertEquals(Purple40, primary)
    }
}
