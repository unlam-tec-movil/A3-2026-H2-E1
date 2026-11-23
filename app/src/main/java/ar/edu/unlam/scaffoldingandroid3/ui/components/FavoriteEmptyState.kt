package ar.edu.unlam.scaffoldingandroid3.ui.components

import ar.edu.unlam.scaffoldingandroid3.R
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun FavoriteEmptyState(onExploreMap: () -> Unit) {
    val colors = MaterialTheme.colorScheme

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .background(colors.surface)
                    .clip(MaterialTheme.shapes.medium),
            colors =
                CardDefaults.cardColors(
                    containerColor = colors.surface,
                    contentColor = colors.onSurface,
                ),
            elevation =
                CardDefaults.cardElevation(
                    defaultElevation = 4.dp,
                ),
        ) {
            FavoriteEmptyStateContent(onExploreMap = onExploreMap)
        }
    }
}

@Composable
private fun FavoriteEmptyStateContent(onExploreMap: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(
            imageVector = Icons.Filled.Favorite,
            contentDescription = stringResource(R.string.favorite_empty_icon_description),
            tint = colors.primary,
            modifier = Modifier.size(56.dp),
        )

        Text(
            text = stringResource(R.string.favorite_empty_title),
            style = MaterialTheme.typography.titleMedium,
            color = colors.onSurface,
            textAlign = TextAlign.Center,
        )

        Text(
            text = stringResource(R.string.favorite_empty_description),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Button(
            onClick = onExploreMap,
        ) {
            Text(stringResource(R.string.favorite_empty_button))
        }
    }
}
