package app.qiap.exercise

enum class Category(val label: String) { LOWER("Lower"), UPPER("Upper"), CORE("Core"), CARDIO("Cardio"), MOBILITY("Mobility") }

/** Where the phone goes (details.md §6). */
enum class CameraView(val hint: String) {
    FRONT("Face the phone, full body in frame, about 2 m away"),
    SIDE("Stand side-on to the phone, about 2 m away"),
    FLOOR("Prop the phone on the floor, a few steps back"),
}

/** How an exercise is counted. */
enum class Kind {
    /** One metric: rest → active → rest is a rep. */
    CYCLE,
    /** Left and right halves counted separately; every completed half is a rep. */
    ALTERNATING,
    /** Seconds held inside a band while the gates pass. */
    HOLD,
    /** Ordered body positions (burpee, inchworm, arm circle). */
    SEQUENCE,
}

/**
 * How one exercise is counted, as data (details.md §7). A rep is: the metric drops below
 * [downBelow], then rises back above [upAbove] (hysteresis, so jitter around one threshold never
 * double counts), at least [minRepMs] after the previous rep. Reaching [goodBelow] at the bottom
 * counts as good form. Holds count seconds inside [holdLow]..[holdHigh] instead; sequences walk
 * [stages] in order.
 */
class ExerciseSpec(
    val id: String,
    val name: String,
    val unit: String,
    val metric: Metric,
    val downBelow: Float = 0f,
    val upAbove: Float = 1f,
    val goodBelow: Float = 0f,
    val minRepMs: Long = 600,
    /** EMA weight for the newest sample, 0..1. Higher = snappier, noisier. */
    val smoothing: Float = 0.5f,
    /** Cue shown while a rep is too shallow. */
    val depthCue: String = "Go a little further",
    /**
     * True while these thresholds are first guesses. CLAUDE.md: tune from recorded landmark
     * fixtures, never by guessing. Flip to false only after a fixture run backs the numbers.
     */
    val provisional: Boolean = true,
    val category: Category = Category.LOWER,
    val kind: Kind = Kind.CYCLE,
    val view: CameraView = CameraView.FRONT,
    /** 1..3, shown as dots. */
    val difficulty: Int = 1,
    /** Reps, or seconds for holds. */
    val defaultTarget: Int = 12,
    val needsFloor: Boolean = false,
    val jumping: Boolean = false,
    val tip: String = "",
    val stepBackCue: String = "Step back so I can see your whole body",
    /** Second half of an ALTERNATING move. */
    val metricB: Metric? = null,
    /** Extra conditions that must hold for frames to count (body line straight, stance wide). */
    val gates: List<Range> = emptyList(),
    val holdLow: Float = 0f,
    val holdHigh: Float = 0f,
    val stages: List<Stage> = emptyList(),
    /** Sequences: how long the start pose must be held mid-move before the rep counts as abandoned (a jump passes through it). */
    val abandonMs: Long = 0,
    /** Key into the pictogram set; defaults to the id. */
    val pictogram: String = id,
) {
    init {
        require(smoothing in 0f..1f) { "$id: smoothing" }
        require(difficulty in 1..3) { "$id: difficulty" }
        when (kind) {
            Kind.CYCLE, Kind.ALTERNATING -> require(goodBelow <= downBelow && downBelow < upAbove) {
                "$id: need goodBelow <= downBelow < upAbove"
            }
            Kind.HOLD -> require(holdLow < holdHigh) { "$id: hold band" }
            Kind.SEQUENCE -> require(stages.size >= 3) { "$id: sequence needs stages" }
        }
        if (kind == Kind.ALTERNATING) require(metricB != null) { "$id: alternating needs metricB" }
    }

    val isHold: Boolean get() = kind == Kind.HOLD
}

/** The only place exercise thresholds live (CLAUDE.md). All 52 exercises from details.md §8. */
object ExerciseCatalog {
    private const val V = 0.5f

    // --- landmark groups -------------------------------------------------------------------
    private val sh = intArrayOf(Landmark.LEFT_SHOULDER, Landmark.RIGHT_SHOULDER)
    private val shL = intArrayOf(Landmark.LEFT_SHOULDER)
    private val shR = intArrayOf(Landmark.RIGHT_SHOULDER)
    private val elL = intArrayOf(Landmark.LEFT_ELBOW)
    private val elR = intArrayOf(Landmark.RIGHT_ELBOW)
    private val wr = intArrayOf(Landmark.LEFT_WRIST, Landmark.RIGHT_WRIST)
    private val wrL = intArrayOf(Landmark.LEFT_WRIST)
    private val wrR = intArrayOf(Landmark.RIGHT_WRIST)
    private val hip = intArrayOf(Landmark.LEFT_HIP, Landmark.RIGHT_HIP)
    private val hipL = intArrayOf(Landmark.LEFT_HIP)
    private val hipR = intArrayOf(Landmark.RIGHT_HIP)
    private val knL = intArrayOf(Landmark.LEFT_KNEE)
    private val knR = intArrayOf(Landmark.RIGHT_KNEE)
    private val anL = intArrayOf(Landmark.LEFT_ANKLE)
    private val anR = intArrayOf(Landmark.RIGHT_ANKLE)
    private val an = intArrayOf(Landmark.LEFT_ANKLE, Landmark.RIGHT_ANKLE)
    private val heelL = intArrayOf(Landmark.LEFT_HEEL)
    private val heelR = intArrayOf(Landmark.RIGHT_HEEL)
    private val nose = intArrayOf(Landmark.NOSE)

    private fun tri(a: Int, b: Int, c: Int) = intArrayOf(a, b, c)

    // --- shared metrics --------------------------------------------------------------------
    private val knee = JointAngle(
        tri(Landmark.LEFT_HIP, Landmark.LEFT_KNEE, Landmark.LEFT_ANKLE),
        tri(Landmark.RIGHT_HIP, Landmark.RIGHT_KNEE, Landmark.RIGHT_ANKLE), V,
    )
    private val kneeL = SideAngle(tri(Landmark.LEFT_HIP, Landmark.LEFT_KNEE, Landmark.LEFT_ANKLE), V)
    private val kneeR = SideAngle(tri(Landmark.RIGHT_HIP, Landmark.RIGHT_KNEE, Landmark.RIGHT_ANKLE), V)
    private val elbow = JointAngle(
        tri(Landmark.LEFT_SHOULDER, Landmark.LEFT_ELBOW, Landmark.LEFT_WRIST),
        tri(Landmark.RIGHT_SHOULDER, Landmark.RIGHT_ELBOW, Landmark.RIGHT_WRIST), V,
    )
    private val hipAngle = JointAngle(
        tri(Landmark.LEFT_SHOULDER, Landmark.LEFT_HIP, Landmark.LEFT_KNEE),
        tri(Landmark.RIGHT_SHOULDER, Landmark.RIGHT_HIP, Landmark.RIGHT_KNEE), V,
    )
    private val hipAngleL = SideAngle(tri(Landmark.LEFT_SHOULDER, Landmark.LEFT_HIP, Landmark.LEFT_KNEE), V)
    private val hipAngleR = SideAngle(tri(Landmark.RIGHT_SHOULDER, Landmark.RIGHT_HIP, Landmark.RIGHT_KNEE), V)
    private val bodyLine = JointAngle(
        tri(Landmark.LEFT_SHOULDER, Landmark.LEFT_HIP, Landmark.LEFT_ANKLE),
        tri(Landmark.RIGHT_SHOULDER, Landmark.RIGHT_HIP, Landmark.RIGHT_ANKLE), V,
    )
    private val kneeBodyLine = JointAngle(
        tri(Landmark.LEFT_SHOULDER, Landmark.LEFT_HIP, Landmark.LEFT_KNEE),
        tri(Landmark.RIGHT_SHOULDER, Landmark.RIGHT_HIP, Landmark.RIGHT_KNEE), V,
    )
    private val lean = TorsoLean(V)
    private val ankleSpread = WidthRatio(anL, anR, shL, shR, V)
    private val wristSpread = WidthRatio(wrL, wrR, shL, shR, V)

    private fun bodyLineAtLeast(min: Float) = Range(bodyLine, min, 400f, "Keep your body in a straight line")
    private fun kneesAtLeast(min: Float) = Range(knee, min, 400f, "Straighten your legs")

    /**
     * Opposite arm and leg both reaching out in line with the torso (bird dog, dead bug): the arm
     * angle at the shoulder and the leg angle at the hip are both near 180 degrees. Flipped and
     * combined with a max, so the value is low only when BOTH are extended.
     */
    private fun armLegLine(armLeft: Boolean): Metric {
        val arm = if (armLeft) tri(Landmark.LEFT_WRIST, Landmark.LEFT_SHOULDER, Landmark.LEFT_HIP)
        else tri(Landmark.RIGHT_WRIST, Landmark.RIGHT_SHOULDER, Landmark.RIGHT_HIP)
        val leg = if (armLeft) tri(Landmark.RIGHT_SHOULDER, Landmark.RIGHT_HIP, Landmark.RIGHT_ANKLE)
        else tri(Landmark.LEFT_SHOULDER, Landmark.LEFT_HIP, Landmark.LEFT_ANKLE)
        return MaxOf(Flip(SideAngle(arm, V), 360f), Flip(SideAngle(leg, V), 360f))
    }

    // --- Lower body (13) -------------------------------------------------------------------

    val Squat = ExerciseSpec(
        id = "squat", name = "Squat", unit = "squats", metric = knee,
        downBelow = 110f, upAbove = 155f, goodBelow = 95f, minRepMs = 600, smoothing = 0.5f,
        depthCue = "Go a little lower", category = Category.LOWER, view = CameraView.FRONT,
        difficulty = 1, defaultTarget = 12, tip = "Sit back like there's a chair behind you.",
        stepBackCue = "Step back so I can see your legs",
    )

    val SumoSquat = ExerciseSpec(
        id = "sumo-squat", name = "Sumo squat", unit = "squats", metric = knee,
        downBelow = 110f, upAbove = 155f, goodBelow = 95f, depthCue = "Go a little lower",
        category = Category.LOWER, view = CameraView.FRONT, difficulty = 1, defaultTarget = 12,
        tip = "Feet wide, toes out, knees over toes.", pictogram = "squat",
        gates = listOf(Range(ankleSpread, 1.5f, 9f, "Feet wider")),
    )

    val JumpSquat = ExerciseSpec(
        id = "jump-squat", name = "Jump squat", unit = "jumps",
        // Ankle lift off the floor in torso lengths, flipped: rest reads high, a jump reads low.
        metric = Flip(AnkleLift(V), 0.5f),
        downBelow = 0.4f, upAbove = 0.47f, goodBelow = 0.3f, minRepMs = 500, smoothing = 0.7f,
        depthCue = "Jump a little higher", category = Category.LOWER, view = CameraView.FRONT,
        difficulty = 3, defaultTarget = 10, jumping = true, tip = "Squat, then explode up.",
        pictogram = "squat",
    )

    val Lunge = ExerciseSpec(
        id = "lunge", name = "Forward lunge", unit = "lunges", metric = kneeL, metricB = kneeR,
        kind = Kind.ALTERNATING, downBelow = 100f, upAbove = 160f, goodBelow = 90f,
        depthCue = "Drop that back knee", category = Category.LOWER, view = CameraView.SIDE,
        difficulty = 2, defaultTarget = 12, tip = "Long step, back knee toward the floor.",
        stepBackCue = "Step back so I can see your legs",
    )

    val ReverseLunge = ExerciseSpec(
        id = "reverse-lunge", name = "Reverse lunge", unit = "lunges", metric = kneeL, metricB = kneeR,
        kind = Kind.ALTERNATING, downBelow = 100f, upAbove = 160f, goodBelow = 90f,
        depthCue = "Drop that back knee", category = Category.LOWER, view = CameraView.SIDE,
        difficulty = 2, defaultTarget = 12, tip = "Step back, lower, drive through the front heel.",
        pictogram = "lunge",
    )

    val SideLunge = ExerciseSpec(
        id = "side-lunge", name = "Side lunge", unit = "lunges", metric = kneeL, metricB = kneeR,
        kind = Kind.ALTERNATING, downBelow = 110f, upAbove = 155f, goodBelow = 100f,
        depthCue = "Sink into the bent leg", category = Category.LOWER, view = CameraView.FRONT,
        difficulty = 2, defaultTarget = 12, tip = "Step wide, sit into one hip, keep the other leg straight.",
        pictogram = "lunge",
    )

    val WallSit = ExerciseSpec(
        id = "wall-sit", name = "Wall sit", unit = "seconds", metric = knee, kind = Kind.HOLD,
        holdLow = 80f, holdHigh = 110f, category = Category.LOWER, view = CameraView.SIDE,
        difficulty = 2, defaultTarget = 30, tip = "Back flat on the wall, thighs level with the floor.",
        pictogram = "squat",
    )

    val GluteBridge = ExerciseSpec(
        id = "bridge", name = "Glute bridge", unit = "bridges",
        // Hips rise to ~170°; flipped so the flat-on-the-floor start reads high.
        metric = Flip(hipAngle, 300f), downBelow = 138f, upAbove = 150f, goodBelow = 130f,
        minRepMs = 800, depthCue = "Squeeze hips higher", category = Category.LOWER, view = CameraView.SIDE,
        difficulty = 1, defaultTarget = 12, needsFloor = true, tip = "Feet flat, push through the heels.",
    )

    val CalfRaise = ExerciseSpec(
        id = "calf-raise", name = "Calf raise", unit = "raises", metric = Flip(AnkleLift(V), 0.5f),
        downBelow = 0.46f, upAbove = 0.485f, goodBelow = 0.43f, minRepMs = 500, smoothing = 0.6f,
        depthCue = "Rise higher onto your toes", category = Category.LOWER, view = CameraView.SIDE,
        difficulty = 1, defaultTarget = 20, tip = "Slow up, slow down.",
        gates = listOf(kneesAtLeast(160f)),
    )

    val HighKnees = ExerciseSpec(
        id = "high-knees", name = "High knees", unit = "knees",
        metric = Flip(Above(knL, hipL, V), 1f), metricB = Flip(Above(knR, hipR, V), 1f), kind = Kind.ALTERNATING,
        downBelow = 1.15f, upAbove = 1.45f, goodBelow = 1.05f, minRepMs = 250, smoothing = 0.6f,
        depthCue = "Knees up to hip height", category = Category.LOWER, view = CameraView.FRONT,
        difficulty = 2, defaultTarget = 30, tip = "Drive each knee up to your hip.",
    )

    val ButtKicks = ExerciseSpec(
        id = "butt-kicks", name = "Butt kicks", unit = "kicks",
        metric = Dist(heelL, hipL, V), metricB = Dist(heelR, hipR, V), kind = Kind.ALTERNATING,
        downBelow = 0.35f, upAbove = 0.7f, goodBelow = 0.25f, minRepMs = 250, smoothing = 0.6f,
        depthCue = "Heel to glute", category = Category.LOWER, view = CameraView.SIDE,
        difficulty = 2, defaultTarget = 30, tip = "Flick each heel up toward your glute.", pictogram = "high-knees",
    )

    val SkaterHop = ExerciseSpec(
        id = "skater-hop", name = "Skater hop", unit = "hops",
        metric = Flip(HipSway(3000f, 1f, V), 1f), metricB = Flip(HipSway(3000f, -1f, V), 1f), kind = Kind.ALTERNATING,
        downBelow = 0.72f, upAbove = 0.85f, goodBelow = 0.6f, minRepMs = 350, smoothing = 0.6f,
        depthCue = "Hop wider", category = Category.LOWER, view = CameraView.FRONT,
        difficulty = 2, defaultTarget = 20, jumping = true, tip = "Leap side to side like a speed skater.",
        pictogram = "lunge",
    )

    val SideLegRaise = ExerciseSpec(
        id = "side-leg-raise", name = "Side leg raise", unit = "raises",
        metric = Flip(FromVertical(hipL, anL, V), 90f), metricB = Flip(FromVertical(hipR, anR, V), 90f),
        kind = Kind.ALTERNATING, downBelow = 62f, upAbove = 75f, goodBelow = 55f, minRepMs = 500,
        depthCue = "Lift the leg higher", category = Category.LOWER, view = CameraView.FRONT,
        difficulty = 1, defaultTarget = 16, tip = "Stand tall, lift out to the side.", pictogram = "jack",
    )

    // --- Upper body (9) --------------------------------------------------------------------

    val PushUp = ExerciseSpec(
        id = "pushup", name = "Push-up", unit = "push-ups", metric = elbow,
        downBelow = 100f, upAbove = 150f, goodBelow = 90f, minRepMs = 600, smoothing = 0.5f,
        depthCue = "Chest a little lower", category = Category.UPPER, view = CameraView.SIDE,
        difficulty = 2, defaultTarget = 10, needsFloor = true, tip = "Body in one line, chest to the floor.",
        gates = listOf(bodyLineAtLeast(150f)),
    )

    val WidePushUp = ExerciseSpec(
        id = "wide-pushup", name = "Wide push-up", unit = "push-ups", metric = elbow,
        downBelow = 100f, upAbove = 150f, goodBelow = 90f, depthCue = "Chest a little lower",
        category = Category.UPPER, view = CameraView.FRONT, difficulty = 2, defaultTarget = 10, needsFloor = true,
        tip = "Hands wider than your shoulders.", pictogram = "pushup",
        gates = listOf(Range(wristSpread, 1.6f, 9f, "Hands wider")),
    )

    val DiamondPushUp = ExerciseSpec(
        id = "diamond-pushup", name = "Diamond push-up", unit = "push-ups", metric = elbow,
        downBelow = 100f, upAbove = 150f, goodBelow = 90f, depthCue = "Chest a little lower",
        category = Category.UPPER, view = CameraView.FRONT, difficulty = 3, defaultTarget = 8, needsFloor = true,
        tip = "Thumbs and fingers touching under your chest.", pictogram = "pushup",
        gates = listOf(Range(wristSpread, 0f, 0.4f, "Hands together")),
    )

    val KneePushUp = ExerciseSpec(
        id = "knee-pushup", name = "Knee push-up", unit = "push-ups", metric = elbow,
        downBelow = 100f, upAbove = 150f, goodBelow = 90f, depthCue = "Chest a little lower",
        category = Category.UPPER, view = CameraView.SIDE, difficulty = 1, defaultTarget = 10, needsFloor = true,
        tip = "Knees down, straight line from knees to head.", pictogram = "pushup",
        gates = listOf(Range(kneeBodyLine, 150f, 400f, "Keep your body in a straight line")),
    )

    val PikePushUp = ExerciseSpec(
        id = "pike-pushup", name = "Pike push-up", unit = "push-ups", metric = elbow,
        downBelow = 105f, upAbove = 150f, goodBelow = 95f, depthCue = "Lower your head toward the floor",
        category = Category.UPPER, view = CameraView.SIDE, difficulty = 3, defaultTarget = 8, needsFloor = true,
        tip = "Hips high in an upside-down V.", pictogram = "pushup",
        gates = listOf(Range(hipAngle, 0f, 115f, "Hips higher")),
    )

    val TricepDip = ExerciseSpec(
        id = "tricep-dip", name = "Tricep dip", unit = "dips", metric = elbow,
        downBelow = 100f, upAbove = 155f, goodBelow = 90f, depthCue = "Lower a little more",
        category = Category.UPPER, view = CameraView.SIDE, difficulty = 2, defaultTarget = 10, needsFloor = true,
        tip = "Hands on a chair behind you, hips off the seat.", pictogram = "pushup",
    )

    val ShoulderTap = ExerciseSpec(
        id = "shoulder-tap", name = "Plank shoulder tap", unit = "taps",
        metric = Dist(wrL, shR, V), metricB = Dist(wrR, shL, V), kind = Kind.ALTERNATING,
        downBelow = 0.4f, upAbove = 0.8f, goodBelow = 0.3f, minRepMs = 350,
        depthCue = "Reach all the way to the shoulder", category = Category.UPPER, view = CameraView.FLOOR,
        difficulty = 3, defaultTarget = 20, needsFloor = true, tip = "Hold the plank, tap the opposite shoulder.",
        pictogram = "plank", gates = listOf(bodyLineAtLeast(150f)),
    )

    val ArmCircles = ExerciseSpec(
        id = "arm-circles", name = "Arm circles", unit = "circles", metric = Above(wrL, shL, V),
        kind = Kind.SEQUENCE, minRepMs = 700, category = Category.UPPER, view = CameraView.FRONT,
        difficulty = 1, defaultTarget = 15, tip = "Big slow circles, arms straight.", pictogram = "jack",
        stages = listOf(
            Stage("down", Range(Above(wrL, shL, V), -3f, -0.7f)),
            Stage("side", Range(Above(wrL, shL, V), -0.35f, 0.35f), Range(Dist(wrL, shL, V), 0.7f, 9f)),
            Stage("up", Range(Above(wrL, shL, V), 0.7f, 9f)),
            Stage("side", Range(Above(wrL, shL, V), -0.35f, 0.35f), Range(Dist(wrL, shL, V), 0.7f, 9f)),
            Stage("down", Range(Above(wrL, shL, V), -3f, -0.7f)),
        ),
    )

    val OverheadPress = ExerciseSpec(
        id = "overhead-press", name = "Shadow overhead press", unit = "presses",
        // Elbows bend at rest and straighten overhead: flipped so rest reads high.
        metric = Flip(elbow, 360f), downBelow = 200f, upAbove = 265f, goodBelow = 190f,
        depthCue = "Press all the way up", category = Category.UPPER, view = CameraView.FRONT,
        difficulty = 1, defaultTarget = 15, tip = "Fists at shoulders, press straight up.", pictogram = "jack",
    )

    // --- Core (14) -------------------------------------------------------------------------

    val SitUp = ExerciseSpec(
        id = "situp", name = "Sit-up", unit = "sit-ups", metric = hipAngle,
        downBelow = 85f, upAbove = 125f, goodBelow = 70f, minRepMs = 800, depthCue = "Come all the way up",
        category = Category.CORE, view = CameraView.SIDE, difficulty = 2, defaultTarget = 12, needsFloor = true,
        tip = "Lie flat, curl all the way up.", pictogram = "situp",
    )

    val Crunch = ExerciseSpec(
        id = "crunch", name = "Crunch", unit = "crunches", metric = lean,
        downBelow = 68f, upAbove = 80f, goodBelow = 62f, minRepMs = 600, depthCue = "Lift your shoulders higher",
        category = Category.CORE, view = CameraView.SIDE, difficulty = 1, defaultTarget = 15, needsFloor = true,
        tip = "Chin off chest, lift your shoulder blades.", pictogram = "situp",
    )

    val BicycleCrunch = ExerciseSpec(
        id = "bicycle-crunch", name = "Bicycle crunch", unit = "crunches",
        metric = Dist(elL, knR, V), metricB = Dist(elR, knL, V), kind = Kind.ALTERNATING,
        downBelow = 0.35f, upAbove = 0.7f, goodBelow = 0.25f, minRepMs = 400,
        depthCue = "Elbow to the opposite knee", category = Category.CORE, view = CameraView.FLOOR,
        difficulty = 2, defaultTarget = 20, needsFloor = true, tip = "Elbow to opposite knee, pedal your legs.",
        pictogram = "situp",
    )

    val LegRaise = ExerciseSpec(
        id = "leg-raise", name = "Leg raise", unit = "raises", metric = hipAngle,
        downBelow = 110f, upAbove = 160f, goodBelow = 95f, minRepMs = 800, depthCue = "Lift the legs higher",
        category = Category.CORE, view = CameraView.SIDE, difficulty = 2, defaultTarget = 12, needsFloor = true,
        tip = "Legs straight, lift to vertical, lower slowly.", pictogram = "situp",
        gates = listOf(kneesAtLeast(150f)),
    )

    val RussianTwist = ExerciseSpec(
        id = "russian-twist", name = "Russian twist", unit = "twists",
        metric = Flip(Lateral(wr, hip, V), 1f), metricB = Flip(Lateral(hip, wr, V), 1f), kind = Kind.ALTERNATING,
        downBelow = 0.82f, upAbove = 0.93f, goodBelow = 0.75f, minRepMs = 400, depthCue = "Rotate further to each side",
        category = Category.CORE, view = CameraView.FRONT, difficulty = 2, defaultTarget = 20, needsFloor = true,
        tip = "Lean back, swing your hands side to side.", pictogram = "situp",
    )

    val FlutterKick = ExerciseSpec(
        id = "flutter-kick", name = "Flutter kick", unit = "kicks",
        metric = Flip(Above(anL, anR, V), 1f), metricB = Flip(Above(anR, anL, V), 1f), kind = Kind.ALTERNATING,
        downBelow = 0.87f, upAbove = 0.95f, goodBelow = 0.8f, minRepMs = 250, smoothing = 0.6f,
        depthCue = "Kick a little higher", category = Category.CORE, view = CameraView.SIDE,
        difficulty = 2, defaultTarget = 30, needsFloor = true, tip = "Small fast kicks, legs straight.",
        pictogram = "situp",
    )

    val Plank = ExerciseSpec(
        id = "plank", name = "Plank", unit = "seconds", metric = bodyLine, kind = Kind.HOLD,
        holdLow = 160f, holdHigh = 185f, category = Category.CORE, view = CameraView.SIDE,
        difficulty = 2, defaultTarget = 30, needsFloor = true, tip = "Forearms down, body in one line.",
    )

    val SidePlank = ExerciseSpec(
        id = "side-plank", name = "Side plank", unit = "seconds", metric = bodyLine, kind = Kind.HOLD,
        holdLow = 160f, holdHigh = 185f, category = Category.CORE, view = CameraView.FLOOR,
        difficulty = 3, defaultTarget = 20, needsFloor = true, tip = "On one forearm, hips lifted.",
        pictogram = "plank",
    )

    val ReverseCrunch = ExerciseSpec(
        id = "reverse-crunch", name = "Reverse crunch", unit = "crunches", metric = hipAngle,
        downBelow = 80f, upAbove = 140f, goodBelow = 70f, minRepMs = 800, depthCue = "Knees closer to your chest",
        category = Category.CORE, view = CameraView.SIDE, difficulty = 2, defaultTarget = 12, needsFloor = true,
        tip = "Curl your knees to your chest, lift the hips.", pictogram = "situp",
    )

    val ToeTouch = ExerciseSpec(
        id = "toe-touch", name = "Standing toe touch", unit = "touches",
        metric = Dist(wr, an, V), downBelow = 0.35f, upAbove = 1.2f, goodBelow = 0.25f, minRepMs = 800,
        depthCue = "Reach down to your toes", category = Category.CORE, view = CameraView.FRONT,
        difficulty = 1, defaultTarget = 12, tip = "Soft knees, reach for your toes.", pictogram = "squat",
    )

    val ObliqueCrunch = ExerciseSpec(
        id = "oblique-crunch", name = "Standing oblique crunch", unit = "crunches",
        metric = Dist(elL, knL, V), metricB = Dist(elR, knR, V), kind = Kind.ALTERNATING,
        downBelow = 0.4f, upAbove = 0.8f, goodBelow = 0.3f, minRepMs = 400,
        depthCue = "Elbow down to the knee", category = Category.CORE, view = CameraView.FRONT,
        difficulty = 1, defaultTarget = 20, tip = "Lift your knee, bring your elbow down to meet it.",
        pictogram = "high-knees",
    )

    val VUp = ExerciseSpec(
        id = "v-up", name = "V-up", unit = "v-ups", metric = hipAngle,
        downBelow = 80f, upAbove = 145f, goodBelow = 70f, minRepMs = 900, depthCue = "Reach hands to feet",
        category = Category.CORE, view = CameraView.SIDE, difficulty = 3, defaultTarget = 10, needsFloor = true,
        tip = "Lift legs and chest at the same time.", pictogram = "situp",
    )

    val DeadBug = ExerciseSpec(
        id = "dead-bug", name = "Dead bug", unit = "reps",
        metric = armLegLine(armLeft = true), metricB = armLegLine(armLeft = false), kind = Kind.ALTERNATING,
        downBelow = 205f, upAbove = 250f, goodBelow = 195f, minRepMs = 800, depthCue = "Reach further out",
        category = Category.CORE, view = CameraView.FLOOR, difficulty = 1, defaultTarget = 16, needsFloor = true,
        tip = "Opposite arm and leg reach out, lower back stays flat.", pictogram = "situp",
    )

    val BirdDog = ExerciseSpec(
        id = "bird-dog", name = "Bird dog", unit = "reps",
        metric = armLegLine(armLeft = true), metricB = armLegLine(armLeft = false),
        kind = Kind.ALTERNATING, downBelow = 205f, upAbove = 250f, goodBelow = 195f, minRepMs = 1000,
        depthCue = "Reach arm and leg into one line", category = Category.CORE, view = CameraView.SIDE,
        difficulty = 2, defaultTarget = 12, needsFloor = true, tip = "Opposite arm and leg out, hold a beat.",
        pictogram = "plank",
    )

    // --- Full body & cardio (10) -----------------------------------------------------------

    val JumpingJack = ExerciseSpec(
        id = "jack", name = "Jumping jack", unit = "jacks",
        // Feet spread in shoulder widths, flipped: feet together reads high.
        metric = Flip(ankleSpread, 3f), downBelow = 1.5f, upAbove = 2.0f, goodBelow = 1.4f,
        minRepMs = 400, smoothing = 0.6f, depthCue = "Jump the feet wider", category = Category.CARDIO,
        view = CameraView.FRONT, difficulty = 1, defaultTarget = 30, jumping = true,
        tip = "Arms and legs out together, then back in.",
    )

    private val standStage = Stage("stand", Range(knee, 150f, 400f), Range(lean, 0f, 35f))
    private val squatStage = Stage("squat", Range(knee, 0f, 120f), Range(lean, 0f, 65f))
    private val plankStage = Stage("plank", Range(bodyLine, 150f, 400f), Range(lean, 60f, 180f))

    /** Airborne: ankles clearly off the floor (separates the burpee jump from a plain stand-up). */
    private val burpeeLift = AnkleLift(V)
    private val jumpStage = Stage("jump", Range(burpeeLift, 0.1f, 9f))

    val Burpee = ExerciseSpec(
        id = "burpee", name = "Burpee", unit = "burpees", metric = burpeeLift, kind = Kind.SEQUENCE,
        minRepMs = 1500, category = Category.CARDIO, view = CameraView.SIDE, difficulty = 3, defaultTarget = 8,
        needsFloor = true, jumping = true, tip = "Squat, plank, squat, then jump. No jump, no rep.",
        stages = listOf(standStage, squatStage, plankStage, squatStage, jumpStage),
        abandonMs = 500,
    )

    val MountainClimber = ExerciseSpec(
        id = "climber", name = "Mountain climber", unit = "climbers",
        metric = hipAngleL, metricB = hipAngleR, kind = Kind.ALTERNATING,
        downBelow = 80f, upAbove = 140f, goodBelow = 70f, minRepMs = 250, smoothing = 0.6f,
        depthCue = "Drive the knee further in", category = Category.CARDIO, view = CameraView.SIDE,
        difficulty = 3, defaultTarget = 30, needsFloor = true, tip = "Plank position, run your knees to your chest.",
        gates = listOf(bodyLineAtLeast(150f)),
    )

    val StarJump = ExerciseSpec(
        id = "star-jump", name = "Star jump", unit = "jumps",
        metric = Flip(ankleSpread, 3f), downBelow = 1.5f, upAbove = 2.0f, goodBelow = 1.4f,
        minRepMs = 500, smoothing = 0.6f, depthCue = "Jump the feet wider", category = Category.CARDIO,
        view = CameraView.FRONT, difficulty = 2, defaultTarget = 20, jumping = true,
        tip = "Jump into a star: arms and legs wide.", pictogram = "jack",
    )

    val SquatThrust = ExerciseSpec(
        id = "squat-thrust", name = "Squat thrust", unit = "thrusts", metric = lean, kind = Kind.SEQUENCE,
        minRepMs = 1200, category = Category.CARDIO, view = CameraView.SIDE, difficulty = 2, defaultTarget = 10,
        needsFloor = true, tip = "Hands down, kick back to plank, jump in, stand.", pictogram = "climber",
        stages = listOf(standStage, squatStage, plankStage, squatStage, standStage),
    )

    val ShadowBoxing = ExerciseSpec(
        id = "shadow-boxing", name = "Shadow boxing", unit = "punches",
        metric = Flip(Dist(wrL, shL, V), 3f), metricB = Flip(Dist(wrR, shR, V), 3f), kind = Kind.ALTERNATING,
        downBelow = 2.1f, upAbove = 2.35f, goodBelow = 2.0f, minRepMs = 250, smoothing = 0.7f,
        depthCue = "Punch all the way out", category = Category.CARDIO, view = CameraView.SIDE,
        difficulty = 1, defaultTarget = 40, tip = "Stand side-on. Fast straight punches, guard back up.", pictogram = "jack",
    )

    val JumpRope = ExerciseSpec(
        id = "jump-rope", name = "Jump rope", unit = "jumps", metric = Flip(AnkleLift(V), 0.5f),
        downBelow = 0.45f, upAbove = 0.48f, goodBelow = 0.42f, minRepMs = 250, smoothing = 0.7f,
        depthCue = "Bounce a little higher", category = Category.CARDIO, view = CameraView.FRONT,
        difficulty = 1, defaultTarget = 40, jumping = true, tip = "Imaginary rope, small quick hops.",
        pictogram = "jack",
    )

    val Jog = ExerciseSpec(
        id = "jog", name = "Jog in place", unit = "steps",
        metric = Flip(Above(knL, hipL, V), 1.0f), metricB = Flip(Above(knR, hipR, V), 1.0f), kind = Kind.ALTERNATING,
        downBelow = 1.55f, upAbove = 1.7f, goodBelow = 1.45f, minRepMs = 250, smoothing = 0.6f,
        depthCue = "Lift your knees higher", category = Category.CARDIO, view = CameraView.FRONT,
        difficulty = 1, defaultTarget = 40, tip = "Jog on the spot, knees up.", pictogram = "high-knees",
    )

    val SealJack = ExerciseSpec(
        id = "seal-jack", name = "Seal jack", unit = "jacks",
        metric = Flip(wristSpread, 5f), downBelow = 3.0f, upAbove = 4.0f, goodBelow = 2.5f,
        minRepMs = 400, smoothing = 0.6f, depthCue = "Open the arms wider", category = Category.CARDIO,
        view = CameraView.FRONT, difficulty = 1, defaultTarget = 30, jumping = true,
        tip = "Arms swing wide and clap in front, feet in and out.", pictogram = "jack",
    )

    private val foldStage = Stage("fold", Range(hipAngle, 0f, 80f))
    private val inchPlank = Stage("plank", Range(bodyLine, 150f, 400f), Range(lean, 60f, 180f))

    val Inchworm = ExerciseSpec(
        id = "inchworm", name = "Inchworm", unit = "inchworms", metric = lean, kind = Kind.SEQUENCE,
        minRepMs = 2000, category = Category.CARDIO, view = CameraView.SIDE, difficulty = 2, defaultTarget = 6,
        needsFloor = true, tip = "Fold forward, walk your hands out to plank, walk back.", pictogram = "plank",
        stages = listOf(standStage, foldStage, inchPlank, foldStage, standStage),
    )

    // --- Mobility & yoga holds (6) ---------------------------------------------------------

    val ChairPose = ExerciseSpec(
        id = "chair-pose", name = "Chair pose", unit = "seconds", metric = knee, kind = Kind.HOLD,
        holdLow = 100f, holdHigh = 140f, category = Category.MOBILITY, view = CameraView.SIDE,
        difficulty = 1, defaultTarget = 20, tip = "Sit back, arms overhead.", pictogram = "squat",
        gates = listOf(Range(Above(wr, nose, V), 0f, 9f, "Arms overhead")),
    )

    val WarriorII = ExerciseSpec(
        id = "warrior-ii", name = "Warrior II", unit = "seconds",
        metric = AngleExtreme(
            tri(Landmark.LEFT_HIP, Landmark.LEFT_KNEE, Landmark.LEFT_ANKLE),
            tri(Landmark.RIGHT_HIP, Landmark.RIGHT_KNEE, Landmark.RIGHT_ANKLE), useMax = false, minVisibility = V,
        ),
        kind = Kind.HOLD, holdLow = 80f, holdHigh = 110f, category = Category.MOBILITY, view = CameraView.FRONT,
        difficulty = 2, defaultTarget = 20, tip = "Wide stance, front knee bent, arms straight out.", pictogram = "lunge",
        gates = listOf(
            Range(
                AngleExtreme(
                    tri(Landmark.LEFT_HIP, Landmark.LEFT_KNEE, Landmark.LEFT_ANKLE),
                    tri(Landmark.RIGHT_HIP, Landmark.RIGHT_KNEE, Landmark.RIGHT_ANKLE), useMax = true, minVisibility = V,
                ),
                165f, 400f, "Straighten the back leg",
            ),
            Range(Above(wr, sh, V), -0.15f, 0.15f, "Arms level with your shoulders"),
        ),
    )

    val TreePose = ExerciseSpec(
        id = "tree-pose", name = "Tree pose", unit = "seconds",
        // How far a raised ankle is above the other knee, whichever foot is up.
        metric = MaxOf(Above(anL, knR, V), Above(anR, knL, V)), kind = Kind.HOLD,
        holdLow = -0.1f, holdHigh = 9f, category = Category.MOBILITY, view = CameraView.FRONT,
        difficulty = 2, defaultTarget = 20, tip = "Foot on the opposite leg, hands together.", pictogram = "squat",
        gates = listOf(Range(Dist(wrL, wrR, V), 0f, 0.5f, "Hands together")),
    )

    val DownwardDog = ExerciseSpec(
        id = "downward-dog", name = "Downward dog", unit = "seconds", metric = hipAngle, kind = Kind.HOLD,
        holdLow = 60f, holdHigh = 110f, category = Category.MOBILITY, view = CameraView.SIDE,
        difficulty = 2, defaultTarget = 20, needsFloor = true, tip = "Hips high, heels reaching down.",
        pictogram = "pushup", gates = listOf(kneesAtLeast(155f), Range(elbow, 155f, 400f, "Straighten your arms")),
    )

    val TorsoTwist = ExerciseSpec(
        id = "torso-twist", name = "Standing torso twist", unit = "twists",
        // Shoulders look narrower as they turn away from the camera (either way: each turn is one twist).
        metric = Flip(ShoulderSpanDrop(V), 1f),
        downBelow = 0.88f, upAbove = 0.93f, goodBelow = 0.84f, minRepMs = 500,
        depthCue = "Twist further around", category = Category.MOBILITY, view = CameraView.FRONT,
        difficulty = 1, defaultTarget = 16, tip = "Feet planted, rotate from the waist.", pictogram = "jack",
    )

    val SideBend = ExerciseSpec(
        id = "side-bend", name = "Side bend", unit = "bends",
        metric = Flip(SignedLean(1f, V), 40f), metricB = Flip(SignedLean(-1f, V), 40f), kind = Kind.ALTERNATING,
        downBelow = 25f, upAbove = 32f, goodBelow = 18f, minRepMs = 800, depthCue = "Reach further over",
        category = Category.MOBILITY, view = CameraView.FRONT, difficulty = 1, defaultTarget = 12,
        tip = "Arms overhead, lean slowly side to side.", pictogram = "side-bend",
    )

    /** All 52, grouped as in details.md §8. */
    val all: List<ExerciseSpec> = listOf(
        Squat, SumoSquat, JumpSquat, Lunge, ReverseLunge, SideLunge, WallSit, GluteBridge, CalfRaise,
        HighKnees, ButtKicks, SkaterHop, SideLegRaise,
        PushUp, WidePushUp, DiamondPushUp, KneePushUp, PikePushUp, TricepDip, ShoulderTap, ArmCircles, OverheadPress,
        SitUp, Crunch, BicycleCrunch, LegRaise, RussianTwist, FlutterKick, Plank, SidePlank, ReverseCrunch,
        ToeTouch, ObliqueCrunch, VUp, DeadBug, BirdDog,
        JumpingJack, Burpee, MountainClimber, StarJump, SquatThrust, ShadowBoxing, JumpRope, Jog, SealJack, Inchworm,
        ChairPose, WarriorII, TreePose, DownwardDog, TorsoTwist, SideBend,
    )

    private val index: Map<String, ExerciseSpec> = all.associateBy { it.id }

    fun byId(id: String): ExerciseSpec? = index[id]
}

/** A named set of exercises: a preset the user can pick, or the pool a random alarm draws from. */
class ExercisePool(val id: String, val name: String, val blurb: String, val members: List<ExerciseSpec>)

object ExercisePools {
    private fun of(vararg ids: String) = ids.map { requireNotNull(ExerciseCatalog.byId(it)) { "unknown exercise $it" } }

    val WakeUpLite = ExercisePool("wake-lite", "Wake-up Lite", "Gentle and quick", of("torso-twist", "side-bend", "jack", "squat", "crunch"))
    val CardioBlast = ExercisePool("cardio", "Cardio Blast", "Heart rate up", of("jack", "climber", "high-knees", "burpee", "jog"))
    val Strength = ExercisePool("strength", "Strength", "Push and pull", of("pushup", "squat", "lunge", "plank", "tricep-dip"))
    val CoreCrusher = ExercisePool("core", "Core Crusher", "Abs awake", of("situp", "bicycle-crunch", "leg-raise", "plank", "v-up"))
    val NoFloor = ExercisePool("no-floor", "No floor", "Small room, no mat", ExerciseCatalog.all.filter { !it.needsFloor })
    val Quiet = ExercisePool("quiet", "Quiet", "No jumping, apartment friendly", ExerciseCatalog.all.filter { !it.jumping })

    val all = listOf(WakeUpLite, CardioBlast, Strength, CoreCrusher, NoFloor, Quiet)

    fun byId(id: String): ExercisePool? = all.firstOrNull { it.id == id }
}
