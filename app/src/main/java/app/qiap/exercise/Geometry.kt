package app.qiap.exercise

import kotlin.math.acos
import kotlin.math.sqrt

/**
 * Interior angle at joint [b] (degrees, 0..180) formed by [a]-[b]-[c], measured in image space.
 * x is scaled by the frame aspect so a right angle on screen reads as 90°, not a squashed value.
 * Returns NaN if two points coincide.
 */
fun PoseFrame.angleDeg(a: Int, b: Int, c: Int): Float {
    val abx = (x[a] - x[b]) * aspect
    val aby = y[a] - y[b]
    val cbx = (x[c] - x[b]) * aspect
    val cby = y[c] - y[b]
    val lenAb = sqrt(abx * abx + aby * aby)
    val lenCb = sqrt(cbx * cbx + cby * cby)
    if (lenAb < 1e-6f || lenCb < 1e-6f) return Float.NaN
    val cos = ((abx * cbx + aby * cby) / (lenAb * lenCb)).coerceIn(-1f, 1f)
    return Math.toDegrees(acos(cos).toDouble()).toFloat()
}

/**
 * Lowest visibility among [indices]. Takes a preallocated array (not vararg) so per-frame callers
 * don't allocate.
 */
fun PoseFrame.minVisibility(indices: IntArray): Float {
    var m = 1f
    for (i in indices) if (visibility[i] < m) m = visibility[i]
    return m
}
