package app.qiap.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Placeholder generator. Run with `./gradlew :app:generateBaselineProfile` on a connected device.
 * Extend the journey (open editor, ringing, workout) once those screens are real.
 */
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {
    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun startup() = rule.collect(packageName = "app.qiap") {
        pressHome()
        startActivityAndWait()
    }
}
