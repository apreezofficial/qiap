package app.qiap.exercise

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class AntiCheatTest {

    private val frame = PoseFrame().apply { aspect = 0.75f; hasPose = true }
    private var t = 0L

    private fun body(top: Float, bottom: Float, vis: Float = 1f) {
        // Spread the 18 torso/limb joints evenly between the top and bottom of the frame.
        for (i in Landmark.LEFT_SHOULDER..Landmark.RIGHT_ANKLE) {
            val f = (i - Landmark.LEFT_SHOULDER) / (Landmark.RIGHT_ANKLE - Landmark.LEFT_SHOULDER).toFloat()
            frame.set(i, 0.5f, top + (bottom - top) * f, vis = vis)
        }
    }

    private fun step(ms: Long = 100) {
        t += ms
        frame.timestampMs = t
    }

    // --- full-body gate --------------------------------------------------------------------

    @Test
    fun aBodyFillingTheFramePassesAndATinyOneDoesNot() {
        body(0.1f, 0.95f)
        assertTrue(FullBody.extent(frame) >= FullBody.MIN_EXTENT)
        body(0.4f, 0.6f)
        assertTrue(FullBody.extent(frame) < FullBody.MIN_EXTENT)
    }

    @Test
    fun aPersonLyingAcrossTheFrameCountsAsLargeEvenThoughNotTall() {
        // Plank, side view: shoulders left, ankles right, all at one height.
        for (i in Landmark.LEFT_SHOULDER..Landmark.RIGHT_ANKLE) {
            frame.set(i, 0.1f + 0.8f * (i - Landmark.LEFT_SHOULDER) / 17f, 0.6f)
        }
        assertTrue(FullBody.extent(frame) >= FullBody.MIN_EXTENT)
    }

    @Test
    fun tooFewVisibleJointsMeansNoExtent() {
        body(0.1f, 0.95f, vis = 0.2f)
        assertEquals(0f, FullBody.extent(frame), 0f)
    }

    // --- shake -----------------------------------------------------------------------------

    @Test
    fun aPhonePropUpAgainstAWallIsNotShaking() {
        val d = ShakeDetector()
        var ms = 0L
        repeat(200) { d.onSample(0.1f, 9.8f, 0.2f, ms); ms += 20 }
        assertFalse(d.shaking)
    }

    @Test
    fun aWavedPhoneIsShakingAndSettlesAgain() {
        val d = ShakeDetector()
        var ms = 0L
        repeat(100) { d.onSample(0f, 9.8f, 0f, ms); ms += 20 }
        repeat(100) { i -> d.onSample(if (i % 2 == 0) 8f else -8f, 9.8f, 0f, ms); ms += 20 }
        assertTrue(d.shaking)
        repeat(300) { d.onSample(0f, 9.8f, 0f, ms); ms += 20 }
        assertFalse("settles once the phone is still", d.shaking)
    }

    // --- liveness --------------------------------------------------------------------------

    private fun standing() {
        frame.set(Landmark.NOSE, 0.5f, 0.15f)
        frame.set(Landmark.LEFT_SHOULDER, 0.4f, 0.3f); frame.set(Landmark.RIGHT_SHOULDER, 0.6f, 0.3f)
        frame.set(Landmark.LEFT_HIP, 0.44f, 0.55f); frame.set(Landmark.RIGHT_HIP, 0.56f, 0.55f)
        frame.set(Landmark.LEFT_WRIST, 0.35f, 0.55f); frame.set(Landmark.RIGHT_WRIST, 0.65f, 0.55f)
    }

    private fun live(gesture: Gesture, hold: Long = 700): Liveness {
        // Pick seeds until the random draw is the gesture we want to test.
        for (seed in 0..500) {
            val l = Liveness(Random(seed), hold)
            if (l.gesture == gesture) return l
        }
        error("no seed gives $gesture")
    }

    private fun holdFor(l: Liveness, ms: Long): Boolean {
        var done = false
        var elapsed = 0L
        while (elapsed <= ms) {
            step(100)
            done = l.onFrame(frame)
            elapsed += 100
        }
        return done
    }

    @Test
    fun raisingTheRightHandSatisfiesTheRightHandGesture() {
        val l = live(Gesture.RIGHT_HAND_UP)
        standing()
        assertFalse(holdFor(l, 1000))
        frame.set(Landmark.RIGHT_WRIST, 0.65f, 0.05f)
        assertTrue(holdFor(l, 1000))
    }

    @Test
    fun raisingTheWrongHandDoesNot() {
        val l = live(Gesture.RIGHT_HAND_UP)
        standing()
        frame.set(Landmark.LEFT_WRIST, 0.35f, 0.05f)
        assertFalse(holdFor(l, 1500))
    }

    @Test
    fun bothHandsGestureNeedsBothHands() {
        val l = live(Gesture.BOTH_HANDS_UP)
        standing()
        frame.set(Landmark.RIGHT_WRIST, 0.65f, 0.05f)
        assertFalse(holdFor(l, 1500))
        frame.set(Landmark.LEFT_WRIST, 0.35f, 0.05f)
        assertTrue(holdFor(l, 1500))
    }

    @Test
    fun touchHeadNeedsAWristNearTheNose() {
        val l = live(Gesture.TOUCH_HEAD)
        standing()
        assertFalse(holdFor(l, 1000))
        frame.set(Landmark.RIGHT_WRIST, 0.52f, 0.17f)
        assertTrue(holdFor(l, 1000))
    }

    @Test
    fun aGestureHeldTooBrieflyDoesNotCount() {
        val l = live(Gesture.LEFT_HAND_UP)
        standing()
        frame.set(Landmark.LEFT_WRIST, 0.35f, 0.05f)
        assertFalse(holdFor(l, 300))
        standing() // dropped the hand: the timer starts over
        assertFalse(holdFor(l, 300))
    }

    @Test
    fun theNextGestureIsNeverTheSameAsThePreviousOne() {
        val l = Liveness(Random(7))
        repeat(50) {
            val before = l.gesture
            l.next()
            assertTrue(l.gesture != before)
        }
    }

    // --- stillness -------------------------------------------------------------------------

    @Test
    fun standingStillForThreeSecondsPasses() {
        val s = Stillness()
        standing()
        var done = false
        repeat(40) { step(100); done = s.onFrame(frame) }
        assertTrue(done)
    }

    @Test
    fun movingAroundKeepsResettingTheClock() {
        val s = Stillness()
        var done = false
        repeat(60) { i ->
            standing()
            frame.set(Landmark.LEFT_SHOULDER, 0.4f + (i % 2) * 0.15f, 0.3f)
            frame.set(Landmark.RIGHT_SHOULDER, 0.6f + (i % 2) * 0.15f, 0.3f)
            frame.set(Landmark.LEFT_HIP, 0.44f + (i % 2) * 0.15f, 0.55f)
            frame.set(Landmark.RIGHT_HIP, 0.56f + (i % 2) * 0.15f, 0.55f)
            step(100)
            done = s.onFrame(frame)
        }
        assertFalse(done)
    }

    @Test
    fun losingThePersonResetsStillness() {
        val s = Stillness()
        standing()
        repeat(25) { step(100); s.onFrame(frame) }
        frame.hasPose = false
        step(100); s.onFrame(frame)
        frame.hasPose = true
        var done = false
        repeat(15) { step(100); done = s.onFrame(frame) }
        assertFalse("clock restarted when the person disappeared", done)
    }
}
