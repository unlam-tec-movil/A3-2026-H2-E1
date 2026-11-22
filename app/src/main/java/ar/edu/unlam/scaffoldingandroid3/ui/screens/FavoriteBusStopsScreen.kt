
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ar.edu.unlam.scaffoldingandroid3.domain.model.FavoriteStopSnapshot
import ar.edu.unlam.scaffoldingandroid3.ui.viewmodel.FavoriteBusStopsViewModel

internal fun filterAndSortFavoriteStops(
    stops: List<FavoriteStopSnapshot>,
    query: String,
): List<FavoriteStopSnapshot> {
    val search = query.trim()

    return stops
        .filter { stop ->
            search.isBlank() ||
                stop.name.contains(search, ignoreCase = true) ||
                stop.lines.contains(search, ignoreCase = true)
        }.sortedWith(
            compareBy(
                String.CASE_INSENSITIVE_ORDER,
            ) { it.name },
        )
}

@Composable
fun FavoriteBusStopsScreen(
    onStopSelected: (FavoriteStopSnapshot) -> Unit = {},
    viewModel: FavoriteBusStopsViewModel = hiltViewModel(),
) {
    val favorites by viewModel.favoriteStops.collectAsState()
    val favoriteIds by viewModel.favoriteStopIds.collectAsState()

    var searchQuery by rememberSaveable {
        mutableStateOf("")
    }

    val filteredFavorites =
        remember(favorites, searchQuery) {
            filterAndSortFavoriteStops(
                stops = favorites,
                query = searchQuery,
            )
        }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Mis paradas favoritas",
            style = MaterialTheme.typography.headlineSmall,
        )

        if (favoriteIds.isEmpty()) {
            Text(
                text = "Todavía no agregaste paradas favoritas",
                modifier = Modifier.padding(top = 12.dp),
            )
        } else {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Buscar favoritas")
                },
                placeholder = {
                    Text("Nombre de parada o línea")
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null,
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { searchQuery = "" },
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Limpiar búsqueda",
                            )
                        }
                    }
                },
                singleLine = true,
            )

            Text(
                text =
                    if (filteredFavorites.size == 1) {
                        "1 parada encontrada"
                    } else {
                        "${filteredFavorites.size} paradas encontradas"
                    },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (favorites.size < favoriteIds.size) {
                Text(
                    text = "Algunas favoritas anteriores necesitan volver a guardarse desde el mapa para recuperar sus datos.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            if (filteredFavorites.isEmpty() && searchQuery.isNotBlank()) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "No encontramos favoritas con esa búsqueda",
                        style = MaterialTheme.typography.titleMedium,
                    )

                    Text(
                        text = "Probá con otro nombre o número de línea.",
                        style = MaterialTheme.typography.bodyMedium,
                    )

                    TextButton(
                        onClick = { searchQuery = "" },
                    ) {
                        Text("Limpiar búsqueda")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(
                        items = filteredFavorites,
                        key = { it.stopId },
                    ) { stop ->
                        Card(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onStopSelected(stop)
                                    },
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
}
