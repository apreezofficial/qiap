package app.qiap.alarm

import kotlin.random.Random

/**
 * The emergency exit (details.md §5): three arithmetic steps for when the workout can't happen
 * (injury, dark room, broken camera). Hard enough that a half-asleep thumb can't tap through it,
 * easy enough that nobody gets stuck. Pure Kotlin, seeded for tests.
 */
class FallbackChallenge(private val random: Random = Random.Default) {

    class Step(val question: String, val options: List<Int>, val answer: Int)

    var index = 0
        private set
    val total = STEPS
    val done: Boolean get() = index >= STEPS

    var current: Step = next()
        private set

    /** Returns true if [choice] was right and the challenge moved on; a wrong pick starts over. */
    fun answer(choice: Int): Boolean {
        if (done) return true
        return if (choice == current.answer) {
            index++
            if (!done) current = next()
            true
        } else {
            index = 0
            current = next()
            false
        }
    }

    private fun next(): Step {
        val kind = random.nextInt(3)
        val (question, answer) = when (kind) {
            0 -> { val a = random.nextInt(12, 60); val b = random.nextInt(12, 60); "$a + $b" to a + b }
            1 -> { val a = random.nextInt(40, 99); val b = random.nextInt(11, 39); "$a − $b" to a - b }
            else -> { val a = random.nextInt(3, 10); val b = random.nextInt(3, 10); "$a × $b" to a * b }
        }
        val options = LinkedHashSet<Int>()
        options.add(answer)
        while (options.size < 4) {
            val off = random.nextInt(1, 10) * (if (random.nextBoolean()) 1 else -1)
            val wrong = answer + off
            if (wrong > 0) options.add(wrong)
        }
        return Step(question, options.shuffled(random), answer)
    }

    companion object {
        const val STEPS = 3
    }
}
