package ar.edu.unlam.scaffoldingandroid3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import ar.edu.unlam.scaffoldingandroid3.ui.navigation.NavigationWrapper
import ar.edu.unlam.scaffoldingandroid3.ui.theme.ScaffoldingAndroid3Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ScaffoldingAndroid3Theme {
                NavigationWrapper()
            }
        }
    }
}
