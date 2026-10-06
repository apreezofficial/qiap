package app.qiap.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import app.qiap.QiapApp
import app.qiap.R
import app.qiap.core.common.twelveHour
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate
import java.time.LocalTime

/** Everything the ringing UI needs, independent of the store (also used for test alarms). */
data class RingRequest(
    val alarmId: Int,
    val hour: Int,
    val minute: Int,
    val label: String,
    val exerciseId: String,
    val target: Int,
    val volume: Float,
) {
    fun toIntent(intent: Intent): Intent = intent
        .putExtra(K_ID, alarmId).putExtra(K_H, hour).putExtra(K_M, minute).putExtra(K_LABEL, label)
        .putExtra(K_EX, exerciseId).putExtra(K_TARGET, target).putExtra(K_VOL, volume)

    companion object {
        private const val K_ID = "ring.id"
        private const val K_H = "ring.h"
        private const val K_M = "ring.m"
        private const val K_LABEL = "ring.label"
        private const val K_EX = "ring.ex"
        private const val K_TARGET = "ring.target"
        private const val K_VOL = "ring.vol"

        fun from(a: Alarm) = RingRequest(a.id, a.hour, a.minute, a.label.ifBlank { a.daysLabel() }, a.exerciseId, a.target, a.volume)

        fun fromIntent(i: Intent): RingRequest? = if (!i.hasExtra(K_ID)) null else RingRequest(
            i.getIntExtra(K_ID, -1), i.getIntExtra(K_H, 0), i.getIntExtra(K_M, 0), i.getStringExtra(K_LABEL) ?: "",
            i.getStringExtra(K_EX) ?: "squat", i.getIntExtra(K_TARGET, 12), i.getFloatExtra(K_VOL, 0.8f),
        )
    }
}

/** Live ringing state for the UI. */
data class RingState(val request: RingRequest, val startedAtMs: Long, val workoutStarted: Boolean)

/**
 * Foreground service that owns a ringing alarm: sound, vibration, wake lock, the full-screen
 * notification that opens [RingingActivity], and the outcome log. It keeps ringing until the
 * workout is done or the user holds the fallback, with a hard stop after [MAX_RING_MS] so a phone
 * left in a drawer doesn't ring forever.
 *
 * FGS type: systemExempted (allowed for apps holding USE_EXACT_ALARM / SCHEDULE_EXACT_ALARM, no
 * boot or time limits), with specialUse as the fallback if exact alarms were revoked.
 */
class AlarmService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var ringer: AlarmRinger
    private var wakeLock: PowerManager.WakeLock? = null
    private val timeout = Runnable { finishRing(Outcome.MISSED, reps = 0, seconds = (MAX_RING_MS / 1000).toInt()) }

    override fun onCreate() {
        super.onCreate()
        ringer = AlarmRinger(this)
        ensureChannel(this)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_RING -> {
                val request = RingRequest.fromIntent(intent)
                val current = _state.value
                // Every startForegroundService must be answered with startForeground, even for a
                // second alarm arriving mid-ring; otherwise the system kills the app.
                goForeground((current?.request ?: request) ?: return START_NOT_STICKY)
                if (current == null && request != null) startRing(request)
                else if (request != null) Log.w(TAG, "alarm ${request.alarmId} fired while ${current?.request?.alarmId} rings; ignoring")
            }
            ACTION_WORKOUT -> {
                _state.value = _state.value?.copy(workoutStarted = true)
                ringer.duck()
            }
            ACTION_FINISH -> finishRing(
                Outcome.valueOf(intent.getStringExtra(EXTRA_OUTCOME) ?: Outcome.FALLBACK.name),
                intent.getIntExtra(EXTRA_REPS, 0),
                intent.getIntExtra(EXTRA_SECONDS, 0),
            )
            else -> if (_state.value == null) stopSelf()
        }
        // If the process dies mid-ring, redeliver the RING intent so it rings again.
        return START_REDELIVER_INTENT
    }

    private fun startRing(request: RingRequest) {
        _state.value = RingState(request, System.currentTimeMillis(), workoutStarted = false)
        wakeLock = getSystemService(PowerManager::class.java)
            .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "qiap:ringing")
            .apply { acquire(MAX_RING_MS + 60_000) }
        ringer.start(request.volume)
        handler.postDelayed(timeout, MAX_RING_MS)
        // Also try to open the ringing screen directly; the full-screen intent covers locked phones.
        runCatching { startActivity(RingingActivity.intent(this).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    }

    private fun goForeground(request: RingRequest) {
        val type = if (Build.VERSION.SDK_INT >= 34) {
            val exactOk = (application as QiapApp).container.alarmScheduler.canScheduleExact()
            if (exactOk) ServiceInfo.FOREGROUND_SERVICE_TYPE_SYSTEM_EXEMPTED else ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        } else {
            0
        }
        ServiceCompat.startForeground(this, NOTIFICATION_ID, buildNotification(this, request), type)
    }

    private fun finishRing(outcome: Outcome, reps: Int, seconds: Int) {
        val s = _state.value
        if (s != null && s.request.alarmId != AlarmScheduler.TEST_ID) {
            val now = LocalTime.now()
            (application as QiapApp).container.historyStore.record(
                HistoryEntry(
                    alarmId = s.request.alarmId,
                    epochDay = LocalDate.now().toEpochDay(),
                    endedAtMs = System.currentTimeMillis(),
                    alarmMinuteOfDay = s.request.hour * 60 + s.request.minute,
                    endedMinuteOfDay = now.hour * 60 + now.minute,
                    outcome = outcome,
                    exerciseId = s.request.exerciseId,
                    reps = reps,
                    seconds = seconds,
                ),
            )
        }
        Log.i(TAG, "ring ended: $outcome, $reps reps")
        handler.removeCallbacks(timeout)
        ringer.stop()
        wakeLock?.takeIf { it.isHeld }?.release()
        wakeLock = null
        _state.value = null
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        ringer.stop()
        wakeLock?.takeIf { it.isHeld }?.release()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "QiapAlarm"
        private const val CHANNEL_ID = "ringing"
        private const val NOTIFICATION_ID = 0x0A1A
        const val MAX_RING_MS = 30 * 60 * 1000L

        private const val ACTION_RING = "app.qiap.action.RING"
        private const val ACTION_WORKOUT = "app.qiap.action.WORKOUT_STARTED"
        private const val ACTION_FINISH = "app.qiap.action.FINISH"
        private const val EXTRA_OUTCOME = "outcome"
        private const val EXTRA_REPS = "reps"
        private const val EXTRA_SECONDS = "seconds"

        private val _state = MutableStateFlow<RingState?>(null)
        /** Non-null while an alarm is ringing (including during its workout). */
        val state: StateFlow<RingState?> = _state

        fun ring(context: Context, request: RingRequest) {
            ContextCompat.startForegroundService(
                context, request.toIntent(Intent(context, AlarmService::class.java).setAction(ACTION_RING)),
            )
        }

        /** Called from the ringing UI (app in foreground, so a plain startService is allowed). */
        fun workoutStarted(context: Context) {
            context.startService(Intent(context, AlarmService::class.java).setAction(ACTION_WORKOUT))
        }

        fun finish(context: Context, outcome: Outcome, reps: Int, seconds: Int) {
            context.startService(
                Intent(context, AlarmService::class.java).setAction(ACTION_FINISH)
                    .putExtra(EXTRA_OUTCOME, outcome.name).putExtra(EXTRA_REPS, reps).putExtra(EXTRA_SECONDS, seconds),
            )
        }

        fun ensureChannel(context: Context) {
            val nm = context.getSystemService(NotificationManager::class.java)
            if (nm.getNotificationChannel(CHANNEL_ID) != null) return
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Ringing alarms", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Shows the alarm over the lock screen while it rings."
                    setSound(null, null) // the service plays its own sound on the alarm stream
                    enableVibration(false)
                    lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                },
            )
        }

        private fun buildNotification(context: Context, r: RingRequest): Notification {
            val open = PendingIntent.getActivity(
                context, 0, RingingActivity.intent(context),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
            val (time, amPm) = twelveHour(r.hour, r.minute)
            return NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_seal)
                .setContentTitle("$time $amPm · ${r.label}")
                .setContentText("${r.target} reps to silence me.")
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOngoing(true)
                .setAutoCancel(false)
                .setContentIntent(open)
                .setFullScreenIntent(open, true)
                .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
                .build()
        }
    }
}
