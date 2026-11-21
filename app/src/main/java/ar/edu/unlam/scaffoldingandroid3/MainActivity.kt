
package ar.edu.unlam.scaffoldingandroid3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import ar.edu.unlam.scaffoldingandroid3.ui.navigation.NavigationWrapper
import ar.edu.unlam.scaffoldingandroid3.ui.screens.SplashScreen
import ar.edu.unlam.scaffoldingandroid3.ui.theme.VoyEnBondiTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            VoyEnBondiTheme {
                var showSplash by rememberSaveable {
                    mutableStateOf(true)
                }

                if (showSplash) {
                    SplashScreen(
                        onAnimationFinished = {
                            showSplash = false
                        },
                    )
                } else {
                    NavigationWrapper()
                }
            }
        }
    }
}
