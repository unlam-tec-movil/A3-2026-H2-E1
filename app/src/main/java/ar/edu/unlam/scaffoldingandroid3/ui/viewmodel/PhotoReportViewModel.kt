package ar.edu.unlam.scaffoldingandroid3.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.edu.unlam.scaffoldingandroid3.domain.model.PhotoReport
import ar.edu.unlam.scaffoldingandroid3.domain.usecase.RegisterPhotoReportUseCase
import ar.edu.unlam.scaffoldingandroid3.ui.uistate.PhotoReportUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class PhotoReportViewModel
    @Inject
    constructor(
        private val registerPhotoReport: RegisterPhotoReportUseCase,
    ) : ViewModel() {
        @Suppress("ktlint:standard:backing-property-naming")
        private val _uiState = MutableStateFlow<PhotoReportUiState>(PhotoReportUiState.Idle)
        val uiState: StateFlow<PhotoReportUiState> = _uiState.asStateFlow()

        fun onPhotoTaken(
            photoUri: String,
            stopId: Long,
        ) {
            _uiState.value = PhotoReportUiState.PhotoTaken(photoUri, stopId)
        }

        fun onPermissionDenied() {
            _uiState.value = PhotoReportUiState.PermissionDenied
        }

        fun onCancelled() {
            _uiState.value = PhotoReportUiState.Idle
        }

        fun onAcknowledged() {
            _uiState.value = PhotoReportUiState.Idle
        }

        fun saveReport(observation: String) {
            val state = _uiState.value as? PhotoReportUiState.PhotoTaken ?: return
            viewModelScope.launch {
                registerPhotoReport(
                    PhotoReport(
                        id = UUID.randomUUID().toString(),
                        photoUri = state.photoUri,
                        observation = observation,
                        timestamp = System.currentTimeMillis(),
                        stopId = state.stopId,
                    ),
                )
                _uiState.value = PhotoReportUiState.Saved
            }
        }
    }
