package app.qiap.alarm

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.content.ContextCompat

/** One thing that must be true for an alarm to ring and be dismissible. */
enum class Check(val title: String, val why: String, val required: Boolean) {
    EXACT_ALARMS("Exact alarms", "Ring at 6:30, not \"around then\"", true),
    NOTIFICATIONS("Notifications", "Needed to show the alarm at all", true),
    FULL_SCREEN("Full-screen alert", "Show over the lock screen", true),
    BATTERY("Battery unrestricted", "Stops some phones pausing Qiap overnight", false),
    CAMERA("Camera", "Count reps. Video never leaves the phone", true),
}

/** Reads the live state of every [Check] and knows how to send the user to fix each one. */
class Reliability(private val context: Context, private val scheduler: AlarmScheduler) {

    fun status(): Map<Check, Boolean> = Check.entries.associateWith { isOk(it) }

    fun isOk(check: Check): Boolean = when (check) {
        Check.EXACT_ALARMS -> scheduler.canScheduleExact()
        Check.NOTIFICATIONS -> Build.VERSION.SDK_INT < 33 || granted(Manifest.permission.POST_NOTIFICATIONS)
        Check.FULL_SCREEN -> Build.VERSION.SDK_INT < 34 || context.getSystemService(NotificationManager::class.java).canUseFullScreenIntent()
        Check.BATTERY -> context.getSystemService(PowerManager::class.java).isIgnoringBatteryOptimizations(context.packageName)
        Check.CAMERA -> granted(Manifest.permission.CAMERA)
    }

    /** Runtime permission to request in-app, or null if this check is fixed in system settings. */
    fun runtimePermission(check: Check): String? = when (check) {
        Check.NOTIFICATIONS -> if (Build.VERSION.SDK_INT >= 33) Manifest.permission.POST_NOTIFICATIONS else null
        Check.CAMERA -> Manifest.permission.CAMERA
        else -> null
    }

    /** System settings screen for checks that can't be requested in-app. */
    fun settingsIntent(check: Check): Intent {
        val pkg = Uri.fromParts("package", context.packageName, null)
        val intent = when (check) {
            Check.EXACT_ALARMS -> if (Build.VERSION.SDK_INT >= 31) Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, pkg) else appDetails(pkg)
            Check.FULL_SCREEN -> if (Build.VERSION.SDK_INT >= 34) Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT, pkg) else appDetails(pkg)
            // The list screen needs no special permission (unlike the direct "ignore" prompt).
            Check.BATTERY -> Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            Check.NOTIFICATIONS -> Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            Check.CAMERA -> appDetails(pkg)
        }
        return intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    private fun appDetails(pkg: Uri) = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, pkg)

    private fun granted(permission: String) =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
}
