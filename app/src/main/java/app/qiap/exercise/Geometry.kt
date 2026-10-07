package app.qiap.exercise

import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.abs
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

/** Mean x of [indices] (image-normalized). */
fun PoseFrame.midX(indices: IntArray): Float {
    var s = 0f
    for (i in indices) s += x[i]
    return s / indices.size
}

/** Mean y of [indices] (image-normalized; y grows downward). */
fun PoseFrame.midY(indices: IntArray): Float {
    var s = 0f
    for (i in indices) s += y[i]
    return s / indices.size
}

private val SHOULDER_PAIR = intArrayOf(Landmark.LEFT_SHOULDER, Landmark.RIGHT_SHOULDER)
private val HIP_PAIR = intArrayOf(Landmark.LEFT_HIP, Landmark.RIGHT_HIP)

/**
 * Torso length: mid-shoulder to mid-hip, aspect-corrected (details.md §6: every distance is
 * normalized by this so thresholds hold at any distance from the camera). NaN if degenerate.
 */
fun PoseFrame.torsoLength(): Float {
    val dx = (midX(SHOULDER_PAIR) - midX(HIP_PAIR)) * aspect
    val dy = midY(SHOULDER_PAIR) - midY(HIP_PAIR)
    val len = sqrt(dx * dx + dy * dy)
    return if (len < 0.02f) Float.NaN else len
}

/** Aspect-corrected distance between the mid-points of two landmark groups. */
fun PoseFrame.distance(a: IntArray, b: IntArray): Float {
    val dx = (midX(a) - midX(b)) * aspect
    val dy = midY(a) - midY(b)
    return sqrt(dx * dx + dy * dy)
}

/** Angle (degrees, 0..180) between the vector [from]→[to] and straight down the screen. */
fun PoseFrame.fromVerticalDeg(from: IntArray, to: IntArray): Float {
    val dx = (midX(to) - midX(from)) * aspect
    val dy = midY(to) - midY(from)
    if (dx * dx + dy * dy < 1e-8f) return Float.NaN
    return Math.toDegrees(atan2(abs(dx).toDouble(), dy.toDouble())).toFloat()
}
