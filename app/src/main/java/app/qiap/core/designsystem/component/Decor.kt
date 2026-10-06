package app.qiap.core.designsystem.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.qiap.core.designsystem.theme.QiapMotion
import app.qiap.core.designsystem.theme.QiapTheme
import app.qiap.core.designsystem.theme.rememberReducedMotion
import kotlin.math.PI
import kotlin.math.cos

/** Tiny week chart: one rounded bar per value (0..1), [highlight] drawn at full strength. */
@Composable
fun MiniBars(values: List<Float>, highlight: Int, modifier: Modifier = Modifier, color: Color = QiapTheme.colors.sky) {
    Canvas(modifier.fillMaxWidth().height(28.dp)) {
        val gap = 4.dp.toPx()
        val w = (size.width - gap * (values.size - 1)) / values.size
        values.forEachIndexed { i, v ->
            val h = size.height * v.coerceIn(0.08f, 1f)
            drawRoundRect(
                color.copy(alpha = if (i == highlight) 1f else 0.35f),
                topLeft = Offset(i * (w + gap), size.height - h),
                size = Size(w, h),
                cornerRadius = CornerRadius(3.dp.toPx()),
            )
        }
    }
}

/** Horizontal share bar (reps per exercise). */
@Composable
fun ShareBar(fraction: Float, modifier: Modifier = Modifier) {
    val colors = QiapTheme.colors
    Canvas(modifier.fillMaxWidth().height(10.dp)) {
        val r = CornerRadius(size.height / 2f)
        drawRoundRect(colors.surfaceMuted, cornerRadius = r)
        drawRoundRect(colors.ink, size = Size(size.width * fraction.coerceIn(0f, 1f), size.height), cornerRadius = r)
    }
}

/** On for the first half, off for the second: a hard blink. */
private val BlinkEasing = Easing { if (it < 0.5f) 0f else 1f }

/** Blinking recording dot for the proof-video indicator. Static with reduced motion. */
@Composable
fun RecordingDot(modifier: Modifier = Modifier) {
    val color = QiapTheme.colors.cinnabar
    val alpha = if (rememberReducedMotion()) {
        null
    } else {
        rememberInfiniteTransition(label = "rec").animateFloat(
            1f, 0.2f, infiniteRepeatable(tween(1200, easing = BlinkEasing), RepeatMode.Restart), label = "recAlpha",
        )
    }
    Canvas(modifier.size(8.dp)) { drawCircle(color.copy(alpha = alpha?.value ?: 1f)) }
}

/**
 * Ringing backdrop: three white sun discs pulsing in opacity only (design.md §10: 3 s, opacity
 * only). Draw-phase animation, no recomposition. Static when reduced motion is on.
 */
@Composable
fun PulsingSun(modifier: Modifier = Modifier) {
    val sun = Color.White
    val t = if (rememberReducedMotion()) {
        null
    } else {
        rememberInfiniteTransition(label = "sun").animateFloat(
            0f, 1f, infiniteRepeatable(tween(QiapMotion.SUN_PULSE_MS, easing = LinearEasing)), label = "sunT",
        )
    }
    Canvas(modifier) {
        val base = size.minDimension / 2f
        val rings = floatArrayOf(1f, 0.75f, 0.55f)
        for (i in rings.indices) {
            val phase = ((t?.value ?: 0.5f) + i / 3f) % 1f
            val a = 0.08f + 0.18f * (1f - cos(phase * 2f * PI.toFloat())) / 2f
            drawCircle(sun.copy(alpha = a), base * rings[i], center)
        }
    }
}
