package app.qiap.exercise

enum class RepEvent {
    NONE,
    /** A full rep that reached good depth (for holds: another second held). */
    REP,
    /** A counted rep that went below [ExerciseSpec.downBelow] but not to [ExerciseSpec.goodBelow]. */
    REP_SHALLOW,
}

/**
 * Counts reps of one exercise from a stream of [PoseFrame]s. Pure state machine: no allocation,
 * no Android, no clock other than the frame timestamps, so it replays identically from fixtures.
 * What it does depends on [ExerciseSpec.kind]: cycle, alternating halves, hold timer or sequence.
 *
 * Until it has seen the rest position once it counts nothing, so starting mid-squat or walking
 * into frame doesn't produce a free rep. Frames where the metric isn't measurable are skipped
 * (the smoothed value and phase are kept), so a brief occlusion doesn't reset a rep in progress.
 * For holds, [reps] is the seconds held so far.
 */
class RepCounter(private val spec: ExerciseSpec) {

    /** One rest → active → rest cycle with hysteresis. Alternating moves use two of these. */
    private class Cycle(private val spec: ExerciseSpec) {
        enum class Phase { WAITING_FOR_UP, UP, DOWN }

        var phase = Phase.WAITING_FOR_UP
        var smoothed = Float.NaN
        var bottom = Float.MAX_VALUE
        var lastRepAt = Long.MIN_VALUE / 2
        var lastGood = true

        val needsMoreDepth: Boolean get() = phase == Phase.DOWN && bottom > spec.goodBelow

        fun reset() {
            phase = Phase.WAITING_FOR_UP
            smoothed = Float.NaN
            bottom = Float.MAX_VALUE
            lastRepAt = Long.MIN_VALUE / 2
            lastGood = true
        }

        fun step(raw: Float, t: Long): RepEvent {
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
                        if (t - lastRepAt >= spec.minRepMs) {
                            lastRepAt = t
                            lastGood = bottom <= spec.goodBelow
                            bottom = Float.MAX_VALUE
                            return if (lastGood) RepEvent.REP else RepEvent.REP_SHALLOW
                        }
                        bottom = Float.MAX_VALUE
                    }
                }
            }
            return RepEvent.NONE
        }
    }

    private val a = Cycle(spec)
    private val b = if (spec.kind == Kind.ALTERNATING) Cycle(spec) else null

    // hold state
    private var holdMs = 0L
    private var brokenMs = 0L
    private var lastHoldT = Long.MIN_VALUE
    private var holdInBand = true

    // sequence state
    private var armed = false
    private var stageIndex = 1
    private var lastSeqRepAt = Long.MIN_VALUE / 2
    private var startPoseSince = -1L

    private var lastValue = Float.NaN

    var reps: Int = 0
        private set

    /** Whether the last frame had the body parts this exercise needs. */
    var tracking: Boolean = false
        private set

    /** Smoothed metric (e.g. knee angle), NaN until the first measurable frame. */
    val value: Float get() = if (spec.kind == Kind.CYCLE || spec.kind == Kind.ALTERNATING) a.smoothed else lastValue

    /** True while descending/at the bottom without good depth yet (holds: while out of the band). */
    val needsMoreDepth: Boolean
        get() = when (spec.kind) {
            Kind.HOLD -> tracking && !holdInBand
            Kind.SEQUENCE -> false
            else -> a.needsMoreDepth || (b?.needsMoreDepth == true)
        }

    /** Form of the most recent counted rep. */
    var lastRepGood: Boolean = true
        private set

    /** Cue of the first failing gate on the last frame, or null when every gate holds. */
    var gateCue: String? = null
        private set

    init {
        resetMetrics()
    }

    fun reset() {
        a.reset()
        b?.reset()
        holdMs = 0
        brokenMs = 0
        lastHoldT = Long.MIN_VALUE
        holdInBand = true
        armed = false
        stageIndex = 1
        lastSeqRepAt = Long.MIN_VALUE / 2
        startPoseSince = -1L
        lastValue = Float.NaN
        reps = 0
        tracking = false
        lastRepGood = true
        gateCue = null
        resetMetrics()
    }

    private fun resetMetrics() {
        spec.metric.reset()
        spec.metricB?.reset()
        for (i in spec.gates.indices) spec.gates[i].metric.reset()
        for (s in spec.stages.indices) for (p in spec.stages[s].parts) p.metric.reset()
    }

    fun onFrame(frame: PoseFrame): RepEvent {
        if (!frame.hasPose) {
            tracking = false
            return RepEvent.NONE
        }

        // Gates: an unmeasurable gate pauses everything; a failing gate keeps tracking but counts nothing.
        var gateFailed = false
        gateCue = null
        for (i in spec.gates.indices) {
            val g = spec.gates[i]
            val r = g.check(frame)
            if (r < 0) {
                tracking = false
                return RepEvent.NONE
            }
            if (r == 0) {
                gateFailed = true
                if (gateCue == null) gateCue = g.cue.ifEmpty { null }
            }
        }

        return when (spec.kind) {
            Kind.CYCLE -> onCycle(frame, gateFailed)
            Kind.ALTERNATING -> onAlternating(frame, gateFailed)
            Kind.HOLD -> onHold(frame, gateFailed)
            Kind.SEQUENCE -> onSequence(frame)
        }
    }

    private fun onCycle(frame: PoseFrame, gateFailed: Boolean): RepEvent {
        val raw = spec.metric.measure(frame)
        if (raw.isNaN()) {
            tracking = false
            return RepEvent.NONE
        }
        tracking = true
        if (gateFailed) return RepEvent.NONE
        val ev = a.step(raw, frame.timestampMs)
        if (ev != RepEvent.NONE) {
            reps++
            lastRepGood = a.lastGood
        }
        return ev
    }

    private fun onAlternating(frame: PoseFrame, gateFailed: Boolean): RepEvent {
        val rawA = spec.metric.measure(frame)
        val rawB = spec.metricB!!.measure(frame)
        if (rawA.isNaN() && rawB.isNaN()) {
            tracking = false
            return RepEvent.NONE
        }
        tracking = true
        if (gateFailed) return RepEvent.NONE
        val evA = if (rawA.isNaN()) RepEvent.NONE else a.step(rawA, frame.timestampMs)
        val evB = if (rawB.isNaN()) RepEvent.NONE else b!!.step(rawB, frame.timestampMs)
        if (evA != RepEvent.NONE) { reps++; lastRepGood = a.lastGood }
        if (evB != RepEvent.NONE) { reps++; lastRepGood = b!!.lastGood }
        return when {
            evA == RepEvent.REP || evB == RepEvent.REP -> RepEvent.REP
            evA == RepEvent.REP_SHALLOW || evB == RepEvent.REP_SHALLOW -> RepEvent.REP_SHALLOW
            else -> RepEvent.NONE
        }
    }

    private fun onHold(frame: PoseFrame, gateFailed: Boolean): RepEvent {
        val v = spec.metric.measure(frame)
        lastValue = v
        if (v.isNaN()) {
            tracking = false
            lastHoldT = Long.MIN_VALUE
            return RepEvent.NONE
        }
        tracking = true
        // Cap the step so a stalled camera can't bank seconds that weren't held.
        val dt = if (lastHoldT == Long.MIN_VALUE) 0L else (frame.timestampMs - lastHoldT).coerceIn(0L, HOLD_MAX_STEP_MS)
        lastHoldT = frame.timestampMs
        holdInBand = !gateFailed && v >= spec.holdLow && v <= spec.holdHigh
        lastRepGood = holdInBand
        if (holdInBand) {
            holdMs += dt
            brokenMs = 0
        } else {
            brokenMs += dt
            // Short wobbles pause the clock; a real break starts the hold over (details.md §7).
            if (brokenMs > HOLD_GRACE_MS) holdMs = 0
        }
        val seconds = (holdMs / 1000).toInt()
        val grew = seconds > reps
        reps = seconds
        return if (grew) RepEvent.REP else RepEvent.NONE
    }

    /** 1 = every part holds, 0 = some part is outside its range, -1 = a part can't be measured. */
    private fun stageState(stage: Stage, frame: PoseFrame): Int {
        var result = 1
        for (p in stage.parts) {
            val r = p.check(frame)
            if (r < 0) return -1
            if (r == 0) result = 0
        }
        return result
    }

    private fun onSequence(frame: PoseFrame): RepEvent {
        lastValue = spec.metric.measure(frame)
        val stages = spec.stages
        val target = if (armed) stageIndex else 0
        val state = stageState(stages[target], frame)
        if (state < 0) {
            tracking = false
            return RepEvent.NONE
        }
        tracking = true
        if (!armed) {
            if (state == 1) {
                armed = true
                stageIndex = 1
            }
            return RepEvent.NONE
        }
        if (state == 1) {
            startPoseSince = -1L
            stageIndex++
            if (stageIndex >= stages.size) {
                stageIndex = 1
                if (frame.timestampMs - lastSeqRepAt >= spec.minRepMs) {
                    lastSeqRepAt = frame.timestampMs
                    reps++
                    lastRepGood = true
                    return RepEvent.REP
                }
            }
        } else if (stageIndex > 1) {
            if (stageState(stages[0], frame) == 1) {
                // Back in the start pose mid-move. A jump passes through it for a frame or two, so
                // only give up once it has been held for abandonMs.
                if (startPoseSince < 0) startPoseSince = frame.timestampMs
                if (frame.timestampMs - startPoseSince >= spec.abandonMs) {
                    stageIndex = 1
                    startPoseSince = -1L
                }
            } else {
                startPoseSince = -1L
            }
        }
        return RepEvent.NONE
    }

    private companion object {
        const val HOLD_GRACE_MS = 1500L
        const val HOLD_MAX_STEP_MS = 250L
    }
}
