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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.qiap.core.common.formatCountdown
import app.qiap.core.common.minutesUntil
import app.qiap.core.designsystem.component.CardSize
import app.qiap.core.designsystem.component.Chip
import app.qiap.core.designsystem.component.ExercisePickerItem
import app.qiap.core.designsystem.component.HairlineDivider
import app.qiap.core.designsystem.component.IconTile
import app.qiap.core.designsystem.component.ListRow
import app.qiap.core.designsystem.component.PillButton
import app.qiap.core.designsystem.component.QiapCard
import app.qiap.core.designsystem.component.QiapIcon
import app.qiap.core.designsystem.component.QiapScreen
import app.qiap.core.designsystem.component.QiapSlider
import app.qiap.core.designsystem.component.QiapStepper
import app.qiap.core.designsystem.component.QiapText
import app.qiap.core.designsystem.component.QiapToggle
import app.qiap.core.designsystem.component.SegmentedTabs
import app.qiap.core.designsystem.component.TimeWheel
import app.qiap.core.designsystem.icon.QiapIcons
import app.qiap.core.designsystem.theme.QiapSpacing
import app.qiap.core.designsystem.theme.QiapTheme
import app.qiap.core.designsystem.theme.bleedHorizontal
import app.qiap.feature.home.NavClearance
import app.qiap.feature.sample.SampleData
import java.time.LocalTime

private val DayNames = listOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su")

/** Alarm editor (design.md §7). Saving is a no-op until the alarm store exists. */
@Composable
fun EditorScreen(onBack: () -> Unit, onSeeAllExercises: () -> Unit) {
    val colors = QiapTheme.colors
    var hour by rememberSaveable { mutableIntStateOf(6) }
    var minute by rememberSaveable { mutableIntStateOf(30) }
    val days = remember { mutableStateListOf(true, true, true, true, true, false, false) }
    var exercise by rememberSaveable { mutableIntStateOf(0) }
    var target by rememberSaveable { mutableIntStateOf(12) }
    var volume by rememberSaveable { mutableFloatStateOf(0.8f) }
    var snooze by rememberSaveable { mutableIntStateOf(0) }
    var proof by rememberSaveable { mutableStateOf(true) }
    val chosen = SampleData.exercises[exercise]
    val countdown = formatCountdown(minutesUntil(LocalTime.now(), hour, minute))

    QiapScreen(
        title = "New alarm",
        onBack = onBack,
        actions = { Chip("Cancel", onClick = onBack) },
        bottomClearance = NavClearance,
        bottomBar = { PillButton("Save · rings in $countdown", onClick = onBack, modifier = Modifier.fillMaxWidth()) },
    ) {
        TimeWheel(hour, minute, onChange = { h, m -> hour = h; minute = m })

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(QiapSpacing.xxs)) {
            DayNames.forEachIndexed { i, d ->
                Chip(d, selected = days[i], onClick = { days[i] = !days[i] }, modifier = Modifier.weight(1f))
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(QiapSpacing.xs)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                QiapText("Exercise", style = QiapTheme.type.title)
                Chip("See all 52", onClick = onSeeAllExercises)
            }
            Row(
                Modifier
                    .bleedHorizontal(QiapSpacing.md)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = QiapSpacing.md, vertical = QiapSpacing.xs),
                horizontalArrangement = Arrangement.spacedBy(QiapSpacing.sm),
            ) {
                SampleData.exercises.forEachIndexed { i, ex ->
                    ExercisePickerItem(ex.name, ex.motion, selected = i == exercise, phase = i * 0.17f, onClick = {
                        exercise = i
                        target = if (ex.unit == "seconds" || ex.unit == "jacks" || ex.unit == "knees") 30 else 12
                    })
                }
            }
        }

        QiapCard(size = CardSize.Small, verticalArrangement = Arrangement.spacedBy(QiapSpacing.xs)) {
            ListRow(
                "Target",
                subtitle = "${chosen.unit} to stop the alarm",
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
                subtitle = "Sunrise chime",
                leading = { IconTile(QiapIcons.Music, null, size = 40.dp, background = colors.bgWash) },
                trailing = { QiapIcon(QiapIcons.ChevronRight, null, tint = colors.ink3) },
                onClick = {},
            )
            HairlineDivider()
            Column(Modifier.padding(top = QiapSpacing.xs)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    QiapText("Volume", style = QiapTheme.type.title)
                    QiapText("Ramps up over 30 s", style = QiapTheme.type.caption, color = colors.ink3)
                }
                QiapSlider(volume, { volume = it }, "Alarm volume")
            }
            HairlineDivider()
            Column(Modifier.padding(vertical = QiapSpacing.xs), verticalArrangement = Arrangement.spacedBy(QiapSpacing.xs)) {
                QiapText("Snooze", style = QiapTheme.type.title)
                SegmentedTabs(listOf("None", "Once", "3 times"), snooze, { snooze = it }, fillWidth = true)
            }
            HairlineDivider()
            ListRow(
                "Video proof",
                subtitle = "Saved on this phone only",
                leading = { IconTile(QiapIcons.Video, null, size = 40.dp, background = colors.bgWash) },
                trailing = { QiapToggle(proof, { proof = it }, "Video proof") },
            )
        }
    }
}
