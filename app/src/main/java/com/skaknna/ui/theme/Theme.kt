package com.skaknna.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val FantasyWoodColorScheme = darkColorScheme(
    primary = GoldenYellow,
    onPrimary = WoodDark,
    primaryContainer = WoodMedium,
    onPrimaryContainer = GoldenYellow,
    
    secondary = LeafGreen,
    onSecondary = WoodDark,
    secondaryContainer = WoodDark,
    onSecondaryContainer = LeafGreen,
    
    tertiary = WoodLight,
    
    background = androidx.compose.ui.graphics.Color.Transparent,
    onBackground = WarmWhite,
    
    surface = WoodDark,
    onSurface = WarmWhite,
    surfaceVariant = WoodMedium,
    onSurfaceVariant = WarmWhite
)

@Composable
fun SkaknnaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> FantasyWoodColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}