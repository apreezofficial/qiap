package app.qiap.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import app.qiap.core.designsystem.component.CardSize
import app.qiap.core.designsystem.component.CardTone
import app.qiap.core.designsystem.component.Chip
import app.qiap.core.designsystem.component.HairlineDivider
import app.qiap.core.designsystem.component.IconTile
import app.qiap.core.designsystem.component.ListRow
import app.qiap.core.designsystem.component.PillButton
import app.qiap.core.designsystem.component.QiapCard
import app.qiap.core.designsystem.component.QiapIcon
import app.qiap.core.designsystem.component.QiapScreen
import app.qiap.core.designsystem.component.QiapText
import app.qiap.core.designsystem.component.StepDots
import app.qiap.core.designsystem.component.TwoToneHeadline
import app.qiap.core.designsystem.component.jadeTint
import app.qiap.core.designsystem.component.saffronTint
import app.qiap.core.designsystem.icon.QiapIcons
import app.qiap.core.designsystem.pictogram.Pictogram
import app.qiap.core.designsystem.pictogram.Pictograms
import app.qiap.core.designsystem.theme.QiapSpacing
import app.qiap.core.designsystem.theme.QiapTheme
import kotlinx.coroutines.delay

private data class Permission(val icon: ImageVector, val title: String, val why: String)

private val Permissions = listOf(
    Permission(QiapIcons.Zap, "Exact alarms", "Ring at 6:30, not \"around then\""),
    Permission(QiapIcons.Layers, "Full-screen alert", "Show over the lock screen"),
    Permission(QiapIcons.Battery, "Battery unrestricted", "Stop Android pausing Qiap overnight"),
    Permission(QiapIcons.Camera, "Camera", "Count reps. Video never leaves the phone"),
)

/**
 * Setup / onboarding (design.md §7). Permission states are sample values until the alarm engine
 * can read the real ones. "Test alarm" counts down 10 s then opens Ringing. [onOpenGallery] is
 * null in release builds.
 */
@Composable
fun SettingsScreen(onBack: () -> Unit, onTestAlarm: () -> Unit, onOpenGallery: (() -> Unit)?) {
    val colors = QiapTheme.colors
    val type = QiapTheme.type
    var batteryAllowed by remember { mutableStateOf(false) }
    var testing by remember { mutableStateOf(false) }
    var left by remember { mutableIntStateOf(10) }
    val ring by rememberUpdatedState(onTestAlarm)
    LaunchedEffect(testing) {
        if (!testing) return@LaunchedEffect
        left = 10
        while (left > 0) {
            delay(1000)
            left--
        }
        testing = false
        ring()
    }

    QiapScreen(
        title = "Setup",
        onBack = onBack,
        bottomClearance = 112.dp,
        bottomBar = {
            PillButton(
                if (testing) "Ringing in $left…" else "Test alarm in 10 s",
                onClick = { testing = true },
                leadingIcon = QiapIcons.Bell,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    ) {
        StepDots(count = 4, current = 2)
        TwoToneHeadline("Let me make sure", "I can actually wake you.")
        QiapText("Android loves killing alarms. Four switches stop that.", style = type.bodySmall, color = colors.ink2)

        QiapCard(size = CardSize.Small, verticalArrangement = Arrangement.spacedBy(QiapSpacing.xs)) {
            Permissions.forEachIndexed { i, p ->
                if (i > 0) HairlineDivider()
                val ok = p.icon != QiapIcons.Battery || batteryAllowed
                ListRow(
                    p.title,
                    subtitle = p.why,
                    leading = { IconTile(p.icon, null, size = 40.dp, background = if (ok) colors.jadeTint() else colors.saffronTint()) },
                    trailing = {
                        if (ok) QiapIcon(QiapIcons.Check, "Allowed", tint = colors.jade)
                        else Chip("Allow", selected = true, onClick = { batteryAllowed = true })
                    },
                )
            }
        }

        QiapCard(size = CardSize.Small, tone = CardTone.Sky) {
            Row(horizontalArrangement = Arrangement.spacedBy(QiapSpacing.sm), verticalAlignment = Alignment.CenterVertically) {
                Pictogram(Pictograms.SideReach, Modifier.size(52.dp))
                Column(Modifier.weight(1f)) {
                    QiapText("Can't do it one morning?", style = type.title)
                    QiapText(
                        "A fallback stops the alarm. You get a saffron-edged seal, no judgement.",
                        style = type.caption,
                        color = colors.ink2,
                    )
                }
            }
        }

        if (onOpenGallery != null) {
            QiapCard(size = CardSize.Small, onClick = onOpenGallery) {
                ListRow("Design gallery", subtitle = "Debug builds only", trailing = { QiapIcon(QiapIcons.ChevronRight, null) })
            }
        }
    }
}
