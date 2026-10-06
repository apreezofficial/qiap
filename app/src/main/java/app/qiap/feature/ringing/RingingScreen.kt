package app.qiap.feature.ringing

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.qiap.core.designsystem.component.PillButton
import app.qiap.core.designsystem.component.PillButtonStyle
import app.qiap.core.designsystem.component.QiapScreen
import app.qiap.core.designsystem.component.QiapText
import app.qiap.core.designsystem.theme.QiapMotion
import app.qiap.core.designsystem.theme.QiapSpacing
import app.qiap.core.designsystem.theme.QiapTheme
import app.qiap.core.designsystem.theme.rememberReducedMotion

/**
 * Full-bleed Cinnabar, giant ink time, slow-pulsing sun, one white pill (design.md §7).
 * Phase 0 shell only: no audio, no lock-screen flags, no fallback yet.
 */
@Composable
fun RingingScreen(onStartWorkout: () -> Unit) {
    val colors = QiapTheme.colors
    QiapScreen(
        bottomClearance = QiapSpacing.section,
        bottomBar = {
            PillButton(
                "Start workout",
                onClick = onStartWorkout,
                style = PillButtonStyle.OnAccent,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    ) {
        Spacer(Modifier.size(QiapSpacing.xxl))
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            PulsingSun(Modifier.size(280.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(QiapSpacing.xs)) {
                QiapText("06:30", style = QiapTheme.type.display, textAlign = TextAlign.Center)
                QiapText("12 reps to silence me.", color = colors.ink2, textAlign = TextAlign.Center)
            }
        }
    }
}

/** Opacity-only 3 s pulse. Static when reduced motion is on. */
@Composable
private fun PulsingSun(modifier: Modifier) {
    val sun = QiapTheme.colors.surface
    val reduced = rememberReducedMotion()
    val alpha = if (reduced) {
        0.22f
    } else {
        rememberInfiniteTransition(label = "sun").animateFloat(
            initialValue = 0.12f,
            targetValue = 0.32f,
            animationSpec = infiniteRepeatable(
                tween(QiapMotion.SUN_PULSE_MS / 2, easing = LinearEasing),
                RepeatMode.Reverse,
            ),
            label = "sunAlpha",
        ).value
    }
    Canvas(modifier.alpha(alpha)) { drawCircle(sun) }
}
