package app.qiap.exercise

/**
 * A scalar the rep counter follows, read from one pose frame. Returns NaN when the body parts it
 * needs aren't visible enough, so the counter pauses instead of guessing.
 */
interface Metric {
    fun measure(frame: PoseFrame): Float
}

/**
 * Interior angle at a joint, averaged over whichever body sides are visible.
 * Each side is [a]-[b]-[c] with the angle measured at [b].
 */
class JointAngle(
    private val left: IntArray,
    private val right: IntArray,
    private val minVisibility: Float,
) : Metric {
    init {
        require(left.size == 3 && right.size == 3) { "JointAngle needs a,b,c per side" }
    }

    override fun measure(frame: PoseFrame): Float {
        var sum = 0f
        var n = 0
        if (frame.minVisibility(left) >= minVisibility) {
            val a = frame.angleDeg(left[0], left[1], left[2])
            if (!a.isNaN()) { sum += a; n++ }
        }
        if (frame.minVisibility(right) >= minVisibility) {
            val a = frame.angleDeg(right[0], right[1], right[2])
            if (!a.isNaN()) { sum += a; n++ }
        }
        return if (n == 0) Float.NaN else sum / n
    }
}

/**
 * How one exercise is counted. A rep is: metric drops below [downBelow], then rises back above
 * [upAbove] (hysteresis, so jitter around one threshold never double counts), at least [minRepMs]
 * after the previous rep. Reaching [goodBelow] at the bottom counts as good form.
 */
class ExerciseSpec(
    val id: String,
    val name: String,
    val unit: String,
    val metric: Metric,
    val downBelow: Float,
    val upAbove: Float,
    val goodBelow: Float,
    val minRepMs: Long,
    /** EMA weight for the newest sample, 0..1. Higher = snappier, noisier. */
    val smoothing: Float,
    /** Cue shown while a rep is too shallow. */
    val depthCue: String,
    /**
     * True while these thresholds are first guesses. CLAUDE.md: tune from recorded landmark
     * fixtures, never by guessing. Flip to false only after a fixture run backs the numbers.
     */
    val provisional: Boolean,
) {
    init {
        require(goodBelow <= downBelow && downBelow < upAbove) { "$id: need goodBelow <= downBelow < upAbove" }
        require(smoothing in 0f..1f)
    }
}

/** The only place exercise thresholds live (CLAUDE.md). */
object ExerciseCatalog {
    private const val MIN_VIS = 0.5f

    val Squat = ExerciseSpec(
        id = "squat",
        name = "Squat",
        unit = "squats",
        metric = JointAngle(
            left = intArrayOf(Landmark.LEFT_HIP, Landmark.LEFT_KNEE, Landmark.LEFT_ANKLE),
            right = intArrayOf(Landmark.RIGHT_HIP, Landmark.RIGHT_KNEE, Landmark.RIGHT_ANKLE),
            minVisibility = MIN_VIS,
        ),
        downBelow = 110f,
        upAbove = 155f,
        goodBelow = 95f,
        minRepMs = 600,
        smoothing = 0.5f,
        depthCue = "Go a little lower",
        provisional = true,
    )

    val PushUp = ExerciseSpec(
        id = "pushup",
        name = "Push-up",
        unit = "push-ups",
        metric = JointAngle(
            left = intArrayOf(Landmark.LEFT_SHOULDER, Landmark.LEFT_ELBOW, Landmark.LEFT_WRIST),
            right = intArrayOf(Landmark.RIGHT_SHOULDER, Landmark.RIGHT_ELBOW, Landmark.RIGHT_WRIST),
            minVisibility = MIN_VIS,
        ),
        downBelow = 100f,
        upAbove = 150f,
        goodBelow = 90f,
        minRepMs = 600,
        smoothing = 0.5f,
        depthCue = "Chest a little lower",
        provisional = true,
    )

    val all: List<ExerciseSpec> = listOf(Squat, PushUp)

    fun byId(id: String): ExerciseSpec? = all.firstOrNull { it.id == id }
}
