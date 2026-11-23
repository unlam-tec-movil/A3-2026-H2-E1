package ar.edu.unlam.scaffoldingandroid3.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ar.edu.unlam.scaffoldingandroid3.ui.theme.DicifilesColor
import ar.edu.unlam.scaffoldingandroid3.ui.theme.ObrasColor
import ar.edu.unlam.scaffoldingandroid3.ui.uistate.NoticeFilter
import ar.edu.unlam.scaffoldingandroid3.ui.uistate.NoticeItem
import ar.edu.unlam.scaffoldingandroid3.ui.uistate.NoticeSortOrder
import ar.edu.unlam.scaffoldingandroid3.ui.uistate.NoticeUiState
import ar.edu.unlam.scaffoldingandroid3.ui.viewmodel.NoticeViewModel
import coil.compose.AsyncImage
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
        NoticeUiState.Loading ->
            CenteredContent { CircularProgressIndicator() }

        NoticeUiState.PermissionDenied ->
            MessageWithRetry(
                message = "Necesitamos el permiso de ubicación para mostrar avisos cercanos.",
                onRetry = viewModel::load,
            )

        NoticeUiState.LocationDisabled ->
            MessageWithRetry(
                message = "Activá la ubicación del teléfono para ver avisos cercanos.",
                onRetry = viewModel::load,
            )

        NoticeUiState.LocationUnavailable ->
            MessageWithRetry(
                message = "No pudimos obtener tu ubicación. Probá de nuevo en unos segundos.",
                onRetry = viewModel::load,
            )

        NoticeUiState.Error ->
            MessageWithRetry(
                message = "No se pudieron cargar los avisos.",
                onRetry = viewModel::load,
            )

        is NoticeUiState.Success ->
            Column(modifier = Modifier.fillMaxSize()) {
                NoticeFilterAndSortSection(
                    selectedFilter = s.selectedFilter,
                    onFilterSelected = viewModel::setFilter,
                    selectedSortOrder = s.selectedSortOrder,
                    onSortSelected = viewModel::setSortOrder,
                )
                if (s.notices.isEmpty()) {
                    Box(modifier = Modifier.weight(1f)) {
                        MessageWithRetry(
                            message = "No hay avisos cerca de vos.",
                            onRetry = viewModel::load,
                            retryLabel = "Actualizar",
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

@Composable
private fun NoticeFilterAndSortSection(
    selectedFilter: NoticeFilter,
    onFilterSelected: (NoticeFilter) -> Unit,
    selectedSortOrder: NoticeSortOrder,
    onSortSelected: (NoticeSortOrder) -> Unit,
) {
    var showSortMenu by rememberSaveable { mutableStateOf(false) }

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier =
                Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FilterChip(
                selected = selectedFilter == NoticeFilter.ALL,
                onClick = { onFilterSelected(NoticeFilter.ALL) },
                label = { Text("Todos") },
            )
            FilterChip(
                selected = selectedFilter == NoticeFilter.WORKS,
                onClick = { onFilterSelected(NoticeFilter.WORKS) },
                label = { Text("Obras") },
            )
            FilterChip(
                selected = selectedFilter == NoticeFilter.HARD_TO_FIND,
                onClick = { onFilterSelected(NoticeFilter.HARD_TO_FIND) },
                label = { Text("Difíciles de encontrar") },
            )
        }

        Box {
            IconButton(onClick = { showSortMenu = true }) {
                Icon(
                    imageVector = Icons.Filled.FilterList,
                    contentDescription = "Ordenar avisos",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }

            DropdownMenu(
                expanded = showSortMenu,
                onDismissRequest = { showSortMenu = false },
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "Más cercanos",
                            fontWeight = if (selectedSortOrder == NoticeSortOrder.CLOSEST) FontWeight.Bold else FontWeight.Normal,
                        )
                    },
                    onClick = {
                        showSortMenu = false
                        onSortSelected(NoticeSortOrder.CLOSEST)
                    },
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "Más recientes",
                            fontWeight = if (selectedSortOrder == NoticeSortOrder.MOST_RECENT) FontWeight.Bold else FontWeight.Normal,
                        )
                    },
                    onClick = {
                        showSortMenu = false
                        onSortSelected(NoticeSortOrder.MOST_RECENT)
                    },
                )
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
    val clipboardManager = LocalClipboardManager.current
    var showShareMenu by rememberSaveable { mutableStateOf(false) }

    val date =
        remember(report.timestamp) {
            DateFormat
                .getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                .format(Date(report.timestamp))
        }

    val observationColor =
        when {
            report.observation.contains("obra", ignoreCase = true) -> ObrasColor
            report.observation.contains("difícil", ignoreCase = true) ||
                report.observation.contains("encontrar", ignoreCase = true) -> DicifilesColor
            else -> MaterialTheme.colorScheme.onSurface
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
                contentDescription = "Foto del aviso",
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
                    color = observationColor,
                    fontWeight = FontWeight.Bold,
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
                        text = "Ver en el mapa",
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
                            Text("Compartir")
                        }

                        DropdownMenu(
                            expanded = showShareMenu,
                            onDismissRequest = { showShareMenu = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text("Enviar por WhatsApp") },
                                onClick = {
                                    showShareMenu = false
                                    val mapsUrl = "https://maps.google.com/?q=${notice.latitude},${notice.longitude}"
                                    val message = "Aviso: ${report.observation}\nParada: ${notice.stopName}\nUbicación: $mapsUrl"

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
                                                    Uri.parse("https://wa.me/?text=${Uri.encode(message)}"),
                                                )
                                            context.startActivity(waIntent)
                                        } catch (_: Exception) {
                                            val chooser =
                                                Intent.createChooser(
                                                    Intent(Intent.ACTION_SEND).apply {
                                                        type = "text/plain"
                                                        putExtra(Intent.EXTRA_TEXT, message)
                                                    },
                                                    "Compartir aviso",
                                                )
                                            context.startActivity(chooser)
                                        }
                                    }
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("Copiar enlace") },
                                onClick = {
                                    showShareMenu = false
                                    val mapsUrl = "https://maps.google.com/?q=${notice.latitude},${notice.longitude}"
                                    clipboardManager.setText(AnnotatedString(mapsUrl))
                                    Toast
                                        .makeText(
                                            context,
                                            "Enlace copiado al portapapeles",
                                            Toast.LENGTH_SHORT,
                                        ).show()
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("Otras apps") },
                                onClick = {
                                    showShareMenu = false
                                    val mapsUrl = "https://maps.google.com/?q=${notice.latitude},${notice.longitude}"
                                    val message = "Aviso: ${report.observation}\nParada: ${notice.stopName}\nUbicación: $mapsUrl"

                                    val sendIntent =
                                        Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, message)
                                        }
                                    val chooser = Intent.createChooser(sendIntent, "Compartir aviso")
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
    retryLabel: String = "Reintentar",
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
