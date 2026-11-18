
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
fun FavoriteBusStopsScreen(
    navigateToHome: () -> Unit,
    viewModel: FavoriteBusStopsViewModel = hiltViewModel(),
) {
    val favoriteStopIds by viewModel.favoriteStopIds.collectAsState()

    // Mismos datos temporales que utiliza Home.
    val busStops = listOf(
        1L to "Av. San Martín y Arieta",
        2L to "Florencio Varela y Villegas",
    )

    val favoriteStops = busStops.filter { (stopId, _) ->
        stopId in favoriteStopIds
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
        ) {
            Text(
                text = "Mis paradas favoritas",
                style = MaterialTheme.typography.headlineSmall,
            )

            if (favoriteStops.isEmpty()) {
                Text(
                    text = "Todavía no tenés paradas favoritas.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 24.dp),
                )

                Text(
                    text = "Explorá las paradas cercanas y tocá " +
                            "el corazón para guardarlas.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            } else {
                LazyColumn {
                    items(
                        items = favoriteStops,
                        key = { (stopId, _) -> stopId },
                    ) { (stopId, stopName) ->
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
                                    viewModel.removeFavorite(stopId)
                                },
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Favorite,
                                    contentDescription = "Quitar de favoritos",
                                    tint = FavoriteRed,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
