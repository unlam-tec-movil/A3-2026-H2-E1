
package ar.edu.unlam.scaffoldingandroid3.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors =
    lightColorScheme(
        primary = Petroleum,
        onPrimary = AppSurface,
        primaryContainer = SurfaceVariantLight,
        onPrimaryContainer = Petroleum,
        secondary = Turquoise,
        onSecondary = TextPrimary,
        tertiary = Amber,
        background = AppBackground,
        onBackground = TextPrimary,
        surface = AppSurface,
        onSurface = TextPrimary,
        surfaceVariant = SurfaceVariantLight,
        onSurfaceVariant = TextSecondary,
        error = ErrorRed,
    )

private val DarkColors =
    darkColorScheme(
        primary = PetroleumDark,
        onPrimary = DarkBackground,
        secondary = TurquoiseDark,
        tertiary = Amber,
        background = DarkBackground,
        onBackground = DarkText,
        surface = DarkSurface,
        onSurface = DarkText,
        error = FavoriteRed,
    )

@Composable
fun VoyEnBondiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
