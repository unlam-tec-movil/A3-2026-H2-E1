package ar.edu.unlam.scaffoldingandroid3.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ar.edu.unlam.scaffoldingandroid3.ui.screens.FavoriteBusStopsScreen
import ar.edu.unlam.scaffoldingandroid3.ui.screens.HomeScreen

@Composable
fun NavigationWrapper() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Home) {
        /* Se crea un composable<> por cada Screen que se quiere navegar
         * Entre <> creamos un serializable (Ctrl click en "Home" para navegar hacia el archivo)
         * En la Screen Composable creamos una lambda para darle acá su comportamiento
         * de navegación (Ctrl Click en "NavigateToHome" para ver el ejemplo)
         * Importante que: Por cada Screen Composable crearle su propio Scaffold y pasarle y pasarle el padding a la Column contenedora
         * Modifier.padding(it)*/

        composable<Home> {
            /*Siempre va a tener la misma estructura:
             * El nombre de mi Screen Composable
             * Dentro el nombre de la lambda que contiene el comportamiento de navegación
             * "De la HomeScreen quiero navegar hacia "FavoriteBusStopsScreen"*/
            HomeScreen(navigateToFavoriteBusStop = { navController.navigate(FavoriteBusStops) })
        }

        composable<FavoriteBusStops> {
            FavoriteBusStopsScreen(navigateToHome = { navController.navigate(Home) })
        }
    }
}
