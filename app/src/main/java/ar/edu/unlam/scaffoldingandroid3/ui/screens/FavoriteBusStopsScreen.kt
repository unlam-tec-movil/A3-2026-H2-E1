
package ar.edu.unlam.scaffoldingandroid3.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ar.edu.unlam.scaffoldingandroid3.domain.model.FavoriteStopSnapshot
import ar.edu.unlam.scaffoldingandroid3.ui.viewmodel.FavoriteBusStopsViewModel

@Composable
fun FavoriteBusStopsScreen(
    onStopSelected: (FavoriteStopSnapshot) -> Unit = {},
    viewModel: FavoriteBusStopsViewModel = hiltViewModel(),
) {
    val favorites by viewModel.favoriteStops.collectAsState()
    val favoriteIds by viewModel.favoriteStopIds.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
    ) {
        Text(
            text = "Mis paradas favoritas",
            style = MaterialTheme.typography.headlineSmall,
        )

        if (favoriteIds.isEmpty()) {
            Text(
                text = "Todavía no agregaste paradas favoritas",
                modifier = Modifier.padding(top = 24.dp),
            )
        } else {
            if (favorites.size < favoriteIds.size) {
                Text(
                    text = "Algunas favoritas anteriores necesitan volver a guardarse desde el mapa para recuperar sus datos.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }

            LazyColumn(
                modifier = Modifier.padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(
                    items = favorites,
                    key = { it.stopId },
                ) { stop ->
                    Card(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clickable { onStopSelected(stop) },
                    ) {
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                            ) {
                                Text(
                                    text = stop.name,
                                    style = MaterialTheme.typography.titleMedium,
                                )

                                Text(
                                    text = stop.lines,
                                    style = MaterialTheme.typography.bodyMedium,
                                )

                                Text(
                                    text = "Ver en el mapa",
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            }

                            IconButton(
                                onClick = {
                                    viewModel.removeFavorite(stop.stopId)
                                },
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Favorite,
                                    contentDescription = "Quitar de favoritos",
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
