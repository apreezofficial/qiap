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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.qiap.QiapApp
import app.qiap.alarm.HistoryStats
import app.qiap.alarm.Outcome
import app.qiap.core.designsystem.component.CardSize
import app.qiap.core.designsystem.component.CardTone
import app.qiap.core.designsystem.component.QiapCard
import app.qiap.core.designsystem.component.QiapScreen
import app.qiap.core.designsystem.component.QiapText
import app.qiap.core.designsystem.component.SealStamp
import app.qiap.core.designsystem.component.SealState
import app.qiap.core.designsystem.component.ShareBar
import app.qiap.core.designsystem.component.TwoToneHeadline
import app.qiap.core.designsystem.pictogram.Pictogram
import app.qiap.core.designsystem.pictogram.Pictograms
import app.qiap.core.designsystem.theme.QiapRadius
import app.qiap.core.designsystem.theme.QiapSpacing
import app.qiap.core.designsystem.theme.QiapTheme
import app.qiap.core.designsystem.theme.tabular
import app.qiap.exercise.ExerciseCatalog
import app.qiap.feature.home.NavClearance
import app.qiap.feature.pictogramFor
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** History (design.md §7): streak card, this month's seals, reps per exercise. From the real log. */
@Composable
fun HistoryScreen() {
    val colors = QiapTheme.colors
    val type = QiapTheme.type
    val context = LocalContext.current
    val container = remember { (context.applicationContext as QiapApp).container }
    val entries by container.historyStore.entries.collectAsStateWithLifecycle()
    val today = remember { LocalDate.now() }
    val stats = remember(entries) { HistoryStats.from(entries, today) }

    QiapScreen(bottomClearance = NavClearance) {
        TwoToneHeadline("Your seals.", "One per morning you won.", Modifier.padding(top = QiapSpacing.xs))

        QiapCard(tone = CardTone.Ink) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    QiapText("Current streak", style = type.label, color = colors.onInk.copy(alpha = 0.65f))
                    QiapText("${stats.streak}", style = type.displayCompact)
                    QiapText(
                        "Best ${stats.bestStreak} · ${stats.sealsThisYear} seals this year",
                        style = type.label,
                        color = colors.onInk.copy(alpha = 0.65f),
                    )
                }
                Pictogram(Pictograms.Squat, Modifier.size(84.dp), color = colors.onInk)
            }
        }

        MonthCard(today, stats.byDay)

        QiapCard(size = CardSize.Small, verticalArrangement = Arrangement.spacedBy(QiapSpacing.sm)) {
            QiapText("Reps so far", style = type.title)
            if (stats.repsByExercise.isEmpty()) {
                QiapText("Your first alarm's reps will show up here.", style = type.bodySmall, color = colors.ink2)
            }
            val max = (stats.repsByExercise.values.maxOrNull() ?: 1).coerceAtLeast(1)
            stats.repsByExercise.entries.sortedByDescending { it.value }.forEach { (id, n) ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Pictogram(pictogramFor(id), Modifier.size(28.dp), showFloor = false)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(QiapSpacing.xxs)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            QiapText(ExerciseCatalog.byId(id)?.name ?: id, style = type.label)
                            QiapText("$n", style = type.label.tabular(), color = colors.ink2)
                        }
                        ShareBar(n.toFloat() / max)
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthCard(today: LocalDate, byDay: Map<Long, Outcome>) {
    val colors = QiapTheme.colors
    val type = QiapTheme.type
    val first = today.withDayOfMonth(1)
    val lead = first.dayOfWeek.value - 1 // Monday-first grid
    val cells: List<Int?> = List(lead) { null } + (1..today.lengthOfMonth()).toList()

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
                week.forEach { day ->
                    val outcome = day?.let { byDay[first.withDayOfMonth(it).toEpochDay()] }
                    DayCell(day, today.dayOfMonth, outcome, Modifier.weight(1f))
                }
                repeat(7 - week.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun DayCell(day: Int?, today: Int, outcome: Outcome?, modifier: Modifier) {
    val colors = QiapTheme.colors
    Box(modifier.aspectRatio(1f), contentAlignment = Alignment.Center) {
        when {
            day == null -> Unit
            outcome == Outcome.EARNED -> SealStamp(size = Dp.Unspecified, state = SealState.Earned)
            outcome == Outcome.FALLBACK -> SealStamp(size = Dp.Unspecified, state = SealState.Fallback)
            outcome == Outcome.MISSED -> SealStamp(size = Dp.Unspecified, state = SealState.Missed)
            else -> QiapText("$day", style = QiapTheme.type.caption.tabular().copy(fontSize = 11.sp), color = colors.ink3)
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
