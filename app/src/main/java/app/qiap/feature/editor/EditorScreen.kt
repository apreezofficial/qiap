package app.qiap.feature.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import app.qiap.core.designsystem.component.Chip
import app.qiap.core.designsystem.component.PillButton
import app.qiap.core.designsystem.component.QiapCard
import app.qiap.core.designsystem.component.QiapScreen
import app.qiap.core.designsystem.component.QiapText
import app.qiap.core.designsystem.theme.QiapSpacing
import app.qiap.core.designsystem.theme.QiapTheme

private val Days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

/** Phase 0 placeholder: day chips + sticky save pill. Time wheel, pickers and policies come later. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditorScreen(onBack: () -> Unit) {
    val selected = remember { mutableStateListOf(true, true, true, true, true, false, false) }
    QiapScreen(
        title = "New alarm",
        onBack = onBack,
        bottomClearance = QiapSpacing.section,
        bottomBar = { PillButton("Save alarm", onClick = onBack, modifier = Modifier.fillMaxWidth()) },
    ) {
        QiapText("06:30", style = QiapTheme.type.display)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(QiapSpacing.xs),
            verticalArrangement = Arrangement.spacedBy(QiapSpacing.xs),
        ) {
            Days.forEachIndexed { i, day ->
                Chip(day, selected = selected[i], onClick = { selected[i] = !selected[i] })
            }
        }
        QiapCard {
            QiapText("Exercise, target, sound, snooze, video proof", style = QiapTheme.type.h3)
            QiapText("Rows arrive with the alarm editor phase.", color = QiapTheme.colors.ink2)
        }
    }
}
