package app.qiap.core.designsystem.theme

import android.provider.Settings
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode

/** The default 200 ms tween with the house curve. */
fun <T> qiapTween(durationMs: Int = QiapMotion.DURATION_MS): FiniteAnimationSpec<T> =
    tween(durationMillis = durationMs, easing = QiapMotion.easing)

/** True when the user turned animations off. Stamps appear without scale, pulses stop. */
@Composable
fun rememberReducedMotion(): Boolean {
    if (LocalInspectionMode.current) return false
    val resolver = LocalContext.current.contentResolver
    return remember(resolver) {
        Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }
}
