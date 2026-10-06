package app.qiap.feature.editor

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.qiap.QiapApp
import app.qiap.alarm.Alarm
import app.qiap.alarm.nextTrigger
import app.qiap.core.common.formatCountdown
import app.qiap.core.designsystem.component.CardSize
import app.qiap.core.designsystem.component.Chip
import app.qiap.core.designsystem.component.ExercisePickerItem
import app.qiap.core.designsystem.component.HairlineDivider
import app.qiap.core.designsystem.component.IconTile
import app.qiap.core.designsystem.component.ListRow
import app.qiap.core.designsystem.component.PillButton
import app.qiap.core.designsystem.component.PillButtonStyle
import app.qiap.core.designsystem.component.QiapCard
import app.qiap.core.designsystem.component.QiapScreen
import app.qiap.core.designsystem.component.QiapSlider
import app.qiap.core.designsystem.component.QiapStepper
import app.qiap.core.designsystem.component.QiapText
import app.qiap.core.designsystem.component.TimeWheel
import app.qiap.core.designsystem.icon.QiapIcons
import app.qiap.core.designsystem.theme.QiapSpacing
import app.qiap.core.designsystem.theme.QiapTheme
import app.qiap.core.designsystem.theme.bleedHorizontal
import app.qiap.exercise.ExerciseCatalog
import app.qiap.feature.home.NavClearance
import app.qiap.feature.pictogramFor
import java.time.Duration
import java.time.ZonedDateTime

private val DayNames = listOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su")

/**
 * Alarm editor (design.md §7). [alarmId] null = new alarm. Save writes the store and arms the
 * alarm immediately; no days selected means a one-shot alarm.
 */
@Composable
fun EditorScreen(alarmId: Int?, onDone: () -> Unit, onSeeAllExercises: () -> Unit) {
    val colors = QiapTheme.colors
    val context = LocalContext.current
    val container = remember { (context.applicationContext as QiapApp).container }
    val existing = remember(alarmId) { alarmId?.let { container.alarmStore.get(it) } }
    val start = existing ?: Alarm(id = 0, hour = 6, minute = 30, days = Alarm.WEEKDAYS)

    var hour by rememberSaveable { mutableIntStateOf(start.hour) }
    var minute by rememberSaveable { mutableIntStateOf(start.minute) }
    var days by rememberSaveable { mutableIntStateOf(start.days) }
    var exerciseId by rememberSaveable { mutableStateOf(start.exerciseId) }
    var target by rememberSaveable { mutableIntStateOf(start.target) }
    var volume by rememberSaveable { mutableFloatStateOf(start.volume) }
    val spec = ExerciseCatalog.byId(exerciseId) ?: ExerciseCatalog.Squat

    val draft = start.copy(hour = hour, minute = minute, days = days, exerciseId = spec.id, target = target, volume = volume, enabled = true)
    val now = ZonedDateTime.now()
    val inMinutes = ((Duration.between(now, nextTrigger(draft, now)).seconds + 59) / 60).toInt()

    QiapScreen(
        title = if (existing == null) "New alarm" else "Edit alarm",
        onBack = onDone,
        actions = { Chip("Cancel", onClick = onDone) },
        bottomClearance = NavClearance,
        bottomBar = {
            PillButton("Save · rings in ${formatCountdown(inMinutes)}", modifier = Modifier.fillMaxWidth(), onClick = {
                val saved = container.alarmStore.upsert(draft)
                container.alarmScheduler.sync(saved)
                onDone()
            })
        },
    ) {
        // Wheel only reports when it settles, so it starts from the stored time.
        TimeWheel(hour, minute, onChange = { h, m -> hour = h; minute = m })

        Column(verticalArrangement = Arrangement.spacedBy(QiapSpacing.xs)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(QiapSpacing.xxs)) {
                DayNames.forEachIndexed { i, d ->
                    val bit = 1 shl i
                    Chip(d, selected = days and bit != 0, onClick = { days = days xor bit }, modifier = Modifier.weight(1f))
                }
            }
            QiapText(
                if (days == 0) "No days picked: rings once, then turns itself off." else "Repeats on the picked days.",
                style = QiapTheme.type.caption,
                color = colors.ink3,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(QiapSpacing.xs)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                QiapText("Exercise", style = QiapTheme.type.title)
                Chip("Browse library", onClick = onSeeAllExercises)
            }
            Row(
                Modifier
                    .bleedHorizontal(QiapSpacing.md)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = QiapSpacing.md, vertical = QiapSpacing.xs),
                horizontalArrangement = Arrangement.spacedBy(QiapSpacing.sm),
            ) {
                // Only exercises the rep counter can actually verify are offered for alarms.
                ExerciseCatalog.all.forEachIndexed { i, ex ->
                    ExercisePickerItem(ex.name, pictogramFor(ex.id), selected = ex.id == spec.id, phase = i * 0.17f, onClick = { exerciseId = ex.id })
                }
            }
        }

        QiapCard(size = CardSize.Small, verticalArrangement = Arrangement.spacedBy(QiapSpacing.xs)) {
            ListRow(
                "Target",
                subtitle = "${spec.unit} to stop the alarm",
                trailing = {
                    QiapStepper(
                        target,
                        onDecrement = { target = (target - if (target > 20) 5 else 1).coerceAtLeast(1) },
                        onIncrement = { target = (target + if (target >= 20) 5 else 1).coerceAtMost(99) },
                    )
                },
            )
            HairlineDivider()
            ListRow(
                "Sound",
                subtitle = "Your phone's alarm sound",
                leading = { IconTile(QiapIcons.Music, null, size = 40.dp, background = colors.bgWash) },
            )
            HairlineDivider()
            Column(Modifier.padding(top = QiapSpacing.xs)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    QiapText("Volume", style = QiapTheme.type.title)
                    QiapText("Ramps up over 30 s", style = QiapTheme.type.caption, color = colors.ink3)
                }
                QiapSlider(volume, { volume = it.coerceAtLeast(0.1f) }, "Alarm volume")
            }
        }

        if (existing != null) {
            PillButton("Delete alarm", style = PillButtonStyle.Secondary, modifier = Modifier.fillMaxWidth(), onClick = {
                container.alarmScheduler.cancel(existing.id)
                container.alarmStore.delete(existing.id)
                onDone()
            })
        }
    }
}
