package ar.edu.unlam.scaffoldingandroid3.ui.uistate

import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationData

sealed interface MapUiState {
    object Loading : MapUiState

    data class Success(
        val location: LocationData,
    ) : MapUiState

    data class Error(
        val message: String,
    ) : MapUiState
}
