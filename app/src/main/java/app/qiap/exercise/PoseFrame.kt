package app.qiap.exercise

/**
 * MediaPipe Pose landmark indices (33-point BlazePose topology). Only the joints the exercise
 * logic reads are named; the rest are still stored.
 */
object Landmark {
    const val COUNT = 33
    const val NOSE = 0
    const val LEFT_SHOULDER = 11
    const val RIGHT_SHOULDER = 12
    const val LEFT_ELBOW = 13
    const val RIGHT_ELBOW = 14
    const val LEFT_WRIST = 15
    const val RIGHT_WRIST = 16
    const val LEFT_HIP = 23
    const val RIGHT_HIP = 24
    const val LEFT_KNEE = 25
    const val RIGHT_KNEE = 26
    const val LEFT_ANKLE = 27
    const val RIGHT_ANKLE = 28

    /** Bones drawn by the skeleton overlay, as index pairs. */
    val BONES: IntArray = intArrayOf(
        LEFT_SHOULDER, RIGHT_SHOULDER,
        LEFT_SHOULDER, LEFT_ELBOW, LEFT_ELBOW, LEFT_WRIST,
        RIGHT_SHOULDER, RIGHT_ELBOW, RIGHT_ELBOW, RIGHT_WRIST,
        LEFT_SHOULDER, LEFT_HIP, RIGHT_SHOULDER, RIGHT_HIP,
        LEFT_HIP, RIGHT_HIP,
        LEFT_HIP, LEFT_KNEE, LEFT_KNEE, LEFT_ANKLE,
        RIGHT_HIP, RIGHT_KNEE, RIGHT_KNEE, RIGHT_ANKLE,
    )
}

/**
 * One pose sample. Mutable and reused: the pose pipeline fills the same instances every frame so
 * the per-frame path never allocates (CLAUDE.md). Copy with [copyFrom] if you need to keep one.
 *
 * Coordinates are normalized to the upright (rotation-corrected) camera image: x,y in 0..1.
 * [aspect] = image width / height, needed to measure real angles from normalized coordinates.
 */
class PoseFrame {
    val x = FloatArray(Landmark.COUNT)
    val y = FloatArray(Landmark.COUNT)
    val z = FloatArray(Landmark.COUNT)
    /** MediaPipe's per-landmark visibility, 0..1 (1 = clearly in view). */
    val visibility = FloatArray(Landmark.COUNT)
    var timestampMs: Long = 0L
    var aspect: Float = 1f
    /** False when no person was found in this frame; the arrays then hold stale data. */
    var hasPose: Boolean = false

    fun copyFrom(other: PoseFrame) {
        other.x.copyInto(x)
        other.y.copyInto(y)
        other.z.copyInto(z)
        other.visibility.copyInto(visibility)
        timestampMs = other.timestampMs
        aspect = other.aspect
        hasPose = other.hasPose
    }

    fun set(i: Int, px: Float, py: Float, pz: Float = 0f, vis: Float = 1f) {
        x[i] = px; y[i] = py; z[i] = pz; visibility[i] = vis
    }
}
