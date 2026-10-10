package app.qiap.exercise

/**
 * Landmark fixtures: recorded pose streams used to tune and regression-test [ExerciseCatalog]
 * thresholds (CLAUDE.md: tune from fixtures, never by guessing).
 *
 * Format: CSV, one frame per line, `#` lines are comments.
 *   timestampMs,aspect,hasPose, then 33 × (x,y,z,visibility)
 * The first comment line should say what was recorded, e.g. `# squat x10, good depth, front cam 2 m`.
 */
object Fixture {
    const val HEADER = "# timestampMs,aspect,hasPose,33x(x,y,z,visibility)"
    private const val FIELDS = 3 + Landmark.COUNT * 4

    /** Appends [frame] as one line. Debug recording only; allocates via the builder. */
    fun appendLine(frame: PoseFrame, out: StringBuilder) {
        out.append(frame.timestampMs).append(',').append(frame.aspect).append(',').append(if (frame.hasPose) 1 else 0)
        for (i in 0 until Landmark.COUNT) {
            out.append(',').append(frame.x[i]).append(',').append(frame.y[i])
                .append(',').append(frame.z[i]).append(',').append(frame.visibility[i])
        }
        out.append('\n')
    }

    /** Parses one line into [into]. Returns false for comments/blank lines. */
    fun parseLine(line: String, into: PoseFrame): Boolean {
        val trimmed = line.trim()
        if (trimmed.isEmpty() || trimmed.startsWith("#")) return false
        val parts = trimmed.split(',')
        require(parts.size == FIELDS) { "fixture line has ${parts.size} fields, expected $FIELDS" }
        into.timestampMs = parts[0].toLong()
        into.aspect = parts[1].toFloat()
        into.hasPose = parts[2] == "1"
        for (i in 0 until Landmark.COUNT) {
            val o = 3 + i * 4
            into.set(i, parts[o].toFloat(), parts[o + 1].toFloat(), parts[o + 2].toFloat(), parts[o + 3].toFloat())
        }
        return true
    }

    /** Runs a whole fixture through a fresh [RepCounter]; returns (reps, shallow reps). */
    fun replay(lines: Sequence<String>, spec: ExerciseSpec): Pair<Int, Int> {
        val counter = RepCounter(spec)
        val frame = PoseFrame()
        var shallow = 0
        for (line in lines) {
            if (!parseLine(line, frame)) continue
            if (counter.onFrame(frame) == RepEvent.REP_SHALLOW) shallow++
        }
        return counter.reps to shallow
    }
}
