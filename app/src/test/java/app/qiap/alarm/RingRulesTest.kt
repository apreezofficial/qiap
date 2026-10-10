package app.qiap.alarm

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RingRulesTest {

    @Test
    fun aRingThatHasRunItsCapHasNothingLeft() {
        val cap = AlarmService.MAX_RING_MS
        assertEquals(cap - 5_000, RingRules.remainingRingMs(startedAtMs = 1_000, nowMs = 6_000, capMs = cap))
        assertTrue(RingRules.remainingRingMs(startedAtMs = 0, nowMs = cap, capMs = cap) <= 0)
        assertTrue("process killed for 40 min: expired", RingRules.remainingRingMs(0, 40 * 60_000L, cap) < 0)
    }

    @Test
    fun miniSetIsAQuarterOfTheTargetWithSensibleBounds() {
        assertEquals(3, RingRules.miniSetTarget(isHold = false, target = 12))
        assertEquals(10, RingRules.miniSetTarget(isHold = false, target = 40))
        assertEquals("never more than the real target", 2, RingRules.miniSetTarget(isHold = false, target = 2))
        assertEquals(10, RingRules.miniSetTarget(isHold = true, target = 30))
        assertEquals(6, RingRules.miniSetTarget(isHold = true, target = 6))
    }

    @Test
    fun routineAlarmStartsWithItsFirstMoveAndKeepsTheList() {
        val a = Alarm(id = 5, hour = 6, minute = 0, routine = listOf("pushup", "plank", "jack"))
        val r = RingRequest.from(a)
        assertEquals("pushup", r.exerciseId)
        assertEquals(listOf("pushup", "plank", "jack"), r.routine)
    }

    @Test
    fun unknownRoutineIdsAreDropped() {
        val r = RingRequest.from(Alarm(id = 5, hour = 6, minute = 0, routine = listOf("nonsense", "plank")))
        assertEquals(listOf("plank"), r.routine)
        assertEquals("plank", r.exerciseId)
    }

    @Test
    fun aRoutineOverridesAPool() {
        val r = RingRequest.from(Alarm(id = 5, hour = 6, minute = 0, poolId = "cardio", routine = listOf("squat")))
        assertEquals("squat", r.exerciseId)
    }

    @Test
    fun routinesAreLimitedToThreeMoves() {
        val tooMany = runCatching { Alarm(id = 1, hour = 6, minute = 0, routine = listOf("squat", "pushup", "plank", "jack")) }
        assertTrue(tooMany.isFailure)
    }

    @Test
    fun routineAndSnoozeOptionsSurviveTheDiskRoundTrip() {
        val a = Alarm(id = 7, hour = 5, minute = 45, routine = listOf("squat", "plank"), snoozeMax = 2, snoozeMini = true)
        val back = Json.decodeFromString(Alarm.serializer(), Json.encodeToString(Alarm.serializer(), a))
        assertEquals(a, back)
        val ring = RingRequest.from(a)
        val persisted = Json.decodeFromString(PersistedRing.serializer(), Json.encodeToString(PersistedRing.serializer(), PersistedRing(ring, 99L)))
        assertEquals(ring, persisted.request)
        assertTrue(persisted.request.snoozeMini)
        assertFalse(persisted.request.routine.isEmpty())
    }
}
