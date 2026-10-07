package app.qiap.alarm

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import app.qiap.QiapApp
import java.time.Instant
import java.time.ZoneId

/**
 * Fired by AlarmManager. Starts the ringing service (allowed from the background: exact-alarm
 * FGS exemption), then arms the next occurrence before anything else can go wrong.
 */
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_FIRE) return
        val container = (context.applicationContext as QiapApp).container
        val id = intent.getIntExtra(EXTRA_ALARM_ID, -1)

        if (intent.getBooleanExtra(EXTRA_IS_SNOOZE, false)) {
            // A snoozed alarm rings again with the settings it had when it was snoozed.
            val again = RingRequest.fromIntent(intent) ?: return
            container.alarmScheduler.snoozeFired()
            AlarmService.ring(context, again)
            return
        }

        val request = if (id == AlarmScheduler.TEST_ID) {
            val at = Instant.ofEpochMilli(intent.getLongExtra(EXTRA_TEST_AT, System.currentTimeMillis())).atZone(ZoneId.systemDefault())
            RingRequest(
                alarmId = id,
                hour = at.hour,
                minute = at.minute,
                label = "Test alarm",
                exerciseId = intent.getStringExtra(EXTRA_TEST_EXERCISE) ?: "squat",
                target = intent.getIntExtra(EXTRA_TEST_TARGET, 5),
                volume = 0.6f,
            )
        } else {
            val alarm = container.alarmStore.get(id)
            if (alarm == null || !alarm.enabled) {
                Log.w(TAG, "stale fire for alarm $id, ignoring")
                return
            }
            // Arm the next ring first: if starting the service fails, tomorrow still works.
            if (alarm.isRepeating) container.alarmScheduler.sync(alarm) else container.alarmStore.setEnabled(alarm.id, false)
            RingRequest.from(alarm)
        }
        AlarmService.ring(context, request)
    }

    companion object {
        private const val TAG = "QiapAlarm"
        const val ACTION_FIRE = "app.qiap.action.FIRE_ALARM"
        const val EXTRA_ALARM_ID = "alarmId"
        const val EXTRA_TEST_EXERCISE = "testExercise"
        const val EXTRA_TEST_TARGET = "testTarget"
        const val EXTRA_TEST_AT = "testAt"
        const val EXTRA_IS_SNOOZE = "isSnooze"
    }
}

/**
 * Alarms don't survive reboots, and setAlarmClock instants go stale when the clock or timezone
 * changes. Re-arm everything on each of those. Direct-boot aware, so alarms are back before the
 * user unlocks. Never starts a foreground service (Android 15 restricts that from boot).
 */
class SystemEventReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_LOCKED_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED,
            -> {
                Log.i("QiapAlarm", "re-arming alarms after ${intent.action}")
                (context.applicationContext as QiapApp).container.alarmScheduler.syncAll()
            }
        }
    }
}
