
package ar.edu.unlam.scaffoldingandroid3.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ar.edu.unlam.scaffoldingandroid3.ui.screens.CompassScreen
import ar.edu.unlam.scaffoldingandroid3.ui.screens.NearbyStopsScreen
import kotlinx.serialization.Serializable

@Serializable
data object MapDestination

@Composable
fun NavigationWrapper() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = MapDestination,
    ) {
        composable<MapDestination> {
            NearbyStopsScreen(
                onNavigateToCompass = { stop ->
                    navController.navigate(
                        Compass(
                            stopName = stop.name,
                            latitude = stop.locationData.latitude.value,
                            longitude = stop.locationData.longitude.value,
                        ),
                    )
                },
            )
        }

        composable<Compass> {
            CompassScreen(
                onBack = { navController.popBackStack() },
            )
        }
    }
}
