package app.qiap.pose

import android.graphics.Bitmap
import app.qiap.exercise.PoseFrame

/**
 * On-device pose estimation behind one seam (CLAUDE.md), so MediaPipe can be swapped or faked.
 *
 * Threading: [submit] is called from the camera analysis thread; [Listener.onPose] fires on the
 * engine's own thread. Exactly one frame is in flight at a time: [submit] returns false while
 * busy and the caller drops that camera frame (keep-only-latest).
 */
interface PoseEngine : AutoCloseable {

    fun interface Listener {
        /**
         * [frame] is owned by the engine and reused: valid until the next callback after this one
         * (engines double-buffer). Copy it with [PoseFrame.copyFrom] to keep it longer.
         * [inferenceMs] is submit-to-result wall time.
         */
        fun onPose(frame: PoseFrame, inferenceMs: Long)
    }

    var listener: Listener?

    /** True while a frame is being processed. Lets the caller skip copying pixels it would drop. */
    val isBusy: Boolean

    /** "GPU" or "CPU": which delegate actually loaded. Shown in debug stats. */
    val delegateName: String

    /**
     * Starts processing [bitmap] (upright after rotating by [rotationDegrees]). The caller must
     * not modify [bitmap] until [Listener.onPose] fires. [timestampMs] must increase per call.
     */
    fun submit(bitmap: Bitmap, rotationDegrees: Int, timestampMs: Long): Boolean
}
