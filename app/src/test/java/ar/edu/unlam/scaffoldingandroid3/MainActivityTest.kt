package ar.edu.unlam.scaffoldingandroid3

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MainActivityTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun `given the app is launched, when the main activity starts, then displays the greeting`() {
        // Given / When
        composeTestRule.waitForIdle()

        // Then
        composeTestRule.onNodeWithText("Hello Android!").assertIsDisplayed()
    }
}
