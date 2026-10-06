package app.qiap.feature.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.qiap.core.designsystem.component.CardSize
import app.qiap.core.designsystem.component.CardTone
import app.qiap.core.designsystem.component.QiapCard
import app.qiap.core.designsystem.component.QiapIcon
import app.qiap.core.designsystem.component.QiapScreen
import app.qiap.core.designsystem.component.QiapText
import app.qiap.core.designsystem.component.SealStamp
import app.qiap.core.designsystem.component.SealState
import app.qiap.core.designsystem.component.ShareBar
import app.qiap.core.designsystem.component.TwoToneHeadline
import app.qiap.core.designsystem.icon.QiapIcons
import app.qiap.core.designsystem.pictogram.Pictogram
import app.qiap.core.designsystem.pictogram.PictogramMotion
import app.qiap.core.designsystem.theme.QiapRadius
import app.qiap.core.designsystem.theme.QiapSpacing
import app.qiap.core.designsystem.theme.QiapTheme
import app.qiap.core.designsystem.theme.tabular
import app.qiap.feature.home.NavClearance
import app.qiap.feature.sample.SampleData
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

// Sample outcomes until the session log exists; deterministic so screenshots are stable.
private val Pattern = listOf(
    SealState.Earned, SealState.Earned, SealState.Earned, SealState.Missed, SealState.Earned, SealState.Fallback,
    SealState.Earned, SealState.Earned, SealState.Earned, SealState.Earned, SealState.Missed, SealState.Earned,
)

/** History (design.md §7): streak card, month of seals, per-exercise totals, proof videos. */
@Composable
fun HistoryScreen() {
    val colors = QiapTheme.colors
    val type = QiapTheme.type
    QiapScreen(bottomClearance = NavClearance) {
        TwoToneHeadline("Your seals.", "One per morning you won.", Modifier.padding(top = QiapSpacing.xs))

        QiapCard(tone = CardTone.Ink) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    QiapText("Current streak", style = type.label, color = colors.onInk.copy(alpha = 0.65f))
                    QiapText("7", style = type.displayCompact)
                    QiapText("Best 11 · 23 seals this year", style = type.label, color = colors.onInk.copy(alpha = 0.65f))
                }
                Pictogram(SampleData.squat.motion, Modifier.size(84.dp), color = colors.onInk)
            }
        }

        MonthCard()

        QiapCard(size = CardSize.Small, verticalArrangement = Arrangement.spacedBy(QiapSpacing.sm)) {
            QiapText("Reps this month", style = type.title)
            listOf(
                Triple(SampleData.squat, 312, 1f),
                Triple(SampleData.pushUp, 140, 0.45f),
                Triple(SampleData.lunge, 96, 0.31f),
                Triple(SampleData.jack, 60, 0.19f),
            ).forEach { (ex, n, share) ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Pictogram(ex.motion, Modifier.size(28.dp), showFloor = false)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(QiapSpacing.xxs)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            QiapText(ex.name, style = type.label)
                            QiapText("$n", style = type.label.tabular(), color = colors.ink2)
                        }
                        ShareBar(share)
                    }
                }
            }
        }

        QiapCard(size = CardSize.Small, verticalArrangement = Arrangement.spacedBy(QiapSpacing.sm)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                QiapText("Proof videos", style = type.title)
                QiapText("On this phone", style = type.caption, color = colors.ink3)
            }
            VideoRow("Today", "6:31 am · Squat × 12 · 0:48", SampleData.squat.motion)
            VideoRow("Yesterday", "6:34 am · Squat × 12 · 1:02", SampleData.squat.motion)
            VideoRow("Sunday", "8:16 am · Side reach × 10 · 1:20", SampleData.reach.motion)
        }
    }
}

@Composable
private fun MonthCard() {
    val colors = QiapTheme.colors
    val type = QiapTheme.type
    val today = remember { LocalDate.now() }
    val first = today.withDayOfMonth(1)
    val lead = first.dayOfWeek.value - 1 // Monday-first grid
    val days = today.lengthOfMonth()
    val cells: List<Int?> = List(lead) { null } + (1..days).toList()

    QiapCard(verticalArrangement = Arrangement.spacedBy(QiapSpacing.sm)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            QiapText(today.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())), style = type.title)
            Row(horizontalArrangement = Arrangement.spacedBy(QiapSpacing.xs), verticalAlignment = Alignment.CenterVertically) {
                Legend(SealState.Earned, "earned")
                Legend(SealState.Fallback, "fallback")
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("M", "T", "W", "T", "F", "S", "S").forEach {
                QiapText(it, style = type.caption, color = colors.ink3, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
            }
        }
        cells.chunked(7).forEach { week ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                week.forEach { day -> DayCell(day, today.dayOfMonth, Modifier.weight(1f)) }
                repeat(7 - week.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun DayCell(day: Int?, today: Int, modifier: Modifier) {
    val colors = QiapTheme.colors
    Box(modifier.aspectRatio(1f), contentAlignment = Alignment.Center) {
        when {
            day == null -> Unit
            day > today -> QiapText("$day", style = QiapTheme.type.caption.tabular().copy(fontSize = 11.sp), color = colors.ink3)
            else -> SealStamp(size = Dp.Unspecified, state = if (day == today) SealState.Earned else Pattern[(day * 7) % Pattern.size])
        }
        if (day == today) {
            Box(Modifier.align(Alignment.BottomCenter).offset(y = 7.dp).size(4.dp).background(colors.ink, QiapRadius.pill))
        }
    }
}

@Composable
private fun Legend(state: SealState, label: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(QiapSpacing.xxs), verticalAlignment = Alignment.CenterVertically) {
        SealStamp(size = 12.dp, state = state)
        QiapText(label, style = QiapTheme.type.caption, color = QiapTheme.colors.ink3)
    }
}

@Composable
private fun VideoRow(title: String, detail: String, motion: PictogramMotion) {
    val colors = QiapTheme.colors
    Row(horizontalArrangement = Arrangement.spacedBy(QiapSpacing.sm), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(56.dp, 72.dp)
                .clip(QiapRadius.tile)
                .drawWithCache {
                    val b = Brush.verticalGradient(listOf(Color(0xFF24221F), Color(0xFF0D0D0F)))
                    onDrawBehind { drawRect(b) }
                },
            contentAlignment = Alignment.Center,
        ) {
            Pictogram(motion, Modifier.size(44.dp), color = Color(0xFFF4EFE6), showFloor = false)
            Box(
                Modifier.align(Alignment.BottomEnd).padding(QiapSpacing.xxs).size(18.dp)
                    .background(Color.White.copy(alpha = 0.9f), QiapRadius.pill),
                contentAlignment = Alignment.Center,
            ) { QiapIcon(QiapIcons.Play, "Play", tint = Color(0xFF111114), size = 10.dp) }
        }
        Column(Modifier.weight(1f)) {
            QiapText(title, style = QiapTheme.type.title)
            QiapText(detail, style = QiapTheme.type.caption, color = colors.ink3)
        }
        QiapIcon(QiapIcons.ChevronRight, null, tint = colors.ink3)
    }
}
