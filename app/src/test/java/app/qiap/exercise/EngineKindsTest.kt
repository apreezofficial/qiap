package app.qiap.exercise

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Counting logic for the four kinds of exercise, driven by a settable metric so only the state machine is under test. */
class EngineKindsTest {

    /** A metric whose value the test sets directly. */
    private class Param(var v: Float = Float.NaN) : Metric {
        override fun measure(frame: PoseFrame): Float = v
    }

    private val frame = PoseFrame().apply { hasPose = true; aspect = 1f }
    private var t = 0L

    private fun tick(counter: RepCounter, stepMs: Long = 100): RepEvent {
        t += stepMs
        frame.timestampMs = t
        return counter.onFrame(frame)
    }

    private fun feed(counter: RepCounter, a: Param, value: Float, frames: Int = 4, stepMs: Long = 100) {
        a.v = value
        repeat(frames) { tick(counter, stepMs) }
    }

    private fun cycleSpec(m: Metric, gates: List<Range> = emptyList()) = ExerciseSpec(
        id = "t", name = "t", unit = "t", metric = m, downBelow = 40f, upAbove = 60f, goodBelow = 30f,
        minRepMs = 100, smoothing = 1f, gates = gates,
    )

    @Test
    fun aFailingGateStopsReps() {
        val m = Param()
        val g = Param()
        val c = RepCounter(cycleSpec(m, listOf(Range(g, 150f, 200f, "Straight line"))))
        g.v = 180f; feed(c, m, 80f)
        feed(c, m, 20f); feed(c, m, 80f)
        assertEquals(1, c.reps)
        g.v = 120f
        feed(c, m, 20f); feed(c, m, 80f)
        assertEquals("gate failed: no new rep", 1, c.reps)
        assertEquals("Straight line", c.gateCue)
    }

    @Test
    fun anUnmeasurableGatePausesTracking() {
        val m = Param(80f)
        val g = Param(Float.NaN)
        val c = RepCounter(cycleSpec(m, listOf(Range(g, 150f, 200f))))
        tick(c)
        assertFalse(c.tracking)
    }

    @Test
    fun alternatingCountsEachSideSeparately() {
        val a = Param()
        val b = Param()
        val spec = ExerciseSpec(
            id = "alt", name = "alt", unit = "t", metric = a, metricB = b, kind = Kind.ALTERNATING,
            downBelow = 40f, upAbove = 60f, goodBelow = 30f, minRepMs = 100, smoothing = 1f,
        )
        val c = RepCounter(spec)
        a.v = 80f; b.v = 80f
        repeat(3) { tick(c) }
        repeat(3) {
            a.v = 20f; repeat(3) { tick(c) }
            a.v = 80f; repeat(3) { tick(c) }
            b.v = 20f; repeat(3) { tick(c) }
            b.v = 80f; repeat(3) { tick(c) }
        }
        assertEquals(6, c.reps)
    }

    @Test
    fun alternatingKeepsCountingWhenOnlyOneSideIsVisible() {
        val a = Param(80f)
        val b = Param(Float.NaN)
        val spec = ExerciseSpec(
            id = "alt", name = "alt", unit = "t", metric = a, metricB = b, kind = Kind.ALTERNATING,
            downBelow = 40f, upAbove = 60f, goodBelow = 30f, minRepMs = 100, smoothing = 1f,
        )
        val c = RepCounter(spec)
        repeat(3) { tick(c) }
        a.v = 20f; repeat(3) { tick(c) }
        a.v = 80f; repeat(3) { tick(c) }
        assertTrue(c.tracking)
        assertEquals(1, c.reps)
    }

    private fun holdSpec(m: Metric) = ExerciseSpec(
        id = "hold", name = "hold", unit = "seconds", metric = m, kind = Kind.HOLD, holdLow = 160f, holdHigh = 185f,
    )

    @Test
    fun holdCountsSecondsInsideTheBand() {
        val m = Param(175f)
        val c = RepCounter(holdSpec(m))
        repeat(31) { tick(c) }
        assertEquals(3, c.reps)
    }

    @Test
    fun aShortWobblePausesTheHoldWithoutResetting() {
        val m = Param(175f)
        val c = RepCounter(holdSpec(m))
        repeat(21) { tick(c) } // 2 s held
        m.v = 120f
        repeat(10) { tick(c) } // 1 s out of the band: pause
        assertTrue(c.needsMoreDepth)
        m.v = 175f
        repeat(10) { tick(c) } // 1 s more
        assertEquals(3, c.reps)
    }

    @Test
    fun aLongBreakStartsTheHoldOver() {
        val m = Param(175f)
        val c = RepCounter(holdSpec(m))
        repeat(31) { tick(c) }
        m.v = 100f
        repeat(20) { tick(c) } // 2 s out: over the 1.5 s grace
        assertEquals(0, c.reps)
    }

    @Test
    fun aStalledCameraCannotBankHoldSeconds() {
        val m = Param(175f)
        val c = RepCounter(holdSpec(m))
        tick(c)
        tick(c, stepMs = 10_000) // one frame after a 10 s freeze
        assertEquals(0, c.reps)
    }

    private fun seqSpec(p: Param) = ExerciseSpec(
        id = "seq", name = "seq", unit = "t", metric = p, kind = Kind.SEQUENCE, minRepMs = 100,
        stages = listOf(
            Stage("a", Range(p, 0f, 1f)),
            Stage("b", Range(p, 2f, 3f)),
            Stage("c", Range(p, 4f, 5f)),
            Stage("b", Range(p, 2f, 3f)),
            Stage("a", Range(p, 0f, 1f)),
        ),
    )

    private fun walk(c: RepCounter, p: Param, vararg values: Float) {
        for (v in values) { p.v = v; tick(c) }
    }

    @Test
    fun sequenceCountsOnlyTheFullOrderedLoop() {
        val p = Param()
        val c = RepCounter(seqSpec(p))
        walk(c, p, 0.5f, 2.5f, 4.5f, 2.5f, 0.5f)
        assertEquals(1, c.reps)
        walk(c, p, 2.5f, 4.5f, 2.5f, 0.5f)
        assertEquals(2, c.reps)
    }

    @Test
    fun sequenceSkippingAStageIsNotARep() {
        val p = Param()
        val c = RepCounter(seqSpec(p))
        walk(c, p, 0.5f, 4.5f, 0.5f)
        assertEquals(0, c.reps)
    }

    @Test
    fun sequenceAbandonedHalfwayStartsOver() {
        val p = Param()
        val c = RepCounter(seqSpec(p))
        walk(c, p, 0.5f, 2.5f, 0.5f) // gave up, back at the start
        walk(c, p, 4.5f, 2.5f, 0.5f) // would complete a stale loop; must not
        assertEquals(0, c.reps)
        walk(c, p, 2.5f, 4.5f, 2.5f, 0.5f)
        assertEquals(1, c.reps)
    }

    @Test
    fun sequenceNeedsTheStartPositionFirst() {
        val p = Param()
        val c = RepCounter(seqSpec(p))
        walk(c, p, 4.5f, 2.5f, 0.5f)
        assertEquals(0, c.reps)
    }

    // --- real catalog entries on real landmarks ---------------------------------------------

    /** A person standing front-on: shoulders at y .3, hips .55 (torso .25), knees .77, ankles .95. */
    private fun standing(leftKneeY: Float = 0.77f, rightKneeY: Float = 0.77f): PoseFrame {
        frame.set(Landmark.LEFT_SHOULDER, 0.45f, 0.3f); frame.set(Landmark.RIGHT_SHOULDER, 0.55f, 0.3f)
        frame.set(Landmark.LEFT_HIP, 0.47f, 0.55f); frame.set(Landmark.RIGHT_HIP, 0.53f, 0.55f)
        frame.set(Landmark.LEFT_KNEE, 0.47f, leftKneeY); frame.set(Landmark.RIGHT_KNEE, 0.53f, rightKneeY)
        frame.set(Landmark.LEFT_ANKLE, 0.47f, 0.95f); frame.set(Landmark.RIGHT_ANKLE, 0.53f, 0.95f)
        frame.hasPose = true
        t += 100
        frame.timestampMs = t
        return frame
    }

    @Test
    fun highKneesCountsEachKneeLift() {
        val c = RepCounter(ExerciseCatalog.HighKnees)
        repeat(4) { c.onFrame(standing()) }
        repeat(3) {
            repeat(4) { c.onFrame(standing(leftKneeY = 0.55f)) }
            repeat(4) { c.onFrame(standing()) }
            repeat(4) { c.onFrame(standing(rightKneeY = 0.55f)) }
            repeat(4) { c.onFrame(standing()) }
        }
        assertEquals(6, c.reps)
    }

    @Test
    fun plankHoldCountsSecondsOnALevelBody() {
        // Side view, body level: shoulder, hip and ankle on one horizontal line = 180°.
        fun level(): PoseFrame {
            for ((s, h, a) in listOf(
                Triple(Landmark.LEFT_SHOULDER, Landmark.LEFT_HIP, Landmark.LEFT_ANKLE),
                Triple(Landmark.RIGHT_SHOULDER, Landmark.RIGHT_HIP, Landmark.RIGHT_ANKLE),
            )) {
                frame.set(s, 0.2f, 0.5f); frame.set(h, 0.5f, 0.5f); frame.set(a, 0.8f, 0.5f)
            }
            frame.hasPose = true
            t += 100
            frame.timestampMs = t
            return frame
        }
        val c = RepCounter(ExerciseCatalog.Plank)
        repeat(41) { c.onFrame(level()) }
        assertEquals(4, c.reps)
    }

    @Test
    fun walkingAwayFromTheCameraStopsTrackingInsteadOfCounting() {
        val c = RepCounter(ExerciseCatalog.HighKnees)
        repeat(4) { c.onFrame(standing()) }
        frame.set(Landmark.LEFT_KNEE, 0.47f, 0.77f, vis = 0.1f)
        frame.set(Landmark.RIGHT_KNEE, 0.53f, 0.77f, vis = 0.1f)
        c.onFrame(frame)
        assertFalse(c.tracking)
    }
}
