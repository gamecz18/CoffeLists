package com.example.coffelists.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

// Coffee-themed dark color scheme for devices without Material You
private val CoffeeDarkColorScheme = darkColorScheme(
    primary = CoffeeBrownDark,
    onPrimary = CoffeeOnPrimaryDark,
    primaryContainer = CoffeePrimaryContainerDark,
    onPrimaryContainer = CoffeeOnPrimaryContainerDark,
    secondary = CoffeeLightDark,
    onSecondary = CoffeeOnSecondaryDark,
    secondaryContainer = CoffeeSecondaryContainerDark,
    onSecondaryContainer = CoffeeOnSecondaryContainerDark,
    tertiary = CoffeeCreamDark,
    tertiaryContainer = CoffeeTertiaryContainerDark,
    onTertiaryContainer = CoffeeOnTertiaryContainerDark,
    background = CoffeeBackgroundDark,
    onBackground = CoffeeOnBackgroundDark,
    surface = CoffeeSurfaceDark,
    onSurface = CoffeeOnSurfaceDark,
    surfaceVariant = CoffeeSurfaceVariantDark,
    onSurfaceVariant = CoffeeOnSurfaceVariantDark,
    outline = CoffeeOutlineDark,
    error = CoffeeErrorDark
)

// Coffee-themed light color scheme for devices without Material You
private val CoffeeLightColorScheme = lightColorScheme(
    primary = CoffeeBrown,
    onPrimary = CoffeeOnPrimary,
    primaryContainer = CoffeePrimaryContainer,
    onPrimaryContainer = CoffeeOnPrimaryContainer,
    secondary = CoffeeLight,
    onSecondary = CoffeeOnSecondary,
    secondaryContainer = CoffeeSecondaryContainer,
    onSecondaryContainer = CoffeeOnSecondaryContainer,
    tertiary = CoffeeCream,
    tertiaryContainer = CoffeeTertiaryContainer,
    onTertiaryContainer = CoffeeOnTertiaryContainer,
    background = CoffeeBackground,
    onBackground = CoffeeOnBackground,
    surface = CoffeeSurface,
    onSurface = CoffeeOnSurface,
    surfaceVariant = CoffeeSurfaceVariant,
    onSurfaceVariant = CoffeeOnSurfaceVariant,
    outline = CoffeeOutline,
    error = CoffeeError
)

// Material You 3 shapes with coffee-inspired rounded corners
private val CoffeeShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun CoffeListsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+ (Material You)
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    val colorScheme = when {
        // Use Material You dynamic colors on Android 12+
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme)
                dynamicDarkColorScheme(context)
            else
                dynamicLightColorScheme(context)
        }
        // Fall back to coffee-themed colors on older devices
        darkTheme -> CoffeeDarkColorScheme
        else -> CoffeeLightColorScheme
    }

    // Apply status bar and navigation bar colors for edge-to-edge experience
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = CoffeeShapes,
        content = content
    )
}
