
package ar.edu.unlam.scaffoldingandroid3.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import ar.edu.unlam.scaffoldingandroid3.domain.model.BusLine
import ar.edu.unlam.scaffoldingandroid3.domain.model.BusStop
import ar.edu.unlam.scaffoldingandroid3.domain.model.Latitude
import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationData
import ar.edu.unlam.scaffoldingandroid3.domain.model.Longitude
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BusStopCardTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createStop(lines: List<BusLine> = emptyList()): BusStop =
        BusStop(
            stopId = 1L,
            name = "Av. Rivadavia y San Martín",
            locationData =
                LocationData(
                    latitude = Latitude(-34.6500),
                    longitude = Longitude(-58.6200),
                ),
            markedAsFavorite = false,
            busLines = lines,
        )

    private fun createLine(number: Int): BusLine =
        BusLine(
            lineId = number.toLong(),
            number = number,
            branch = "",
            destination = "",
        )

    @Test
    fun displaysStopNameAndDistance() {
        composeTestRule.setContent {
            MaterialTheme {
                BusStopCard(
                    stop = createStop(),
                    distanceMeters = 250.0,
                    selected = false,
                    onClick = {},
                )
            }
        }

        composeTestRule
            .onNodeWithText("Av. Rivadavia y San Martín")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("250 m")
            .assertIsDisplayed()
    }

    @Test
    fun clickingStopInvokesSelectionCallback() {
        var clickCount = 0

        composeTestRule.setContent {
            MaterialTheme {
                BusStopCard(
                    stop = createStop(),
                    distanceMeters = 150.0,
                    selected = false,
                    onClick = {
                        clickCount++
                    },
                )
            }
        }

        composeTestRule
            .onNodeWithText("Av. Rivadavia y San Martín")
            .performClick()

        composeTestRule.runOnIdle {
            assertEquals(1, clickCount)
        }
    }

    @Test
    fun clickingFavoriteInvokesCallback() {
        var favoriteClicked = false

        composeTestRule.setContent {
            MaterialTheme {
                BusStopCard(
                    stop = createStop(),
                    distanceMeters = 100.0,
                    selected = false,
                    onClick = {},
                    isFavorite = false,
                    onFavoriteClick = {
                        favoriteClicked = true
                    },
                )
            }
        }

        composeTestRule
            .onNodeWithContentDescription("Agregar a favoritos")
            .performClick()

        composeTestRule.runOnIdle {
            assertTrue(favoriteClicked)
        }
    }

    @Test
    fun favoriteStopShowsRemoveFavoriteAction() {
        composeTestRule.setContent {
            MaterialTheme {
                BusStopCard(
                    stop = createStop(),
                    distanceMeters = 100.0,
                    selected = false,
                    onClick = {},
                    isFavorite = true,
                    onFavoriteClick = {},
                )
            }
        }

        composeTestRule
            .onNodeWithContentDescription("Quitar de favoritos")
            .assertIsDisplayed()
    }

    @Test
    fun displaysBusLines() {
        val stop =
            createStop(
                lines =
                    listOf(
                        createLine(136),
                        createLine(166),
                    ),
            )

        composeTestRule.setContent {
            MaterialTheme {
                BusStopCard(
                    stop = stop,
                    distanceMeters = 300.0,
                    selected = false,
                    onClick = {},
                )
            }
        }

        composeTestRule
            .onNodeWithText("136")
            .assertExists()

        composeTestRule
            .onNodeWithText("166")
            .assertExists()
    }

    @Test
    fun expandsAndCollapsesBusLines() {
        val stop =
            createStop(
                lines =
                    listOf(
                        createLine(1),
                        createLine(2),
                        createLine(3),
                        createLine(4),
                        createLine(5),
                        createLine(6),
                    ),
            )

        composeTestRule.setContent {
            MaterialTheme {
                BusStopCard(
                    stop = stop,
                    distanceMeters = 200.0,
                    selected = false,
                    onClick = {},
                )
            }
        }

        composeTestRule
            .onNodeWithText("+2 más")
            .assertExists()

        composeTestRule
            .onNodeWithText("+2 más")
            .performClick()

        composeTestRule
            .onNodeWithText("6")
            .assertExists()

        composeTestRule
            .onNodeWithText("Ver menos")
            .performClick()

        composeTestRule
            .onNodeWithText("+2 más")
            .assertExists()
    }

    @Test
    fun selectedStopShowsShareAction() {
        composeTestRule.setContent {
            MaterialTheme {
                BusStopCard(
                    stop = createStop(),
                    distanceMeters = 100.0,
                    selected = true,
                    onClick = {},
                )
            }
        }

        composeTestRule
            .onNodeWithText("Compartir")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Compartir")
            .performClick()

        composeTestRule
            .onNodeWithText("Copiar enlace")
            .assertExists()

        composeTestRule
            .onNodeWithText("Otras apps")
            .assertExists()
    }

    @Test
    fun unselectedStopDoesNotShowShareAction() {
        composeTestRule.setContent {
            MaterialTheme {
                BusStopCard(
                    stop = createStop(),
                    distanceMeters = 100.0,
                    selected = false,
                    onClick = {},
                )
            }
        }

        composeTestRule
            .onNodeWithText("Compartir")
            .assertDoesNotExist()
    }
}
