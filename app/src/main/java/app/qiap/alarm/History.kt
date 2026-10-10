package app.qiap.alarm

import kotlinx.serialization.Serializable
import java.time.LocalDate

/** How a ringing alarm ended. Drives the seal on the History calendar. */
@Serializable
enum class Outcome {
    /** Workout finished: Cinnabar seal. */
    EARNED,
    /** Dismissed via the hold-to-skip fallback: saffron-edged seal. */
    FALLBACK,
    /** Rang out without being dismissed. */
    MISSED,
}

@Serializable
data class HistoryEntry(
    val alarmId: Int,
    /** Local date the alarm rang, as epoch day. */
    val epochDay: Long,
    /** Wall-clock millis when it was dismissed (or gave up). */
    val endedAtMs: Long,
    /** Minutes after midnight the alarm was set for (for "avg out of bed"). */
    val alarmMinuteOfDay: Int,
    /** Minutes after midnight the alarm actually ended. */
    val endedMinuteOfDay: Int,
    val outcome: Outcome,
    val exerciseId: String,
    val reps: Int,
    val seconds: Int,
    /** Absolute path of the private video proof, if one was recorded (and not yet aged out). */
    val videoPath: String? = null,
)

/** Derived numbers for Home and History. Pure, so it's unit-tested. */
data class HistoryStats(
    val streak: Int,
    val bestStreak: Int,
    val sealsThisYear: Int,
    val repsThisWeek: Int,
    /** Reps per day Mon..Sun of the current week, scaled 0..1 against the biggest day. */
    val weekShape: List<Float>,
    /** Average minutes from alarm time to dismissal for earned seals, or null without data. */
    val avgMinutesToUp: Int?,
    /** Average dismissal minute-of-day for earned seals, or null. */
    val avgUpMinuteOfDay: Int?,
    /** Best outcome per day (EARNED beats FALLBACK beats MISSED). */
    val byDay: Map<Long, Outcome>,
    val repsByExercise: Map<String, Int>,
) {
    companion object {
        /**
         * A day counts toward the streak only with an EARNED seal. The streak may end today or
         * yesterday (today's alarm may not have rung yet).
         */
        fun from(entries: List<HistoryEntry>, today: LocalDate): HistoryStats {
            val byDay = HashMap<Long, Outcome>()
            for (e in entries) {
                val prev = byDay[e.epochDay]
                if (prev == null || e.outcome.ordinal < prev.ordinal) byDay[e.epochDay] = e.outcome
            }
            val earnedDays = byDay.filterValues { it == Outcome.EARNED }.keys.toSortedSet()

            val t = today.toEpochDay()
            var start = if (t in earnedDays) t else t - 1
            var streak = 0
            while (start in earnedDays) { streak++; start-- }

            var best = 0
            var run = 0
            var last = Long.MIN_VALUE
            for (d in earnedDays) {
                run = if (d == last + 1) run + 1 else 1
                if (run > best) best = run
                last = d
            }

            val monday = today.minusDays((today.dayOfWeek.value - 1).toLong()).toEpochDay()
            val perDay = IntArray(7)
            for (e in entries) {
                val i = (e.epochDay - monday).toInt()
                if (i in 0..6) perDay[i] += e.reps
            }
            val maxDay = perDay.max().coerceAtLeast(1)
            val earned = entries.filter { it.outcome == Outcome.EARNED }

            return HistoryStats(
                streak = streak,
                bestStreak = best,
                sealsThisYear = earnedDays.count { LocalDate.ofEpochDay(it).year == today.year },
                repsThisWeek = perDay.sum(),
                weekShape = perDay.map { it.toFloat() / maxDay },
                avgMinutesToUp = earned.takeIf { it.isNotEmpty() }
                    ?.map { ((it.endedMinuteOfDay - it.alarmMinuteOfDay) + 1440) % 1440 }?.average()?.toInt(),
                avgUpMinuteOfDay = earned.takeIf { it.isNotEmpty() }?.map { it.endedMinuteOfDay }?.average()?.toInt(),
                byDay = byDay,
                repsByExercise = entries.groupBy { it.exerciseId }.mapValues { (_, v) -> v.sumOf { it.reps } },
            )
        }
    }
}
