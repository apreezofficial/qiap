package app.qiap.feature.workout

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.qiap.exercise.ExerciseSpec
import app.qiap.exercise.Fixture
import app.qiap.exercise.PoseFrame
import app.qiap.exercise.RepCounter
import app.qiap.exercise.RepEvent
import app.qiap.pose.PoseEngine

/**
 * Glue between the pose thread and the workout UI. [onPose] runs on the engine thread: it feeds
 * the [RepCounter], keeps the latest frame for the overlay, and publishes a handful of Compose
 * states. State writes only happen when a value changes, and nothing here allocates per frame
 * (except while debug fixture recording is on).
 */
class PoseSession(val spec: ExerciseSpec) : PoseEngine.Listener {

    private val counter = RepCounter(spec)

    // Overlay double buffer: the pose thread writes the back frame, then flips `front`.
    private val overlayFrames = arrayOf(PoseFrame(), PoseFrame())
    @Volatile private var front = 0

    /** The frame the overlay should draw. Read it inside a draw block that also reads [frameTick]. */
    val overlayFrame: PoseFrame get() = overlayFrames[front]

    /** Bumped once per processed frame so draw-phase readers invalidate without recomposing. */
    val frameTick = mutableIntStateOf(0)

    var reps by mutableIntStateOf(0)
        private set
    /** Whether the needed body parts are in view. */
    var tracking by mutableStateOf(false)
        private set
    /** Short coaching line, or null when all is well. Always a constant string (no per-frame allocation). */
    var cue by mutableStateOf<String?>(spec.stepBackCue)
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

        val event = counter.onFrame(frame)
        if (counter.reps != reps) reps = counter.reps
        if (counter.tracking != tracking) tracking = counter.tracking
        val newCue = when {
            !counter.tracking -> spec.stepBackCue
            counter.gateCue != null -> counter.gateCue
            counter.needsMoreDepth || event == RepEvent.REP_SHALLOW -> spec.depthCue
            else -> null
        }
        if (newCue != cue) cue = newCue
        val off = counter.tracking && (counter.needsMoreDepth || !counter.lastRepGood)
        if (off != offForm) offForm = off

        measure(frame.timestampMs, inferenceMs)
        recording?.let { synchronized(it) { Fixture.appendLine(frame, it) } }
        frameTick.intValue++
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
}
