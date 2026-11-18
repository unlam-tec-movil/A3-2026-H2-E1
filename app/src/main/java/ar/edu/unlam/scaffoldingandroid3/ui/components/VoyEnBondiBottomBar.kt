
package ar.edu.unlam.scaffoldingandroid3.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

enum class BottomTab(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    HOME("Inicio", Icons.Filled.Home, Icons.Outlined.Home),
    MAP("Mapa", Icons.Filled.Map, Icons.Outlined.Map),
    ROUTES("Recorridos", Icons.Filled.Route, Icons.Outlined.Route),
    FAVORITES(
        "Favoritos",
        Icons.Filled.Favorite,
        Icons.Outlined.FavoriteBorder,
    ),
}

@Composable
fun VoyEnBondiBottomBar(
    selectedTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit,
) {
    NavigationBar {
        BottomTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = if (selectedTab == tab) {
                            tab.selectedIcon
                        } else {
                            tab.unselectedIcon
                        },
                        contentDescription = tab.label,
                    )
                },
                label = { Text(tab.label) },
                alwaysShowLabel = true,
            )
        }
    }
}
