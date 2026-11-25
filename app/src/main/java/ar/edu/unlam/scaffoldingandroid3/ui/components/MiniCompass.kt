package ar.edu.unlam.scaffoldingandroid3.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp

@Composable
fun MiniCompass(
    targetBearing: Double,
    deviceAzimuth: Float,
    modifier: Modifier = Modifier,
) {
    val delta = (targetBearing - deviceAzimuth.toDouble() + 360.0) % 360.0
    val targetRotation = (if (delta > 180.0) delta - 360.0 else delta).toFloat()
    val animatedAngle by
        animateFloatAsState(
            targetValue = targetRotation,
            label = "miniCompassArrowAngle",
        )

    Surface(
        modifier = modifier.size(56.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary,
        shadowElevation = 6.dp,
        tonalElevation = 3.dp,
    ) {
        Box(
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowUpward,
                contentDescription = "Dirección a la parada",
                tint = MaterialTheme.colorScheme.primary,
                modifier =
                    Modifier
                        .size(30.dp)
                        .rotate(animatedAngle),
            )
        }
    }
}
