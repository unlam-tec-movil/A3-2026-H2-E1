package ar.edu.unlam.scaffoldingandroid3.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun HomeScreen(navigateToFavoriteBusStop: () -> Unit){
    Scaffold() {
        Column(Modifier.fillMaxSize().padding(it)) {
            Text("HOME")
            Button(onClick = navigateToFavoriteBusStop) {
                Text("Nav")
            }
        }
    }
}