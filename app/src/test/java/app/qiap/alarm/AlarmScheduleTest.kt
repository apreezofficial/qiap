package app.qiap.alarm

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

class AlarmScheduleTest {

    private val london = ZoneId.of("Europe/London")
    private val lagos = ZoneId.of("Africa/Lagos")

    // 2026-10-06 is a Tuesday.
    private fun at(y: Int, mo: Int, d: Int, h: Int, mi: Int, zone: ZoneId = lagos) =
        ZonedDateTime.of(LocalDateTime.of(y, mo, d, h, mi), zone)

    @Test
    fun oneShotLaterTodayRingsToday() {
        val a = Alarm(id = 1, hour = 6, minute = 30)
        assertEquals(at(2026, 10, 6, 6, 30), nextTrigger(a, at(2026, 10, 6, 5, 0)))
    }

    @Test
    fun oneShotAlreadyPassedRingsTomorrow() {
        val a = Alarm(id = 1, hour = 6, minute = 30)
        assertEquals(at(2026, 10, 7, 6, 30), nextTrigger(a, at(2026, 10, 6, 6, 30)))
        assertEquals(at(2026, 10, 7, 6, 30), nextTrigger(a, at(2026, 10, 6, 23, 59)))
    }

    @Test
    fun weekdayAlarmOnFridayEveningSkipsTheWeekend() {
        val a = Alarm(id = 1, hour = 6, minute = 30, days = Alarm.WEEKDAYS)
        // Fri 2026-10-09 20:00 → Mon 2026-10-12 06:30
        assertEquals(at(2026, 10, 12, 6, 30), nextTrigger(a, at(2026, 10, 9, 20, 0)))
    }

    @Test
    fun singleDayAlarmJustAfterItsTimeWaitsAWeek() {
        val a = Alarm(id = 1, hour = 6, minute = 30, days = Alarm.dayMask(DayOfWeek.TUESDAY))
        assertEquals(at(2026, 10, 13, 6, 30), nextTrigger(a, at(2026, 10, 6, 6, 31)))
    }

    @Test
    fun weekendMask() {
        val a = Alarm(id = 1, hour = 8, minute = 0, days = Alarm.WEEKEND)
        assertEquals(at(2026, 10, 10, 8, 0), nextTrigger(a, at(2026, 10, 6, 9, 0)))
        assertTrue(a.ringsOn(DayOfWeek.SUNDAY))
        assertFalse(a.ringsOn(DayOfWeek.MONDAY))
    }

    @Test
    fun springForwardGapRingsAtFirstValidInstant() {
        // UK clocks jump 01:00 → 02:00 on 2026-03-29. An alarm at 01:30 rings at 02:30 BST.
        val a = Alarm(id = 1, hour = 1, minute = 30, days = Alarm.EVERY_DAY)
        val ring = nextTrigger(a, at(2026, 3, 29, 0, 0, london))
        assertEquals(LocalDateTime.of(2026, 3, 29, 2, 30), ring.toLocalDateTime())
        assertTrue(ring.isAfter(at(2026, 3, 29, 0, 0, london)))
    }

    @Test
    fun fallBackOverlapRingsOnceAtTheEarlierInstant() {
        // UK clocks go 02:00 → 01:00 on 2026-10-25; 01:30 happens twice.
        val a = Alarm(id = 1, hour = 1, minute = 30, days = Alarm.EVERY_DAY)
        val first = nextTrigger(a, at(2026, 10, 25, 0, 0, london))
        assertEquals(1, first.offset.totalSeconds / 3600) // BST, the earlier one
        // After it rings, the next one is tomorrow, not the repeated 01:30.
        assertEquals(LocalDate.of(2026, 10, 26), nextTrigger(a, first).toLocalDate())
    }

    @Test
    fun alwaysStrictlyAfterNowAndWithinAWeek() {
        val now = at(2026, 10, 6, 12, 0)
        for (mask in 1..0x7F) for (h in listOf(0, 6, 12, 23)) {
            val t = nextTrigger(Alarm(id = 1, hour = h, minute = 0, days = mask), now)
            assertTrue(t.isAfter(now))
            assertTrue(t.isBefore(now.plusDays(8)))
        }
    }

    @Test
    fun labelsAndFlags() {
        assertEquals("Weekdays", Alarm(1, 6, 30, Alarm.WEEKDAYS).daysLabel())
        assertEquals("Once", Alarm(1, 6, 30, 0).daysLabel())
        assertEquals("Mon · Wed · Fri", Alarm(1, 6, 30, Alarm.dayMask(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)).daysLabel())
        assertEquals(listOf(true, true, true, true, true, false, false), Alarm(1, 6, 30, Alarm.WEEKDAYS).dayFlags())
    }

    @Test
    fun alarmsSurviveJsonRoundTripWithDefaults() {
        val a = Alarm(id = 3, hour = 5, minute = 45, days = Alarm.WEEKDAYS, exerciseId = "pushup", target = 15, volume = 0.6f)
        assertEquals(a, Json.decodeFromString(Alarm.serializer(), Json.encodeToString(Alarm.serializer(), a)))
        // An older file without newer fields still loads.
        assertEquals(12, Json.decodeFromString(Alarm.serializer(), """{"id":1,"hour":6,"minute":30}""").target)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsImpossibleTimes() {
        Alarm(id = 1, hour = 24, minute = 0)
    }

    // --- History stats ---

    private val today = LocalDate.of(2026, 10, 6)

    private fun entry(daysAgo: Long, outcome: Outcome, reps: Int = 12, alarmMin: Int = 390, endMin: Int = 400) = HistoryEntry(
        alarmId = 1, epochDay = today.minusDays(daysAgo).toEpochDay(), endedAtMs = 0,
        alarmMinuteOfDay = alarmMin, endedMinuteOfDay = endMin, outcome = outcome,
        exerciseId = "squat", reps = reps, seconds = 60,
    )

    @Test
    fun streakCountsEarnedDaysEndingTodayOrYesterday() {
        val s = HistoryStats.from(listOf(entry(1, Outcome.EARNED), entry(2, Outcome.EARNED), entry(3, Outcome.FALLBACK), entry(4, Outcome.EARNED)), today)
        assertEquals(2, s.streak) // a fallback breaks the streak
        assertEquals(2, s.bestStreak)
    }

    @Test
    fun aMissedDayEndsTheStreakButTheBestIsKept() {
        val s = HistoryStats.from((3L..9L).map { entry(it, Outcome.EARNED) } + entry(1, Outcome.MISSED), today)
        assertEquals(0, s.streak)
        assertEquals(7, s.bestStreak)
    }

    @Test
    fun bestOutcomeWinsPerDay() {
        val s = HistoryStats.from(listOf(entry(0, Outcome.MISSED), entry(0, Outcome.EARNED)), today)
        assertEquals(Outcome.EARNED, s.byDay[today.toEpochDay()])
        assertEquals(1, s.streak)
    }

    @Test
    fun weekRepsAndAverages() {
        // Monday 2026-10-05 and Tuesday 2026-10-06 are this week; 2026-10-04 (Sunday) is not.
        val s = HistoryStats.from(listOf(entry(0, Outcome.EARNED, reps = 10, endMin = 400), entry(1, Outcome.EARNED, reps = 20, endMin = 410), entry(2, Outcome.EARNED, reps = 99)), today)
        assertEquals(30, s.repsThisWeek)
        assertEquals(listOf(1f, 0.5f, 0f, 0f, 0f, 0f, 0f), s.weekShape)
        assertEquals((400 + 410 + 400) / 3, s.avgUpMinuteOfDay)
        assertEquals(129, s.repsByExercise["squat"])
    }
}
