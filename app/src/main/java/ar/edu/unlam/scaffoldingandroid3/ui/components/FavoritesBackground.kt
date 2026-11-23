package ar.edu.unlam.scaffoldingandroid3.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp

@Composable
fun FavoritesBackground(
    hasFavorites: Boolean,
    content: @Composable () -> Unit,
) {
    val colors = MaterialTheme.colorScheme

    val circleSize by animateDpAsState(
        targetValue = if (hasFavorites) 110.dp else 260.dp,
        label = "CircleSize",
    )

    val circleOpacity by animateFloatAsState(
        targetValue = if (hasFavorites) 0.35f else 1f,
        label = "CircleOpacity",
    )

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(colors.background),
    ) {
        Box(
            modifier =
                Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 35.dp, y = (-30).dp)
                    .size(circleSize)
                    .alpha(circleOpacity)
                    .background(
                        color = colors.primaryContainer,
                        shape = CircleShape,
                    ),
        )

        Box(
            modifier =
                Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = (-45).dp, y = 45.dp)
                    .size(circleSize)
                    .alpha(circleOpacity)
                    .background(
                        color = colors.secondaryContainer,
                        shape = CircleShape,
                    ),
        )

        content()
    }
}
