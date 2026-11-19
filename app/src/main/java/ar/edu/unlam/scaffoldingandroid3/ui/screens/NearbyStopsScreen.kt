
package ar.edu.unlam.scaffoldingandroid3.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ar.edu.unlam.scaffoldingandroid3.domain.model.BusStop
import ar.edu.unlam.scaffoldingandroid3.domain.model.FavoriteStopSnapshot
import ar.edu.unlam.scaffoldingandroid3.domain.model.Latitude
import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationData
import ar.edu.unlam.scaffoldingandroid3.domain.model.Longitude
import ar.edu.unlam.scaffoldingandroid3.domain.model.SearchHistory
import ar.edu.unlam.scaffoldingandroid3.domain.model.distanceMetersTo
import ar.edu.unlam.scaffoldingandroid3.ui.components.BusStopCard
import ar.edu.unlam.scaffoldingandroid3.ui.components.OsmMapView
import ar.edu.unlam.scaffoldingandroid3.ui.components.SearchHistoryChips
import ar.edu.unlam.scaffoldingandroid3.ui.uistate.MapUiState
import ar.edu.unlam.scaffoldingandroid3.ui.viewmodel.FavoriteBusStopsViewModel
import ar.edu.unlam.scaffoldingandroid3.ui.viewmodel.MapViewModel

@Composable
fun NearbyStopsScreen(
    viewModel: MapViewModel = hiltViewModel(),
    onNavigateToCompass: (BusStop) -> Unit = {},
    favoriteViewModel: FavoriteBusStopsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val recentSearches by viewModel.recentSearches.collectAsState()
    val favoriteIds by favoriteViewModel.favoriteStopIds.collectAsState()

    var selectedTab by rememberSaveable {
        mutableStateOf(HomeTab.Map)
    }

    var selectedFavorite by remember {
        mutableStateOf<FavoriteStopSnapshot?>(null)
    }

    var query by rememberSaveable {
        mutableStateOf("")
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions(),
        ) { result ->
            viewModel.onPermissionResult(
                fineGranted =
                    result[Manifest.permission.ACCESS_FINE_LOCATION] == true,
                coarseGranted =
                    result[Manifest.permission.ACCESS_COARSE_LOCATION] == true,
            )
        }

    fun resolveLocationPermission() {
        val fine =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED

        val coarse =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED

        if (!fine && !coarse) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
            )
        } else {
            viewModel.loadUserLocationAndStops(
                isApproximate = coarse && !fine,
            )
        }
    }

    LaunchedEffect(Unit) {
        resolveLocationPermission()
    }

    Scaffold(
        topBar = {
            HomeHeader(
                query = query,
                onQueryChange = {
                    query = it
                    viewModel.onSearchQueryChange(it)
                },
                onSearchSubmitted = {
                    viewModel.submitSearch(it)
                },
                recentSearches = recentSearches,
                showSearch = selectedTab == HomeTab.Map,
            )
        },
        bottomBar = {
            HomeBottomBar(
                selectedTab = selectedTab,
                onTabSelected = {
                    selectedTab = it
                },
            )
        },
    ) { padding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding),
        ) {
            when (selectedTab) {
                HomeTab.Map -> {
                    when (val state = uiState) {
                        MapUiState.Loading -> {
                            MessageContent(
                                message = "Buscando paradas cerca de vos...",
                                showProgress = true,
                            )
                        }

                        MapUiState.PermissionDenied -> {
                            MessageContent(
                                title = "Permiso de ubicación rechazado",
                                message = "Usamos tu ubicación para mostrarte las paradas más cercanas.",
                                primaryLabel = "Permitir ubicación",
                                onPrimary = {
                                    resolveLocationPermission()
                                },
                                secondaryLabel = "Explorar el mapa manualmente",
                                onSecondary = {
                                    viewModel.exploreManually()
                                },
                            )
                        }

                        MapUiState.LocationDisabled -> {
                            MessageContent(
                                title = "Ubicación desactivada",
                                message = "Activá la ubicación del dispositivo.",
                                primaryLabel = "Abrir ajustes",
                                onPrimary = {
                                    context.startActivity(
                                        Intent(
                                            Settings.ACTION_LOCATION_SOURCE_SETTINGS,
                                        ),
                                    )
                                },
                                secondaryLabel = "Reintentar",
                                onSecondary = {
                                    resolveLocationPermission()
                                },
                            )
                        }

                        MapUiState.LocationUnavailable -> {
                            MessageContent(
                                message = "No pudimos obtener tu ubicación.",
                                primaryLabel = "Reintentar",
                                onPrimary = {
                                    resolveLocationPermission()
                                },
                                secondaryLabel = "Explorar manualmente",
                                onSecondary = {
                                    viewModel.exploreManually()
                                },
                            )
                        }

                        is MapUiState.Error -> {
                            MessageContent(
                                title = "Error de conexión",
                                message = state.message,
                                primaryLabel = "Reintentar",
                                onPrimary = {
                                    resolveLocationPermission()
                                },
                            )
                        }

                        is MapUiState.Success -> {
                            NearbyStopsContent(
                                state = state,
                                onStopClick = { stop ->
                                    selectedFavorite = null
                                    viewModel.selectBusStop(stop.stopId)
                                },
                                onOrientClick = onNavigateToCompass,
                                favoriteIds = favoriteIds,
                                onToggleFavorite = { stopId ->
                                    val stop =
                                        state.busStops.firstOrNull {
                                            it.stopId == stopId
                                        }

                                    if (stop != null) {
                                        favoriteViewModel.toggleFavorite(stop)
                                    }
                                },
                                selectedFavorite = selectedFavorite,
                            )
                        }
                    }
                }

                HomeTab.Notices -> {
                    EmptyTab(
                        message = "Todavía no hay avisos de cortes o desvíos disponibles.",
                    )
                }

                HomeTab.Favorites -> {
                    FavoriteBusStopsScreen(
                        onStopSelected = { stop ->
                            selectedFavorite = stop
                            viewModel.selectBusStop(stop.stopId)
                            selectedTab = HomeTab.Map
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeHeader(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearchSubmitted: (String) -> Unit,
    recentSearches: List<SearchHistory>,
    showSearch: Boolean,
) {
    Surface(tonalElevation = 2.dp) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(
                        horizontal = 16.dp,
                        vertical = 12.dp,
                    ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Voy en Bondi",
                style = MaterialTheme.typography.headlineSmall,
            )

            if (showSearch) {
                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text("Buscar parada o línea…")
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null,
                        )
                    },
                    singleLine = true,
                    keyboardOptions =
                        KeyboardOptions(
                            imeAction = ImeAction.Search,
                        ),
                    keyboardActions =
                        KeyboardActions(
                            onSearch = {
                                onSearchSubmitted(query)
                            },
                        ),
                )

                if (query.isBlank() && recentSearches.isNotEmpty()) {
                    SearchHistoryChips(
                        searches = recentSearches,
                        onSearchClick = {
                            onQueryChange(it)
                            onSearchSubmitted(it)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeBottomBar(
    selectedTab: HomeTab,
    onTabSelected: (HomeTab) -> Unit,
) {
    NavigationBar {
        HomeTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = selectedTab == tab,
                onClick = {
                    onTabSelected(tab)
                },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = null,
                    )
                },
                label = {
                    Text(tab.label)
                },
            )
        }
    }
}

private enum class HomeTab(
    val label: String,
    val icon: ImageVector,
) {
    Map(
        label = "Mapa",
        icon = Icons.Filled.Place,
    ),
    Notices(
        label = "Avisos",
        icon = Icons.Filled.Notifications,
    ),
    Favorites(
        label = "Favoritas",
        icon = Icons.Filled.Favorite,
    ),
}

@Composable
private fun NearbyStopsContent(
    state: MapUiState.Success,
    onStopClick: (BusStop) -> Unit,
    onOrientClick: (BusStop) -> Unit,
    favoriteIds: Set<Long>,
    onToggleFavorite: (Long) -> Unit,
    selectedFavorite: FavoriteStopSnapshot? = null,
) {
    val selected =
        state.busStops.firstOrNull {
            it.stopId == state.selectedStopId
        }

    val favoriteStop =
        selectedFavorite?.takeIf {
            it.stopId == state.selectedStopId
        }

    val mapStops =
        if (favoriteStop != null && selected == null) {
            state.busStops +
                BusStop(
                    stopId = favoriteStop.stopId,
                    name = favoriteStop.name,
                    locationData =
                        LocationData(
                            latitude = Latitude(favoriteStop.latitude),
                            longitude = Longitude(favoriteStop.longitude),
                        ),
                    markedAsFavorite = true,
                    busLines = emptyList(),
                )
        } else {
            state.busStops
        }

    val selectedForMap =
        mapStops.firstOrNull {
            it.stopId == state.selectedStopId
        }

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        if (state.isApproximate) {
            Surface(
                color = MaterialTheme.colorScheme.tertiaryContainer,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "Ubicación aproximada. Las distancias pueden ser imprecisas.",
                    modifier = Modifier.padding(8.dp),
                    textAlign = TextAlign.Center,
                )
            }
        }

        if (state.isFromCache) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "Sin conexión. Mostrando paradas guardadas en caché.",
                    modifier = Modifier.padding(8.dp),
                )
            }
        }

        if (state.isDemo) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "Sin datos del servidor. Mostrando paradas de ejemplo.",
                    modifier = Modifier.padding(8.dp),
                )
            }
        }

        Box(
            modifier = Modifier.fillMaxWidth(),
        ) {
            OsmMapView(
                userLocation = state.center,
                stops = mapStops,
                selectedStopId = state.selectedStopId,
                onStopClick = onStopClick,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(260.dp),
            )

            if (selectedForMap != null) {
                Surface(
                    modifier =
                        Modifier
                            .align(Alignment.BottomCenter)
                            .padding(12.dp),
                    shape = MaterialTheme.shapes.medium,
                    tonalElevation = 3.dp,
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.End,
                    ) {
                        Text(
                            text = selectedForMap.name,
                            style = MaterialTheme.typography.titleSmall,
                        )

                        val lines =
                            if (
                                favoriteStop != null &&
                                selectedForMap.busLines.isEmpty()
                            ) {
                                favoriteStop.lines
                            } else {
                                selectedForMap.busLines.joinToString(" · ") {
                                    "Línea ${it.number}"
                                }
                            }

                        Text(
                            text = lines,
                        )

                        TextButton(
                            onClick = {
                                onOrientClick(selectedForMap)
                            },
                        ) {
                            Text("Orientarme")
                        }
                    }
                }
            }
        }

        Text(
            text = "Paradas cerca de vos",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp),
        )

        when {
            state.busStops.isEmpty() -> {
                Text(
                    text = "No hay paradas cerca de tu ubicación.",
                    modifier = Modifier.padding(16.dp),
                )
            }

            state.filteredStops.isEmpty() -> {
                Text(
                    text = "No encontramos paradas con esa búsqueda.",
                    modifier = Modifier.padding(16.dp),
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding =
                        PaddingValues(
                            horizontal = 16.dp,
                            vertical = 4.dp,
                        ),
                    verticalArrangement =
                        Arrangement.spacedBy(8.dp),
                ) {
                    items(
                        items = state.filteredStops,
                        key = {
                            it.stopId
                        },
                    ) { stop ->
                        BusStopCard(
                            stop = stop,
                            distanceMeters =
                                state.center.distanceMetersTo(
                                    stop.locationData,
                                ),
                            selected =
                                stop.stopId == state.selectedStopId,
                            onClick = {
                                onStopClick(stop)
                            },
                            isFavorite =
                                stop.stopId in favoriteIds,
                            onFavoriteClick = {
                                onToggleFavorite(stop.stopId)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyTab(message: String) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun MessageContent(
    message: String,
    title: String? = null,
    showProgress: Boolean = false,
    primaryLabel: String? = null,
    onPrimary: () -> Unit = {},
    secondaryLabel: String? = null,
    onSecondary: () -> Unit = {},
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (showProgress) {
            CircularProgressIndicator()

            Spacer(
                modifier = Modifier.height(16.dp),
            )
        }

        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )

            Spacer(
                modifier = Modifier.height(8.dp),
            )
        }

        Text(
            text = message,
            textAlign = TextAlign.Center,
        )

        if (primaryLabel != null) {
            Spacer(
                modifier = Modifier.height(16.dp),
            )

            Button(
                onClick = onPrimary,
            ) {
                Text(primaryLabel)
            }
        }

        if (secondaryLabel != null) {
            Spacer(
                modifier = Modifier.height(8.dp),
            )

            OutlinedButton(
                onClick = onSecondary,
            ) {
                Text(secondaryLabel)
            }
        }
    }
}
