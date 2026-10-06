package app.qiap.core.common

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalTime

class TimeTextTest {

    @Test
    fun countsForwardToLaterToday() {
        assertEquals(7 * 60 + 32, minutesUntil(LocalTime.of(22, 58), 6, 30))
        assertEquals(30, minutesUntil(LocalTime.of(6, 0), 6, 30))
    }

    @Test
    fun anAlarmForNowOrEarlierIsTomorrow() {
        assertEquals(24 * 60, minutesUntil(LocalTime.of(6, 30), 6, 30))
        assertEquals(24 * 60 - 1, minutesUntil(LocalTime.of(6, 31), 6, 30))
    }

    @Test
    fun formatsCountdowns() {
        assertEquals("7h 32m", formatCountdown(452))
        assertEquals("1h 05m", formatCountdown(65))
        assertEquals("45m", formatCountdown(45))
        assertEquals("24h 00m", formatCountdown(1440))
    }

    @Test
    fun twelveHourClock() {
        assertEquals("6:30" to "am", twelveHour(6, 30))
        assertEquals("12:00" to "am", twelveHour(0, 0))
        assertEquals("12:05" to "pm", twelveHour(12, 5))
        assertEquals("11:45" to "pm", twelveHour(23, 45))
    }
}
