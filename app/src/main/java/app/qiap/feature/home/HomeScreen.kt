package app.qiap.feature.home

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.qiap.BuildConfig
import app.qiap.QiapApp
import app.qiap.alarm.Alarm
import app.qiap.alarm.Check
import app.qiap.alarm.HistoryStats
import app.qiap.alarm.dayFlags
import app.qiap.alarm.daysLabel
import app.qiap.alarm.nextTrigger
import app.qiap.core.common.formatCountdown
import app.qiap.core.common.twelveHour
import app.qiap.core.designsystem.component.CardSize
import app.qiap.core.designsystem.component.CardTone
import app.qiap.core.designsystem.component.Chip
import app.qiap.core.designsystem.component.ChipTone
import app.qiap.core.designsystem.component.DayDots
import app.qiap.core.designsystem.component.IconTile
import app.qiap.core.designsystem.component.MiniBars
import app.qiap.core.designsystem.component.PillButton
import app.qiap.core.designsystem.component.PillButtonStyle
import app.qiap.core.designsystem.component.QiapCard
import app.qiap.core.designsystem.component.QiapIcon
import app.qiap.core.designsystem.component.QiapScreen
import app.qiap.core.designsystem.component.QiapText
import app.qiap.core.designsystem.component.QiapToggle
import app.qiap.core.designsystem.component.QiapWordmark
import app.qiap.core.designsystem.component.SealStamp
import app.qiap.core.designsystem.component.ToggleAccent
import app.qiap.core.designsystem.icon.QiapIcons
import app.qiap.core.designsystem.pictogram.Pictogram
import app.qiap.core.designsystem.theme.QiapSpacing
import app.qiap.core.designsystem.theme.QiapTheme
import app.qiap.core.designsystem.theme.bleedHorizontal
import app.qiap.exercise.ExerciseCatalog
import app.qiap.exercise.ExercisePools
import app.qiap.feature.pictogramFor
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.ZonedDateTime

/** Space under the content for the floating nav + "New" pill. */
internal val NavClearance = 112.dp

/** Home (design.md §7): countdown, insight tiles, alarm cards, reliability card. Live data. */
@Composable
fun HomeScreen(
    onEditAlarm: (Int) -> Unit,
    onPreviewRinging: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val colors = QiapTheme.colors
    val type = QiapTheme.type
    val context = LocalContext.current
    val container = remember { (context.applicationContext as QiapApp).container }
    val alarms by container.alarmStore.alarms.collectAsStateWithLifecycle()
    val entries by container.historyStore.entries.collectAsStateWithLifecycle()
    val now by produceState(ZonedDateTime.now()) {
        while (true) {
            delay(15_000)
            value = ZonedDateTime.now()
        }
    }
    val stats = remember(entries, now.toLocalDate()) { HistoryStats.from(entries, now.toLocalDate()) }
    var checks by remember { mutableStateOf(container.reliability.status()) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { checks = container.reliability.status() }

    val next = alarms.filter { it.enabled }.map { it to nextTrigger(it, now) }.minByOrNull { it.second }
    val greeting = when (now.hour) {
        in 0..11 -> "Good morning"
        in 12..17 -> "Good afternoon"
        else -> "Good evening"
    }

    QiapScreen(wash = true, bottomClearance = NavClearance) {
        Row(
            Modifier.fillMaxWidth().padding(top = QiapSpacing.xs),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            QiapWordmark()
            IconTile(QiapIcons.Settings, "Setup", background = colors.surface, onClick = onOpenSettings)
        }

        Column(Modifier.padding(top = QiapSpacing.xs), verticalArrangement = Arrangement.spacedBy(QiapSpacing.xxs)) {
            QiapText(if (next != null) "$greeting. Next alarm" else "$greeting.", style = type.body, color = colors.ink2)
            QiapText(
                if (next != null) "in ${countdown(now, next.second)}" else "No alarm set",
                style = type.displayCompact.copy(fontSize = 52.sp, lineHeight = 56.sp),
            )
        }

        Row(
            Modifier
                .bleedHorizontal(QiapSpacing.md)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = QiapSpacing.md, vertical = QiapSpacing.xs),
            horizontalArrangement = Arrangement.spacedBy(QiapSpacing.sm),
        ) {
            InsightCard {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    QiapText("Streak", style = type.label, color = colors.ink2)
                    SealStamp(size = 22.dp)
                }
                BigStat("${stats.streak}", if (stats.streak == 1) "day" else "days")
                Chip("Best ${stats.bestStreak}", leadingIcon = QiapIcons.Flame, tone = ChipTone.Saffron)
            }
            InsightCard(tone = CardTone.Sky) {
                QiapText("This week", style = type.label, color = colors.ink2)
                BigStat("${stats.repsThisWeek}", "reps")
                MiniBars(stats.weekShape, highlight = now.dayOfWeek.value - 1)
            }
            InsightCard {
                QiapText("Avg. out of bed", style = type.label, color = colors.ink2)
                val up = stats.avgUpMinuteOfDay
                BigStat(if (up != null) twelveHour(up / 60, up % 60).first else "–", null)
                QiapText(
                    stats.avgMinutesToUp?.let { "$it min after ring" } ?: "After your first seal",
                    style = type.caption,
                    color = colors.ink3,
                )
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            QiapText("Alarms", style = type.h3)
            QiapText("${alarms.count { it.enabled }} on", style = type.label, color = colors.ink3)
        }
        if (alarms.isEmpty()) {
            QiapCard(tone = CardTone.Muted, modifier = Modifier.fillMaxWidth()) {
                QiapText("No alarms yet.", style = type.title)
                QiapText("Your bed is winning. Tap New to fix that.", style = type.bodySmall, color = colors.ink2)
            }
        }
        alarms.forEachIndexed { i, alarm ->
            AlarmCard(
                alarm = alarm,
                countdown = if (alarm.id == next?.first?.id) countdown(now, next.second) else null,
                phase = i * 0.3f,
                onClick = { onEditAlarm(alarm.id) },
                onToggle = { on ->
                    container.alarmStore.setEnabled(alarm.id, on)?.let { container.alarmScheduler.sync(it) }
                },
            )
        }

        ReliabilityCard(checks, onFix = onOpenSettings)

        // Developer shortcut: real users test the whole flow with "Test alarm" in Setup.
        if (BuildConfig.DEBUG) {
            PillButton(
                "Preview ringing",
                onClick = onPreviewRinging,
                style = PillButtonStyle.Secondary,
                leadingIcon = QiapIcons.Play,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

private fun countdown(now: ZonedDateTime, at: ZonedDateTime): String =
    formatCountdown(((Duration.between(now, at).seconds + 59) / 60).toInt().coerceAtLeast(1))

@Composable
private fun InsightCard(tone: CardTone = CardTone.Surface, content: @Composable () -> Unit) {
    QiapCard(
        Modifier.width(148.dp),
        size = CardSize.Small,
        tone = tone,
        verticalArrangement = Arrangement.spacedBy(QiapSpacing.sm),
    ) { content() }
}

@Composable
private fun BigStat(value: String, unit: String?) {
    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(QiapSpacing.xxs)) {
        QiapText(value, style = QiapTheme.type.displayCompact.copy(fontSize = 34.sp, lineHeight = 36.sp))
        if (unit != null) {
            QiapText(unit, style = QiapTheme.type.label, color = QiapTheme.colors.ink2, modifier = Modifier.padding(bottom = QiapSpacing.xs))
        }
    }
}

@Composable
private fun AlarmCard(alarm: Alarm, countdown: String?, phase: Float, onClick: () -> Unit, onToggle: (Boolean) -> Unit) {
    val colors = QiapTheme.colors
    val type = QiapTheme.type
    val (time, amPm) = twelveHour(alarm.hour, alarm.minute)
    val spec = ExerciseCatalog.byId(alarm.exerciseId) ?: ExerciseCatalog.Squat
    QiapCard(
        Modifier.alpha(if (alarm.enabled) 1f else 0.55f),
        onClick = onClick,
        contentPadding = PaddingValues(QiapSpacing.md),
        verticalArrangement = Arrangement.spacedBy(QiapSpacing.sm),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Column {
                Row(verticalAlignment = Alignment.Bottom) {
                    QiapText(time, style = type.displayCompact)
                    QiapText(
                        amPm,
                        style = type.label.copy(fontSize = 18.sp, fontWeight = FontWeight.SemiBold),
                        color = colors.ink3,
                        modifier = Modifier.padding(start = QiapSpacing.xxs, bottom = 10.dp),
                    )
                }
                QiapText(
                    alarm.label.ifBlank { alarm.daysLabel() } + (countdown?.let { " · in $it" } ?: ""),
                    style = type.label.merge(type.numeric.copy(fontSize = type.label.fontSize)),
                    color = colors.ink2,
                )
            }
            QiapToggle(alarm.enabled, onToggle, "Alarm at $time $amPm", accent = ToggleAccent.Alarm)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            DayDots(alarm.dayFlags())
            val pool = ExercisePools.byId(alarm.poolId)
            Chip(
                when {
                    alarm.routine.isNotEmpty() -> "Routine · ${alarm.routine.size} moves"
                    pool != null -> "Random · ${pool.name}"
                    else -> "${alarm.target} ${spec.unit}"
                },
                tone = ChipTone.Muted,
                leading = { Pictogram(pictogramFor(spec.id), Modifier.size(18.dp), showFloor = false, phase = phase) },
            )
        }
    }
}

@Composable
private fun ReliabilityCard(checks: Map<Check, Boolean>, onFix: () -> Unit) {
    val colors = QiapTheme.colors
    val required = checks.filterKeys { it.required && it != Check.CAMERA }
    QiapCard(size = CardSize.Small, verticalArrangement = Arrangement.spacedBy(QiapSpacing.sm)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            QiapText("Will it ring?", style = QiapTheme.type.title)
            val ok = required.count { it.value }
            Chip(
                "$ok of ${required.size}",
                leadingIcon = QiapIcons.Shield,
                tone = if (ok == required.size) ChipTone.Jade else ChipTone.Saffron,
            )
        }
        required.forEach { (check, ok) ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                QiapText(check.title, style = QiapTheme.type.bodySmall)
                if (ok) {
                    Row(horizontalArrangement = Arrangement.spacedBy(QiapSpacing.xxs), verticalAlignment = Alignment.CenterVertically) {
                        QiapIcon(QiapIcons.Check, null, tint = colors.jade, size = 16.dp)
                        QiapText("On", style = QiapTheme.type.label, color = colors.jade)
                    }
                } else {
                    Chip("Fix", tone = ChipTone.Saffron, onClick = onFix)
                }
            }
        }
    }
}
