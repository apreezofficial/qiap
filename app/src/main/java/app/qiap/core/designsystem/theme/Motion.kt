package app.qiap.core.designsystem.theme

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode

/** The default 200 ms tween with the house curve. */
fun <T> qiapTween(durationMs: Int = QiapMotion.DURATION_MS): FiniteAnimationSpec<T> =
    tween(durationMillis = durationMs, easing = QiapMotion.easing)

/**
 * Fade + 12dp rise on first composition, after [delayMs]. Used to stagger the success screen.
 * Appears instantly with reduced motion.
 */
@Composable
fun Modifier.riseIn(delayMs: Int = 0): Modifier {
    val reduced = rememberReducedMotion()
    val p = remember { Animatable(if (reduced) 1f else 0f) }
    LaunchedEffect(Unit) {
        if (reduced) return@LaunchedEffect
        delay(delayMs.toLong())
        p.animateTo(1f, tween(500, easing = QiapMotion.easing))
    }
    return graphicsLayer {
        alpha = p.value
        translationY = (1f - p.value) * 12.dp.toPx()
    }
}

/** True when the user turned animations off. Stamps appear without scale, pulses stop. */
@Composable
fun rememberReducedMotion(): Boolean {
    if (LocalInspectionMode.current) return false
    val resolver = LocalContext.current.contentResolver
    return remember(resolver) {
        Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }
}
