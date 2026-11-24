package ar.edu.unlam.scaffoldingandroid3.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ar.edu.unlam.scaffoldingandroid3.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

private const val BUS_SCALE_DURATION_MS = 850
private const val BUS_OPACITY_DURATION_MS = 650
private const val TEXT_DELAY_MS = 350
private const val TEXT_OPACITY_DURATION_MS = 500
private const val FINISH_DELAY_MS = 550
private const val BUS_SIZE_DP = 220
private val BACKGROUND_COLOR = Color(0xFFDDF4FF)
private val TITLE_COLOR = Color(0xFF10345A)
private val SUBTITLE_COLOR = Color(0xFF356A91)

@Composable
fun SplashScreen(onAnimationFinished: () -> Unit) {
    val busScale = remember { Animatable(0.65f) }
    val busOpacity = remember { Animatable(0f) }
    val textOpacity = remember { Animatable(0f) }

    SplashScreenAnimation(busScale, busOpacity, textOpacity, onAnimationFinished())
    SplashScreenContent(busScale, busOpacity, textOpacity)
}

@Composable
fun SplashScreenAnimation(
    busScale: Animatable<Float, AnimationVector1D>,
    busOpacity: Animatable<Float, AnimationVector1D>,
    textOpacity: Animatable<Float, AnimationVector1D>,
    onAnimationFinished: Unit,
) {
    LaunchedEffect(Unit) {
        launch {
            busScale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = BUS_SCALE_DURATION_MS),
            )
        }

        launch {
            busOpacity.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = BUS_OPACITY_DURATION_MS),
            )
        }

        delay(TEXT_DELAY_MS.milliseconds)

        textOpacity.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = TEXT_OPACITY_DURATION_MS),
        )

        delay(FINISH_DELAY_MS.milliseconds)

        onAnimationFinished.run { }
    }
}

@Composable
private fun SplashScreenContent(
    busScale: Animatable<Float, AnimationVector1D>,
    busOpacity: Animatable<Float, AnimationVector1D>,
    textOpacity: Animatable<Float, AnimationVector1D>,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(BACKGROUND_COLOR),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.voy_en_bondi_bus),
            contentDescription = stringResource(R.string.splash_bus_content_description),
            modifier =
                Modifier
                    .size(BUS_SIZE_DP.dp)
                    .scale(busScale.value)
                    .alpha(busOpacity.value),
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.splash_title),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = TITLE_COLOR,
            modifier = Modifier.alpha(textOpacity.value),
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.splash_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = SUBTITLE_COLOR,
            textAlign = TextAlign.Center,
            modifier = Modifier.alpha(textOpacity.value),
        )
    }
}
