package app.qiap.feature.success

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.qiap.core.designsystem.component.CardSize
import app.qiap.core.designsystem.component.CardTone
import app.qiap.core.designsystem.component.IconTile
import app.qiap.core.designsystem.component.PillButton
import app.qiap.core.designsystem.component.QiapCard
import app.qiap.core.designsystem.component.QiapScreen
import app.qiap.core.designsystem.component.QiapText
import app.qiap.core.designsystem.component.SealStamp
import app.qiap.core.designsystem.component.TwoToneHeadline
import app.qiap.core.designsystem.component.saffronTint
import app.qiap.core.designsystem.icon.QiapIcons
import app.qiap.core.designsystem.theme.QiapSpacing
import app.qiap.core.designsystem.theme.QiapTheme
import app.qiap.core.designsystem.theme.riseIn

/** Success (design.md §7): the seal slams in, then the copy and stats rise in after it. */
@Composable
fun SuccessScreen(reps: Int, seconds: Int, streak: Int, best: Int, onDone: () -> Unit) {
    val colors = QiapTheme.colors
    val type = QiapTheme.type
    QiapScreen(
        wash = true,
        bottomClearance = 112.dp,
        bottomBar = { PillButton("Alarm off", onClick = onDone, modifier = Modifier.fillMaxWidth()) },
    ) {
        SealStamp(
            Modifier.align(Alignment.CenterHorizontally).padding(top = 28.dp),
            size = 168.dp,
            stampIn = true,
            shockwave = true,
            startDelayMs = 250,
        )
        TwoToneHeadline(
            "Seal earned.",
            "Your bed lost.",
            style = type.h2.copy(fontSize = 34.sp, lineHeight = 36.sp),
            textAlign = TextAlign.Center,
            breakLine = true,
            modifier = Modifier.fillMaxWidth().padding(top = QiapSpacing.xs).riseIn(600),
        )
        Row(Modifier.riseIn(750), horizontalArrangement = Arrangement.spacedBy(QiapSpacing.xs)) {
            Stat("Reps", "$reps", Modifier.weight(1f))
            Stat("Time", "${seconds / 60}:${(seconds % 60).toString().padStart(2, '0')}", Modifier.weight(1f))
            Stat("Best streak", "$best", Modifier.weight(1f))
        }
        QiapCard(Modifier.riseIn(900), size = CardSize.Small) {
            Row(horizontalArrangement = Arrangement.spacedBy(QiapSpacing.sm), verticalAlignment = Alignment.CenterVertically) {
                IconTile(QiapIcons.Flame, null, background = colors.saffronTint())
                Column(Modifier.weight(1f)) {
                    QiapText(if (streak == 1) "First seal of a streak" else "$streak-day streak", style = type.title)
                    QiapText(
                        when {
                            streak >= best -> "Your best yet."
                            else -> "${best - streak} more to beat your best"
                        },
                        style = type.caption,
                        color = colors.ink3,
                    )
                }
                QiapText("$streak", style = type.displayCompact.copy(fontSize = 30.sp, lineHeight = 32.sp))
            }
        }
    }
}

@Composable
private fun Stat(label: String, value: String, modifier: Modifier) {
    QiapCard(modifier, size = CardSize.Small, tone = CardTone.Muted, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        QiapText(label, style = QiapTheme.type.caption, color = QiapTheme.colors.ink3)
        QiapText(value, style = QiapTheme.type.displayCompact.copy(fontSize = 22.sp, lineHeight = 26.sp))
    }
}
