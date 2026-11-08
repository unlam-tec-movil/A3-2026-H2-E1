package ar.edu.unlam.scaffoldingandroid3.ui.screens

import android.Manifest
import android.content.Intent
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ar.edu.unlam.scaffoldingandroid3.domain.BusStop
import ar.edu.unlam.scaffoldingandroid3.domain.GeoPoint
import ar.edu.unlam.scaffoldingandroid3.domain.SearchHistory
import ar.edu.unlam.scaffoldingandroid3.domain.filterBusStops
import ar.edu.unlam.scaffoldingandroid3.ui.components.BusStopCard
import ar.edu.unlam.scaffoldingandroid3.ui.components.GoogleMapView
import ar.edu.unlam.scaffoldingandroid3.ui.components.SearchHistoryChips
import kotlinx.coroutines.withTimeoutOrNull

private const val LOCATION_TIMEOUT_MILLIS = 10_000L
private val DEFAULT_MANUAL_LOCATION = GeoPoint(-34.6705, -58.5628)

@Composable
fun NearbyStopsScreen(
    viewModel: NearbyStopsViewModel = hiltViewModel(),
    onNavigateToCompass: (BusStop) -> Unit = {},
) {
    val context = LocalContext.current
    val locationRepo = viewModel.locationRepo
    val recentSearches by viewModel.recentSearches.collectAsState()

    var uiState by remember { mutableStateOf<NearbyStopsUiState>(NearbyStopsUiState.Locating) }
    var selectedStopId by remember { mutableStateOf<String?>(null) }
    var selectedTab by rememberSaveable { mutableStateOf(HomeTab.Map) }
    var query by rememberSaveable { mutableStateOf("") }
    var attempt by remember { mutableIntStateOf(0) }
    var isApproximateLocation by remember { mutableStateOf(false) }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions(),
        ) { result ->
            val fineGranted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true
            val coarseGranted = result[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            if (fineGranted || coarseGranted) {
                isApproximateLocation = !fineGranted && coarseGranted
                attempt++
            } else {
                uiState = NearbyStopsUiState.PermissionDenied
            }
        }

    LaunchedEffect(attempt) {
        if (!locationRepo.hasPermission()) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
            )
            return@LaunchedEffect
        }
        if (!locationRepo.isLocationEnabled()) {
            uiState = NearbyStopsUiState.LocationDisabled
            return@LaunchedEffect
        }
        uiState = NearbyStopsUiState.Locating
        val center = withTimeoutOrNull(LOCATION_TIMEOUT_MILLIS) { locationRepo.currentLocation() }
        if (center == null) {
            uiState = NearbyStopsUiState.LocationUnavailable
        } else {
            viewModel.loadStopsForLocation(center, isApproximateLocation) { state ->
                uiState = state
            }
        }
    }

    Scaffold(
        topBar = {
            HomeHeader(
                query = query,
                onQueryChange = { query = it },
                onSearchSubmitted = { viewModel.onSearchSubmitted(it) },
                recentSearches = recentSearches,
                showSearch = selectedTab == HomeTab.Map,
            )
        },
        bottomBar = {
            HomeBottomBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
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
                HomeTab.Map ->
                    when (val state = uiState) {
                        NearbyStopsUiState.Locating ->
                            MessageContent(
                                message = "Buscando paradas cerca de vos...",
                                showProgress = true,
                            )

                        NearbyStopsUiState.PermissionDenied ->
                            MessageContent(
                                title = "Permiso de ubicación rechazado",
                                message = "Usamos tu ubicación para mostrarte las paradas más cercanas.",
                                primaryLabel = "Permitir ubicación",
                                onPrimary = { attempt++ },
                                secondaryLabel = "Explorar el mapa manualmente",
                                onSecondary = {
                                    viewModel.loadStopsForLocation(DEFAULT_MANUAL_LOCATION, false) { newState ->
                                        uiState = newState
                                    }
                                },
                            )

                        NearbyStopsUiState.LocationDisabled ->
                            MessageContent(
                                title = "Ubicación desactivada",
                                message = "Activá la ubicación del dispositivo para ver paradas cercanas.",
                                primaryLabel = "Abrir ajustes",
                                onPrimary = {
                                    context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                                },
                                secondaryLabel = "Reintentar",
                                onSecondary = { attempt++ },
                            )

                        NearbyStopsUiState.LocationUnavailable ->
                            MessageContent(
                                message = "No pudimos obtener tu ubicación. Intentá de nuevo.",
                                primaryLabel = "Reintentar",
                                onPrimary = { attempt++ },
                            )

                        is NearbyStopsUiState.Error ->
                            MessageContent(
                                title = "Error de conexión",
                                message = state.message,
                                primaryLabel = "Reintentar",
                                onPrimary = { attempt++ },
                            )

                        is NearbyStopsUiState.Ready ->
                            NearbyStopsContent(
                                state = state,
                                query = query,
                                selectedStopId = selectedStopId,
                                onStopClick = { stop -> selectedStopId = stop.id },
                                onOrientClick = onNavigateToCompass,
                            )
                    }

                HomeTab.Notices -> EmptyTab(message = "No hay avisos cerca de vos.")

                HomeTab.Favorites -> EmptyTab(message = "Todavía no agregaste paradas favoritas.")
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
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Voy en Bondi",
                style = MaterialTheme.typography.headlineSmall,
            )
            if (showSearch) {
                OutlinedTextField(
                    value = query,
                    onValueChange = {
                        onQueryChange(it)
                        onSearchSubmitted(it)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(text = "Buscar parada o línea…") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Filled.Search, contentDescription = null)
                    },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
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
                onClick = { onTabSelected(tab) },
                icon = { Icon(imageVector = tab.icon, contentDescription = null) },
                label = { Text(text = tab.label) },
            )
        }
    }
}

private enum class HomeTab(
    val label: String,
    val icon: ImageVector,
) {
    Map(label = "Mapa", icon = Icons.Filled.Place),
    Notices(label = "Avisos", icon = Icons.Filled.Notifications),
    Favorites(label = "Favoritas", icon = Icons.Filled.Favorite),
}

@Composable
private fun NearbyStopsContent(
    state: NearbyStopsUiState.Ready,
    query: String,
    selectedStopId: String?,
    onStopClick: (BusStop) -> Unit,
    onOrientClick: (BusStop) -> Unit,
) {
    val selected = state.stops.firstOrNull { it.id == selectedStopId }
    val stops = remember(state.stops, query) { filterBusStops(state.stops, query) }

    Column(modifier = Modifier.fillMaxSize()) {
        if (state.isApproximate) {
            Surface(
                color = MaterialTheme.colorScheme.tertiaryContainer,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "Ubicación aproximada. Las distancias pueden ser imprecisas.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
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
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    textAlign = TextAlign.Center,
                )
            }
        }

        Box(modifier = Modifier.fillMaxWidth()) {
            GoogleMapView(
                userLocation = state.center,
                stops = state.stops,
                selectedStopId = selectedStopId,
                onStopClick = onStopClick,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(260.dp),
            )
            if (selected != null) {
                Surface(
                    modifier =
                        Modifier
                            .align(Alignment.BottomCenter)
                            .padding(12.dp),
                    shape = MaterialTheme.shapes.medium,
                    tonalElevation = 3.dp,
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        horizontalAlignment = Alignment.End,
                    ) {
                        Text(
                            text =
                                selected.lines.joinToString(" · ") {
                                    "${it.code} → ${it.destination}"
                                },
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                        TextButton(onClick = { onOrientClick(selected) }) {
                            Text(text = "Orientarme")
                        }
                    }
                }
            }
        }

        Text(
            text = "Paradas cerca de vos",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        )

        when {
            state.stops.isEmpty() ->
                Text(
                    text = "No hay paradas cerca de tu ubicación.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )

            stops.isEmpty() ->
                Text(
                    text = "No encontramos paradas con esa búsqueda.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )

            else ->
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(items = stops, key = { it.id }) { stop ->
                        BusStopCard(
                            stop = stop,
                            distanceMeters = state.center.distanceTo(stop.location),
                            selected = stop.id == selectedStopId,
                            onClick = { onStopClick(stop) },
                        )
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
            style = MaterialTheme.typography.bodyLarge,
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
            Spacer(modifier = Modifier.height(16.dp))
        }
        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        if (primaryLabel != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onPrimary) {
                Text(text = primaryLabel)
            }
        }
        if (secondaryLabel != null) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick = onSecondary) {
                Text(text = secondaryLabel)
            }
        }
    }
}
