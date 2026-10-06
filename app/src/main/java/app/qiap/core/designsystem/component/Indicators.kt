package app.qiap.core.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import app.qiap.core.designsystem.theme.QiapRadius
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import app.qiap.core.designsystem.icon.QiapIcons
import app.qiap.core.designsystem.theme.QiapSpacing
import app.qiap.core.designsystem.theme.QiapTheme
import app.qiap.core.designsystem.theme.qiapTween

/** Thick rounded progress bar for the workout screen (design.md §7). One draw call, no layout per frame. */
@Composable
fun QiapProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = QiapTheme.colors.jade,
) {
    val track = QiapTheme.colors.surfaceMuted
    val value by animateFloatAsState(progress.coerceIn(0f, 1f), qiapTween(), label = "progress")
    Canvas(
        modifier
            .fillMaxWidth()
            .height(QiapSpacing.sm)
            .semantics { progressBarRangeInfo = ProgressBarRangeInfo(progress, 0f..1f) },
    ) {
        val r = CornerRadius(size.height / 2f)
        drawRoundRect(track, cornerRadius = r)
        if (value > 0f) drawRoundRect(color, size = Size(size.width * value, size.height), cornerRadius = r)
    }
}

/** Form feedback for the workout screen: jade = good form, saffron = off. Opaque, never glass. */
enum class FormState { Good, Off }

@Composable
fun FormFeedbackPill(text: String, state: FormState, modifier: Modifier = Modifier) {
    val colors = QiapTheme.colors
    val dot = if (state == FormState.Good) colors.jade else colors.saffron
    Row(
        modifier = modifier
            .background(colors.surface, QiapRadius.pill)
            .padding(horizontal = QiapSpacing.sm, vertical = QiapSpacing.xs),
        horizontalArrangement = Arrangement.spacedBy(QiapSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Canvas(Modifier.size(8.dp)) { drawCircle(dot) }
        QiapText(text, style = QiapTheme.type.label, color = colors.ink)
        if (state == FormState.Good) QiapIcon(QiapIcons.Check, null, tint = colors.jade, size = 16.dp)
    }
}
