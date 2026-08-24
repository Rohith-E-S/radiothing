package com.radiothing.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val LocalRadioColors = staticCompositionLocalOf { RadioColors }
val LocalRadioTypography = staticCompositionLocalOf { RadioTypography }
val LocalRadioShapes = staticCompositionLocalOf { RadioShapes }

private val DarkColorScheme = darkColorScheme(
    primary = BrightRed,
    secondary = LiveRed,
    tertiary = SignalAmber,
    background = PureBlack,
    surface = Panel,
    surfaceVariant = GridLine,
    outline = GridLine,
    outlineVariant = Hairline,
    onPrimary = TextWhite100,
    onSecondary = TextWhite100,
    onBackground = TextWhite100,
    onSurface = TextWhite100,
    onSurfaceVariant = TextWhite70
)

@Composable
fun RadioThingTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = PureBlack.toArgb()
            window.navigationBarColor = PureBlack.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    CompositionLocalProvider(
        LocalRadioColors provides RadioColors,
        LocalRadioTypography provides RadioTypography,
        LocalRadioShapes provides RadioShapes
    ) {
        MaterialTheme(
            colorScheme = DarkColorScheme,
            typography = RadioTypography,
            content = content
        )
    }
}
