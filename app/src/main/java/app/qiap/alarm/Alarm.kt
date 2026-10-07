package app.qiap.alarm

import kotlinx.serialization.Serializable
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.ZonedDateTime

/**
 * One alarm as the user configured it. Pure data: no Android types, so scheduling math and
 * persistence are unit-testable. [days] is a bitmask, Monday = bit 0 … Sunday = bit 6;
 * 0 means one-shot (rings once at the next [hour]:[minute], then disables itself).
 */
@Serializable
data class Alarm(
    val id: Int,
    val hour: Int,
    val minute: Int,
    val days: Int = 0,
    val label: String = "",
    val exerciseId: String = "squat",
    val target: Int = 12,
    val enabled: Boolean = true,
    /** 0..1 peak volume the ringer ramps up to. */
    val volume: Float = 0.8f,
    /** How many 5-minute snoozes the alarm allows. 0 = none (default): the workout is the only way out. */
    val snoozeMax: Int = 0,
    /** Non-empty = "surprise me": each ring draws a random exercise from this pool (ExercisePools id). */
    val poolId: String = "",
    /** Up to three exercises done back to back (ids in order). Non-empty overrides [exerciseId] and [poolId]. */
    val routine: List<String> = emptyList(),
    /** True = snoozing isn't free: it needs a short mini set of the exercise first. */
    val snoozeMini: Boolean = false,
) {
    init {
        require(hour in 0..23 && minute in 0..59) { "bad time $hour:$minute" }
        require(days in 0..0x7F) { "bad day mask $days" }
        require(target in 1..999) { "bad target $target" }
        require(snoozeMax in 0..MAX_SNOOZES) { "bad snooze count $snoozeMax" }
        require(routine.size <= MAX_ROUTINE) { "routine too long" }
    }

    val isRepeating: Boolean get() = days != 0

    fun ringsOn(day: DayOfWeek): Boolean = days == 0 || (days shr (day.value - 1)) and 1 == 1

    companion object {
        const val WEEKDAYS = 0b0011111
        const val WEEKEND = 0b1100000
        const val EVERY_DAY = 0b1111111
        const val MAX_SNOOZES = 3
        const val MAX_ROUTINE = 3
        const val SNOOZE_MS = 5 * 60 * 1000L

        fun dayMask(vararg days: DayOfWeek): Int = days.fold(0) { m, d -> m or (1 shl (d.value - 1)) }
    }
}

/**
 * Next time [alarm] should ring strictly after [now], in [now]'s zone.
 *
 * Built with [ZonedDateTime.of], so DST is handled the java.time way: a time inside a spring-forward
 * gap rings at the first valid instant after it (02:30 → 03:30); in an autumn overlap it rings at
 * the earlier of the two instants. Searches 8 days, which always covers a full week of repeats.
 */
fun nextTrigger(alarm: Alarm, now: ZonedDateTime): ZonedDateTime {
    val time = LocalTime.of(alarm.hour, alarm.minute)
    for (offset in 0L..7L) {
        val date = now.toLocalDate().plusDays(offset)
        if (!alarm.ringsOn(date.dayOfWeek)) continue
        val at = ZonedDateTime.of(date, time, now.zone)
        if (at.isAfter(now)) return at
    }
    error("unreachable: a non-empty day mask always matches within 8 days")
}

/** Short day summary for cards: "Weekdays", "Weekends", "Every day", "Once", or "Mon · Wed · Fri". */
fun Alarm.daysLabel(): String = when (days) {
    0 -> "Once"
    Alarm.EVERY_DAY -> "Every day"
    Alarm.WEEKDAYS -> "Weekdays"
    Alarm.WEEKEND -> "Weekends"
    else -> DayOfWeek.entries.filter { ringsOn(it) }.joinToString(" · ") { it.name.take(3).lowercase().replaceFirstChar(Char::uppercase) }
}

/** Seven booleans Monday..Sunday, for the day dots. */
fun Alarm.dayFlags(): List<Boolean> = if (days == 0) List(7) { false } else DayOfWeek.entries.map { ringsOn(it) }
