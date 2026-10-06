package app.qiap.exercise

enum class RepEvent {
    NONE,
    /** A full rep that reached good depth. */
    REP,
    /** A counted rep that went below [ExerciseSpec.downBelow] but not to [ExerciseSpec.goodBelow]. */
    REP_SHALLOW,
}

/**
 * Counts reps of one exercise from a stream of [PoseFrame]s. Pure state machine: no allocation,
 * no Android, no clock other than the frame timestamps, so it replays identically from fixtures.
 *
 * Until it has seen the "up" position once it counts nothing, so starting mid-squat or walking
 * into frame doesn't produce a free rep. Frames where the metric isn't measurable are skipped
 * (the smoothed value and phase are kept), so a brief occlusion doesn't reset a rep in progress.
 */
class RepCounter(private val spec: ExerciseSpec) {
    private enum class Phase { WAITING_FOR_UP, UP, DOWN }

    private var phase = Phase.WAITING_FOR_UP
    private var smoothed = Float.NaN
    private var bottom = Float.MAX_VALUE
    private var lastRepAt = Long.MIN_VALUE / 2

    var reps: Int = 0
        private set

    /** Whether the last frame had the body parts this exercise needs. */
    var tracking: Boolean = false
        private set

    /** Smoothed metric (e.g. knee angle), NaN until the first measurable frame. */
    val value: Float get() = smoothed

    /** True while descending/at the bottom without having reached good depth yet. */
    val needsMoreDepth: Boolean get() = phase == Phase.DOWN && bottom > spec.goodBelow

    /** Form of the most recent counted rep. */
    var lastRepGood: Boolean = true
        private set

    fun reset() {
        phase = Phase.WAITING_FOR_UP
        smoothed = Float.NaN
        bottom = Float.MAX_VALUE
        lastRepAt = Long.MIN_VALUE / 2
        reps = 0
        tracking = false
        lastRepGood = true
    }

    fun onFrame(frame: PoseFrame): RepEvent {
        if (!frame.hasPose) {
            tracking = false
            return RepEvent.NONE
        }
        val raw = spec.metric.measure(frame)
        if (raw.isNaN()) {
            tracking = false
            return RepEvent.NONE
        }
        tracking = true
        smoothed = if (smoothed.isNaN()) raw else smoothed + spec.smoothing * (raw - smoothed)

        when (phase) {
            Phase.WAITING_FOR_UP -> if (smoothed >= spec.upAbove) phase = Phase.UP
            Phase.UP -> if (smoothed <= spec.downBelow) {
                phase = Phase.DOWN
                bottom = smoothed
            }
            Phase.DOWN -> {
                if (smoothed < bottom) bottom = smoothed
                if (smoothed >= spec.upAbove) {
                    phase = Phase.UP
                    if (frame.timestampMs - lastRepAt >= spec.minRepMs) {
                        reps++
                        lastRepAt = frame.timestampMs
                        lastRepGood = bottom <= spec.goodBelow
                        bottom = Float.MAX_VALUE
                        return if (lastRepGood) RepEvent.REP else RepEvent.REP_SHALLOW
                    }
                    bottom = Float.MAX_VALUE
                }
            }
        }
        return RepEvent.NONE
    }
}
