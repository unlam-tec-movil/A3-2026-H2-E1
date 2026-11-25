package ar.edu.unlam.scaffoldingandroid3.ui.screens

import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ar.edu.unlam.scaffoldingandroid3.R
import ar.edu.unlam.scaffoldingandroid3.ui.components.NoticeFilterChip
import ar.edu.unlam.scaffoldingandroid3.ui.uistate.NoticeItem
import ar.edu.unlam.scaffoldingandroid3.ui.uistate.NoticeUiState
import ar.edu.unlam.scaffoldingandroid3.ui.viewmodel.NoticeViewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date
import kotlin.math.roundToInt

@Composable
fun NoticeScreen(
    viewModel: NoticeViewModel = hiltViewModel(),
    onStopSelected: (Long) -> Unit = {},
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    when (val s = state) {
        NoticeUiState.Loading -> {
            CenteredContent { CircularProgressIndicator() }
        }

        NoticeUiState.PermissionDenied -> {
            MessageWithRetry(
                message = stringResource(R.string.notice_permission_denied),
                onRetry = viewModel::load,
            )
        }

        NoticeUiState.LocationDisabled -> {
            MessageWithRetry(
                message = stringResource(R.string.notice_location_disabled),
                onRetry = viewModel::load,
            )
        }

        NoticeUiState.LocationUnavailable -> {
            MessageWithRetry(
                message = stringResource(R.string.notice_location_unavailable),
                onRetry = viewModel::load,
            )
        }

        NoticeUiState.Error -> {
            MessageWithRetry(
                message = stringResource(R.string.notice_error_loading),
                onRetry = viewModel::load,
            )
        }

        is NoticeUiState.Success -> {
            Column(modifier = Modifier.fillMaxSize()) {
                NoticeFilterChip(
                    selectedFilter = s.selectedFilter,
                    onFilterSelected = viewModel::setFilter,
                )
                if (s.notices.isEmpty()) {
                    Box(modifier = Modifier.weight(1f)) {
                        MessageWithRetry(
                            message = stringResource(R.string.notice_empty),
                            onRetry = viewModel::load,
                            retryLabel = stringResource(R.string.notice_retry_loading),
                        )
                    }
                } else {
                    Box(modifier = Modifier.weight(1f)) {
                        NoticeList(notices = s.notices, onStopSelected = onStopSelected)
                    }
                }
            }
        }
    }
}

@Composable
private fun NoticeList(
    notices: List<NoticeItem>,
    onStopSelected: (Long) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(notices, key = { it.report.id }) { notice ->
            NoticeCard(notice, onStopSelected)
        }
    }
}

@Composable
private fun NoticeCard(
    notice: NoticeItem,
    onStopSelected: (Long) -> Unit,
) {
    val report = notice.report
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    var showShareMenu by rememberSaveable { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val date =
        remember(report.timestamp) {
            DateFormat
                .getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                .format(Date(report.timestamp))
        }

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable { onStopSelected(report.stopId) },
    ) {
        Column {
            AsyncImage(
                model = report.photoUri,
                contentDescription = stringResource(R.string.notice_photo_content_description),
                contentScale = ContentScale.Crop,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(180.dp),
            )
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = report.observation,
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = notice.stopName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "A ${notice.distanceMeters.roundToInt()} m · $date",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.notice_view_map),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )

                    Box {
                        OutlinedButton(
                            onClick = { showShareMenu = true },
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = "Compartir aviso",
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.notice_share))
                        }

                        DropdownMenu(
                            expanded = showShareMenu,
                            onDismissRequest = { showShareMenu = false },
                        ) {
                            val intentMessage = stringResource(R.string.notice_share_intent_title)

                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.notice_whatsapp)) },
                                onClick = {
                                    showShareMenu = false
                                    val mapsUrl =
                                        "https://maps.google.com/?q=${notice.latitude},${notice.longitude}"
                                    val message =
                                        "Aviso: ${report.observation}\nParada: ${notice.stopName}\nUbicación: $mapsUrl"

                                    val intent =
                                        Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, message)
                                            setPackage("com.whatsapp")
                                        }
                                    try {
                                        context.startActivity(intent)
                                    } catch (_: Exception) {
                                        try {
                                            val waIntent =
                                                Intent(
                                                    Intent.ACTION_VIEW,
                                                    "https://wa.me/?text=${Uri.encode(message)}".toUri(),
                                                )
                                            context.startActivity(waIntent)
                                        } catch (_: Exception) {
                                            val chooser =
                                                Intent.createChooser(
                                                    Intent(Intent.ACTION_SEND).apply {
                                                        type = "text/plain"
                                                        putExtra(Intent.EXTRA_TEXT, message)
                                                    },
                                                    intentMessage,
                                                )
                                            context.startActivity(chooser)
                                        }
                                    }
                                },
                            )

                            val clipboardMessage = stringResource(R.string.notice_clipboard_title)
                            val toastMessage = stringResource(R.string.notice_clipboard_copied)

                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.notice_copy_link)) },
                                onClick = {
                                    val mapsUrl =
                                        "https://maps.google.com/?q=${notice.latitude},${notice.longitude}"
                                    showShareMenu = false
                                    coroutineScope.launch {
                                        clipboard.setClipEntry(
                                            ClipEntry(
                                                ClipData.newPlainText(
                                                    clipboardMessage,
                                                    mapsUrl,
                                                ),
                                            ),
                                        )

                                        Toast
                                            .makeText(
                                                context,
                                                toastMessage,
                                                Toast.LENGTH_SHORT,
                                            ).show()
                                    }
                                },
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.notice_other_apps)) },
                                onClick = {
                                    showShareMenu = false
                                    val mapsUrl =
                                        "https://maps.google.com/?q=${notice.latitude},${notice.longitude}"
                                    val message =
                                        "Aviso: ${report.observation}\nParada: ${notice.stopName}\nUbicación: $mapsUrl"

                                    val sendIntent =
                                        Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, message)
                                        }
                                    val chooser =
                                        Intent.createChooser(sendIntent, "Compartir aviso")
                                    context.startActivity(chooser)
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageWithRetry(
    message: String,
    onRetry: () -> Unit,
    retryLabel: String = stringResource(R.string.notice_retry),
) {
    CenteredContent {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = message,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
            )
            Button(onClick = onRetry) {
                Text(retryLabel)
            }
        }
    }
}

@Composable
private fun CenteredContent(content: @Composable () -> Unit) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
