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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
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
import ar.edu.unlam.scaffoldingandroid3.ui.components.PhotoReportCameraButton
import ar.edu.unlam.scaffoldingandroid3.ui.components.PhotoReportDialogs
import ar.edu.unlam.scaffoldingandroid3.ui.components.SearchHistoryChips
import ar.edu.unlam.scaffoldingandroid3.ui.uistate.MapUiState
import ar.edu.unlam.scaffoldingandroid3.ui.viewmodel.FavoriteBusStopsViewModel
import ar.edu.unlam.scaffoldingandroid3.ui.viewmodel.MapViewModel
import ar.edu.unlam.scaffoldingandroid3.ui.viewmodel.PhotoReportViewModel
import kotlinx.coroutines.launch

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
                                query = query,
                                onQueryChange = {
                                    query = it
                                    viewModel.onSearchQueryChange(it)
                                },
                                onSearchSubmitted = {
                                    viewModel.submitSearch(it)
                                },
                                recentSearches = recentSearches,
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
) {
    Surface(
        tonalElevation = 2.dp,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(
                        horizontal = 16.dp,
                        vertical = 8.dp,
                    ),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "Voy en Bondi",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )

            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text("Buscar parada o línea…")
                },
                leadingIcon = {
                    IconButton(onClick = { onSearchSubmitted(query) }) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Buscar",
                        )
                    }
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Limpiar búsqueda",
                            )
                        }
                    }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NearbyStopsContent(
    state: MapUiState.Success,
    query: String,
    onQueryChange: (String) -> Unit,
    onSearchSubmitted: (String) -> Unit,
    recentSearches: List<SearchHistory>,
    onStopClick: (BusStop) -> Unit,
    onOrientClick: (BusStop) -> Unit,
    favoriteIds: Set<Long>,
    onToggleFavorite: (Long) -> Unit,
    selectedFavorite: FavoriteStopSnapshot? = null,
) {
    val scaffoldState = rememberBottomSheetScaffoldState()
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val photoReportViewModel: PhotoReportViewModel = hiltViewModel()
    val photoReportState by photoReportViewModel.uiState.collectAsState()

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

    val selectedForReport =
        mapStops.firstOrNull {
            it.stopId == state.selectedStopId
        }

    LaunchedEffect(state.selectedStopId) {
        if (state.selectedStopId != null) {
            val index = state.filteredStops.indexOfFirst { it.stopId == state.selectedStopId }
            if (index >= 0) {
                listState.animateScrollToItem(index)
            }
        }
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 50.dp,
        containerColor = Color.Transparent,
        sheetContainerColor = MaterialTheme.colorScheme.surface,
        sheetContent = {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.5f),
            ) {
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

                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Paradas cerca de vos",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        if (selectedForReport != null) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                TextButton(
                                    onClick = {
                                        onOrientClick(selectedForReport)
                                    },
                                ) {
                                    Text("Orientarme")
                                }

                                PhotoReportCameraButton(
                                    stopId = selectedForReport.stopId,
                                    onPhotoTaken = photoReportViewModel::onPhotoTaken,
                                    onPermissionDenied = photoReportViewModel::onPermissionDenied,
                                    onCancelled = photoReportViewModel::onCancelled,
                                )
                            }
                        }
                    }

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
                                state = listState,
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
        },
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .clipToBounds(),
        ) {
            OsmMapView(
                userLocation = state.center,
                stops = mapStops,
                selectedStopId = state.selectedStopId,
                onStopClick = { stop ->
                    onStopClick(stop)
                    coroutineScope.launch {
                        scaffoldState.bottomSheetState.expand()
                    }
                },
                modifier = Modifier.fillMaxSize(),
            )

            HomeHeader(
                query = query,
                onQueryChange = onQueryChange,
                onSearchSubmitted = onSearchSubmitted,
                recentSearches = recentSearches,
            )
        }
    }

    PhotoReportDialogs(
        state = photoReportState,
        onConfirm = photoReportViewModel::saveReport,
        onDismiss = photoReportViewModel::onCancelled,
        onAcknowledge = photoReportViewModel::onAcknowledged,
    )
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
