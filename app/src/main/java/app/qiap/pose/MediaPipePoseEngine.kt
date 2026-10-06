package app.qiap.pose

import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import android.util.Log
import app.qiap.exercise.Landmark
import app.qiap.exercise.PoseFrame
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.ImageProcessingOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import java.util.concurrent.atomic.AtomicBoolean

/**
 * [PoseEngine] on MediaPipe Pose Landmarker (lite model, live-stream mode, one person).
 *
 * Per-frame path on our side is allocation-free: results are copied into two preallocated
 * [PoseFrame]s, rotation options are prebuilt, the [MPImage] wrapper is reused for the reused
 * camera bitmap, and visibility is read without boxing. MediaPipe itself still allocates its
 * result objects; that is outside our control.
 */
class MediaPipePoseEngine private constructor(
    context: Context,
    private val delegate: Delegate,
) : PoseEngine {

    private val frames = arrayOf(PoseFrame(), PoseFrame())
    private var writeIndex = 0
    private val inFlight = AtomicBoolean(false)
    @Volatile private var closed = false
    @Volatile private var submittedAt = 0L
    @Volatile private var pendingAspect = 1f

    private val rotationOptions = Array(4) { ImageProcessingOptions.builder().setRotationDegrees(it * 90).build() }
    private var wrappedBitmap: Bitmap? = null
    private var wrapped: MPImage? = null

    @set:Synchronized
    override var listener: PoseEngine.Listener? = null

    override val isBusy: Boolean get() = inFlight.get()
    override val delegateName: String = delegate.name

    private val landmarker: PoseLandmarker = PoseLandmarker.createFromOptions(
        context,
        PoseLandmarker.PoseLandmarkerOptions.builder()
            .setBaseOptions(BaseOptions.builder().setModelAssetPath(MODEL_ASSET).setDelegate(delegate).build())
            .setRunningMode(RunningMode.LIVE_STREAM)
            .setNumPoses(1)
            .setMinPoseDetectionConfidence(0.5f)
            .setMinPosePresenceConfidence(0.5f)
            .setMinTrackingConfidence(0.5f)
            .setOutputSegmentationMasks(false)
            .setResultListener { result, _ -> onResult(result) }
            .setErrorListener { e ->
                Log.w(TAG, "pose error", e)
                inFlight.set(false)
            }
            .build(),
    )

    @Synchronized
    override fun submit(bitmap: Bitmap, rotationDegrees: Int, timestampMs: Long): Boolean {
        if (closed || !inFlight.compareAndSet(false, true)) return false
        if (bitmap !== wrappedBitmap) {
            // Rebuilt only when the camera hands us a new buffer size (rare), not per frame.
            wrapped = BitmapImageBuilder(bitmap).build()
            wrappedBitmap = bitmap
        }
        val quarter = ((rotationDegrees % 360 + 360) % 360) / 90
        pendingAspect = if (quarter % 2 == 0) bitmap.width.toFloat() / bitmap.height else bitmap.height.toFloat() / bitmap.width
        submittedAt = SystemClock.elapsedRealtime()
        return try {
            landmarker.detectAsync(wrapped, rotationOptions[quarter], timestampMs)
            true
        } catch (e: RuntimeException) {
            // e.g. non-increasing timestamp after a camera restart; drop the frame.
            Log.w(TAG, "detectAsync rejected frame", e)
            inFlight.set(false)
            false
        }
    }

    private fun onResult(result: PoseLandmarkerResult) {
        val frame = frames[writeIndex]
        val poses = result.landmarks()
        if (poses.isEmpty()) {
            frame.hasPose = false
        } else {
            val lm = poses[0]
            val n = minOf(lm.size, Landmark.COUNT)
            for (i in 0 until n) {
                val p = lm[i]
                val vis = p.visibility()
                frame.set(i, p.x(), p.y(), p.z(), if (vis.isPresent) vis.get() else 0f)
            }
            frame.hasPose = true
        }
        frame.timestampMs = result.timestampMs()
        frame.aspect = pendingAspect
        val elapsed = SystemClock.elapsedRealtime() - submittedAt
        writeIndex = 1 - writeIndex
        inFlight.set(false)
        listener?.onPose(frame, elapsed)
    }

    @Synchronized
    override fun close() {
        if (closed) return
        closed = true
        listener = null
        landmarker.close()
    }

    companion object {
        private const val TAG = "QiapPose"
        const val MODEL_ASSET = "pose_landmarker_lite.task"

        /** GPU first; falls back to CPU on devices whose GPU delegate fails to initialize. Slow: call off the main thread. */
        fun create(context: Context): MediaPipePoseEngine = try {
            MediaPipePoseEngine(context.applicationContext, Delegate.GPU)
        } catch (e: RuntimeException) {
            Log.w(TAG, "GPU delegate unavailable, using CPU", e)
            MediaPipePoseEngine(context.applicationContext, Delegate.CPU)
        }
    }
}
