package app.qiap.core.designsystem.pictogram

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import app.qiap.core.designsystem.theme.QiapTheme
import app.qiap.core.designsystem.theme.rememberReducedMotion
import kotlin.math.PI
import kotlin.math.cos

/**
 * Animated stick-figure pictogram. The animation value is read only in the draw phase, so a
 * looping figure redraws without recomposing. [phase] (0..1) staggers figures shown side by side.
 * With [showJoints] it becomes the workout overlay: jade joints, saffron hip/knees when [offForm].
 */
@Composable
fun Pictogram(
    motion: PictogramMotion,
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current,
    showFloor: Boolean = true,
    showJoints: Boolean = false,
    offForm: Boolean = false,
    phase: Float = 0f,
) {
    val colors = QiapTheme.colors
    val progress: State<Float>? = if (rememberReducedMotion()) {
        null
    } else {
        rememberInfiniteTransition(label = "pictogram").animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(motion.periodMs, easing = LinearEasing), RepeatMode.Restart),
            label = "pictogramT",
        )
    }
    val jointRing = colors.bg
    val good = colors.jade
    val off = colors.saffron

    Canvas(modifier.semantics { contentDescription = "${motion.name} pictogram" }) {
        val t = ((progress?.value ?: STATIC_T) + phase) % 1f
        val k = (1f - cos(t * 2f * PI.toFloat())) / 2f
        val s = size.minDimension / 100f
        val ox = (size.width - 100f * s) / 2f
        val oy = (size.height - 100f * s) / 2f

        if (showFloor) {
            drawLine(
                color.copy(alpha = 0.18f), Offset(ox + 6f * s, oy + 93f * s), Offset(ox + 94f * s, oy + 93f * s),
                strokeWidth = 2f * s, cap = StrokeCap.Round,
            )
        }
        val w = 6.5f * s
        bone(motion, k, s, ox, oy, Joint.NECK, Joint.HIP, color, w)
        bone(motion, k, s, ox, oy, Joint.NECK, Joint.ELBOW_L, color, w)
        bone(motion, k, s, ox, oy, Joint.ELBOW_L, Joint.HAND_L, color, w)
        bone(motion, k, s, ox, oy, Joint.NECK, Joint.ELBOW_R, color, w)
        bone(motion, k, s, ox, oy, Joint.ELBOW_R, Joint.HAND_R, color, w)
        bone(motion, k, s, ox, oy, Joint.HIP, Joint.KNEE_L, color, w)
        bone(motion, k, s, ox, oy, Joint.KNEE_L, Joint.FOOT_L, color, w)
        bone(motion, k, s, ox, oy, Joint.HIP, Joint.KNEE_R, color, w)
        bone(motion, k, s, ox, oy, Joint.KNEE_R, Joint.FOOT_R, color, w)
        drawCircle(color, 7f * s, point(motion, k, s, ox, oy, Joint.HEAD))

        if (showJoints) {
            for (j in Joint.NECK until Joint.COUNT) {
                val r = (if (j == Joint.HIP) 3.4f else 2.8f) * s
                val c = point(motion, k, s, ox, oy, j)
                val lowerBody = j == Joint.HIP || j == Joint.KNEE_L || j == Joint.KNEE_R
                drawCircle(jointRing, r + 1.5f * s, c)
                drawCircle(if (offForm && lowerBody) off else good, r, c)
            }
        }
    }
}

private const val STATIC_T = 0.35f

private fun point(m: PictogramMotion, k: Float, s: Float, ox: Float, oy: Float, j: Int): Offset {
    val x = m.from[2 * j] + (m.to[2 * j] - m.from[2 * j]) * k
    val y = m.from[2 * j + 1] + (m.to[2 * j + 1] - m.from[2 * j + 1]) * k
    return Offset(ox + x * s, oy + y * s)
}

private fun DrawScope.bone(
    m: PictogramMotion, k: Float, s: Float, ox: Float, oy: Float, a: Int, b: Int, color: Color, width: Float,
) {
    drawLine(color, point(m, k, s, ox, oy, a), point(m, k, s, ox, oy, b), strokeWidth = width, cap = StrokeCap.Round)
}
