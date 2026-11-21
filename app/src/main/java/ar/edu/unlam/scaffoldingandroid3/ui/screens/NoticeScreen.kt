package ar.edu.unlam.scaffoldingandroid3.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ar.edu.unlam.scaffoldingandroid3.ui.uistate.NoticeItem
import ar.edu.unlam.scaffoldingandroid3.ui.uistate.NoticeUiState
import ar.edu.unlam.scaffoldingandroid3.ui.viewmodel.NoticeViewModel
import coil.compose.AsyncImage
import java.text.DateFormat
import java.util.Date
import kotlin.math.roundToInt

@Composable
fun NoticeScreen(viewModel: NoticeViewModel = hiltViewModel()) {
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
            if (s.notices.isEmpty()) {
                MessageWithRetry(
                    message = "No hay avisos a menos de 1 km de tu ubicación.",
                    onRetry = viewModel::load,
                    retryLabel = "Actualizar",
                )
            } else {
                NoticeList(notices = s.notices)
            }
    }
}

@Composable
private fun NoticeList(notices: List<NoticeItem>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(notices, key = { it.report.id }) { notice ->
            NoticeCard(notice)
        }
    }
}

@Composable
private fun NoticeCard(notice: NoticeItem) {
    val report = notice.report
    val date =
        remember(report.timestamp) {
            DateFormat
                .getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                .format(Date(report.timestamp))
        }

    Card(modifier = Modifier.fillMaxWidth()) {
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
                )
                Text(
                    text = "A ${notice.distanceMeters.roundToInt()} m · $date",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
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
