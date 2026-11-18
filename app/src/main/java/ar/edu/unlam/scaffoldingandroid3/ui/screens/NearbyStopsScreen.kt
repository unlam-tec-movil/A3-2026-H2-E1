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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ar.edu.unlam.scaffoldingandroid3.domain.BusStop
import ar.edu.unlam.scaffoldingandroid3.domain.GeoPoint
import ar.edu.unlam.scaffoldingandroid3.domain.SearchHistory
import ar.edu.unlam.scaffoldingandroid3.domain.filterBusStops
import ar.edu.unlam.scaffoldingandroid3.ui.components.BusStopCard
import ar.edu.unlam.scaffoldingandroid3.ui.components.OsmMapView
import ar.edu.unlam.scaffoldingandroid3.ui.components.SearchHistoryChips
import kotlinx.coroutines.withTimeoutOrNull

private const val LOCATION_TIMEOUT_MILLIS = 10_000L
private val DEFAULT_MANUAL_LOCATION = GeoPoint(-34.6705, -58.5628)

@Composable
fun NearbyStopsScreen(viewModel: NearbyStopsViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val locationRepo = viewModel.locationRepo
    val recentSearches by viewModel.recentSearches.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var selectedStopId by remember { mutableStateOf<String?>(null) }
    var focusTrigger by remember { mutableIntStateOf(0) }
    var query by rememberSaveable { mutableStateOf("") }
    var submittedQuery by rememberSaveable { mutableStateOf("") }
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
                viewModel.onPermissionDenied()
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
            viewModel.onLocationDisabled()
            return@LaunchedEffect
        }
        viewModel.setLocating()
        viewModel.startLocationTracking(isApproximateLocation)

        val center = withTimeoutOrNull(LOCATION_TIMEOUT_MILLIS) { locationRepo.currentLocation() }
        if (center == null && viewModel.uiState.value is NearbyStopsUiState.Locating) {
            viewModel.onLocationUnavailable()
        }
    }

    Scaffold(
        topBar = {
            HomeHeader(
                query = query,
                onQueryChange = {
                    query = it
                    if (it.isBlank()) {
                        submittedQuery = ""
                    }
                },
                onSearchSubmitted = {
                    submittedQuery = it
                    viewModel.onSearchSubmitted(it)
                },
                recentSearches = recentSearches,
            )
        },
    ) { padding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding),
        ) {
            when (val state = uiState) {
                NearbyStopsUiState.Locating -> {
                    MessageContent(
                        message = "Buscando paradas cerca de vos...",
                        showProgress = true,
                    )
                }

                NearbyStopsUiState.PermissionDenied -> {
                    MessageContent(
                        title = "Permiso de ubicación rechazado",
                        message = "Usamos tu ubicación para mostrarte las paradas más cercanas.",
                        primaryLabel = "Permitir ubicación",
                        onPrimary = { attempt++ },
                        secondaryLabel = "Explorar el mapa manualmente",
                        onSecondary = {
                            viewModel.loadStopsForLocation(DEFAULT_MANUAL_LOCATION, false)
                        },
                    )
                }

                NearbyStopsUiState.LocationDisabled -> {
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
                }

                NearbyStopsUiState.LocationUnavailable -> {
                    MessageContent(
                        message = "No pudimos obtener tu ubicación. Intentá de nuevo.",
                        primaryLabel = "Reintentar",
                        onPrimary = { attempt++ },
                    )
                }

                is NearbyStopsUiState.Error -> {
                    MessageContent(
                        title = "Error de conexión",
                        message = state.message,
                        primaryLabel = "Reintentar",
                        onPrimary = { attempt++ },
                    )
                }

                is NearbyStopsUiState.Ready -> {
                    NearbyStopsContent(
                        state = state,
                        query = submittedQuery,
                        selectedStopId = selectedStopId,
                        focusTrigger = focusTrigger,
                        onStopClick = { stop ->
                            selectedStopId = stop.id
                            focusTrigger++
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
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(text = "Buscar parada o línea…") },
                leadingIcon = {
                    IconButton(onClick = { onSearchSubmitted(query) }) {
                        Icon(imageVector = Icons.Filled.Search, contentDescription = "Buscar")
                    }
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(imageVector = Icons.Filled.Close, contentDescription = "Limpiar búsqueda")
                        }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions =
                    KeyboardActions(
                        onSearch = { onSearchSubmitted(query) },
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
private fun NearbyStopsContent(
    state: NearbyStopsUiState.Ready,
    query: String,
    selectedStopId: String?,
    focusTrigger: Int,
    onStopClick: (BusStop) -> Unit,
) {
    val stops = remember(state.stops, query) { filterBusStops(state.stops, query) }
    val listState = rememberLazyListState()

    LaunchedEffect(selectedStopId) {
        if (selectedStopId != null) {
            val index = stops.indexOfFirst { it.id == selectedStopId }
            if (index >= 0) {
                listState.animateScrollToItem(index)
            }
        }
    }

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

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clipToBounds(),
        ) {
            OsmMapView(
                userLocation = state.center,
                stops = stops,
                selectedStopId = selectedStopId,
                focusTrigger = focusTrigger,
                onStopClick = onStopClick,
                modifier = Modifier.fillMaxSize(),
            )
        }

        Text(
            text = "Paradas cerca de vos",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        )

        when {
            query.isBlank() -> {
                Text(
                    text = "Ingresá una línea o parada en el buscador para ver los colectivos.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }

            stops.isEmpty() -> {
                Text(
                    text = "No encontramos paradas con esa búsqueda.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }

            else -> {
                LazyColumn(
                    state = listState,
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
