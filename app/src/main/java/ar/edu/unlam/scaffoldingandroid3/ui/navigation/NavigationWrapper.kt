
package ar.edu.unlam.scaffoldingandroid3.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ar.edu.unlam.scaffoldingandroid3.ui.components.BottomTab
import ar.edu.unlam.scaffoldingandroid3.ui.components.VoyEnBondiBottomBar
import ar.edu.unlam.scaffoldingandroid3.ui.screens.FavoriteBusStopsScreen
import ar.edu.unlam.scaffoldingandroid3.ui.screens.HomeScreen
import ar.edu.unlam.scaffoldingandroid3.ui.screens.MapScreen
import ar.edu.unlam.scaffoldingandroid3.ui.screens.RoutesScreen
import kotlinx.serialization.Serializable

@Serializable
data object MapDestination

@Serializable
data object RoutesDestination

@Composable
fun NavigationWrapper() {
    val navController = rememberNavController()

    val backStackEntry =
        navController.currentBackStackEntryAsState().value

    val selectedTab =
        when (
            backStackEntry?.destination?.route
        ) {
            MapDestination::class.qualifiedName -> BottomTab.MAP
            RoutesDestination::class.qualifiedName -> BottomTab.ROUTES
            FavoriteBusStops::class.qualifiedName -> BottomTab.FAVORITES
            else -> BottomTab.HOME
        }

    // Desde cualquier pestaña secundaria, Atrás vuelve a Inicio.
    BackHandler(enabled = selectedTab != BottomTab.HOME) {
        navController.popBackStack(Home, inclusive = false)
    }

    Scaffold(
        bottomBar = {
            VoyEnBondiBottomBar(
                selectedTab = selectedTab,
                onTabSelected = { tab ->
                    if (tab != selectedTab) {
                        when (tab) {
                            BottomTab.HOME -> {
                                navController.popBackStack(
                                    Home,
                                    inclusive = false,
                                )
                            }

                            BottomTab.MAP -> {
                                navController.navigate(MapDestination) {
                                    popUpTo(Home) {
                                        inclusive = false
                                    }
                                    launchSingleTop = true
                                }
                            }

                            BottomTab.ROUTES -> {
                                navController.navigate(RoutesDestination) {
                                    popUpTo(Home) {
                                        inclusive = false
                                    }
                                    launchSingleTop = true
                                }
                            }

                            BottomTab.FAVORITES -> {
                                navController.navigate(FavoriteBusStops) {
                                    popUpTo(Home) {
                                        inclusive = false
                                    }
                                    launchSingleTop = true
                                }
                            }
                        }
                    }
                },
            )
        },
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Home,
            modifier = Modifier.padding(paddingValues),
        ) {
            composable<Home> {
                HomeScreen(
                    navigateToFavoriteBusStop = {
                        navController.navigate(FavoriteBusStops) {
                            popUpTo(Home) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
                    },
                )
            }

            composable<MapDestination> {
                MapScreen()
            }

            composable<RoutesDestination> {
                RoutesScreen()
            }

            composable<FavoriteBusStops> {
                FavoriteBusStopsScreen(
                    navigateToHome = {
                        navController.popBackStack(
                            Home,
                            inclusive = false,
                        )
                    },
                )
            }
        }
    }
}
