package app.qiap.feature.ringing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import app.qiap.alarm.FallbackChallenge
import app.qiap.core.designsystem.component.CardSize
import app.qiap.core.designsystem.component.Chip
import app.qiap.core.designsystem.component.ChipTone
import app.qiap.core.designsystem.component.PillButton
import app.qiap.core.designsystem.component.PillButtonStyle
import app.qiap.core.designsystem.component.QiapCard
import app.qiap.core.designsystem.component.QiapProgressBar
import app.qiap.core.designsystem.component.QiapScreen
import app.qiap.core.designsystem.component.QiapText
import app.qiap.core.designsystem.component.TwoToneHeadline
import app.qiap.core.designsystem.theme.QiapSpacing
import app.qiap.core.designsystem.theme.QiapTheme

/**
 * The emergency exit (details.md §5): three sums, tap the right answer. Used when the workout
 * can't happen. A wrong answer restarts it, so it can't be tapped through half asleep.
 * [reason] explains why the user is here ("The camera isn't available").
 */
@Composable
fun FallbackScreen(reason: String, onSolved: () -> Unit, onBackToWorkout: (() -> Unit)?) {
    val colors = QiapTheme.colors
    val challenge = remember { FallbackChallenge() }
    // The challenge is plain Kotlin (not observable): bump `version` after each answer to redraw.
    var version by remember { mutableIntStateOf(0) }
    var wrong by remember { mutableStateOf(false) }
    version.hashCode()

    QiapScreen(
        bottomClearance = QiapSpacing.xl,
        bottomBar = if (onBackToWorkout != null) {
            { PillButton("Go back", onClick = onBackToWorkout, style = PillButtonStyle.Secondary, modifier = Modifier.fillMaxWidth()) }
        } else {
            null
        },
    ) {
        Chip("Emergency exit", tone = ChipTone.Saffron)
        TwoToneHeadline("Three sums,", "then you're free.", style = QiapTheme.type.h2, breakLine = true)
        QiapText(reason, style = QiapTheme.type.body, color = colors.ink2)
        QiapProgressBar(challenge.index / challenge.total.toFloat())

        val current = challenge.current
        QiapCard(Modifier.fillMaxWidth(), size = CardSize.Small) {
            QiapText("Step ${(challenge.index + 1).coerceAtMost(challenge.total)} of ${challenge.total}", style = QiapTheme.type.caption, color = colors.ink3)
            QiapText(
                current.question,
                style = QiapTheme.type.display.copy(fontSize = 56.sp, lineHeight = 60.sp),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = QiapSpacing.sm),
            )
            if (wrong) QiapText("Not quite. Starting over.", style = QiapTheme.type.caption, color = colors.crimson, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(QiapSpacing.xs), modifier = Modifier.fillMaxWidth()) {
            current.options.take(2).forEach { opt -> Choice(opt, Modifier.weight(1f)) { wrong = !pick(challenge, opt, onSolved) { version++ } } }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(QiapSpacing.xs), modifier = Modifier.fillMaxWidth()) {
            current.options.drop(2).forEach { opt -> Choice(opt, Modifier.weight(1f)) { wrong = !pick(challenge, opt, onSolved) { version++ } } }
        }
    }
}

private inline fun pick(challenge: FallbackChallenge, choice: Int, onSolved: () -> Unit, changed: () -> Unit): Boolean {
    val right = challenge.answer(choice)
    changed()
    if (challenge.done) onSolved()
    return right
}

@Composable
private fun Choice(value: Int, modifier: Modifier, onClick: () -> Unit) {
    PillButton("$value", onClick = onClick, style = PillButtonStyle.Secondary, modifier = modifier)
}
