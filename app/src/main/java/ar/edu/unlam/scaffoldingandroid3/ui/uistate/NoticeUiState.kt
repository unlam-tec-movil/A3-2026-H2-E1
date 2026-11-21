package ar.edu.unlam.scaffoldingandroid3.ui.uistate

import ar.edu.unlam.scaffoldingandroid3.domain.model.PhotoReport

data class NoticeItem(
    val report: PhotoReport,
    val distanceMeters: Double,
    val stopName: String,
    val latitude: Double,
    val longitude: Double,
)

enum class NoticeFilter {
    ALL,
    WORKS,
    HARD_TO_FIND,
}

sealed interface NoticeUiState {
    data object Loading : NoticeUiState

    data object PermissionDenied : NoticeUiState

    data object LocationDisabled : NoticeUiState

    data object LocationUnavailable : NoticeUiState

    data object Error : NoticeUiState

    data class Success(
        val notices: List<NoticeItem>,
        val selectedFilter: NoticeFilter = NoticeFilter.ALL,
    ) : NoticeUiState
}
