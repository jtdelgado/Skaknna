package com.skaknna.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.Shapes
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

// ============================================================================
// DARK THEME COLOR SCHEME (Primary)
// ============================================================================
private val ModernClassicDarkColorScheme = darkColorScheme(
    primary = PrimaryGreen,
    onPrimary = WarmWhite,
    primaryContainer = PrimaryGreen,
    onPrimaryContainer = WarmWhite,
    
    secondary = PrimaryGold,
    onSecondary = DeepEspresso,
    secondaryContainer = PrimaryGold,
    onSecondaryContainer = DeepEspresso,
    
    tertiary = LeafGreen,
    onTertiary = WarmWhite,
    tertiaryContainer = LeafGreen,
    onTertiaryContainer = SurfaceDark,
    
    error = ErrorColor,
    onError = WhiteColor,
    errorContainer = ErrorColor,
    onErrorContainer = WhiteColor,
    
    background = SurfaceDark,
    onBackground = WarmWhite,
    
    surface = SurfaceGreen,
    onSurface = WarmWhite,
    surfaceVariant = DeepEspresso,
    onSurfaceVariant = WarmWhite,
    
    outline = OutlineColor,
    outlineVariant = OutlineColor,
    
    scrim = Color(0x00000000)
)

// ============================================================================
// LIGHT THEME COLOR SCHEME (Infrastructure for future use)
// ============================================================================
private val ModernClassicLightColorScheme = lightColorScheme(
    primary = Color(0xFF2D6A4F),
    onPrimary = WhiteColor,
    primaryContainer = Color(0xFF2D6A4F),
    onPrimaryContainer = WhiteColor,
    
    secondary = Color(0xFFD4A574),
    onSecondary = WhiteColor,
    secondaryContainer = Color(0xFFD4A574),
    onSecondaryContainer = Color(0xFF3D2510),
    
    tertiary = LeafGreen,
    onTertiary = WhiteColor,
    tertiaryContainer = LeafGreen,
    onTertiaryContainer = WhiteColor,
    
    error = ErrorColor,
    onError = WhiteColor,
    errorContainer = ErrorColor,
    onErrorContainer = WhiteColor,
    
    background = Color(0xFFFAFAF8),
    onBackground = Color(0xFF1A1A1A),
    
    surface = Color(0xFFFAFAF8),
    onSurface = Color(0xFF1A1A1A),
    surfaceVariant = Color(0xFFEEEEEC),
    onSurfaceVariant = Color(0xFF5D4037),
    
    outline = Color(0xFF998E8A),
    outlineVariant = Color(0xFFD9C7C3),
    
    scrim = BlackColor
)

private val SkaknnaShapes = Shapes(
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun SkaknnaTheme(
    darkTheme: Boolean = true, // Forced dark theme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> ModernClassicDarkColorScheme
        else -> ModernClassicLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MontserratTypography,
        shapes = SkaknnaShapes,
        content = content
    )
}