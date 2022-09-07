package dev.datlag.dxvkotool.ui.theme

import androidx.compose.material.Colors
import androidx.compose.material.Shapes
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.ui.graphics.Color
import javax.swing.plaf.ColorUIResource

fun androidx.compose.material3.ColorScheme.toLegacyColors(isDark: Boolean): Colors {
    return if (isDark) {
        darkColors(
            primary = this.primary,
            primaryVariant = this.primaryContainer,
            secondary = this.secondary,
            secondaryVariant = this.secondaryContainer,
            background = this.background,
            surface = this.surface,
            error = this.error,
            onPrimary = this.onPrimary,
            onSecondary = this.onSecondary,
            onBackground = this.onBackground,
            onSurface = this.onSurface,
            onError = this.onError
        )
    } else {
        lightColors(
            primary = this.primary,
            primaryVariant = this.primaryContainer,
            secondary = this.secondary,
            secondaryVariant = this.secondaryContainer,
            background = this.background,
            surface = this.surface,
            error = this.error,
            onPrimary = this.onPrimary,
            onSecondary = this.onSecondary,
            onBackground = this.onBackground,
            onSurface = this.onSurface,
            onError = this.onError
        )
    }
}

fun androidx.compose.material3.Shapes.toLegacyShapes(): Shapes {
    return Shapes(this.small, this.medium, this.large)
}

fun Color.toColorUIResource(): ColorUIResource {
    return ColorUIResource(this.red, this.green, this.blue)
}