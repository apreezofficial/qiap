package app.qiap.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import app.qiap.MainActivity
import java.time.Clock
import java.time.ZonedDateTime

/**
 * Puts alarms into AlarmManager with `setAlarmClock`: the most reliable alarm type. The system
 * never delays it, leaves Doze for it, and shows it in the status bar / lock screen.
 *
 * One PendingIntent per alarm id. The fire intent carries only the id; the receiver re-reads the
 * store, so an edited alarm can never ring with stale settings.
 */
class AlarmScheduler(
    private val context: Context,
    private val store: AlarmStore,
    private val clock: Clock,
    private val snoozeStore: JsonValueStore<PendingSnooze>? = null,
) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    /** Exact alarms allowed? Always true on 33+ (USE_EXACT_ALARM); user-revocable on 31-32. */
    fun canScheduleExact(): Boolean = Build.VERSION.SDK_INT < 31 || alarmManager.canScheduleExactAlarms()

    /** (Re)schedules [alarm] if enabled, cancels it otherwise. Returns the trigger time, if any. */
    fun sync(alarm: Alarm): ZonedDateTime? {
        if (!alarm.enabled) {
            cancel(alarm.id)
            return null
        }
        val at = nextTrigger(alarm, ZonedDateTime.now(clock))
        setAt(at.toInstant().toEpochMilli(), fireIntent(alarm.id, test = null))
        Log.i(TAG, "alarm ${alarm.id} scheduled for $at")
        return at
    }

    fun cancel(id: Int) {
        alarmManager.cancel(fireIntent(id, test = null))
    }

    /** Reschedules every enabled alarm, and any snooze still waiting. Called after boot, updates, and clock/timezone changes. */
    fun syncAll() {
        for (a in store.alarms.value) sync(a)
        snoozeStore?.get()?.let { pending ->
            // A snooze that came due while the phone was off rings a few seconds after boot.
            val at = maxOf(pending.atMs, clock.millis() + 5_000)
            setAt(at, snoozeIntent(pending.request))
        }
    }

    /** Rings [request] again at [atMs]. Persisted, so a reboot during the snooze keeps it. */
    fun scheduleSnooze(request: RingRequest, atMs: Long) {
        snoozeStore?.set(PendingSnooze(request, atMs))
        setAt(atMs, snoozeIntent(request))
        Log.i(TAG, "snooze scheduled for alarm ${request.alarmId} at $atMs")
    }

    /** Called once the snoozed ring has actually started. */
    fun snoozeFired() {
        snoozeStore?.clear()
    }

    /** "Test alarm in 10 s": a one-off ring that isn't stored and doesn't touch real alarms. */
    fun scheduleTest(inMillis: Long, exerciseId: String, target: Int) {
        val at = clock.millis() + inMillis
        setAt(at, fireIntent(TEST_ID, test = TestSpec(exerciseId, target, at)))
    }

    /** The soonest enabled alarm and when it rings, for the Home countdown. */
    fun nextUp(now: ZonedDateTime = ZonedDateTime.now(clock)): Pair<Alarm, ZonedDateTime>? =
        store.alarms.value.filter { it.enabled }.map { it to nextTrigger(it, now) }.minByOrNull { it.second }

    private fun setAt(triggerAtMs: Long, operation: PendingIntent) {
        if (canScheduleExact()) {
            val show = PendingIntent.getActivity(
                context, 0, Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
            alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(triggerAtMs, show), operation)
        } else {
            // Exact alarms revoked (API 31-32 only). Best effort until the user re-allows it;
            // the Setup screen and the Home reliability card both flag this state.
            Log.w(TAG, "exact alarms not allowed, falling back to inexact")
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMs, operation)
        }
    }

    private class TestSpec(val exerciseId: String, val target: Int, val atMs: Long)

    private fun snoozeIntent(request: RingRequest): PendingIntent {
        val intent = request.toIntent(
            Intent(context, AlarmReceiver::class.java)
                .setAction(AlarmReceiver.ACTION_FIRE)
                .putExtra(AlarmReceiver.EXTRA_ALARM_ID, SNOOZE_ID)
                .putExtra(AlarmReceiver.EXTRA_IS_SNOOZE, true),
        )
        return PendingIntent.getBroadcast(context, SNOOZE_ID, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun fireIntent(id: Int, test: TestSpec?): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java)
            .setAction(AlarmReceiver.ACTION_FIRE)
            .putExtra(AlarmReceiver.EXTRA_ALARM_ID, id)
        if (test != null) {
            intent.putExtra(AlarmReceiver.EXTRA_TEST_EXERCISE, test.exerciseId)
                .putExtra(AlarmReceiver.EXTRA_TEST_TARGET, test.target)
                .putExtra(AlarmReceiver.EXTRA_TEST_AT, test.atMs)
        }
        return PendingIntent.getBroadcast(
            context, id, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    companion object {
        private const val TAG = "QiapAlarm"
        const val TEST_ID = Int.MAX_VALUE
        const val SNOOZE_ID = Int.MAX_VALUE - 1
    }
}
