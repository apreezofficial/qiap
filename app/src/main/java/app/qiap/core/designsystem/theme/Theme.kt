package app.qiap.core.designsystem.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Which token set a screen uses (design.md §3).
 * - [Dawn]: default for Home, Editor, Library, History, Settings. Maps to Night when the system is dark.
 * - [Night]: always dark. Workout (sits on a camera feed).
 * - [Ringing]: full-bleed Cinnabar. The only loud screen.
 */
enum class QiapThemeVariant { Dawn, Night, Ringing }

private val LocalQiapColors = staticCompositionLocalOf { DawnColors }
private val LocalQiapTypography = staticCompositionLocalOf { DefaultTypography }

/** Feature flag for the sky wash (design.md §3: `wash.enabled`). */
val LocalWashEnabled = staticCompositionLocalOf { true }

fun resolveColors(variant: QiapThemeVariant, systemDark: Boolean): QiapColors = when (variant) {
    QiapThemeVariant.Dawn -> if (systemDark) NightColors else DawnColors
    QiapThemeVariant.Night -> NightColors
    QiapThemeVariant.Ringing -> RingingColors
}

@Composable
fun QiapTheme(
    variant: QiapThemeVariant = QiapThemeVariant.Dawn,
    systemDark: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = resolveColors(variant, systemDark)

    // Material is only here so ripples, text selection and any stray M3 widget pick up our tokens.
    val material = if (colors.isNight) {
        darkColorScheme(
            primary = colors.ink, onPrimary = colors.onInk,
            background = colors.bg, onBackground = colors.ink,
            surface = colors.surface, onSurface = colors.ink,
            outline = colors.border, error = colors.crimson,
        )
    } else {
        lightColorScheme(
            primary = colors.ink, onPrimary = colors.onInk,
            background = colors.bg, onBackground = colors.ink,
            surface = colors.surface, onSurface = colors.ink,
            outline = colors.border, error = colors.crimson,
        )
    }

    SystemBarIcons(darkIcons = !colors.isNight)

    MaterialTheme(colorScheme = material) {
        CompositionLocalProvider(
            LocalQiapColors provides colors,
            LocalQiapTypography provides DefaultTypography,
            LocalContentColor provides colors.ink,
            content = content,
        )
    }
}

@Composable
private fun SystemBarIcons(darkIcons: Boolean) {
    if (LocalInspectionMode.current) return
    val view = LocalView.current
    SideEffect {
        val window = (view.context as? Activity)?.window ?: return@SideEffect
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = darkIcons
            isAppearanceLightNavigationBars = darkIcons
        }
    }
}

/** Accessors: `QiapTheme.colors.ink`, `QiapTheme.type.h2`, `QiapTheme.spacing.md`. */
object QiapTheme {
    val colors: QiapColors
        @Composable @ReadOnlyComposable get() = LocalQiapColors.current
    val type: QiapTypography
        @Composable @ReadOnlyComposable get() = LocalQiapTypography.current
    val spacing get() = QiapSpacing
    val radius get() = QiapRadius
    val shadow get() = QiapShadow
    val motion get() = QiapMotion
    val size get() = QiapSize
}
