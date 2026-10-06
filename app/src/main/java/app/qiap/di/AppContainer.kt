package app.qiap.di

import android.content.Context
import app.qiap.pose.MediaPipePoseEngine
import app.qiap.pose.PoseEngine
import java.time.Clock

/**
 * Manual DI root. One instance per process, owned by [app.qiap.QiapApp].
 * Add dependencies here as plain constructor calls (`val x by lazy { X(...) }`); no framework.
 */
class AppContainer(context: Context) {
    val appContext: Context = context.applicationContext

    /** Injected everywhere time matters so alarm math is testable. */
    val clock: Clock = Clock.systemDefaultZone()

    /**
     * A new pose engine per workout (it holds the model and GPU context, so it's closed when the
     * workout ends rather than kept alive). Slow: call off the main thread.
     */
    fun createPoseEngine(): PoseEngine = MediaPipePoseEngine.create(appContext)
}
