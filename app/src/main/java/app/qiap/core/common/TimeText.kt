package app.qiap.core.common

import java.time.LocalTime

/** Minutes from [now] until the next [hour]:[minute] (today or tomorrow). Never 0: an alarm set for "now" is a day away. */
fun minutesUntil(now: LocalTime, hour: Int, minute: Int): Int {
    val nowMin = now.hour * 60 + now.minute
    val target = hour * 60 + minute
    val diff = target - nowMin
    return if (diff > 0) diff else diff + 24 * 60
}

/** "7h 32m" / "45m". Always tabular-friendly: minutes zero-padded once hours are shown. */
fun formatCountdown(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    return if (h == 0) "${m}m" else "${h}h ${m.toString().padStart(2, '0')}m"
}

/** 12-hour clock parts: (6, 30) → ("6:30", "am"). */
fun twelveHour(hour: Int, minute: Int): Pair<String, String> {
    val h = (hour + 11) % 12 + 1
    return "$h:${minute.toString().padStart(2, '0')}" to if (hour < 12) "am" else "pm"
}
