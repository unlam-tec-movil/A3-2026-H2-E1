package ar.edu.unlam.scaffoldingandroid3.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ar.edu.unlam.scaffoldingandroid3.R
import ar.edu.unlam.scaffoldingandroid3.domain.model.BusStop
import ar.edu.unlam.scaffoldingandroid3.domain.model.FavoriteStopSnapshot
import ar.edu.unlam.scaffoldingandroid3.domain.model.Latitude
import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationData
import ar.edu.unlam.scaffoldingandroid3.domain.model.Longitude
import ar.edu.unlam.scaffoldingandroid3.domain.model.SearchHistory
import ar.edu.unlam.scaffoldingandroid3.domain.model.distanceMetersTo
import ar.edu.unlam.scaffoldingandroid3.ui.components.BusStopCard
import ar.edu.unlam.scaffoldingandroid3.ui.components.LocationPermissionManager
import ar.edu.unlam.scaffoldingandroid3.ui.components.MessageContent
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
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarScope = rememberCoroutineScope()
    val favoriteAddedMessage = stringResource(favoriteActionMessageRes(wasFavorite = false))
    val favoriteRemovedMessage = stringResource(favoriteActionMessageRes(wasFavorite = true))

    var selectedTab by rememberSaveable {
        mutableStateOf(HomeTab.Map)
    }

    var selectedFavorite by remember {
        mutableStateOf<FavoriteStopSnapshot?>(null)
    }

    var query by rememberSaveable {
        mutableStateOf("")
    }

    var resolveRequest by remember { mutableIntStateOf(0) }

    LocationPermissionManager(
        viewModel = viewModel,
        onRequestPermissionsResult = {},
        resolveRequest = resolveRequest,
    )

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
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
                                message = stringResource(R.string.nearby_loading),
                                showProgress = true,
                            )
                        }

                        MapUiState.PermissionDenied -> {
                            MessageContent(
                                title = stringResource(R.string.nearby_permission_denied_title),
                                message = stringResource(R.string.nearby_permission_denied_message),
                                primaryLabel = stringResource(R.string.nearby_allow_location),
                                onPrimary = {
                                    resolveRequest++
                                },
                                secondaryLabel = stringResource(R.string.nearby_explore_map_manually),
                                onSecondary = {
                                    viewModel.exploreManually()
                                },
                            )
                        }

                        MapUiState.LocationDisabled -> {
                            MessageContent(
                                title = stringResource(R.string.nearby_location_disabled_title),
                                message = stringResource(R.string.nearby_location_disabled_message),
                                primaryLabel = stringResource(R.string.nearby_open_settings),
                                onPrimary = {
                                    context.startActivity(
                                        Intent(
                                            Settings.ACTION_LOCATION_SOURCE_SETTINGS,
                                        ),
                                    )
                                },
                                secondaryLabel = stringResource(R.string.nearby_retry),
                                onSecondary = {
                                    resolveRequest++
                                },
                            )
                        }

                        MapUiState.LocationUnavailable -> {
                            MessageContent(
                                message = stringResource(R.string.nearby_location_unavailable),
                                primaryLabel = stringResource(R.string.nearby_retry),
                                onPrimary = {
                                    resolveRequest++
                                },
                                secondaryLabel = stringResource(R.string.nearby_explore_manually),
                                onSecondary = {
                                    viewModel.exploreManually()
                                },
                            )
                        }

                        is MapUiState.Error -> {
                            MessageContent(
                                title = stringResource(R.string.nearby_connection_error_title),
                                message = state.message,
                                primaryLabel = stringResource(R.string.nearby_retry),
                                onPrimary = {
                                    resolveRequest++
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
                                onClusterClick = { clusterStops ->
                                    selectedFavorite = null
                                    viewModel.filterByCluster(
                                        clusterStops.map { it.stopId }.toSet(),
                                    )
                                },
                                onClearClusterFilter = {
                                    viewModel.clearClusterFilter()
                                },
                                onOrientClick = onNavigateToCompass,
                                favoriteIds = favoriteIds,
                                onToggleFavorite = { stopId ->
                                    val stop =
                                        state.busStops.firstOrNull {
                                            it.stopId == stopId
                                        }

                                    if (stop != null) {
                                        val wasFavorite = stopId in favoriteIds

                                        favoriteViewModel.toggleFavorite(stop)

                                        snackbarScope.launch {
                                            snackbarHostState.currentSnackbarData?.dismiss()

                                            snackbarHostState.showSnackbar(
                                                message =
                                                    if (wasFavorite) {
                                                        favoriteRemovedMessage
                                                    } else {
                                                        favoriteAddedMessage
                                                    },
                                                duration = SnackbarDuration.Short,
                                            )
                                        }
                                    }
                                },
                                selectedFavorite = selectedFavorite,
                            )
                        }
                    }
                }

                HomeTab.Notices -> {
                    NoticeScreen(
                        onStopSelected = { stopId ->
                            viewModel.selectBusStop(stopId)
                            selectedTab = HomeTab.Map
                        },
                    )
                }

                HomeTab.Favorites -> {
                    FavoriteBusStopsScreen(
                        onStopSelected = { stop ->
                            selectedFavorite = stop
                            viewModel.selectBusStop(stop.stopId)
                            selectedTab = HomeTab.Map
                        },
                        onExploreMap = {
                            selectedTab = HomeTab.Map
                        },
                        viewModel = favoriteViewModel,
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
                text = stringResource(R.string.nearby_home_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )

            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(stringResource(R.string.nearby_search_placeholder))
                },
                leadingIcon = {
                    IconButton(onClick = { onSearchSubmitted(query) }) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = stringResource(R.string.nearby_search_description),
                        )
                    }
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = stringResource(R.string.nearby_clear_search_description),
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
                    Text(stringResource(tab.labelRes))
                },
            )
        }
    }
}

private enum class HomeTab(
    @StringRes val labelRes: Int,
    val icon: ImageVector,
) {
    Map(
        labelRes = R.string.nearby_tab_map,
        icon = Icons.Filled.Place,
    ),
    Notices(
        labelRes = R.string.nearby_tab_notices,
        icon = Icons.Filled.Notifications,
    ),
    Favorites(
        labelRes = R.string.nearby_tab_favorites,
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
    onClusterClick: (List<BusStop>) -> Unit = {},
    onClearClusterFilter: () -> Unit = {},
    selectedFavorite: FavoriteStopSnapshot? = null,
) {
    val scaffoldState = rememberBottomSheetScaffoldState()
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val photoReportViewModel: PhotoReportViewModel = hiltViewModel()
    val photoReportState by photoReportViewModel.uiState.collectAsState()

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var userFocusTrigger by remember { mutableIntStateOf(0) }

    val handleSearchSubmitted: (String) -> Unit = { submittedQuery ->
        onSearchSubmitted(submittedQuery)
        keyboardController?.hide()
        focusManager.clearFocus()
        coroutineScope.launch {
            scaffoldState.bottomSheetState.expand()
        }
    }

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
            state.filteredStops +
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
            state.filteredStops
        }

    val selectedForReport =
        mapStops.firstOrNull {
            it.stopId == state.selectedStopId
        }

    LaunchedEffect(state.selectedStopId) {
        if (state.selectedStopId != null) {
            val index =
                state.filteredStops.indexOfFirst {
                    it.stopId == state.selectedStopId
                }

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
                                text = stringResource(R.string.nearby_approximate_location),
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
                                text = stringResource(R.string.nearby_offline_cache),
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
                                text = stringResource(R.string.nearby_demo_data),
                                modifier = Modifier.padding(8.dp),
                            )
                        }
                    }

                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = 16.dp,
                                    vertical = 8.dp,
                                ),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.nearby_stops_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )

                        if (selectedForReport != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Button(
                                    onClick = {
                                        onOrientClick(selectedForReport)
                                    },
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Text(stringResource(R.string.nearby_orient_me))
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

                    if (state.clusterFilterStopIds != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = MaterialTheme.shapes.small,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        horizontal = 16.dp,
                                        vertical = 4.dp,
                                    ),
                        ) {
                            Row(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            horizontal = 12.dp,
                                            vertical = 6.dp,
                                        ),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text =
                                        stringResource(
                                            R.string.nearby_cluster_group,
                                            state.filteredStops.size,
                                        ),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Medium,
                                )

                                IconButton(
                                    onClick = onClearClusterFilter,
                                    modifier = Modifier.size(24.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription =
                                            stringResource(R.string.nearby_clear_cluster_filter_description),
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                            }
                        }
                    }

                    when {
                        state.busStops.isEmpty() -> {
                            Text(
                                text = stringResource(R.string.nearby_no_stops),
                                modifier = Modifier.padding(16.dp),
                            )
                        }

                        state.filteredStops.isEmpty() -> {
                            Text(
                                text = stringResource(R.string.nearby_no_search_results),
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
                selectedClusterStopIds = state.clusterFilterStopIds,
                userFocusTrigger = userFocusTrigger,
                onStopClick = { stop ->
                    onStopClick(stop)

                    coroutineScope.launch {
                        scaffoldState.bottomSheetState.expand()
                    }
                },
                onClusterClick = { clusterStops ->
                    onClusterClick(clusterStops)

                    coroutineScope.launch {
                        scaffoldState.bottomSheetState.expand()
                    }
                },
                modifier = Modifier.fillMaxSize(),
            )

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter),
            ) {
                HomeHeader(
                    query = query,
                    onQueryChange = onQueryChange,
                    onSearchSubmitted = handleSearchSubmitted,
                    recentSearches = recentSearches,
                )

                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(
                                end = 16.dp,
                                top = 12.dp,
                            ),
                ) {
                    FloatingActionButton(
                        onClick = {
                            userFocusTrigger++
                        },
                        modifier = Modifier.align(Alignment.TopEnd),
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Place,
                            contentDescription =
                                stringResource(R.string.nearby_center_on_location_description),
                        )
                    }
                }
            }
        }
    }

    PhotoReportDialogs(
        state = photoReportState,
        onConfirm = photoReportViewModel::saveReport,
        onDismiss = photoReportViewModel::onCancelled,
        onAcknowledge = photoReportViewModel::onAcknowledged,
    )
}

@StringRes
internal fun favoriteActionMessageRes(wasFavorite: Boolean): Int =
    if (wasFavorite) {
        R.string.favorite_removed_message
    } else {
        R.string.favorite_added_message
    }
