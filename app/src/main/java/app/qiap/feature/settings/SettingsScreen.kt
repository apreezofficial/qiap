package app.qiap.feature.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import app.qiap.QiapApp
import app.qiap.alarm.Check
import app.qiap.alarm.OemGuides
import app.qiap.camera.ProofStore
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

private val CheckIcons: Map<Check, ImageVector> = mapOf(
    Check.EXACT_ALARMS to QiapIcons.Zap,
    Check.NOTIFICATIONS to QiapIcons.Bell,
    Check.FULL_SCREEN to QiapIcons.Layers,
    Check.BATTERY to QiapIcons.Battery,
    Check.CAMERA to QiapIcons.Camera,
)

private const val TEST_DELAY_S = 10

/**
 * Setup (design.md §7): live status of everything an alarm needs, each with a one-tap fix, and a
 * real "Test alarm in 10 s" that goes through AlarmManager exactly like a normal alarm.
 * [onOpenGallery] is null in release builds.
 */
@Composable
fun SettingsScreen(onBack: () -> Unit, onOpenGallery: (() -> Unit)?) {
    val colors = QiapTheme.colors
    val type = QiapTheme.type
    val context = LocalContext.current
    val container = remember { (context.applicationContext as QiapApp).container }
    val reliability = container.reliability
    var status by remember { mutableStateOf(reliability.status()) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { status = reliability.status() }
    val askPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        status = reliability.status()
    }

    var testLeft by remember { mutableIntStateOf(0) }
    LaunchedEffect(testLeft > 0) {
        while (testLeft > 0) {
            delay(1000)
            testLeft--
        }
    }
    val ready = status.filterKeys { it.required }.count { it.value }
    val requiredCount = status.keys.count { it.required }

    QiapScreen(
        title = "Setup",
        onBack = onBack,
        bottomClearance = 112.dp,
        bottomBar = {
            PillButton(
                if (testLeft > 0) "Ringing in $testLeft… lock your phone" else "Test alarm in $TEST_DELAY_S s",
                leadingIcon = QiapIcons.Bell,
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    if (testLeft == 0) {
                        container.alarmScheduler.scheduleTest(TEST_DELAY_S * 1000L, exerciseId = "squat", target = 5)
                        testLeft = TEST_DELAY_S
                    }
                },
            )
        },
    ) {
        StepDots(count = requiredCount, current = (ready - 1).coerceAtLeast(0))
        TwoToneHeadline("Let me make sure", "I can actually wake you.")
        QiapText(
            if (ready == requiredCount) "All set. Run a test to hear it." else "Android loves killing alarms. These switches stop that.",
            style = type.bodySmall,
            color = colors.ink2,
        )

        QiapCard(size = CardSize.Small, verticalArrangement = Arrangement.spacedBy(QiapSpacing.xs)) {
            Check.entries.forEachIndexed { i, check ->
                if (i > 0) HairlineDivider()
                val ok = status[check] == true
                ListRow(
                    check.title + if (check.required) "" else " (recommended)",
                    subtitle = check.why,
                    leading = {
                        IconTile(CheckIcons.getValue(check), null, size = 40.dp, background = if (ok) colors.jadeTint() else colors.saffronTint())
                    },
                    trailing = {
                        if (ok) {
                            QiapIcon(QiapIcons.Check, "Allowed", tint = colors.jade)
                        } else {
                            Chip("Allow", selected = true, onClick = {
                                val permission = reliability.runtimePermission(check)
                                if (permission != null) askPermission.launch(permission)
                                else context.startActivity(reliability.settingsIntent(check))
                            })
                        }
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
                        "Hold the button on the alarm for 3 s, then solve three quick sums. It stops, and you get a saffron-edged seal. No judgement.",
                        style = type.caption,
                        color = colors.ink2,
                    )
                }
            }
        }

        // Battery-killer guide for this phone's maker (details.md §5): plain steps + an "I did this" tick.
        val guide = remember { OemGuides.forManufacturer(Build.MANUFACTURER) }
        val settingsPrefs = remember { context.getSharedPreferences("settings", Context.MODE_PRIVATE) }
        var guideDone by remember { mutableStateOf(settingsPrefs.getBoolean("oem_guide_done", false)) }
        QiapCard(size = CardSize.Small, verticalArrangement = Arrangement.spacedBy(QiapSpacing.xs)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                QiapText("Keep Qiap alive on ${guide.brand}", style = type.title, modifier = Modifier.weight(1f))
                Chip(
                    if (guideDone) "Done" else "Not yet",
                    selected = guideDone,
                    leadingIcon = if (guideDone) QiapIcons.Check else null,
                    onClick = {
                        guideDone = !guideDone
                        settingsPrefs.edit().putBoolean("oem_guide_done", guideDone).apply()
                    },
                )
            }
            QiapText(
                "Phone makers love closing alarm apps to save battery. These steps stop that.",
                style = type.caption,
                color = colors.ink3,
            )
            guide.steps.forEachIndexed { i, step ->
                Row(horizontalArrangement = Arrangement.spacedBy(QiapSpacing.sm), verticalAlignment = Alignment.Top) {
                    QiapText("${i + 1}", style = type.label, color = colors.ink3)
                    QiapText(step, style = type.bodySmall, color = colors.ink2, modifier = Modifier.weight(1f))
                }
            }
            Chip("Open Qiap's app settings", leadingIcon = QiapIcons.Settings, onClick = {
                context.startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                )
            })
        }

        var keepDays by remember { mutableIntStateOf(ProofStore.retentionDays(context)) }
        QiapCard(size = CardSize.Small, verticalArrangement = Arrangement.spacedBy(QiapSpacing.xs)) {
            ListRow(
                "Keep video proof for",
                subtitle = "Videos stay on this phone and are deleted after this long.",
                leading = { IconTile(QiapIcons.Video, null, size = 40.dp, background = colors.bgWash) },
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(QiapSpacing.xs)) {
                for (days in listOf(7, 14, 30)) {
                    Chip(
                        "$days days",
                        selected = keepDays == days,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            keepDays = days
                            ProofStore.setRetentionDays(context, days)
                        },
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
