package app.qiap.di

import android.content.Context
import app.qiap.alarm.AlarmScheduler
import app.qiap.alarm.AlarmStore
import app.qiap.alarm.HistoryStore
import app.qiap.alarm.JsonValueStore
import app.qiap.alarm.PendingSnooze
import app.qiap.alarm.PersistedRing
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
    /** The ring in progress, so a killed process can ring again. */
    val ringStore by lazy { JsonValueStore(appContext, "ringing.json", PersistedRing.serializer()) }
    private val snoozeStore by lazy { JsonValueStore(appContext, "snooze.json", PendingSnooze.serializer()) }
    val alarmScheduler by lazy { AlarmScheduler(appContext, alarmStore, clock, snoozeStore) }
    val reliability by lazy { Reliability(appContext, alarmScheduler) }

    /**
     * A new pose engine per workout (it holds the model and GPU context, so it's closed when the
     * workout ends rather than kept alive). Slow: call off the main thread.
     */
    fun createPoseEngine(): PoseEngine = takeWarmPoseEngine() ?: MediaPipePoseEngine.create(appContext)

    private var warmEngine: PoseEngine? = null
    private var warming = false

    /**
     * Loads the pose model in the background while an alarm rings, so "Start workout" opens the
     * camera instantly. Best effort: any failure just means the workout loads it itself.
     */
    fun prewarmPoseEngine() {
        synchronized(this) {
            if (warmEngine != null || warming) return
            warming = true
        }
        Thread({
            val engine = try { MediaPipePoseEngine.create(appContext) } catch (e: Throwable) { null }
            synchronized(this) {
                warming = false
                if (engine != null) warmEngine = engine
            }
        }, "qiap-pose-prewarm").start()
    }

    private fun takeWarmPoseEngine(): PoseEngine? = synchronized(this) { warmEngine.also { warmEngine = null } }

    /** Frees a prewarmed engine nobody used (the alarm ended without a workout). */
    fun discardWarmPoseEngine() {
        takeWarmPoseEngine()?.close()
    }
}
