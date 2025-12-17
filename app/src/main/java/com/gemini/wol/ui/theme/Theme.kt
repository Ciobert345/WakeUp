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
    
    // Simple logic to generate readable content colors
    // In a real production app we'd use Material Dynamic Colors library, but here we approximate for a quick "Premium" feel.
    val isSeedLight = seedColor.luminance() > 0.5f
    val onPrimaryColor = if (isSeedLight) Color.Black else Color.White
    
    val primaryContainer = seedColor.copy(alpha = 0.2f).compositeOver(if(darkTheme) DarkBackground else Color.White)
    val onPrimaryContainer = seedColor // Use the seed itself for text on container if legible, or darker
    
    val secondary = seedColor.copy(alpha = 0.8f) // Slightly softer
    val tertiary = if (darkTheme) Color(0xFFEFB8C8) else Color(0xFF7D5260) // Complementary default or derived? Let's use generic.
    
    val lightScheme = lightColorScheme(
        primary = seedColor,
        onPrimary = onPrimaryColor,
        primaryContainer = primaryContainer,
        onPrimaryContainer = if (darkTheme) Color.White else Color.Black, // Simplified
        secondary = secondary,
        onSecondary = if (isSeedLight) Color.Black else Color.White,
        tertiary = tertiary
    )

    val darkScheme = darkColorScheme(
        primary = seedColor,
        onPrimary = onPrimaryColor,
        primaryContainer = seedColor.copy(alpha = 0.3f),
        onPrimaryContainer = Color.White, // Usually light text on dark container
        secondary = secondary,
        onSecondary = if (isSeedLight) Color.Black else Color.White,
        tertiary = tertiary,
        background = DarkBackground,
        surface = DarkSurface,
        onSurface = Color.White
    )

    val amoledScheme = darkColorScheme(
        primary = seedColor,
        onPrimary = onPrimaryColor,
        primaryContainer = seedColor.copy(alpha = 0.3f),
        onPrimaryContainer = Color.White,
        secondary = secondary,
        onSecondary = if (isSeedLight) Color.Black else Color.White,
        tertiary = tertiary,
        background = AmoledBackground,
        surface = AmoledSurface,
        onSurface = Color.White
    )
    
    val colorScheme = when (appTheme) {
        AppTheme.LIGHT -> lightScheme
        AppTheme.DARK -> darkScheme
        AppTheme.AMOLED -> amoledScheme
        AppTheme.SYSTEM -> {
             if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                 val context = LocalContext.current
                 if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
             } else {
                 if (darkTheme) darkScheme else lightScheme
             }
        }
    }
    
    // Logic to enforce custom accent.
    // Use the custom scheme UNLESS dynamic color is explicitly requested AND we are in System theme.
    // For this user request, we prioritize the manual accent color as "The User's Choice".
    val finalScheme = if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && appTheme == AppTheme.SYSTEM) {
        // If user wants system theme + dynamic, we give them dynamic. 
        // But if we want to support "System Theme" with "Custom Accent", we should disable dynamic in the call from MainActivity.
        colorScheme 
    } else {
        // Enforce the computed scheme
        when (appTheme) {
             AppTheme.AMOLED -> amoledScheme
             AppTheme.DARK -> darkScheme
             else -> if (darkTheme && appTheme == AppTheme.SYSTEM) darkScheme else lightScheme
        }
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            
            val isLight = (appTheme == AppTheme.LIGHT || (appTheme == AppTheme.SYSTEM && !darkTheme)) && appTheme != AppTheme.AMOLED
            
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
