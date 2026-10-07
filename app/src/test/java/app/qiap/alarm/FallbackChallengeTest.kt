package app.qiap.alarm

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class FallbackChallengeTest {

    @Test
    fun threeRightAnswersFinishIt() {
        val c = FallbackChallenge(Random(1))
        repeat(FallbackChallenge.STEPS) {
            assertFalse(c.done)
            assertTrue(c.answer(c.current.answer))
        }
        assertTrue(c.done)
    }

    @Test
    fun aWrongAnswerStartsOver() {
        val c = FallbackChallenge(Random(2))
        assertTrue(c.answer(c.current.answer))
        assertTrue(c.answer(c.current.answer))
        assertEquals(2, c.index)
        val wrong = c.current.options.first { it != c.current.answer }
        assertFalse(c.answer(wrong))
        assertEquals(0, c.index)
        assertFalse(c.done)
    }

    @Test
    fun everyStepOffersFourDistinctOptionsIncludingTheAnswer() {
        val c = FallbackChallenge(Random(3))
        repeat(200) {
            val s = c.current
            assertEquals(4, s.options.size)
            assertEquals(4, s.options.toSet().size)
            assertTrue(s.answer in s.options)
            assertTrue(s.options.all { it > 0 })
            if (!c.answer(s.answer) || c.done) return@repeat
        }
    }

    @Test
    fun oldAlarmJsonWithoutSnoozeStillLoads() {
        val old = """{"id":1,"hour":6,"minute":30,"days":31,"exerciseId":"squat","target":12,"enabled":true,"volume":0.8}"""
        val a = Json.decodeFromString(Alarm.serializer(), old)
        assertEquals(0, a.snoozeMax)
    }

    @Test
    fun ringRequestSurvivesTheDiskRoundTrip() {
        val r = RingRequest(4, 6, 30, "Weekdays", "pushup", 10, 0.7f, snoozeMax = 2, snoozesUsed = 1)
        val back = Json.decodeFromString(PersistedRing.serializer(), Json.encodeToString(PersistedRing.serializer(), PersistedRing(r, 123L)))
        assertEquals(r, back.request)
        assertEquals(1, back.request.snoozesLeft)
    }

    @Test
    fun testAlarmsNeverSnooze() {
        assertEquals(0, RingRequest(AlarmScheduler.TEST_ID, 6, 0, "Test", "squat", 5, 0.6f, snoozeMax = 3).snoozesLeft)
    }
}
