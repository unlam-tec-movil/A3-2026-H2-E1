package ar.edu.unlam.scaffoldingandroid3.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ar.edu.unlam.scaffoldingandroid3.R
import ar.edu.unlam.scaffoldingandroid3.ui.uistate.NoticeFilter

@Composable
fun NoticeFilterChip(
    selectedFilter: NoticeFilter,
    onFilterSelected: (NoticeFilter) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(
            selected = selectedFilter == NoticeFilter.ALL,
            onClick = { onFilterSelected(NoticeFilter.ALL) },
            label = { Text(stringResource(R.string.notice_filter_all)) },
        )
        FilterChip(
            selected = selectedFilter == NoticeFilter.WORKS,
            onClick = { onFilterSelected(NoticeFilter.WORKS) },
            label = { Text(stringResource(R.string.notice_filter_works)) },
        )
        FilterChip(
            selected = selectedFilter == NoticeFilter.HARD_TO_FIND,
            onClick = { onFilterSelected(NoticeFilter.HARD_TO_FIND) },
            label = { Text(stringResource(R.string.notice_filter_hard_to_find)) },
        )
    }
}
