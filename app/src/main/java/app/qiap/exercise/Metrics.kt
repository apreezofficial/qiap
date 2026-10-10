package app.qiap.exercise

import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.exp

/**
 * A scalar the rep counter follows, read from one pose frame. Returns NaN when the body parts it
 * needs aren't visible enough, so the counter pauses instead of guessing.
 *
 * Convention for cycle exercises: the *rest* position reads high and the *active* position reads
 * low (a squat's knee angle). Metrics that naturally grow with effort (a raised knee) are wrapped
 * in [Flip] so every definition in the catalog speaks the same way.
 *
 * Most metrics are stateless. [HipRise] and [HipSway] keep a slow baseline and are [reset] by the
 * counter; all metric calls happen on the single pose thread.
 */
interface Metric {
    fun measure(frame: PoseFrame): Float
    fun reset() {}
}

/** Interior angle at a joint, averaged over whichever body sides are visible. a-b-c, measured at b. */
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

/** Interior angle at one specific joint (one body side): the left or right half of an alternating move. */
class SideAngle(private val abc: IntArray, private val minVisibility: Float) : Metric {
    init {
        require(abc.size == 3) { "SideAngle needs a,b,c" }
    }

    override fun measure(frame: PoseFrame): Float =
        if (frame.minVisibility(abc) < minVisibility) Float.NaN else frame.angleDeg(abc[0], abc[1], abc[2])
}

/** Larger or smaller of the two sides' angle (warrior stance: bent front knee vs straight back knee). */
class AngleExtreme(
    private val left: IntArray,
    private val right: IntArray,
    private val useMax: Boolean,
    private val minVisibility: Float,
) : Metric {
    override fun measure(frame: PoseFrame): Float {
        val l = if (frame.minVisibility(left) >= minVisibility) frame.angleDeg(left[0], left[1], left[2]) else Float.NaN
        val r = if (frame.minVisibility(right) >= minVisibility) frame.angleDeg(right[0], right[1], right[2]) else Float.NaN
        return when {
            l.isNaN() -> r
            r.isNaN() -> l
            useMax -> maxOf(l, r)
            else -> minOf(l, r)
        }
    }
}

/** Distance between the mid-points of [a] and [b], in torso lengths. */
class Dist(private val a: IntArray, private val b: IntArray, private val minVisibility: Float) : Metric {
    override fun measure(frame: PoseFrame): Float {
        if (frame.minVisibility(a) < minVisibility || frame.minVisibility(b) < minVisibility) return Float.NaN
        val t = frame.torsoLength()
        return if (t.isNaN()) Float.NaN else frame.distance(a, b) / t
    }
}

/** Distance a1↔a2 divided by distance b1↔b2 (stance width vs shoulder width). */
class WidthRatio(
    private val a1: IntArray,
    private val a2: IntArray,
    private val b1: IntArray,
    private val b2: IntArray,
    private val minVisibility: Float,
) : Metric {
    override fun measure(frame: PoseFrame): Float {
        if (frame.minVisibility(a1) < minVisibility || frame.minVisibility(a2) < minVisibility) return Float.NaN
        if (frame.minVisibility(b1) < minVisibility || frame.minVisibility(b2) < minVisibility) return Float.NaN
        val den = frame.distance(b1, b2)
        return if (den < 0.02f) Float.NaN else frame.distance(a1, a2) / den
    }
}

/** How far [upper] sits above [lower] on screen, in torso lengths (negative = below). */
class Above(private val upper: IntArray, private val lower: IntArray, private val minVisibility: Float) : Metric {
    override fun measure(frame: PoseFrame): Float {
        if (frame.minVisibility(upper) < minVisibility || frame.minVisibility(lower) < minVisibility) return Float.NaN
        val t = frame.torsoLength()
        return if (t.isNaN()) Float.NaN else (frame.midY(lower) - frame.midY(upper)) / t
    }
}

/** Signed horizontal offset of [a] from [b] in torso lengths (positive = a is further right on screen). */
class Lateral(private val a: IntArray, private val b: IntArray, private val minVisibility: Float) : Metric {
    override fun measure(frame: PoseFrame): Float {
        if (frame.minVisibility(a) < minVisibility || frame.minVisibility(b) < minVisibility) return Float.NaN
        val t = frame.torsoLength()
        return if (t.isNaN()) Float.NaN else (frame.midX(a) - frame.midX(b)) * frame.aspect / t
    }
}

/** Angle of the vector [from]→[to] away from straight down, 0..180. Abduction of a leg. */
class FromVertical(private val from: IntArray, private val to: IntArray, private val minVisibility: Float) : Metric {
    override fun measure(frame: PoseFrame): Float =
        if (frame.minVisibility(from) < minVisibility || frame.minVisibility(to) < minVisibility) Float.NaN
        else frame.fromVerticalDeg(from, to)
}

private val SHOULDER_PAIR = intArrayOf(Landmark.LEFT_SHOULDER, Landmark.RIGHT_SHOULDER)
private val HIP_PAIR = intArrayOf(Landmark.LEFT_HIP, Landmark.RIGHT_HIP)

/**
 * Torso angle from upright: 0° standing tall, 90° lying flat. Uses the hip→shoulder vector, so
 * it works for floor work (crunch, sit-up) and for burpee/inchworm phases alike.
 */
class TorsoLean(private val minVisibility: Float) : Metric {
    override fun measure(frame: PoseFrame): Float {
        if (frame.minVisibility(SHOULDER_PAIR) < minVisibility || frame.minVisibility(HIP_PAIR) < minVisibility) return Float.NaN
        val dx = (frame.midX(SHOULDER_PAIR) - frame.midX(HIP_PAIR)) * frame.aspect
        val dy = frame.midY(HIP_PAIR) - frame.midY(SHOULDER_PAIR) // positive when the shoulders are above the hips
        if (dx * dx + dy * dy < 1e-8f) return Float.NaN
        return Math.toDegrees(atan2(abs(dx).toDouble(), dy.toDouble())).toFloat()
    }
}

/** Sideways lean of the torso, degrees; [sign] picks which direction reads positive. */
class SignedLean(private val sign: Float, private val minVisibility: Float) : Metric {
    override fun measure(frame: PoseFrame): Float {
        if (frame.minVisibility(SHOULDER_PAIR) < minVisibility || frame.minVisibility(HIP_PAIR) < minVisibility) return Float.NaN
        val dx = (frame.midX(SHOULDER_PAIR) - frame.midX(HIP_PAIR)) * frame.aspect
        val dy = frame.midY(HIP_PAIR) - frame.midY(SHOULDER_PAIR)
        if (dx * dx + dy * dy < 1e-8f) return Float.NaN
        return sign * Math.toDegrees(atan2(dx.toDouble(), dy.toDouble())).toFloat()
    }
}

/**
 * How much narrower the shoulders look than usual, 0 = square to the camera, ~0.2 = turned about
 * 35°. Uses the on-screen shoulder width only (MediaPipe's depth is too noisy to trust), measured
 * against the widest recent width, which slowly decays so walking away doesn't read as a twist.
 */
class ShoulderSpanDrop(private val minVisibility: Float, private val decayTauMs: Float = 6000f) : Metric {
    private var peak = Float.NaN
    private var lastT = 0L

    override fun measure(frame: PoseFrame): Float {
        if (frame.minVisibility(SHOULDER_PAIR) < minVisibility) return Float.NaN
        val span = abs(frame.x[Landmark.LEFT_SHOULDER] - frame.x[Landmark.RIGHT_SHOULDER]) * frame.aspect
        if (span < 0.02f) return Float.NaN
        val dt = (frame.timestampMs - lastT).coerceAtLeast(0L)
        lastT = frame.timestampMs
        peak = when {
            peak.isNaN() || span >= peak -> span
            else -> peak - (peak - span) * (1f - exp(-dt / decayTauMs))
        }
        return (peak - span) / peak
    }

    override fun reset() {
        peak = Float.NaN
    }
}

/** `c - inner`: turns an "effort grows" signal into the rest-high / active-low convention. */
class Flip(private val inner: Metric, private val c: Float) : Metric {
    override fun measure(frame: PoseFrame): Float = c - inner.measure(frame)
    override fun reset() = inner.reset()
}

/** Larger of two metrics (either foot raised, whichever side). NaN only if both are. */
class MaxOf(private val a: Metric, private val b: Metric) : Metric {
    override fun measure(frame: PoseFrame): Float {
        val x = a.measure(frame)
        val y = b.measure(frame)
        return when {
            x.isNaN() -> y
            y.isNaN() -> x
            else -> maxOf(x, y)
        }
    }

    override fun reset() {
        a.reset()
        b.reset()
    }
}

/** Slow exponential follower. [step] returns the baseline *before* absorbing the new sample. */
internal class Baseline(private val tauMs: Float) {
    private var base = Float.NaN
    private var lastT = 0L

    fun reset() {
        base = Float.NaN
    }

    fun step(v: Float, t: Long): Float {
        if (base.isNaN()) {
            base = v
            lastT = t
            return v
        }
        val before = base
        val dt = (t - lastT).coerceAtLeast(0L)
        lastT = t
        base += (1f - exp(-dt / tauMs)) * (v - base)
        return before
    }
}

/**
 * How far the hips are above where they usually are, in torso lengths. The reference is a slow
 * moving average, so it ignores a person drifting around the room but sees a hop (airborne for
 * ~0.4 s) or a calf raise. Positive = hips raised.
 */
class HipRise(tauMs: Float, private val minVisibility: Float) : Metric {
    private val baseline = Baseline(tauMs)

    override fun measure(frame: PoseFrame): Float {
        if (frame.minVisibility(HIP_PAIR) < minVisibility) return Float.NaN
        val t = frame.torsoLength()
        if (t.isNaN()) return Float.NaN
        val y = frame.midY(HIP_PAIR)
        return (baseline.step(y, frame.timestampMs) - y) / t
    }

    override fun reset() = baseline.reset()
}

/**
 * How far the feet are off the floor, in torso lengths: the one signal that separates a real
 * jump from squatting, standing up or dropping into a plank (feet stay planted in all of those).
 * The floor level is the lowest the ankles have recently been: it follows the ankles down fast
 * (walking toward the camera) and back up slowly, so an airborne moment reads as clear lift.
 */
class AnkleLift(private val minVisibility: Float) : Metric {
    private val ankles = intArrayOf(Landmark.LEFT_ANKLE, Landmark.RIGHT_ANKLE)
    private var floor = Float.NaN
    private var lastT = 0L

    override fun measure(frame: PoseFrame): Float {
        if (frame.minVisibility(ankles) < minVisibility) return Float.NaN
        val t = frame.torsoLength()
        if (t.isNaN()) return Float.NaN
        val y = frame.midY(ankles) // larger y = lower on screen
        val dt = (frame.timestampMs - lastT).coerceAtLeast(0L)
        lastT = frame.timestampMs
        val lift: Float
        if (floor.isNaN()) {
            floor = y
            lift = 0f
        } else {
            lift = (floor - y) / t
            val tau = if (y > floor) FLOOR_DOWN_TAU_MS else FLOOR_UP_TAU_MS
            floor += (1f - exp(-dt / tau)) * (y - floor)
        }
        return lift
    }

    override fun reset() {
        floor = Float.NaN
    }

    private companion object {
        const val FLOOR_DOWN_TAU_MS = 300f
        const val FLOOR_UP_TAU_MS = 5000f
    }
}

/** Sideways hip travel from the usual spot, in torso lengths; [sign] picks which direction reads positive. */
class HipSway(tauMs: Float, private val sign: Float, private val minVisibility: Float) : Metric {
    private val baseline = Baseline(tauMs)

    override fun measure(frame: PoseFrame): Float {
        if (frame.minVisibility(HIP_PAIR) < minVisibility) return Float.NaN
        val t = frame.torsoLength()
        if (t.isNaN()) return Float.NaN
        val x = frame.midX(HIP_PAIR) * frame.aspect
        return sign * (x - baseline.step(x, frame.timestampMs)) / t
    }

    override fun reset() = baseline.reset()
}

/** One condition on a metric: `min <= value <= max`. [cue] is what to say when it fails. */
class Range(val metric: Metric, val min: Float, val max: Float, val cue: String = "") {
    /** 1 = holds, 0 = measurable but outside the range, -1 = not measurable right now. */
    fun check(frame: PoseFrame): Int {
        val v = metric.measure(frame)
        return when {
            v.isNaN() -> -1
            v in min..max -> 1
            else -> 0
        }
    }
}

/** A body position in a multi-phase move: every [Range] must hold at once. */
class Stage(val name: String, vararg val parts: Range)
