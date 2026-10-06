package app.qiap.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.qiap.core.designsystem.component.CardSize
import app.qiap.core.designsystem.component.Chip
import app.qiap.core.designsystem.component.IconTile
import app.qiap.core.designsystem.component.PillButton
import app.qiap.core.designsystem.component.PillButtonStyle
import app.qiap.core.designsystem.component.QiapCard
import app.qiap.core.designsystem.component.QiapScreen
import app.qiap.core.designsystem.component.QiapText
import app.qiap.core.designsystem.component.QiapWordmark
import app.qiap.core.designsystem.icon.QiapIcons
import app.qiap.core.designsystem.theme.QiapSpacing
import app.qiap.core.designsystem.theme.QiapTheme
import app.qiap.core.designsystem.theme.tabular

/** Phase 0 placeholder. Real alarm list and reliability card arrive with the alarm engine. */
@Composable
fun HomeScreen(
    onNewAlarm: () -> Unit,
    onPreviewRinging: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val colors = QiapTheme.colors
    val type = QiapTheme.type
    QiapScreen(bottomClearance = QiapSpacing.section) {
        Row(
            Modifier.fillMaxWidth().padding(top = QiapSpacing.xs),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            QiapWordmark()
            IconTile(QiapIcons.Settings, "Settings", onClick = onOpenSettings)
        }

        QiapText("Good morning", style = type.body, color = colors.ink2)
        QiapText("in 7h 32m", style = type.h2.tabular())

        QiapCard {
            QiapText("06:30", style = type.displayCompact)
            QiapText("Mon · Tue · Wed · Thu · Fri", style = type.caption, color = colors.ink3)
            Chip("12 squats", leadingIcon = QiapIcons.Alarm)
        }

        QiapCard(size = CardSize.Small) {
            QiapText("Reliability", style = type.h3)
            QiapText("Permission checks land with the alarm engine.", style = type.body, color = colors.ink2)
        }

        PillButton("New alarm", onClick = onNewAlarm, leadingIcon = QiapIcons.Plus, modifier = Modifier.fillMaxWidth())
        PillButton(
            "Preview ringing",
            onClick = onPreviewRinging,
            style = PillButtonStyle.Secondary,
            leadingIcon = QiapIcons.Play,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
