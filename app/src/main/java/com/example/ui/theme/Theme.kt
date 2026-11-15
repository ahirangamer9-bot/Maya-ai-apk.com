package com.example.ui.theme

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

private val DarkColorScheme =
    darkColorScheme(
        primary = MayaBlueDark,
        onPrimary = Color.Black,
        primaryContainer = MayaDarkBlue,
        onPrimaryContainer = Color.White,
        secondary = AccentCyan,
        onSecondary = Color.Black,
        background = BackgroundDark,
        surface = SurfaceDark,
        onBackground = TextLight,
        onSurface = TextLight,
        surfaceVariant = Color(0xFF334155),
        onSurfaceVariant = Color(0xFFCBD5E1)
    )

private val LightColorScheme =
    lightColorScheme(
        primary = MayaBlue,
        onPrimary = Color.White,
        primaryContainer = MayaLightBlue,
        onPrimaryContainer = MayaDarkBlue,
        secondary = AccentCyan,
        onSecondary = Color.White,
        background = BackgroundBlue,
        surface = CardWhite,
        onBackground = TextDark,
        onSurface = TextDark,
        surfaceVariant = Color(0xFFE2E8F0),
        onSurfaceVariant = TextMuted
    )

@Composable
fun MayaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set false to prioritize consistent Maya branding
    content: @Composable () -> Unit,
) {
    val colorScheme =
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }
            darkTheme -> DarkColorScheme
            else -> LightColorScheme
        }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

// For backward compatibility
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) = MayaTheme(darkTheme, dynamicColor, content)
