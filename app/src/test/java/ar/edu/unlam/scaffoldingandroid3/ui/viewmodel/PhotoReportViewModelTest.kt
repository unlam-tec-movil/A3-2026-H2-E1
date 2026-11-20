package ar.edu.unlam.scaffoldingandroid3.ui.viewmodel

import ar.edu.unlam.scaffoldingandroid3.dispatcher.MainDispatcherRule
import ar.edu.unlam.scaffoldingandroid3.domain.model.PhotoReport
import ar.edu.unlam.scaffoldingandroid3.domain.usecase.RegisterPhotoReportUseCase
import ar.edu.unlam.scaffoldingandroid3.ui.uistate.PhotoReportUiState
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class PhotoReportViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val register = mockk<RegisterPhotoReportUseCase>(relaxed = true)
    private val viewModel = PhotoReportViewModel(register)

    @Test
    fun `given a captured photo, when notifying it, then the report is ready with the stop id`() {
        viewModel.onPhotoTaken("content://photo/1", 42L)

        assertEquals(
            PhotoReportUiState.PhotoTaken("content://photo/1", 42L),
            viewModel.uiState.value,
        )
    }

    @Test
    fun `given denied permission, then the user is informed and stays on the stop screen`() {
        viewModel.onPermissionDenied()

        assertEquals(PhotoReportUiState.PermissionDenied, viewModel.uiState.value)
    }

    @Test
    fun `given a cancelled capture, then nothing is saved`() {
        viewModel.onPhotoTaken("content://photo/1", 42L)

        viewModel.onCancelled()

        assertEquals(PhotoReportUiState.Idle, viewModel.uiState.value)
        coVerify(exactly = 0) { register(any()) }
    }

    @Test
    fun `given an observation, when confirming, then the report is saved with date and stop`() =
        runTest {
            viewModel.onPhotoTaken("content://photo/1", 42L)

            viewModel.saveReport("Sin cartel")

            coVerify {
                register(
                    match<PhotoReport> {
                        it.observation == "Sin cartel" &&
                            it.stopId == 42L &&
                            it.photoUri == "content://photo/1" &&
                            it.timestamp > 0
                    },
                )
            }
            assertEquals(PhotoReportUiState.Saved, viewModel.uiState.value)
        }
}
