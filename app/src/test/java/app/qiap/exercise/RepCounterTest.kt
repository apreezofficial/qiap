package app.qiap.exercise

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.cos
import kotlin.math.sin

/**
 * State-machine behaviour of [RepCounter] on synthetic poses. These test the counting logic, not
 * the thresholds: threshold values get validated against recorded fixtures (see Fixture).
 */
class RepCounterTest {

    private val spec = ExerciseCatalog.Squat
    private val frame = PoseFrame().apply { aspect = 1f; hasPose = true }
    private var t = 0L

    /** Poses a side-view leg with the given knee angle (degrees) at hip-knee-ankle, both sides. */
    private fun knee(angleDeg: Float, visibility: Float = 1f, stepMs: Long = 40): PoseFrame {
        val knee = 0.5f to 0.6f
        // Thigh points straight up from the knee; shin rotates by the knee angle.
        val hip = knee.first to knee.second - 0.2f
        val rad = Math.toRadians(angleDeg.toDouble())
        val ankle = (knee.first + 0.2f * sin(rad).toFloat()) to (knee.second - 0.2f * cos(rad).toFloat())
        for ((h, k, a) in listOf(
            Triple(Landmark.LEFT_HIP, Landmark.LEFT_KNEE, Landmark.LEFT_ANKLE),
            Triple(Landmark.RIGHT_HIP, Landmark.RIGHT_KNEE, Landmark.RIGHT_ANKLE),
        )) {
            frame.set(h, hip.first, hip.second, vis = visibility)
            frame.set(k, knee.first, knee.second, vis = visibility)
            frame.set(a, ankle.first, ankle.second, vis = visibility)
        }
        t += stepMs
        frame.timestampMs = t
        return frame
    }

    /** One rep: stand → down to [bottom] → stand, sampled every 40 ms over ~1.2 s. */
    private fun RepCounter.rep(bottom: Float): List<RepEvent> {
        val events = mutableListOf<RepEvent>()
        for (a in (170 downTo bottom.toInt() step 5)) events += onFrame(knee(a.toFloat()))
        for (a in (bottom.toInt()..170 step 5)) events += onFrame(knee(a.toFloat()))
        return events
    }

    @Test
    fun angleHelperMeasuresTheKnee() {
        assertEquals(90f, knee(90f).angleDeg(Landmark.LEFT_HIP, Landmark.LEFT_KNEE, Landmark.LEFT_ANKLE), 0.5f)
        assertEquals(170f, knee(170f).angleDeg(Landmark.LEFT_HIP, Landmark.LEFT_KNEE, Landmark.LEFT_ANKLE), 0.5f)
    }

    @Test
    fun aspectIsAppliedToX() {
        val f = PoseFrame().apply { aspect = 2f }
        f.set(0, 0.5f, 0f); f.set(1, 0f, 0f); f.set(2, 0f, 0.5f)
        // In pixels the x leg is twice as long; the angle at (0,0) is still a right angle.
        assertEquals(90f, f.angleDeg(0, 1, 2), 0.01f)
    }

    @Test
    fun countsFullDeepRepsAsGood() {
        val c = RepCounter(spec)
        repeat(3) { assertTrue(c.rep(bottom = 80f).contains(RepEvent.REP)) }
        assertEquals(3, c.reps)
        assertTrue(c.lastRepGood)
    }

    @Test
    fun countsButFlagsShallowReps() {
        val c = RepCounter(spec)
        val events = c.rep(bottom = 105f)
        assertEquals(1, c.reps)
        assertTrue(events.contains(RepEvent.REP_SHALLOW))
        assertFalse(c.lastRepGood)
    }

    @Test
    fun aHalfSquatThatNeverCrossesDownIsNotARep() {
        val c = RepCounter(spec)
        c.rep(bottom = 130f)
        assertEquals(0, c.reps)
    }

    @Test
    fun jitterAroundOneThresholdDoesNotDoubleCount() {
        val c = RepCounter(spec)
        c.onFrame(knee(170f))
        // Hover around the down threshold, never reaching up again.
        repeat(20) { i -> c.onFrame(knee(if (i % 2 == 0) spec.downBelow - 4 else spec.downBelow + 4)) }
        assertEquals(0, c.reps)
        repeat(6) { c.onFrame(knee(170f)) }
        assertEquals(1, c.reps)
    }

    @Test
    fun startingAtTheBottomNeedsAStandFirst() {
        val c = RepCounter(spec)
        repeat(5) { c.onFrame(knee(80f)) }
        repeat(5) { c.onFrame(knee(170f)) }
        assertEquals("no free rep for walking in crouched", 0, c.reps)
        c.rep(bottom = 80f)
        assertEquals(1, c.reps)
    }

    @Test
    fun lowVisibilityFramesAreSkippedNotCounted() {
        val c = RepCounter(spec)
        c.onFrame(knee(170f))
        repeat(10) { c.onFrame(knee(80f, visibility = 0.2f)) }
        assertFalse(c.tracking)
        repeat(6) { c.onFrame(knee(170f)) }
        assertEquals(0, c.reps)
    }

    @Test
    fun repsFasterThanMinDurationAreIgnored() {
        val c = RepCounter(spec)
        c.onFrame(knee(170f, stepMs = 10))
        repeat(4) {
            repeat(3) { c.onFrame(knee(80f, stepMs = 10)) }
            repeat(3) { c.onFrame(knee(170f, stepMs = 10)) }
        }
        // ~60 ms per bounce, far under minRepMs: only the first one counts.
        assertEquals(1, c.reps)
    }

    @Test
    fun noPoseMeansNotTracking() {
        val c = RepCounter(spec)
        val empty = PoseFrame().apply { hasPose = false }
        assertEquals(RepEvent.NONE, c.onFrame(empty))
        assertFalse(c.tracking)
    }

    @Test
    fun fixtureRoundTripsAndReplays() {
        val out = StringBuilder(Fixture.HEADER).append('\n')
        val record = { f: PoseFrame -> Fixture.appendLine(f, out) }
        record(knee(170f))
        repeat(2) {
            for (a in 170 downTo 80 step 5) record(knee(a.toFloat()))
            for (a in 80..170 step 5) record(knee(a.toFloat()))
        }
        val (reps, shallow) = Fixture.replay(out.lineSequence(), spec)
        assertEquals(2, reps)
        assertEquals(0, shallow)
        // Parsing restores the exact frame.
        val parsed = PoseFrame()
        assertFalse("header is a comment", Fixture.parseLine(out.lineSequence().first(), parsed))
        assertTrue(Fixture.parseLine(out.lineSequence().drop(1).first(), parsed))
        assertEquals(40L, parsed.timestampMs)
        assertTrue(parsed.hasPose)
        assertEquals(0.5f, parsed.x[Landmark.LEFT_KNEE], 0f)
        assertEquals(0.6f, parsed.y[Landmark.LEFT_KNEE], 0f)
        assertEquals(1f, parsed.visibility[Landmark.LEFT_KNEE], 0f)
    }

    @Test
    fun catalogThresholdsAreOrdered() {
        for (s in ExerciseCatalog.all) {
            assertTrue(s.id, s.goodBelow <= s.downBelow && s.downBelow < s.upAbove)
        }
    }
}
