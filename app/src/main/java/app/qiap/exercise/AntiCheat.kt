package app.qiap.exercise

import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * Checks that the person in front of the camera is really awake and really doing the work
 * (details.md §9). Pure Kotlin, allocation-free per frame, driven by frame timestamps so every
 * rule replays identically in tests.
 *
 * Honest limit: a determined cheater can still fool any on-device check. The aim is to defeat
 * half-asleep shortcuts (waving the phone, a replayed video, tapping through), not an attacker.
 */
object FullBody {
    /** Body must cover at least this fraction of the frame height (details.md §9). */
    const val MIN_EXTENT = 0.40f

    private const val FIRST = Landmark.LEFT_SHOULDER
    private const val LAST = Landmark.RIGHT_ANKLE

    /**
     * How much of the frame the visible body spans: the larger of its height and its width
     * (a person lying down is wide, not tall), as a fraction of the frame height. 0 when too few
     * joints are visible to say.
     */
    fun extent(frame: PoseFrame, minVisibility: Float = 0.5f): Float {
        var minX = Float.MAX_VALUE
        var maxX = -Float.MAX_VALUE
        var minY = Float.MAX_VALUE
        var maxY = -Float.MAX_VALUE
        var n = 0
        for (i in FIRST..LAST) {
            if (frame.visibility[i] < minVisibility) continue
            n++
            if (frame.x[i] < minX) minX = frame.x[i]
            if (frame.x[i] > maxX) maxX = frame.x[i]
            if (frame.y[i] < minY) minY = frame.y[i]
            if (frame.y[i] > maxY) maxY = frame.y[i]
        }
        if (n < MIN_JOINTS) return 0f
        return maxOf(maxY - minY, (maxX - minX) * frame.aspect)
    }

    private const val MIN_JOINTS = 6
}

/**
 * Detects the phone being waved about rather than propped up. Feed it accelerometer samples
 * (m/s², any axis order); [shaking] turns on when the motion left after removing gravity stays
 * strong, and off again only once it settles, so it doesn't flicker.
 */
class ShakeDetector(
    private val onAbove: Float = 2.0f,
    private val offBelow: Float = 1.2f,
    private val smoothingMs: Float = 500f,
) {
    private var gx = 0f
    private var gy = 0f
    private var gz = 0f
    private var primed = false
    private var lastT = 0L
    private var energy = 0f

    @Volatile
    var shaking: Boolean = false
        private set

    fun reset() {
        primed = false
        energy = 0f
        shaking = false
    }

    fun onSample(ax: Float, ay: Float, az: Float, timestampMs: Long) {
        if (!primed) {
            gx = ax; gy = ay; gz = az
            lastT = timestampMs
            primed = true
            return
        }
        val dt = (timestampMs - lastT).coerceIn(1L, 200L)
        lastT = timestampMs
        // Slow average = gravity; what's left is the phone actually moving.
        val g = 0.92f
        gx = g * gx + (1f - g) * ax
        gy = g * gy + (1f - g) * ay
        gz = g * gz + (1f - g) * az
        val lx = ax - gx
        val ly = ay - gy
        val lz = az - gz
        val sq = lx * lx + ly * ly + lz * lz
        val a = dt / (smoothingMs + dt)
        energy += a * (sq - energy)
        val rms = sqrt(energy)
        shaking = if (shaking) rms > offBelow else rms > onAbove
    }
}

/** A one-second action that is hard to fake with a replayed recording (details.md §9). */
enum class Gesture(val prompt: String) {
    RIGHT_HAND_UP("Raise your right hand"),
    LEFT_HAND_UP("Raise your left hand"),
    BOTH_HANDS_UP("Both hands up high"),
    TOUCH_HEAD("Touch your head"),
}

/**
 * Picks a random [Gesture] and watches for it to be held for [holdMs]. Uses the person's own
 * left/right (MediaPipe labels are anatomical), so the mirrored front-camera preview is no problem.
 */
class Liveness(private val random: Random = Random.Default, private val holdMs: Long = 700) {
    var gesture: Gesture = pick(null)
        private set

    private var since = NOT_HELD

    /** Starts over with a new gesture, never the same one twice in a row. */
    fun next() {
        gesture = pick(gesture)
        since = NOT_HELD
    }

    /** True once the gesture has been held long enough. */
    fun onFrame(frame: PoseFrame): Boolean {
        if (!frame.hasPose || !performed(frame)) {
            since = NOT_HELD
            return false
        }
        if (since == NOT_HELD) since = frame.timestampMs
        return frame.timestampMs - since >= holdMs
    }

    private fun performed(f: PoseFrame): Boolean {
        val nose = Landmark.NOSE
        if (f.visibility[nose] < VIS) return false
        val lift = 0.03f // wrist clearly above the nose, in frame heights
        val rightUp = f.visibility[Landmark.RIGHT_WRIST] >= VIS && f.y[Landmark.RIGHT_WRIST] < f.y[nose] - lift
        val leftUp = f.visibility[Landmark.LEFT_WRIST] >= VIS && f.y[Landmark.LEFT_WRIST] < f.y[nose] - lift
        return when (gesture) {
            Gesture.RIGHT_HAND_UP -> rightUp && !leftUp
            Gesture.LEFT_HAND_UP -> leftUp && !rightUp
            Gesture.BOTH_HANDS_UP -> rightUp && leftUp
            Gesture.TOUCH_HEAD -> nearHead(f, Landmark.LEFT_WRIST) || nearHead(f, Landmark.RIGHT_WRIST)
        }
    }

    private fun nearHead(f: PoseFrame, wrist: Int): Boolean {
        if (f.visibility[wrist] < VIS) return false
        val t = f.torsoLength()
        if (t.isNaN()) return false
        val dx = (f.x[wrist] - f.x[Landmark.NOSE]) * f.aspect
        val dy = f.y[wrist] - f.y[Landmark.NOSE]
        return sqrt(dx * dx + dy * dy) < 0.5f * t
    }

    private fun pick(not: Gesture?): Gesture {
        val all = Gesture.entries
        var g = all[random.nextInt(all.size)]
        while (g == not) g = all[random.nextInt(all.size)]
        return g
    }

    private companion object {
        const val NOT_HELD = Long.MIN_VALUE
        const val VIS = 0.5f
    }
}

/** True once the body has stayed put (within [tolerance] torso lengths) for [holdMs]: "I'm awake" check. */
class Stillness(private val holdMs: Long = 3000, private val tolerance: Float = 0.15f) {
    private val shoulders = intArrayOf(Landmark.LEFT_SHOULDER, Landmark.RIGHT_SHOULDER)
    private val hips = intArrayOf(Landmark.LEFT_HIP, Landmark.RIGHT_HIP)
    private var anchorX = 0f
    private var anchorY = 0f
    private var since = NOT_SET

    fun reset() {
        since = NOT_SET
    }

    fun onFrame(frame: PoseFrame): Boolean {
        if (!frame.hasPose || frame.minVisibility(shoulders) < 0.5f || frame.minVisibility(hips) < 0.5f) {
            since = NOT_SET
            return false
        }
        val torso = frame.torsoLength()
        if (torso.isNaN()) {
            since = NOT_SET
            return false
        }
        val x = (frame.midX(shoulders) + frame.midX(hips)) / 2f * frame.aspect
        val y = (frame.midY(shoulders) + frame.midY(hips)) / 2f
        if (since == NOT_SET || abs(x - anchorX) > tolerance * torso || abs(y - anchorY) > tolerance * torso) {
            anchorX = x
            anchorY = y
            since = frame.timestampMs
            return false
        }
        return frame.timestampMs - since >= holdMs
    }

    private companion object {
        const val NOT_SET = Long.MIN_VALUE
    }
}
