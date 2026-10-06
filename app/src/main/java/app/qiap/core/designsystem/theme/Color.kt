package app.qiap.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/** Raw palette from design.md §3. Screens never use these directly; they read [QiapColors] via [QiapTheme]. */
internal object QiapPalette {
    val White = Color(0xFFFFFFFF)
    val SkyWash = Color(0xFFEEF5FF)
    val SkyWash2 = Color(0xFFDCEBFF)
    val Mist = Color(0xFFF5F5F6)
    val Hairline = Color(0xFFECECEE)
    val Ink = Color(0xFF111114)
    val Ink2 = Color(0xFF6B6B72)
    val Ink3 = Color(0xFF9A9AA2)
    val Sky = Color(0xFF4A90E2)
    val SkyStrong = Color(0xFF1A56F0)
    val Cinnabar = Color(0xFFF2542D)
    val Jade = Color(0xFF34C38F)
    val Saffron = Color(0xFFF5B83D)
    val Crimson = Color(0xFFD7263D)

    val NightBg = Color(0xFF0D0D0F)
    val NightSurface = Color(0xFF17171A)
    val NightSurface2 = Color(0xFF202024)
    val Paper = Color(0xFFF4EFE6)
    val NightInk2 = Color(0xFFA09B92)

    // Not in the spec; derived so Night has a full token set. Tune by eye.
    val NightHairline = Color(0xFF2A2A2F)
    val NightInk3 = Color(0xFF7A766F)
}

/**
 * Semantic color slots. Dawn and Night fill the same slots so components never branch on theme.
 * [onInk] is the content color on an [ink]-filled surface (the primary pill, the active nav item).
 */
@Immutable
data class QiapColors(
    val bg: Color,
    val bgWash: Color,
    val bgWash2: Color,
    val surface: Color,
    val surfaceMuted: Color,
    val border: Color,
    val ink: Color,
    val ink2: Color,
    val ink3: Color,
    val onInk: Color,
    /** Content on sky / cinnabar fills (active tab, seal glyph). White in every theme. */
    val onAccent: Color,
    val sky: Color,
    val skyStrong: Color,
    val cinnabar: Color,
    val jade: Color,
    val saffron: Color,
    val crimson: Color,
    /** Fill for glass pills and overlay cards. Flat translucent, never blurred. */
    val glass: Color,
    val isNight: Boolean,
)

val DawnColors = QiapColors(
    bg = QiapPalette.White,
    bgWash = QiapPalette.SkyWash,
    bgWash2 = QiapPalette.SkyWash2,
    surface = QiapPalette.White,
    surfaceMuted = QiapPalette.Mist,
    border = QiapPalette.Hairline,
    ink = QiapPalette.Ink,
    ink2 = QiapPalette.Ink2,
    ink3 = QiapPalette.Ink3,
    onInk = QiapPalette.White,
    onAccent = QiapPalette.White,
    sky = QiapPalette.Sky,
    skyStrong = QiapPalette.SkyStrong,
    cinnabar = QiapPalette.Cinnabar,
    jade = QiapPalette.Jade,
    saffron = QiapPalette.Saffron,
    crimson = QiapPalette.Crimson,
    glass = QiapPalette.White.copy(alpha = 0.75f),
    isNight = false,
)

val NightColors = QiapColors(
    bg = QiapPalette.NightBg,
    bgWash = QiapPalette.NightSurface,
    bgWash2 = QiapPalette.NightSurface2,
    surface = QiapPalette.NightSurface,
    surfaceMuted = QiapPalette.NightSurface2,
    border = QiapPalette.NightHairline,
    ink = QiapPalette.Paper,
    ink2 = QiapPalette.NightInk2,
    ink3 = QiapPalette.NightInk3,
    onInk = QiapPalette.NightBg,
    onAccent = QiapPalette.White,
    sky = QiapPalette.Sky,
    skyStrong = QiapPalette.SkyStrong,
    cinnabar = QiapPalette.Cinnabar,
    jade = QiapPalette.Jade,
    saffron = QiapPalette.Saffron,
    crimson = QiapPalette.Crimson,
    glass = QiapPalette.NightSurface2.copy(alpha = 0.8f),
    isNight = true,
)

/**
 * Ringing screen: full-bleed Cinnabar with ink-black type (design.md §3).
 * The one action on Cinnabar is a white pill ([app.qiap.core.designsystem.component.PillButtonStyle.OnAccent]).
 */
val RingingColors = DawnColors.copy(
    bg = QiapPalette.Cinnabar,
    ink2 = QiapPalette.Ink.copy(alpha = 0.72f),
    ink3 = QiapPalette.Ink.copy(alpha = 0.55f),
)
