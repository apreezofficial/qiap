package app.qiap.feature.ringing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.qiap.core.common.twelveHour
import app.qiap.core.designsystem.component.PillButton
import app.qiap.core.designsystem.component.PillButtonStyle
import app.qiap.core.designsystem.component.PulsingSun
import app.qiap.core.designsystem.component.QiapScreen
import app.qiap.core.designsystem.component.QiapText
import app.qiap.core.designsystem.component.TwoToneHeadline
import app.qiap.core.designsystem.icon.QiapIcons
import app.qiap.core.designsystem.pictogram.Pictogram
import app.qiap.core.designsystem.pictogram.Pictograms
import app.qiap.core.designsystem.theme.QiapSpacing
import app.qiap.core.designsystem.theme.QiapTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Full-bleed Cinnabar, giant ink time, pulsing sun, one white pill (design.md §7).
 * UI shell only: no audio, lock-screen flags or fallback yet (alarm engine phase).
 */
@Composable
fun RingingScreen(onStartWorkout: () -> Unit) {
    val colors = QiapTheme.colors
    val (time, _) = twelveHour(6, 30)
    val date = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE d MMMM", Locale.getDefault()))

    QiapScreen(
        bottomClearance = 140.dp,
        bottomBar = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(QiapSpacing.sm)) {
                PillButton(
                    "Start workout",
                    onClick = onStartWorkout,
                    style = PillButtonStyle.OnAccent,
                    leadingIcon = QiapIcons.Play,
                    modifier = Modifier.fillMaxWidth(),
                )
                QiapText("Can't do it today? Hold here for 3 s", style = QiapTheme.type.caption, color = colors.ink2)
            }
        },
    ) {
        QiapText(
            "$date · Weekdays",
            style = QiapTheme.type.label,
            color = colors.ink2,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = QiapSpacing.md),
        )
        Box(Modifier.fillMaxWidth().height(330.dp), contentAlignment = Alignment.Center) {
            PulsingSun(Modifier.requiredSize(400.dp))
            QiapText(time, style = QiapTheme.type.display.copy(fontSize = 112.sp, lineHeight = 112.sp))
        }
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            TwoToneHeadline(
                "12 squats",
                "to silence me.",
                style = QiapTheme.type.h2.copy(fontSize = 26.sp, lineHeight = 30.sp),
                textAlign = TextAlign.Center,
                breakLine = true,
            )
            Pictogram(Pictograms.Squat, Modifier.padding(top = QiapSpacing.xs).size(96.dp), color = colors.ink)
        }
    }
}
