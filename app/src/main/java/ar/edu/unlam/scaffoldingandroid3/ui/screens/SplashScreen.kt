
package ar.edu.unlam.scaffoldingandroid3.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ar.edu.unlam.scaffoldingandroid3.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(onAnimationFinished: () -> Unit) {
    val busScale = remember { Animatable(0.65f) }
    val busOpacity = remember { Animatable(0f) }
    val textOpacity = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            busScale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 850),
            )
        }

        launch {
            busOpacity.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 650),
            )
        }

        delay(350)

        textOpacity.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 500),
        )

        delay(550)
        onAnimationFinished()
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color(0xFFDDF4FF)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.voy_en_bondi_bus),
            contentDescription = "Colectivo de Voy en Bondi",
            modifier =
                Modifier
                    .size(220.dp)
                    .scale(busScale.value)
                    .alpha(busOpacity.value),
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Voy en Bondi",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF10345A),
            modifier = Modifier.alpha(textOpacity.value),
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Tu compañero de viaje",
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF356A91),
            textAlign = TextAlign.Center,
            modifier = Modifier.alpha(textOpacity.value),
        )
    }
}
