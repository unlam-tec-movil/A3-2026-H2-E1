package ar.edu.unlam.scaffoldingandroid3.ui.uistate

import ar.edu.unlam.scaffoldingandroid3.domain.model.PhotoReport

data class NoticeItem(
    val report: PhotoReport,
    val distanceMeters: Double,
)

sealed interface NoticeUiState {
    data object Loading : NoticeUiState

    data object PermissionDenied : NoticeUiState

    data object LocationDisabled : NoticeUiState

    data object LocationUnavailable : NoticeUiState

    data object Error : NoticeUiState

    data class Success(
        val notices: List<NoticeItem>,
    ) : NoticeUiState
}
