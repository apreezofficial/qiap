package app.qiap.core.designsystem.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.qiap.core.designsystem.theme.QiapMotion
import app.qiap.core.designsystem.theme.QiapTheme
import app.qiap.core.designsystem.theme.rememberReducedMotion
import kotlinx.coroutines.launch

enum class SealState {
    /** Workout completed: Cinnabar seal. */
    Earned,
    /** Alarm dismissed via fallback: seal with a saffron edge. */
    Fallback,
    /** No seal that day: empty dashed square. */
    Missed,
}

/**
 * The 醒 seal (design.md §1, §6): Cinnabar rounded square, white character, rotated −4°.
 * With [stampIn] it slams in (1.6 → 1.0, tiny settle, haptic on contact, 400 ms total) unless the
 * user has reduced motion on.
 */
@Composable
fun SealStamp(
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    state: SealState = SealState.Earned,
    stampIn: Boolean = false,
    onStamped: () -> Unit = {},
) {
    val colors = QiapTheme.colors
    val glyph = remember { sealGlyphPath() }
    val reducedMotion = rememberReducedMotion()
    val haptics = LocalHapticFeedback.current
    val animate = stampIn && !reducedMotion && state != SealState.Missed
    val scale = remember { Animatable(if (animate) 1.6f else 1f) }
    val alpha = remember { Animatable(if (animate) 0f else 1f) }

    LaunchedEffect(animate) {
        if (!animate) return@LaunchedEffect
        val slam = (QiapMotion.SEAL_STAMP_MS * 0.65f).toInt()
        val settle = QiapMotion.SEAL_STAMP_MS - slam
        launch { alpha.animateTo(1f, tween(slam / 2)) }
        scale.animateTo(0.96f, tween(slam, easing = FastOutLinearInEasing))
        haptics.performHapticFeedback(HapticFeedbackType.Confirm)
        scale.animateTo(1f, tween(settle, easing = QiapMotion.easing))
        onStamped()
    }

    val label = when (state) {
        SealState.Earned -> "Seal earned"
        SealState.Fallback -> "Seal, fallback used"
        SealState.Missed -> "No seal"
    }

    Canvas(
        modifier
            .size(size)
            .semantics { contentDescription = label }
            .rotate(if (state == SealState.Missed) 0f else SEAL_ROTATION)
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
                this.alpha = alpha.value
            },
    ) {
        val side = this.size.minDimension
        val corner = CornerRadius(side * SEAL_CORNER)
        if (state == SealState.Missed) {
            val stroke = 2.dp.toPx()
            drawRoundRect(
                color = colors.border,
                topLeft = Offset(stroke / 2, stroke / 2),
                size = Size(side - stroke, side - stroke),
                cornerRadius = corner,
                style = Stroke(stroke, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx()))),
            )
            return@Canvas
        }
        drawRoundRect(color = colors.cinnabar, cornerRadius = corner)
        if (state == SealState.Fallback) {
            val edge = 3.dp.toPx()
            drawRoundRect(
                color = colors.saffron,
                topLeft = Offset(edge / 2, edge / 2),
                size = Size(side - edge, side - edge),
                cornerRadius = corner,
                style = Stroke(edge),
            )
        }
        scale(side / SealGlyph.VIEWBOX, pivot = Offset.Zero) {
            drawPath(glyph, colors.onAccent)
        }
    }
}

private const val SEAL_ROTATION = -4f
private const val SEAL_CORNER = 0.22f

private fun sealGlyphPath(): Path = PathParser().parsePathString(SealGlyph.PATH).toPath()

/**
 * 醒 ("awake") outline on a 100×100 grid, from Noto Serif CJK SC Bold (SIL OFL 1.1).
 * Shared with the web seal (web/components/Brand.tsx) and the launcher icon, so no CJK font ships.
 */
object SealGlyph {
    const val VIEWBOX = 100f
    const val PATH =
        "M60.81 28.73H78.18V38.57H60.81ZM60.81 26.25V16.77H78.18V26.25ZM51.95 14.29V45.22H53.54C58.15 45.22 60.81 43.71 60.81 43.09V41.05H78.18V44.42H79.77C84.38 44.42 87.48 42.73 87.48 42.29V17.48C89.34 17.13 90.23 16.51 90.76 15.8L82.25 9.33L77.82 14.29H61.87L51.95 10.39ZM52.66 45.84C52.22 54.87 50.27 64.09 47.61 70.65L48.85 71.27C52.48 68.16 55.41 63.82 57.71 58.86H65.33V69.76H52.48L53.19 72.33H65.33V86.15H47.61L48.32 88.63H89.61C90.85 88.63 91.73 88.19 92 87.22C88.72 84.03 83.14 79.42 83.14 79.42L78.27 86.15H74.28V72.33H87.48C88.63 72.33 89.61 71.89 89.78 70.91C86.86 67.99 81.9 63.82 81.9 63.82L77.56 69.76H74.28V58.86H88.37C89.61 58.86 90.58 58.42 90.76 57.44C87.66 54.43 82.43 50.09 82.43 50.09L77.73 56.38H74.28V47.61C76.41 47.34 76.94 46.54 77.11 45.39L65.33 44.33V56.38H58.77C59.48 54.61 60.1 52.75 60.72 50.8C62.67 50.8 63.65 49.91 64 48.85ZM29.18 17.92V30.86H26.08V17.92ZM26.08 15.44H8L8.71 17.92H19.52V30.86H18.37L10.22 27.23V91.29H11.46C14.91 91.29 17.92 89.34 17.92 88.46V82.96H38.13V90.23H39.37C42.29 90.23 46.19 88.19 46.37 87.57V34.67C47.96 34.32 49.2 33.7 49.73 32.99L41.41 26.43L37.24 30.86H35.73V17.92H47.87C49.11 17.92 50.09 17.48 50.27 16.51C46.81 13.32 41.05 8.71 41.05 8.71L35.91 15.44ZM38.13 68.16V80.48H17.92V68.16ZM38.13 65.59H17.92V59.13L18.1 59.3C25.54 52.84 26.08 43.09 26.08 36.89V33.43H29.18V50.8C29.18 54.16 29.71 55.58 33.43 55.58H35.47L38.13 55.41ZM38.13 49.56 37.77 49.65C37.59 49.65 37.24 49.65 36.97 49.65C36.71 49.65 36.35 49.65 36 49.65H35.03C34.58 49.65 34.41 49.38 34.41 48.49V33.43H38.13ZM17.92 56.29V33.43H20.94V36.8C20.94 42.47 21.03 49.73 17.92 56.29Z"
}
