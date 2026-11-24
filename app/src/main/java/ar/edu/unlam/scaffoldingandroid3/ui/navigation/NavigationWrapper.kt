
package ar.edu.unlam.scaffoldingandroid3.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ar.edu.unlam.scaffoldingandroid3.ui.screens.NearbyStopsScreen
import kotlinx.serialization.Serializable

@Serializable
data object MapDestination

@Serializable
data object RoutesDestination

@Composable
fun NavigationWrapper() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = MapDestination,
    ) {
        composable<MapDestination> {
            NearbyStopsScreen()
        }
    }
}
