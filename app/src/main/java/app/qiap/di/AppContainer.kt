package app.qiap.di

import android.content.Context
import app.qiap.alarm.AlarmScheduler
import app.qiap.alarm.AlarmStore
import app.qiap.alarm.HistoryStore
import app.qiap.alarm.Reliability
import app.qiap.pose.MediaPipePoseEngine
import app.qiap.pose.PoseEngine
import java.time.Clock

/**
 * Manual DI root. One instance per process, owned by [app.qiap.QiapApp].
 * Add dependencies here as plain constructor calls (`val x by lazy { X(...) }`); no framework.
 * Everything the alarm path touches must work in direct boot (device-protected storage only).
 */
class AppContainer(context: Context) {
    val appContext: Context = context.applicationContext

    /** Injected everywhere time matters so alarm math is testable. */
    val clock: Clock = Clock.systemDefaultZone()

    val alarmStore by lazy { AlarmStore(appContext) }
    val historyStore by lazy { HistoryStore(appContext) }
    val alarmScheduler by lazy { AlarmScheduler(appContext, alarmStore, clock) }
    val reliability by lazy { Reliability(appContext, alarmScheduler) }

    /**
     * A new pose engine per workout (it holds the model and GPU context, so it's closed when the
     * workout ends rather than kept alive). Slow: call off the main thread.
     */
    fun createPoseEngine(): PoseEngine = MediaPipePoseEngine.create(appContext)
}
