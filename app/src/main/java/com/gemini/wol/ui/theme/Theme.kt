package com.gemini.wol.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.gemini.wol.data.repository.AppTheme

// Premium Colors
val PrimaryColor = Color(0xFF6200EE)
val SecondaryColor = Color(0xFF03DAC6)
val TertiaryColor = Color(0xFFFF0266)

val DarkPrimary = Color(0xFFBB86FC)
val DarkSecondary = Color(0xFF03DAC6)
val DarkBackground = Color(0xFF121212)
val DarkSurface = Color(0xFF1E1E1E)

val AmoledBackground = Color(0xFF000000)
val AmoledSurface = Color(0xFF000000)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryColor,
    secondary = SecondaryColor,
    tertiary = TertiaryColor
    // other colors default
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    secondary = DarkSecondary,
    background = DarkBackground,
    surface = DarkSurface,
)

private val AmoledColorScheme = darkColorScheme(
    primary = DarkPrimary,
    secondary = DarkSecondary,
    background = AmoledBackground,
    surface = AmoledSurface, // Surface is also black for AMOLED
    onSurface = Color.White
)

@Composable
fun WakeOnLanSchedulerTheme(
    appTheme: AppTheme = AppTheme.SYSTEM,
    accentColor: Int = 0xFF6200EE.toInt(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val darkTheme = isSystemInDarkTheme()
    
    // Generate palette from accent color
    val seedColor = Color(accentColor)
    val isDark = appTheme == AppTheme.DARK || appTheme == AppTheme.AMOLED || (appTheme == AppTheme.SYSTEM && isSystemInDarkTheme())
    
    // Calculate a "Visible Primary" for containers/indicators in dark modes
    // If the accent is too dark, we lighten it for visibility on dark backgrounds
    val visiblePrimary = if (isDark && seedColor.luminance() < 0.2f) {
        seedColor.copy(alpha = 0.4f).compositeOver(Color.White) // Lighten significantly
    } else {
        seedColor
    }
    
    val isSeedLight = seedColor.luminance() > 0.5f
    val onPrimaryColor = if (isSeedLight) Color.Black else Color.White
    
    // Better container colors
    val primaryContainerLight = seedColor.copy(alpha = 0.15f).compositeOver(Color.White)
    val onPrimaryContainerLight = if (isSeedLight) seedColor.copy(alpha = 0.9f) else seedColor
    
    val currentBg = if (appTheme == AppTheme.AMOLED) AmoledBackground else DarkBackground
    val primaryContainerDark = visiblePrimary.copy(alpha = 0.25f).compositeOver(currentBg)
    
    val secondary = seedColor.copy(alpha = 0.7f)
    val tertiary = if (isDark) Color(0xFFEFB8C8) else Color(0xFF7D5260)

    val lightScheme = lightColorScheme(
        primary = seedColor,
        onPrimary = onPrimaryColor,
        primaryContainer = primaryContainerLight,
        onPrimaryContainer = onPrimaryContainerLight,
        secondary = secondary,
        onSecondary = if (isSeedLight) Color.Black else Color.White,
        tertiary = tertiary
    )

    val darkScheme = darkColorScheme(
        primary = visiblePrimary,
        onPrimary = if (visiblePrimary.luminance() > 0.5f) Color.Black else Color.White,
        primaryContainer = primaryContainerDark,
        onPrimaryContainer = if (visiblePrimary.luminance() < 0.5f) Color.White else seedColor,
        secondary = secondary,
        onSecondary = if (isSeedLight) Color.Black else Color.White,
        tertiary = tertiary,
        background = DarkBackground,
        surface = DarkSurface,
        onSurface = Color.White
    )

    val amoledScheme = darkColorScheme(
        primary = visiblePrimary,
        onPrimary = if (visiblePrimary.luminance() > 0.5f) Color.Black else Color.White,
        primaryContainer = visiblePrimary.copy(alpha = 0.25f).compositeOver(AmoledBackground),
        onPrimaryContainer = if (visiblePrimary.luminance() < 0.5f) Color.White else seedColor,
        secondary = secondary,
        onSecondary = if (isSeedLight) Color.Black else Color.White,
        tertiary = tertiary,
        background = AmoledBackground,
        surface = AmoledSurface,
        onSurface = Color.White
    )
    
    val finalScheme = when (appTheme) {
        AppTheme.LIGHT -> lightScheme
        AppTheme.DARK -> darkScheme
        AppTheme.AMOLED -> amoledScheme
        AppTheme.SYSTEM -> if (isSystemInDarkTheme()) darkScheme else lightScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            
            val surfaceLuminance = finalScheme.surface.luminance()
            val isLight = surfaceLuminance > 0.5f && appTheme != AppTheme.AMOLED
            
            WindowCompat.setDecorFitsSystemWindows(window, false) // Enable Edge-to-Edge
            
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = isLight
            controller.isAppearanceLightNavigationBars = isLight
        }
    }

    MaterialTheme(
        colorScheme = finalScheme,
        typography = Typography,
        content = content
    )
}
