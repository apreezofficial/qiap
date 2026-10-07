package app.qiap.feature.workout

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.qiap.exercise.ExerciseSpec
import app.qiap.exercise.Fixture
import app.qiap.exercise.FullBody
import app.qiap.exercise.Liveness
import app.qiap.exercise.PoseFrame
import app.qiap.exercise.RepCounter
import app.qiap.exercise.RepEvent
import app.qiap.exercise.ShakeDetector
import app.qiap.exercise.Stillness
import app.qiap.pose.PoseEngine

/** Where a real-alarm workout is: prove you're awake, do the reps, prove it again, done. */
enum class WorkoutPhase { LIVENESS, COUNTING, FINAL_STILL, FINAL_GESTURE, DONE }

/**
 * Glue between the pose thread and the workout UI. [onPose] runs on the engine thread: it feeds
 * the [RepCounter], keeps the latest frame for the overlay, and publishes a handful of Compose
 * states. State writes only happen when a value changes, and nothing here allocates per frame
 * (except while debug fixture recording is on).
 *
 * With [antiCheat] (real alarms; off for practice) the session runs the details.md §9 checks:
 * a random gesture before counting, reps ignored while the phone is shaken or the body is too
 * small in frame, and after the last rep a stand-still plus a second gesture before it is done.
 * Each wait has a time limit, so a camera that cannot see the gesture never traps anyone.
 */
class PoseSession(
    val spec: ExerciseSpec,
    private val target: Int = Int.MAX_VALUE,
    private val antiCheat: Boolean = false,
) : PoseEngine.Listener {

    private val counter = RepCounter(spec)
    private val liveness = Liveness()
    private val stillness = Stillness()

    /** Fed accelerometer samples by the screen; reps are ignored while it says the phone is shaking. */
    val shake = ShakeDetector()

    // Overlay double buffer: the pose thread writes the back frame, then flips `front`.
    private val overlayFrames = arrayOf(PoseFrame(), PoseFrame())
    @Volatile private var front = 0

    /** The frame the overlay should draw. Read it inside a draw block that also reads [frameTick]. */
    val overlayFrame: PoseFrame get() = overlayFrames[front]

    /** Bumped once per processed frame so draw-phase readers invalidate without recomposing. */
    val frameTick = mutableIntStateOf(0)

    var phase by mutableStateOf(if (antiCheat) WorkoutPhase.LIVENESS else WorkoutPhase.COUNTING)
        private set
    private var phaseStartedAt = Long.MIN_VALUE

    var reps by mutableIntStateOf(0)
        private set
    /** Whether the needed body parts are in view. */
    var tracking by mutableStateOf(false)
        private set
    /** Short coaching line, or null when all is well. Always a constant string (no per-frame allocation). */
    var cue by mutableStateOf<String?>(if (antiCheat) Gestures.first(liveness) else spec.stepBackCue)
        private set
    /** Lower body is short of good depth right now (overlay paints hips/knees saffron). */
    var offForm by mutableStateOf(false)
        private set

    var fps by mutableIntStateOf(0)
        private set
    var inferenceMs by mutableLongStateOf(0L)
        private set

    private var windowStart = Long.MIN_VALUE
    private var windowFrames = 0
    private var inferenceSum = 0L

    @Volatile private var recording: StringBuilder? = null
    val isRecording: Boolean get() = recording != null

    override fun onPose(frame: PoseFrame, inferenceMs: Long) {
        val back = 1 - front
        overlayFrames[back].copyFrom(frame)
        front = back

        if (phaseStartedAt == Long.MIN_VALUE) phaseStartedAt = frame.timestampMs
        when (phase) {
            WorkoutPhase.LIVENESS -> stepGesture(frame, WorkoutPhase.COUNTING, GESTURE_TIMEOUT_MS)
            WorkoutPhase.COUNTING -> stepCounting(frame)
            WorkoutPhase.FINAL_STILL -> {
                showCue(STILL_CUE)
                if (stillness.onFrame(frame)) enter(WorkoutPhase.FINAL_GESTURE, frame)
                else if (frame.timestampMs - phaseStartedAt > STILL_TIMEOUT_MS) enter(WorkoutPhase.DONE, frame)
            }
            WorkoutPhase.FINAL_GESTURE -> stepGesture(frame, WorkoutPhase.DONE, GESTURE_TIMEOUT_MS)
            WorkoutPhase.DONE -> showCue(null)
        }

        measure(frame.timestampMs, inferenceMs)
        recording?.let { synchronized(it) { Fixture.appendLine(frame, it) } }
        frameTick.intValue++
    }

    private fun stepGesture(frame: PoseFrame, next: WorkoutPhase, timeoutMs: Long) {
        showCue(Gestures.prompt(liveness))
        if (liveness.onFrame(frame) || frame.timestampMs - phaseStartedAt > timeoutMs) enter(next, frame)
    }

    private fun stepCounting(frame: PoseFrame) {
        // Real alarms only: ignore reps while the phone is being shaken or the body is too small to trust.
        val shaking = antiCheat && shake.shaking
        val tooSmall = antiCheat && FullBody.extent(frame) < FullBody.MIN_EXTENT
        val gated = shaking || tooSmall
        val event = if (gated) RepEvent.NONE else counter.onFrame(frame)
        if (counter.reps != reps) reps = counter.reps
        val seen = !gated && counter.tracking
        if (seen != tracking) tracking = seen
        showCue(
            when {
                shaking -> SHAKE_CUE
                tooSmall -> FRAME_CUE
                !counter.tracking -> spec.stepBackCue
                counter.gateCue != null -> counter.gateCue
                counter.needsMoreDepth || event == RepEvent.REP_SHALLOW -> spec.depthCue
                else -> null
            },
        )
        val off = seen && (counter.needsMoreDepth || !counter.lastRepGood)
        if (off != offForm) offForm = off
        if (counter.reps >= target) enter(if (antiCheat) WorkoutPhase.FINAL_STILL else WorkoutPhase.DONE, frame)
    }

    private fun enter(next: WorkoutPhase, frame: PoseFrame) {
        phase = next
        phaseStartedAt = frame.timestampMs
        if (next == WorkoutPhase.FINAL_GESTURE) liveness.next()
        if (next == WorkoutPhase.FINAL_STILL) stillness.reset()
        if (next != WorkoutPhase.COUNTING && offForm) offForm = false
    }

    private fun showCue(newCue: String?) {
        if (newCue != cue) cue = newCue
    }

    private fun measure(timestampMs: Long, inference: Long) {
        if (windowStart == Long.MIN_VALUE) windowStart = timestampMs
        windowFrames++
        inferenceSum += inference
        val span = timestampMs - windowStart
        if (span >= 1000) {
            val newFps = (windowFrames * 1000 / span).toInt()
            if (newFps != fps) fps = newFps
            val avg = inferenceSum / windowFrames
            if (avg != inferenceMs) inferenceMs = avg
            windowStart = timestampMs
            windowFrames = 0
            inferenceSum = 0
        }
    }

    /** Debug: start capturing frames as a landmark fixture. */
    fun startRecording(note: String) {
        recording = StringBuilder(64 * 1024).append("# ").append(note).append('\n').append(Fixture.HEADER).append('\n')
    }

    /** Debug: stop capturing and return the fixture text (null if not recording). */
    fun stopRecording(): String? {
        val sb = recording ?: return null
        recording = null
        return synchronized(sb) { sb.toString() }
    }

    /** Gesture prompts are enum constants, so reading them per frame allocates nothing. */
    private object Gestures {
        fun prompt(l: Liveness): String = l.gesture.prompt
        fun first(l: Liveness): String = l.gesture.prompt
    }

    private companion object {
        const val SHAKE_CUE = "Prop the phone up and keep it still"
        const val FRAME_CUE = "Step back so I can see all of you"
        const val STILL_CUE = "Done! Stand still for a moment"
        const val GESTURE_TIMEOUT_MS = 20_000L
        const val STILL_TIMEOUT_MS = 20_000L
    }
}
