package app.qiap.feature.ringing

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.qiap.core.common.twelveHour
import app.qiap.core.designsystem.component.PillButton
import app.qiap.core.designsystem.component.PillButtonStyle
import app.qiap.core.designsystem.component.PulsingSun
import app.qiap.core.designsystem.component.QiapScreen
import app.qiap.core.designsystem.component.QiapText
import app.qiap.core.designsystem.component.TwoToneHeadline
import app.qiap.core.designsystem.icon.QiapIcons
import app.qiap.core.designsystem.pictogram.Pictogram
import app.qiap.core.designsystem.theme.QiapRadius
import app.qiap.core.designsystem.theme.QiapSpacing
import app.qiap.core.designsystem.theme.QiapTheme
import app.qiap.feature.pictogramFor
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val HOLD_MS = 3000

/**
 * Full-bleed Cinnabar, giant ink time, pulsing sun, one white pill (design.md §7).
 * The fallback is hidden behind a deliberate 3 s hold, so a sleepy thumb can't hit it by accident.
 */
@Composable
fun RingingScreen(
    hour: Int,
    minute: Int,
    label: String,
    exerciseId: String,
    exerciseUnit: String,
    target: Int,
    onStartWorkout: () -> Unit,
    onFallback: () -> Unit,
    snoozesLeft: Int = 0,
    onSnooze: (() -> Unit)? = null,
) {
    val colors = QiapTheme.colors
    val (time, _) = twelveHour(hour, minute)
    val date = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE d MMMM", Locale.getDefault()))

    QiapScreen(
        bottomClearance = if (snoozesLeft > 0 && onSnooze != null) 208.dp else 150.dp,
        bottomBar = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(QiapSpacing.sm)) {
                PillButton(
                    "Start workout",
                    onClick = onStartWorkout,
                    style = PillButtonStyle.OnAccent,
                    leadingIcon = QiapIcons.Play,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (snoozesLeft > 0 && onSnooze != null) {
                    PillButton(
                        "Snooze 5 min · $snoozesLeft left",
                        onClick = onSnooze,
                        style = PillButtonStyle.Secondary,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                HoldToSkip(onFallback)
            }
        },
    ) {
        QiapText(
            if (label.isBlank()) date else "$date · $label",
            style = QiapTheme.type.label,
            color = colors.ink2,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = QiapSpacing.md),
        )
        Box(Modifier.fillMaxWidth().height(330.dp), contentAlignment = Alignment.Center) {
            PulsingSun(Modifier.requiredSize(400.dp))
            QiapText(time, style = QiapTheme.type.display.copy(fontSize = 112.sp, lineHeight = 112.sp))
        }
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            TwoToneHeadline(
                "$target $exerciseUnit",
                "to silence me.",
                style = QiapTheme.type.h2.copy(fontSize = 26.sp, lineHeight = 30.sp),
                textAlign = TextAlign.Center,
                breakLine = true,
            )
            Pictogram(pictogramFor(exerciseId), Modifier.padding(top = QiapSpacing.xs).size(96.dp), color = colors.ink)
        }
    }
}

/** A quiet pill that fills while held; releasing early cancels. Haptic tick on completion. */
@Composable
private fun HoldToSkip(onFallback: () -> Unit) {
    val colors = QiapTheme.colors
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val progress = remember { Animatable(0f) }
    val fire by rememberUpdatedState(onFallback)
    Box(
        Modifier
            .fillMaxWidth(0.8f)
            .height(40.dp)
            .clip(QiapRadius.pill)
            .background(Color(0x1F111114))
            .semantics {
                contentDescription = "Can't do it today? Hold for 3 seconds to stop without the workout"
                onLongClick { fire(); true }
            }
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown()
                    scope.launch { progress.animateTo(1f, tween((HOLD_MS * (1f - progress.value)).toInt(), easing = LinearEasing)) }
                    val released = withTimeoutOrNull(HOLD_MS.toLong()) { waitForUpOrCancellation() }
                    if (released == null) {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        fire()
                    } else {
                        scope.launch { progress.animateTo(0f, tween(200)) }
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .align(Alignment.CenterStart)
                .fillMaxHeight()
                .fillMaxWidth(progress.value)
                .background(Color(0x33111114)),
        )
        QiapText("Can't do it today? Hold for 3 s", style = QiapTheme.type.caption, color = colors.ink)
    }
}
