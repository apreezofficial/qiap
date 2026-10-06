package app.qiap.feature.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.qiap.core.designsystem.component.FormFeedbackPill
import app.qiap.core.designsystem.component.FormState
import app.qiap.core.designsystem.component.PillButton
import app.qiap.core.designsystem.component.PillButtonStyle
import app.qiap.core.designsystem.component.QiapProgressBar
import app.qiap.core.designsystem.component.QiapText
import app.qiap.core.designsystem.theme.QiapSpacing
import app.qiap.core.designsystem.theme.QiapTheme

private const val TARGET = 12

/**
 * Night theme over the camera feed (design.md §7). Phase 0: no camera; a tap counter stands in
 * for pose-detected reps. Nothing here may be glass or blurred once the preview is underneath.
 */
@Composable
fun WorkoutScreen(onFinish: () -> Unit) {
    val colors = QiapTheme.colors
    var reps by rememberSaveable { mutableIntStateOf(0) }

    Box(Modifier.fillMaxSize().background(colors.bg)) {
        QiapText(
            "Camera preview (Phase 1)",
            style = QiapTheme.type.caption,
            color = colors.ink3,
            modifier = Modifier.align(Alignment.Center),
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(QiapSpacing.md),
            verticalArrangement = Arrangement.spacedBy(QiapSpacing.md),
        ) {
            FormFeedbackPill(
                if (reps % 3 == 2) "Go lower" else "Good depth",
                if (reps % 3 == 2) FormState.Off else FormState.Good,
            )
            QiapText("$reps", style = QiapTheme.type.display)
            QiapText("of $TARGET squats", color = colors.ink2)
            QiapProgressBar(reps / TARGET.toFloat())
            if (reps < TARGET) {
                PillButton("Fake a rep", onClick = { reps++ }, style = PillButtonStyle.Secondary, modifier = Modifier.fillMaxWidth())
            } else {
                PillButton("Alarm off", onClick = onFinish, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}
