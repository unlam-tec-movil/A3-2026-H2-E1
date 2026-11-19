package ar.edu.unlam.scaffoldingandroid3.ui

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import ar.edu.unlam.scaffoldingandroid3.ui.components.Greeting
import ar.edu.unlam.scaffoldingandroid3.ui.components.GreetingPreview
import ar.edu.unlam.scaffoldingandroid3.ui.theme.ScaffoldingAndroid3Theme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import ar.edu.unlam.scaffoldingandroid3.Greeting as ActivityGreeting
import ar.edu.unlam.scaffoldingandroid3.GreetingPreview as ActivityGreetingPreview

@RunWith(RobolectricTestRunner::class)
class GreetingTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `given a name, when showing the greeting component, then displays the hello message`() {
        // Given / When
        composeTestRule.setContent { ScaffoldingAndroid3Theme { Greeting("Android") } }

        // Then
        composeTestRule.onNodeWithText("Hello Android!").assertIsDisplayed()
    }

    @Test
    fun `given a name, when showing the activity greeting, then displays the hello message`() {
        // Given / When
        composeTestRule.setContent { ScaffoldingAndroid3Theme { ActivityGreeting("Claude") } }

        // Then
        composeTestRule.onNodeWithText("Hello Claude!").assertIsDisplayed()
    }

    @Test
    fun `given the previews, when rendering them, then both display the default greeting`() {
        // Given / When
        composeTestRule.setContent {
            GreetingPreview()
            ActivityGreetingPreview()
        }

        // Then
        composeTestRule.onAllNodesWithText("Hello Android!").assertCountEquals(2)
    }
}
