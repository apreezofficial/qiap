package app.qiap.camera

import android.content.Context
import android.util.Log
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

/** Where video proofs live and how long they stay (details.md §10: private, never uploaded, auto-deleted). */
object ProofStore {
    const val DEFAULT_RETENTION_DAYS = 14
    private const val DIR = "proofs"
    private const val PREFS = "settings"
    private const val KEY_DAYS = "proof_days"

    fun dir(context: Context): File = File(context.filesDir, DIR).apply { mkdirs() }

    fun newFile(context: Context, nowMs: Long = System.currentTimeMillis()): File = File(dir(context), "proof-$nowMs.mp4")

    fun retentionDays(context: Context): Int =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(KEY_DAYS, DEFAULT_RETENTION_DAYS)

    fun setRetentionDays(context: Context, days: Int) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putInt(KEY_DAYS, days).apply()
    }

    /** Deletes proofs older than [retentionDays]. Safe to call often; returns how many files went. */
    fun cleanup(context: Context, retentionDays: Int = retentionDays(context), nowMs: Long = System.currentTimeMillis()): Int {
        val cutoff = nowMs - retentionDays * DAY_MS
        var deleted = 0
        dir(context).listFiles()?.forEach { f ->
            if (f.isFile && f.lastModified() < cutoff && f.delete()) deleted++
        }
        return deleted
    }

    /** True if the proof still exists (it may have aged out). */
    fun exists(path: String?): Boolean = path != null && File(path).let { it.isFile && it.length() > 0 }

    private const val DAY_MS = 24L * 60 * 60 * 1000
}

enum class ProofState { OFF, RECORDING, UNAVAILABLE }

/**
 * Records the workout to a private MP4 next to the pose analysis. 720p where the phone allows it
 * (else the nearest lower quality), H.264, no audio so no microphone permission, capped at 60 s.
 * Not every phone can run preview + analysis + recording at once: [PoseCamera] binds this only
 * if the combination works, and the alarm carries on without video otherwise ([state]).
 */
class ProofRecorder(private val context: Context) {
    val videoCapture: VideoCapture<Recorder> = VideoCapture.withOutput(
        Recorder.Builder()
            .setQualitySelector(
                QualitySelector.from(Quality.HD, FallbackStrategy.lowerQualityOrHigherThan(Quality.SD)),
            )
            .setTargetVideoEncodingBitRate(BITRATE)
            .build(),
    )

    private val _state = MutableStateFlow(ProofState.OFF)
    val state: StateFlow<ProofState> = _state

    /** The file being written, once [start] succeeds. */
    @Volatile var file: File? = null
        private set

    private var recording: Recording? = null

    fun markUnavailable() {
        _state.value = ProofState.UNAVAILABLE
    }

    /** Starts recording. Never throws: any failure just marks the proof unavailable. */
    fun start() {
        if (recording != null) return
        try {
            val out = ProofStore.newFile(context)
            val options = FileOutputOptions.Builder(out).setDurationLimitMillis(MAX_MS).build()
            val executor = ContextCompat.getMainExecutor(context)
            recording = videoCapture.output.prepareRecording(context, options).start(executor) { event ->
                if (event is androidx.camera.video.VideoRecordEvent.Finalize && event.hasError()) {
                    Log.w(TAG, "proof recording ended with error ${event.error}")
                }
            }
            file = out
            _state.value = ProofState.RECORDING
        } catch (e: Exception) {
            Log.w(TAG, "could not start proof recording", e)
            _state.value = ProofState.UNAVAILABLE
        }
    }

    fun stop() {
        recording?.runCatching { stop() }
        recording = null
        if (_state.value == ProofState.RECORDING) _state.value = ProofState.OFF
    }

    private companion object {
        const val TAG = "QiapProof"
        const val MAX_MS = 60_000L
        const val BITRATE = 2_000_000
    }
}
