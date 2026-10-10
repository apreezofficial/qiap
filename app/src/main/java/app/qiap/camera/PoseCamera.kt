package app.qiap.camera

import android.content.Context
import android.graphics.Bitmap
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.lifecycle.LifecycleOwner
import app.qiap.pose.PoseEngine
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Binds a CameraX preview + analysis pair and feeds analysis frames to a [PoseEngine].
 *
 * Preview and analysis share a 4:3 aspect so landmark coordinates line up with what the user sees.
 * Analysis runs at ~640×480 RGBA, keep-only-latest. Frames are dropped (not queued) while the
 * engine is busy, and pixels are copied into one reused [Bitmap], so steady state allocates nothing.
 */
class PoseCamera(private val context: Context, private val engine: PoseEngine) {
    private val _surfaceRequest = MutableStateFlow<SurfaceRequest?>(null)
    private val _mirrored = MutableStateFlow(true)

    /** Hand this to `CameraXViewfinder`. Null until the camera is bound. */
    val surfaceRequest: StateFlow<SurfaceRequest?> = _surfaceRequest

    /** True for the front camera: the overlay must flip x to match the mirrored preview. */
    val mirrored: StateFlow<Boolean> = _mirrored

    // Touched only on the analysis thread.
    private var bitmap: Bitmap? = null
    private var packed: ByteBuffer? = null

    /** Binds to [owner] and suspends until cancelled, then unbinds. Run from a LaunchedEffect. */
    suspend fun run(owner: LifecycleOwner) {
        val provider = ProcessCameraProvider.awaitInstance(context)
        val front = provider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)
        _mirrored.value = front
        val selector = if (front) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
        val ratio = AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY
        // One analysis thread per bind, shut down only after the analyzer is detached.
        val executor: ExecutorService = Executors.newSingleThreadExecutor { r -> Thread(r, "qiap-pose-analysis") }

        val preview = Preview.Builder()
            .setResolutionSelector(ResolutionSelector.Builder().setAspectRatioStrategy(ratio).build())
            .build()
            .also { it.setSurfaceProvider { request -> _surfaceRequest.value = request } }

        val analysis = ImageAnalysis.Builder()
            .setResolutionSelector(
                ResolutionSelector.Builder()
                    .setAspectRatioStrategy(ratio)
                    .setResolutionStrategy(
                        ResolutionStrategy(Size(640, 480), ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER),
                    )
                    .build(),
            )
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .build()
            .also { it.setAnalyzer(executor, ::analyze) }

        provider.unbindAll()
        provider.bindToLifecycle(owner, selector, preview, analysis)
        try {
            awaitCancellation()
        } finally {
            analysis.clearAnalyzer()
            provider.unbind(preview, analysis)
            executor.shutdown()
            _surfaceRequest.value = null
        }
    }

    private fun analyze(image: ImageProxy) {
        try {
            if (engine.isBusy) return
            val w = image.width
            val h = image.height
            val bmp = bitmap?.takeIf { it.width == w && it.height == h }
                ?: Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).also { bitmap = it }
            val plane = image.planes[0]
            val src = plane.buffer
            src.rewind()
            if (plane.rowStride == w * 4) {
                bmp.copyPixelsFromBuffer(src)
            } else {
                // Rows are padded: pack them into a reused tight buffer first.
                val dst = packed?.takeIf { it.capacity() == w * h * 4 }
                    ?: ByteBuffer.allocateDirect(w * h * 4).also { packed = it }
                dst.clear()
                for (row in 0 until h) {
                    src.limit(row * plane.rowStride + w * 4)
                    src.position(row * plane.rowStride)
                    dst.put(src)
                }
                src.limit(src.capacity())
                dst.rewind()
                bmp.copyPixelsFromBuffer(dst)
            }
            engine.submit(bmp, image.imageInfo.rotationDegrees, image.imageInfo.timestamp / 1_000_000)
        } finally {
            image.close()
        }
    }
}
