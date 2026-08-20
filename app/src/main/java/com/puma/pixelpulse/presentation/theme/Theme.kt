package com.puma.pixelpulse.presentation.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.puma.pixelpulse.data.local.UserPreferences

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF80DEEA),
    onPrimary = Color(0xFF00363A),
    primaryContainer = Color(0xFF005056),
    onPrimaryContainer = Color(0xFF97F0F5),
    secondary = Color(0xFFCDB4FF),
    onSecondary = Color(0xFF381E72),
    secondaryContainer = Color(0xFF4F378B),
    onSecondaryContainer = Color(0xFFE9DDFF),
    tertiary = Color(0xFFEFBC9C),
    onTertiary = Color(0xFF4A2710),
    tertiaryContainer = Color(0xFF663D26),
    onTertiaryContainer = Color(0xFFFFD8C1),
    background = Color(0xFF0F1419),
    onBackground = Color(0xFFE1E3E5),
    surface = Color(0xFF151A20),
    onSurface = Color(0xFFE1E3E5),
    surfaceVariant = Color(0xFF1E2530),
    onSurfaceVariant = Color(0xFFC3C7CF),
    outline = Color(0xFF3D4450),
    surfaceContainerLow = Color(0xFF12171D),
    surfaceContainer = Color(0xFF1A2028),
    surfaceContainerHigh = Color(0xFF222830),
    surfaceContainerHighest = Color(0xFF2A3038)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF006A6A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF6FF6F6),
    onPrimaryContainer = Color(0xFF002020),
    secondary = Color(0xFF5B5390),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE3DFFF),
    onSecondaryContainer = Color(0xFF180F5C),
    tertiary = Color(0xFF7C5739),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDCC2),
    onTertiaryContainer = Color(0xFF2D1700),
    background = Color(0xFFF8FAFB),
    onBackground = Color(0xFF191C1D),
    surface = Color(0xFFF8FAFB),
    onSurface = Color(0xFF191C1D),
    surfaceVariant = Color(0xFFDAE5E5),
    onSurfaceVariant = Color(0xFF3F4949),
    outline = Color(0xFF6F7979),
    surfaceContainerLow = Color(0xFFF0F3F4),
    surfaceContainer = Color(0xFFF2F5F6),
    surfaceContainerHigh = Color(0xFFECF0F1),
    surfaceContainerHighest = Color(0xFFE7EAEC)
)

private val AmoledColorScheme = darkColorScheme(
    primary = Color(0xFF80DEEA),
    onPrimary = Color(0xFF00363A),
    primaryContainer = Color(0xFF005056),
    onPrimaryContainer = Color(0xFF97F0F5),
    secondary = Color(0xFFCDB4FF),
    onSecondary = Color(0xFF381E72),
    secondaryContainer = Color(0xFF4F378B),
    onSecondaryContainer = Color(0xFFE9DDFF),
    tertiary = Color(0xFFEFBC9C),
    onTertiary = Color(0xFF4A2710),
    tertiaryContainer = Color(0xFF663D26),
    onTertiaryContainer = Color(0xFFFFD8C1),
    background = Color.Black,
    onBackground = Color(0xFFE1E3E5),
    surface = Color.Black,
    onSurface = Color(0xFFE1E3E5),
    surfaceVariant = Color(0xFF0A0A0A),
    onSurfaceVariant = Color(0xFFC3C7CF),
    outline = Color(0xFF2A2A2A),
    surfaceContainerLow = Color(0xFF050505),
    surfaceContainer = Color.Black,
    surfaceContainerHigh = Color(0xFF0A0A0A),
    surfaceContainerHighest = Color(0xFF101010)
)

@Composable
fun PixelPulseTheme(
    themeMode: UserPreferences.ThemeMode = UserPreferences.ThemeMode.SYSTEM,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val isDarkTheme = when (themeMode) {
        UserPreferences.ThemeMode.LIGHT -> false
        UserPreferences.ThemeMode.DARK -> true
        UserPreferences.ThemeMode.AMOLED -> true
        UserPreferences.ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        themeMode == UserPreferences.ThemeMode.AMOLED && isDarkTheme -> AmoledColorScheme
        isDarkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
