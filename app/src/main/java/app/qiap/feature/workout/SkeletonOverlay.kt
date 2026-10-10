package app.qiap.feature.workout

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import app.qiap.core.designsystem.theme.QiapTheme
import app.qiap.exercise.Landmark
import kotlin.math.max

private const val MIN_VIS = 0.5f
private val LowerBody = intArrayOf(Landmark.LEFT_HIP, Landmark.RIGHT_HIP, Landmark.LEFT_KNEE, Landmark.RIGHT_KNEE)

/**
 * Paper-white skeleton over the camera preview with jade joints (saffron hips/knees when form is
 * off), design.md §7. Opaque strokes only: no glass or blur over the camera.
 *
 * Maps normalized landmarks with the same center-crop the preview uses, and flips x when the
 * preview is [mirrored] (front camera). Reads the frame in the draw phase, so new poses redraw
 * without recomposition, and draws with value types only (no per-frame allocation).
 */
@Composable
fun SkeletonOverlay(session: PoseSession, mirrored: Boolean, modifier: Modifier = Modifier) {
    val colors = QiapTheme.colors
    val bone = colors.ink
    val good = colors.jade
    val off = colors.saffron
    val ring = colors.bg

    Canvas(modifier) {
        session.frameTick.intValue // subscribe this draw to new frames
        val f = session.overlayFrame
        if (!f.hasPose) return@Canvas

        // Center-crop of an image with aspect f.aspect (width/height, height = 1 unit).
        val s = max(size.width / f.aspect, size.height)
        val ox = (size.width - f.aspect * s) / 2f
        val oy = (size.height - s) / 2f
        val sx = f.aspect * s
        val stroke = 5f * density
        val offForm = session.offForm

        var i = 0
        while (i < Landmark.BONES.size) {
            val a = Landmark.BONES[i]
            val b = Landmark.BONES[i + 1]
            i += 2
            if (f.visibility[a] < MIN_VIS || f.visibility[b] < MIN_VIS) continue
            val ax = if (mirrored) 1f - f.x[a] else f.x[a]
            val bx = if (mirrored) 1f - f.x[b] else f.x[b]
            drawLine(
                bone,
                Offset(ox + ax * sx, oy + f.y[a] * s),
                Offset(ox + bx * sx, oy + f.y[b] * s),
                strokeWidth = stroke,
                cap = StrokeCap.Round,
            )
        }
        var j = 0
        while (j < Landmark.BONES.size) {
            val k = Landmark.BONES[j]
            j++
            if (f.visibility[k] < MIN_VIS) continue
            val kx = if (mirrored) 1f - f.x[k] else f.x[k]
            val c = Offset(ox + kx * sx, oy + f.y[k] * s)
            val lower = k == LowerBody[0] || k == LowerBody[1] || k == LowerBody[2] || k == LowerBody[3]
            drawCircle(ring, 7f * density, c)
            drawCircle(if (offForm && lower) off else good, 5f * density, c)
        }
    }
}
