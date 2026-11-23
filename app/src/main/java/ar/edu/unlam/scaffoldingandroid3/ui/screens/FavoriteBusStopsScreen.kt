
package ar.edu.unlam.scaffoldingandroid3.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ar.edu.unlam.scaffoldingandroid3.domain.model.FavoriteStopSnapshot
import ar.edu.unlam.scaffoldingandroid3.ui.viewmodel.FavoriteBusStopsViewModel
import kotlinx.coroutines.launch

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
            compareBy(String.CASE_INSENSITIVE_ORDER) { it.name },
        )
}

@Composable
fun FavoriteBusStopsScreen(
    onStopSelected: (FavoriteStopSnapshot) -> Unit = {},
    onExploreMap: () -> Unit = {},
    viewModel: FavoriteBusStopsViewModel = hiltViewModel(),
) {
    val favorites by viewModel.favoriteStops.collectAsState()
    val favoriteIds by viewModel.favoriteStopIds.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val colors = MaterialTheme.colorScheme

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

    Scaffold(
        containerColor = colors.background,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    ) { padding ->
        FavoritesBackground(
            hasFavorites = favoriteIds.isNotEmpty(),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "Mis paradas favoritas",
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.onBackground,
                )

                AnimatedContent(
                    targetState = favoriteIds.isEmpty(),
                    modifier = Modifier.weight(1f),
                    transitionSpec = {
                        (
                                fadeIn(animationSpec = tween(350)) +
                                        slideIntoContainer(
                                            towards = AnimatedContentTransitionScope.SlideDirection.Up,
                                            animationSpec = tween(350),
                                        )
                                ).togetherWith(
                                fadeOut(animationSpec = tween(250)),
                            )
                    },
                    label = "FavoritesTransition",
                ) { isEmpty ->
                    if (isEmpty) {
                        FavoriteEmptyState(
                            onExploreMap = onExploreMap,
                        )
                    } else {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = {
                                    searchQuery = it
                                },
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
                                            onClick = {
                                                searchQuery = ""
                                            },
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
                                color = colors.onSurfaceVariant,
                            )

                            if (favorites.size < favoriteIds.size) {
                                Text(
                                    text =
                                        "Algunas favoritas anteriores necesitan volver a guardarse " +
                                                "desde el mapa para recuperar sus datos.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.onSurfaceVariant,
                                )
                            }

                            if (
                                filteredFavorites.isEmpty() &&
                                searchQuery.isNotBlank()
                            ) {
                                FavoriteSearchEmptyState(
                                    onClearSearch = {
                                        searchQuery = ""
                                    },
                                )
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
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
                                            colors =
                                                CardDefaults.cardColors(
                                                    containerColor = colors.surface,
                                                    contentColor = colors.onSurface,
                                                ),
                                            elevation =
                                                CardDefaults.cardElevation(
                                                    defaultElevation = 3.dp,
                                                ),
                                        ) {
                                            Row(
                                                modifier =
                                                    Modifier
                                                        .fillMaxWidth()
                                                        .padding(16.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                            ) {
                                                Column(
                                                    modifier = Modifier.weight(1f),
                                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                                ) {
                                                    Text(
                                                        text = stop.name,
                                                        style = MaterialTheme.typography.titleMedium,
                                                        color = colors.onSurface,
                                                    )

                                                    Text(
                                                        text = stop.lines,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = colors.onSurfaceVariant,
                                                    )

                                                    Text(
                                                        text = "Ver en el mapa",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = colors.primary,
                                                    )
                                                }

                                                IconButton(
                                                    onClick = {
                                                        viewModel.removeFavorite(stop.stopId)

                                                        coroutineScope.launch {
                                                            snackbarHostState.currentSnackbarData?.dismiss()

                                                            val result =
                                                                snackbarHostState.showSnackbar(
                                                                    message = "Parada eliminada de favoritas",
                                                                    actionLabel = "Deshacer",
                                                                    withDismissAction = true,
                                                                    duration = SnackbarDuration.Short,
                                                                )

                                                            if (
                                                                result ==
                                                                SnackbarResult.ActionPerformed
                                                            ) {
                                                                viewModel.restoreFavorite(stop)
                                                            }
                                                        }
                                                    },
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Filled.Favorite,
                                                        contentDescription = "Quitar de favoritos",
                                                        tint = colors.primary,
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
            }
        }
    }
}

@Composable
private fun FavoriteEmptyState(onExploreMap: () -> Unit) {
    val colors = MaterialTheme.colorScheme

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
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
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(56.dp),
                )

                Text(
                    text = "Todavía no agregaste paradas favoritas",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.onSurface,
                    textAlign = TextAlign.Center,
                )

                Text(
                    text = "Explorá el mapa y guardá las paradas que usás con frecuencia para encontrarlas más rápido.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                Button(
                    onClick = onExploreMap,
                ) {
                    Text("Explorar mapa")
                }
            }
        }
    }
}

@Composable
private fun FavoriteSearchEmptyState(onClearSearch: () -> Unit) {
    val colors = MaterialTheme.colorScheme

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = colors.surface,
                contentColor = colors.onSurface,
            ),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(40.dp),
            )

            Text(
                text = "No encontramos favoritas con esa búsqueda",
                style = MaterialTheme.typography.titleMedium,
                color = colors.onSurface,
                textAlign = TextAlign.Center,
            )

            Text(
                text = "Probá con otro nombre o número de línea.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            TextButton(
                onClick = onClearSearch,
            ) {
                Text("Limpiar búsqueda")
            }
        }
    }
}

@Composable
private fun FavoritesBackground(
    hasFavorites: Boolean,
    content: @Composable () -> Unit,
) {
    val colors = MaterialTheme.colorScheme

    val circleSize by animateDpAsState(
        targetValue = if (hasFavorites) 110.dp else 260.dp,
        label = "CircleSize",
    )

    val circleOpacity by animateFloatAsState(
        targetValue = if (hasFavorites) 0.35f else 1f,
        label = "CircleOpacity",
    )

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(colors.background),
    ) {
        Box(
            modifier =
                Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 35.dp, y = (-30).dp)
                    .size(circleSize)
                    .alpha(circleOpacity)
                    .background(
                        color = colors.primaryContainer,
                        shape = CircleShape,
                    ),
        )

        Box(
            modifier =
                Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = (-45).dp, y = 45.dp)
                    .size(circleSize)
                    .alpha(circleOpacity)
                    .background(
                        color = colors.secondaryContainer,
                        shape = CircleShape,
                    ),
        )

        content()
    }
}
