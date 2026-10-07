package app.qiap.exercise

import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.cos
import kotlin.math.sin

/**
 * Ideal stick-figure motion for the main exercises, run through the real catalog entries. This
 * proves the geometry and thresholds are self-consistent (good reps count, cheats do not). It is
 * NOT a substitute for recorded fixtures: real bodies and cameras are noisier. CLAUDE.md: the
 * `provisional` flag stays until a recorded fixture backs each exercise.
 */
class SyntheticPoseTest {

    private val frame = PoseFrame().apply { aspect = 1f; hasPose = true }
    private var t = 0L

    private fun at(i: Int, x: Float, y: Float) = frame.set(i, x, y)

    /** Same point for the left and right landmark: a side view where the two sides overlap. */
    private fun pair(l: Int, r: Int, x: Float, y: Float) {
        frame.set(l, x, y)
        frame.set(r, x, y)
    }

    private fun hold(c: RepCounter, frames: Int = 5) {
        repeat(frames) {
            t += 100
            frame.timestampMs = t
            c.onFrame(frame)
        }
    }

    /** Side view: torso, hip, knee, ankle on both sides. */
    private fun side(sx: Float, sy: Float, hx: Float, hy: Float, kx: Float, ky: Float, ax: Float, ay: Float) {
        pair(Landmark.LEFT_SHOULDER, Landmark.RIGHT_SHOULDER, sx, sy)
        pair(Landmark.LEFT_HIP, Landmark.RIGHT_HIP, hx, hy)
        pair(Landmark.LEFT_KNEE, Landmark.RIGHT_KNEE, kx, ky)
        pair(Landmark.LEFT_ANKLE, Landmark.RIGHT_ANKLE, ax, ay)
    }

    /** An arm whose elbow angle is exactly [alphaDeg] (180 = straight). */
    private fun arm(s: Int, e: Int, w: Int, sx: Float, sy: Float, alphaDeg: Double) {
        val len = 0.075f
        val d = Math.toRadians(100.0)
        val ex = sx + len * cos(d).toFloat()
        val ey = sy + len * sin(d).toFloat()
        val wd = Math.toRadians(100.0 + 180.0 + alphaDeg)
        frame.set(s, sx, sy)
        frame.set(e, ex, ey)
        frame.set(w, ex + len * cos(wd).toFloat(), ey + len * sin(wd).toFloat())
    }

    /** Front view, legs straight. [ankleHalf] is half the distance between the feet. */
    private fun front(ankleHalf: Float, shoulderHalf: Float = 0.1f, ankleY: Float = 0.95f, hipY: Float = 0.55f) {
        at(Landmark.LEFT_SHOULDER, 0.5f - shoulderHalf, 0.3f); at(Landmark.RIGHT_SHOULDER, 0.5f + shoulderHalf, 0.3f)
        at(Landmark.LEFT_HIP, 0.44f, hipY); at(Landmark.RIGHT_HIP, 0.56f, hipY)
        at(Landmark.LEFT_ANKLE, 0.5f - ankleHalf, ankleY); at(Landmark.RIGHT_ANKLE, 0.5f + ankleHalf, ankleY)
        at(Landmark.LEFT_KNEE, (0.44f + 0.5f - ankleHalf) / 2f, (hipY + ankleY) / 2f)
        at(Landmark.RIGHT_KNEE, (0.56f + 0.5f + ankleHalf) / 2f, (hipY + ankleY) / 2f)
    }

    // --- cycles ----------------------------------------------------------------------------

    @Test
    fun jumpingJackCountsOpenAndClosedAndRejectsHalfOpen() {
        val c = RepCounter(ExerciseCatalog.JumpingJack)
        front(0.07f); hold(c)
        repeat(5) { front(0.2f); hold(c); front(0.07f); hold(c) }
        assertEquals(5, c.reps)
        repeat(3) { front(0.13f); hold(c); front(0.07f); hold(c) }
        assertEquals("half-open jacks must not count", 5, c.reps)
    }

    @Test
    fun pushUpCountsFullReps() {
        val c = RepCounter(ExerciseCatalog.PushUp)
        fun body(hipY: Float = 0.52f, elbow: Double) {
            side(0.25f, 0.5f, 0.5f, hipY, 0.65f, 0.54f, 0.8f, 0.55f)
            arm(Landmark.LEFT_SHOULDER, Landmark.LEFT_ELBOW, Landmark.LEFT_WRIST, 0.25f, 0.5f, elbow)
            arm(Landmark.RIGHT_SHOULDER, Landmark.RIGHT_ELBOW, Landmark.RIGHT_WRIST, 0.25f, 0.5f, elbow)
        }
        body(elbow = 170.0); hold(c)
        repeat(5) { body(elbow = 70.0); hold(c); body(elbow = 170.0); hold(c) }
        assertEquals(5, c.reps)
    }

    @Test
    fun pushUpShallowRepsAndSaggingHipsDoNotCount() {
        fun body(hipY: Float, elbow: Double) {
            side(0.25f, 0.5f, 0.5f, hipY, 0.65f, 0.54f, 0.8f, 0.55f)
            arm(Landmark.LEFT_SHOULDER, Landmark.LEFT_ELBOW, Landmark.LEFT_WRIST, 0.25f, 0.5f, elbow)
            arm(Landmark.RIGHT_SHOULDER, Landmark.RIGHT_ELBOW, Landmark.RIGHT_WRIST, 0.25f, 0.5f, elbow)
        }
        val shallow = RepCounter(ExerciseCatalog.PushUp)
        body(0.52f, 170.0); hold(shallow)
        repeat(4) { body(0.52f, 112.0); hold(shallow); body(0.52f, 170.0); hold(shallow) }
        assertEquals("elbows never reached 100 degrees", 0, shallow.reps)

        val sagging = RepCounter(ExerciseCatalog.PushUp)
        body(0.62f, 170.0); hold(sagging)
        repeat(4) { body(0.62f, 70.0); hold(sagging); body(0.62f, 170.0); hold(sagging) }
        assertEquals("body line broken: nothing counts", 0, sagging.reps)
    }

    @Test
    fun sitUpWorksWithBentKnees() {
        val c = RepCounter(ExerciseCatalog.SitUp)
        fun lying() = side(0.2f, 0.8f, 0.45f, 0.8f, 0.6f, 0.62f, 0.7f, 0.8f)
        fun sitting() = side(0.4f, 0.55f, 0.45f, 0.8f, 0.6f, 0.62f, 0.7f, 0.8f)
        lying(); hold(c, 6)
        repeat(5) { sitting(); hold(c, 6); lying(); hold(c, 6) }
        assertEquals(5, c.reps)
    }

    @Test
    fun crunchCountsAShoulderLift() {
        val c = RepCounter(ExerciseCatalog.Crunch)
        fun flat() = side(0.2f, 0.8f, 0.45f, 0.8f, 0.6f, 0.62f, 0.7f, 0.8f)
        fun curled() = side(0.27f, 0.68f, 0.45f, 0.8f, 0.6f, 0.62f, 0.7f, 0.8f)
        flat(); hold(c, 6)
        repeat(5) { curled(); hold(c, 6); flat(); hold(c, 6) }
        assertEquals(5, c.reps)
    }

    @Test
    fun gluteBridgeCountsHipsRaisedToALine() {
        val c = RepCounter(ExerciseCatalog.GluteBridge)
        fun down() = side(0.2f, 0.8f, 0.45f, 0.8f, 0.6f, 0.62f, 0.7f, 0.8f)
        fun up() = side(0.2f, 0.8f, 0.45f, 0.65f, 0.7f, 0.5f, 0.75f, 0.8f)
        down(); hold(c, 6)
        repeat(4) { up(); hold(c, 6); down(); hold(c, 6) }
        assertEquals(4, c.reps)
    }

    @Test
    fun torsoTwistCountsEachTurnOfTheShoulders() {
        val c = RepCounter(ExerciseCatalog.TorsoTwist)
        front(0.07f, shoulderHalf = 0.1f); hold(c)
        repeat(3) { front(0.07f, shoulderHalf = 0.08f); hold(c); front(0.07f, shoulderHalf = 0.1f); hold(c) }
        assertEquals(3, c.reps)
    }

    @Test
    fun shadowBoxingCountsPunchesFromEitherHand() {
        val c = RepCounter(ExerciseCatalog.ShadowBoxing)
        fun guard(leftX: Float, rightX: Float) {
            side(0.5f, 0.3f, 0.5f, 0.55f, 0.5f, 0.77f, 0.5f, 0.95f)
            at(Landmark.LEFT_WRIST, leftX, 0.33f); at(Landmark.RIGHT_WRIST, rightX, 0.33f)
        }
        guard(0.52f, 0.52f); hold(c, 4)
        repeat(4) { guard(0.8f, 0.52f); hold(c, 4); guard(0.52f, 0.52f); hold(c, 4) }
        repeat(4) { guard(0.52f, 0.8f); hold(c, 4); guard(0.52f, 0.52f); hold(c, 4) }
        assertEquals(8, c.reps)
    }

    // --- jumps (ankle lift, not hips) ------------------------------------------------------

    @Test
    fun jumpSquatNeedsFeetOffTheFloorNotJustLowHips() {
        val c = RepCounter(ExerciseCatalog.JumpSquat)
        front(0.07f); hold(c, 4)
        repeat(3) { front(0.07f, ankleY = 0.8f); hold(c, 3); front(0.07f); hold(c, 5) }
        assertEquals(3, c.reps)
        // Squatting down with feet planted: hips drop, ankles stay put.
        repeat(3) { front(0.07f, hipY = 0.65f); hold(c, 4); front(0.07f); hold(c, 5) }
        assertEquals("a plain squat is not a jump", 3, c.reps)
    }

    @Test
    fun calfRaiseCountsSmallHeelLifts() {
        val c = RepCounter(ExerciseCatalog.CalfRaise)
        front(0.06f); hold(c, 4)
        repeat(4) { front(0.06f, ankleY = 0.93f); hold(c, 5); front(0.06f); hold(c, 5) }
        assertEquals(4, c.reps)
    }

    // --- alternating limbs -----------------------------------------------------------------

    @Test
    fun birdDogCountsEachArmLegPairWhenBothReachOut() {
        val c = RepCounter(ExerciseCatalog.BirdDog)
        fun pose(leftArmOut: Boolean, rightArmOut: Boolean, leftLegOut: Boolean, rightLegOut: Boolean) {
            pair(Landmark.LEFT_SHOULDER, Landmark.RIGHT_SHOULDER, 0.3f, 0.5f)
            pair(Landmark.LEFT_HIP, Landmark.RIGHT_HIP, 0.6f, 0.5f)
            if (leftArmOut) at(Landmark.LEFT_WRIST, 0.1f, 0.5f) else at(Landmark.LEFT_WRIST, 0.3f, 0.7f)
            if (rightArmOut) at(Landmark.RIGHT_WRIST, 0.1f, 0.5f) else at(Landmark.RIGHT_WRIST, 0.3f, 0.7f)
            if (leftLegOut) at(Landmark.LEFT_ANKLE, 0.85f, 0.5f) else at(Landmark.LEFT_ANKLE, 0.6f, 0.75f)
            if (rightLegOut) at(Landmark.RIGHT_ANKLE, 0.85f, 0.5f) else at(Landmark.RIGHT_ANKLE, 0.6f, 0.75f)
        }
        pose(false, false, false, false); hold(c, 6)
        repeat(3) {
            pose(true, false, false, true); hold(c, 6); pose(false, false, false, false); hold(c, 6)
            pose(false, true, true, false); hold(c, 6); pose(false, false, false, false); hold(c, 6)
        }
        assertEquals(6, c.reps)
        // Arm out without the opposite leg is not a bird dog.
        repeat(3) { pose(true, false, false, false); hold(c, 6); pose(false, false, false, false); hold(c, 6) }
        assertEquals(6, c.reps)
    }

    // --- sequences -------------------------------------------------------------------------

    private fun stand() = side(0.5f, 0.3f, 0.5f, 0.55f, 0.5f, 0.77f, 0.5f, 0.95f)
    private fun squat() = side(0.55f, 0.45f, 0.45f, 0.7f, 0.6f, 0.78f, 0.5f, 0.95f)
    private fun plank() = side(0.2f, 0.78f, 0.5f, 0.85f, 0.65f, 0.9f, 0.8f, 0.95f)
    private fun airborne() = side(0.5f, 0.3f, 0.5f, 0.55f, 0.5f, 0.63f, 0.5f, 0.72f)

    @Test
    fun burpeeCountsOnlyWithTheJump() {
        val c = RepCounter(ExerciseCatalog.Burpee)
        stand(); hold(c, 4)
        // A burpee that finishes standing still: no jump, no rep.
        squat(); hold(c, 5); plank(); hold(c, 5); squat(); hold(c, 5); stand(); hold(c, 8)
        assertEquals("standing up without a jump is not a burpee", 0, c.reps)
        repeat(2) {
            squat(); hold(c, 5); plank(); hold(c, 5); squat(); hold(c, 5)
            stand(); hold(c, 1); airborne(); hold(c, 3); stand(); hold(c, 4)
        }
        assertEquals(2, c.reps)
    }

    @Test
    fun squatThrustCountsWithoutAJump() {
        val c = RepCounter(ExerciseCatalog.SquatThrust)
        stand(); hold(c, 4)
        repeat(3) { squat(); hold(c, 5); plank(); hold(c, 5); squat(); hold(c, 5); stand(); hold(c, 5) }
        assertEquals(3, c.reps)
    }

    // --- holds -----------------------------------------------------------------------------

    @Test
    fun plankHoldBreaksWhenTheHipsPike() {
        val c = RepCounter(ExerciseCatalog.Plank)
        fun level() = side(0.2f, 0.5f, 0.5f, 0.5f, 0.65f, 0.5f, 0.8f, 0.5f)
        fun piked() = side(0.2f, 0.5f, 0.5f, 0.3f, 0.65f, 0.4f, 0.8f, 0.5f)
        level(); hold(c, 31) // ~3 s
        assertEquals(3, c.reps)
        piked(); hold(c, 20) // 2 s of bad form resets it
        assertEquals(0, c.reps)
    }
}
