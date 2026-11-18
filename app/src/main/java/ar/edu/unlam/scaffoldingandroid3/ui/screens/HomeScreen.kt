
package ar.edu.unlam.scaffoldingandroid3.ui.screens

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
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ar.edu.unlam.scaffoldingandroid3.ui.theme.FavoriteRed
import ar.edu.unlam.scaffoldingandroid3.ui.viewmodel.FavoriteBusStopsViewModel

@Composable
fun HomeScreen(
    navigateToFavoriteBusStop: () -> Unit,
    viewModel: FavoriteBusStopsViewModel = hiltViewModel(),
) {
    val favoriteStopIds by viewModel.favoriteStopIds.collectAsState()

    // Datos temporales hasta integrar las paradas reales.
    val busStops = listOf(
        1L to "Av. San Martín y Arieta",
        2L to "Florencio Varela y Villegas",
    )

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
        ) {
            Text(
                text = "Paradas cercanas",
                style = MaterialTheme.typography.headlineSmall,
            )

            LazyColumn {
                items(
                    items = busStops,
                    key = { (stopId, _) -> stopId },
                ) { (stopId, stopName) ->
                    val isFavorite = stopId in favoriteStopIds

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stopName,
                            modifier = Modifier.weight(1f),
                        )

                        IconButton(
                            onClick = {
                                viewModel.toggleFavorite(stopId)
                            },
                        ) {
                            Icon(
                                imageVector = if (isFavorite) {
                                    Icons.Filled.Favorite
                                } else {
                                    Icons.Outlined.FavoriteBorder
                                },
                                contentDescription = if (isFavorite) {
                                    "Quitar de favoritos"
                                } else {
                                    "Agregar a favoritos"
                                },
                                tint = if (isFavorite) {
                                    FavoriteRed
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
