package ar.edu.unlam.scaffoldingandroid3.ui.uistate

sealed interface PhotoReportUiState {
    data object Idle : PhotoReportUiState

    data class PhotoTaken(
        val photoUri: String,
        val stopId: Long,
    ) : PhotoReportUiState

    data object PermissionDenied : PhotoReportUiState

    data object Saved : PhotoReportUiState
}
