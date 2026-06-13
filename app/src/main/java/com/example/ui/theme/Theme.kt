package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = CosmicSage,
    onPrimary = DeepForest,
    primaryContainer = CosmicDarkCard,
    onPrimaryContainer = CosmicSage,
    secondary = CosmicTeal,
    onSecondary = DeepTeal,
    secondaryContainer = CosmicDarkSurface,
    onSecondaryContainer = CosmicTeal,
    background = CosmicDarkBg,
    onBackground = CosmicDarkText,
    surface = CosmicDarkSurface,
    onSurface = CosmicDarkText,
    surfaceVariant = CosmicDarkCard,
    onSurfaceVariant = CosmicDarkMuted,
    outline = CosmicDarkMuted
)

private val LightColorScheme = lightColorScheme(
    primary = BentoPrimaryGreenText,
    onPrimary = WarmWhite,
    primaryContainer = BentoAccentGreen,
    onPrimaryContainer = BentoTextDark,
    secondary = BentoBreathingAccent,
    onSecondary = WarmWhite,
    secondaryContainer = BentoBreathingBg,
    onSecondaryContainer = BentoBreathingAccent,
    background = BentoBg,
    onBackground = BentoTextDark,
    surface = WarmWhite,
    onSurface = BentoTextDark,
    surfaceVariant = BentoNavBg,
    onSurfaceVariant = BentoTextMuted,
    outline = BentoCardBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disable dynamic colors to keep our calming tailored theme consistent
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
