package app.qiap.alarm

/** Small pure rules of the ringing flow, kept out of the service/UI so they are unit-tested. */
object RingRules {
    /** Milliseconds a ring that started at [startedAtMs] may still run; <= 0 means it already rang out its cap. */
    fun remainingRingMs(startedAtMs: Long, nowMs: Long, capMs: Long): Long = startedAtMs + capMs - nowMs

    /** The "earn your snooze" mini set: about a quarter of the real target, never below 3 reps or above 10 s of a hold. */
    fun miniSetTarget(isHold: Boolean, target: Int): Int =
        if (isHold) minOf(10, target) else (target / 4).coerceAtLeast(3).coerceAtMost(target)
}
