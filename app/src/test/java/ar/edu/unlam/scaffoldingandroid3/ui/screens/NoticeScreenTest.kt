package ar.edu.unlam.scaffoldingandroid3.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import ar.edu.unlam.scaffoldingandroid3.domain.model.PhotoReport
import ar.edu.unlam.scaffoldingandroid3.ui.theme.VoyEnBondiTheme
import ar.edu.unlam.scaffoldingandroid3.ui.uistate.NoticeFilter
import ar.edu.unlam.scaffoldingandroid3.ui.uistate.NoticeItem
import ar.edu.unlam.scaffoldingandroid3.ui.uistate.NoticeUiState
import ar.edu.unlam.scaffoldingandroid3.ui.viewmodel.NoticeViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NoticeScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var uiState: MutableStateFlow<NoticeUiState>
    private lateinit var viewModel: NoticeViewModel
    private var selectedStopId: Long? = null

    private fun showScreen(initialState: NoticeUiState) {
        uiState = MutableStateFlow(initialState)
        viewModel = mockk(relaxed = true)
        every { viewModel.uiState } returns uiState
        composeTestRule.setContent {
            VoyEnBondiTheme {
                NoticeScreen(
                    viewModel = viewModel,
                    onStopSelected = { stopId ->
                        selectedStopId = stopId
                    },
                )
            }
        }
    }

    @Test
    fun `given permission denied state, when shown, then displays retry message and invokes retry`() {
        showScreen(NoticeUiState.PermissionDenied)

        composeTestRule
            .onNodeWithText("Necesitamos el permiso de ubicación para mostrar avisos cercanos.")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Reintentar")
            .performClick()

        verify(exactly = 1) { viewModel.load() }
    }

    @Test
    fun `given location disabled state, when shown, then displays message`() {
        showScreen(NoticeUiState.LocationDisabled)

        composeTestRule
            .onNodeWithText("Activá la ubicación del teléfono para ver avisos cercanos.")
            .assertIsDisplayed()
    }

    @Test
    fun `given location unavailable state, when shown, then displays message`() {
        showScreen(NoticeUiState.LocationUnavailable)

        composeTestRule
            .onNodeWithText("No pudimos obtener tu ubicación. Probá de nuevo en unos segundos.")
            .assertIsDisplayed()
    }

    @Test
    fun `given error state, when shown, then displays message`() {
        showScreen(NoticeUiState.Error)

        composeTestRule
            .onNodeWithText("No se pudieron cargar los avisos.")
            .assertIsDisplayed()
    }

    @Test
    fun `given success with empty notices, when shown, then displays empty message`() {
        showScreen(NoticeUiState.Success(emptyList()))

        composeTestRule
            .onNodeWithText("No hay avisos cerca de vos.")
            .assertIsDisplayed()
    }

    @Test
    fun `given success with notices, when shown, then displays observation, stop name, map label and handles click`() {
        val report =
            PhotoReport(
                id = "1",
                photoUri = "content://photo/1",
                observation = "Parada sin señalizar",
                timestamp = System.currentTimeMillis(),
                stopId = 42L,
            )
        showScreen(NoticeUiState.Success(listOf(NoticeItem(report, 150.0, "Av. San Martín y Arieta"))))

        composeTestRule
            .onNodeWithText("Parada sin señalizar")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Av. San Martín y Arieta")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Ver en el mapa")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Parada sin señalizar")
            .performClick()

        assertEquals(42L, selectedStopId)
    }

    @Test
    fun `given success with notices, when clicking works filter, then view model sets filter`() {
        val report =
            PhotoReport(
                id = "1",
                photoUri = "content://photo/1",
                observation = "Obras en la parada",
                timestamp = System.currentTimeMillis(),
                stopId = 1L,
            )
        showScreen(NoticeUiState.Success(listOf(NoticeItem(report, 150.0, "Parada"))))

        composeTestRule
            .onNodeWithText("Obras")
            .performClick()

        verify(exactly = 1) { viewModel.setFilter(NoticeFilter.WORKS) }
    }
}
