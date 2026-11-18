package ar.edu.unlam.scaffoldingandroid3.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
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
class VoyEnBondiBottomBarTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private fun showBar(
        selectedTab: BottomTab,
        onTabSelected: (BottomTab) -> Unit = {},
    ) {
        composeTestRule.setContent {
            VoyEnBondiTheme {
                VoyEnBondiBottomBar(selectedTab = selectedTab, onTabSelected = onTabSelected)
            }
        }
    }

    @Test
    fun `given the bottom bar, when it is shown, then displays every tab label`() {
        // Given / When
        showBar(BottomTab.HOME)

        // Then
        BottomTab.entries.forEach { composeTestRule.onNodeWithText(it.label).assertIsDisplayed() }
    }

    @Test
    fun `given a selected tab, when it is shown, then only that tab is selected`() {
        // Given / When
        showBar(BottomTab.MAP)

        // Then
        composeTestRule.onNodeWithText("Mapa").assertIsSelected()
    }

    @Test
    fun `given the bottom bar, when tapping a tab, then reports the tapped tab`() {
        // Given
        val selected = mutableListOf<BottomTab>()
        showBar(BottomTab.HOME) { selected.add(it) }

        // When
        composeTestRule.onNodeWithText("Favoritos").performClick()

        // Then
        assertEquals(listOf(BottomTab.FAVORITES), selected)
    }
}
