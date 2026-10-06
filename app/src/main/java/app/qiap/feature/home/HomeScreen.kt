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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.qiap.core.common.formatCountdown
import app.qiap.core.common.minutesUntil
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
import app.qiap.feature.sample.SampleAlarm
import app.qiap.feature.sample.SampleData
import kotlinx.coroutines.delay
import java.time.LocalTime

/** Space under the content for the floating nav + "New" pill. */
internal val NavClearance = 112.dp

/** Home (design.md §7): countdown, insight tiles, alarm cards, reliability card. Sample data until the alarm engine lands. */
@Composable
fun HomeScreen(
    onPreviewRinging: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val colors = QiapTheme.colors
    val type = QiapTheme.type
    val alarms = remember { mutableStateListOf(*SampleData.alarms.toTypedArray()) }
    val now by produceState(LocalTime.now()) {
        while (true) {
            delay(15_000)
            value = LocalTime.now()
        }
    }
    val next = alarms.filter { it.on }.minByOrNull { minutesUntil(now, it.hour, it.minute) }
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
            IconTile(QiapIcons.Settings, "Settings", background = colors.surface, onClick = onOpenSettings)
        }

        Column(Modifier.padding(top = QiapSpacing.xs), verticalArrangement = Arrangement.spacedBy(QiapSpacing.xxs)) {
            QiapText("$greeting. Next alarm", style = type.body, color = colors.ink2)
            QiapText(
                if (next != null) "in ${formatCountdown(minutesUntil(now, next.hour, next.minute))}" else "none set",
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
                BigStat("6", "days")
                Chip("Best 11", leadingIcon = QiapIcons.Flame, tone = ChipTone.Saffron)
            }
            InsightCard(tone = CardTone.Sky) {
                QiapText("This week", style = type.label, color = colors.ink2)
                BigStat("64", "reps")
                MiniBars(listOf(0.4f, 0.7f, 0.55f, 1f, 0.3f, 0f, 0f), highlight = 3)
            }
            InsightCard {
                QiapText("Avg. out of bed", style = type.label, color = colors.ink2)
                BigStat("6:41", null)
                QiapText("11 min after ring", style = type.caption, color = colors.ink3)
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            QiapText("Alarms", style = type.h3)
            QiapText("${alarms.count { it.on }} on", style = type.label, color = colors.ink3)
        }
        alarms.forEachIndexed { i, alarm ->
            AlarmCard(
                alarm = alarm,
                countdown = if (alarm == next) formatCountdown(minutesUntil(now, alarm.hour, alarm.minute)) else null,
                phase = i * 0.3f,
                onToggle = { alarms[i] = alarm.copy(on = it) },
            )
        }

        ReliabilityCard(onFix = onOpenSettings)

        PillButton(
            "Preview ringing",
            onClick = onPreviewRinging,
            style = PillButtonStyle.Secondary,
            leadingIcon = QiapIcons.Play,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun InsightCard(tone: CardTone = CardTone.Surface, content: @Composable () -> Unit) {
    QiapCard(
        Modifier.width(148.dp),
        size = CardSize.Small,
        tone = tone,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) { content() }
}

@Composable
private fun BigStat(value: String, unit: String?) {
    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(QiapSpacing.xxs)) {
        QiapText(value, style = QiapTheme.type.displayCompact.copy(fontSize = 34.sp, lineHeight = 36.sp))
        if (unit != null) {
            QiapText(unit, style = QiapTheme.type.label, color = QiapTheme.colors.ink2, modifier = Modifier.padding(bottom = 6.dp))
        }
    }
}

@Composable
private fun AlarmCard(alarm: SampleAlarm, countdown: String?, phase: Float, onToggle: (Boolean) -> Unit) {
    val colors = QiapTheme.colors
    val type = QiapTheme.type
    val (time, amPm) = twelveHour(alarm.hour, alarm.minute)
    QiapCard(
        Modifier.alpha(if (alarm.on) 1f else 0.55f),
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
                    alarm.label + (countdown?.let { " · in $it" } ?: ""),
                    style = type.label.merge(type.numeric.copy(fontSize = type.label.fontSize)),
                    color = colors.ink2,
                )
            }
            QiapToggle(alarm.on, onToggle, "Alarm at $time $amPm", accent = ToggleAccent.Alarm)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            DayDots(alarm.days)
            Chip(
                "${alarm.target} ${alarm.exercise.unit}",
                tone = ChipTone.Muted,
                leading = { Pictogram(alarm.exercise.motion, Modifier.size(18.dp), showFloor = false, phase = phase) },
            )
        }
    }
}

@Composable
private fun ReliabilityCard(onFix: () -> Unit) {
    val colors = QiapTheme.colors
    val checks = listOf("Exact alarms" to true, "Full-screen alert" to true, "Battery unrestricted" to false, "Notifications" to true)
    QiapCard(size = CardSize.Small, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            QiapText("Will it ring?", style = QiapTheme.type.title)
            Chip("${checks.count { it.second }} of ${checks.size}", leadingIcon = QiapIcons.Shield, tone = ChipTone.Jade)
        }
        checks.forEach { (label, ok) ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                QiapText(label, style = QiapTheme.type.bodySmall)
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
