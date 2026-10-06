package app.qiap.feature.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.qiap.core.designsystem.component.CardSize
import app.qiap.core.designsystem.component.QiapCard
import app.qiap.core.designsystem.component.QiapText
import app.qiap.core.designsystem.component.QiapScreen
import app.qiap.core.designsystem.component.SealStamp
import app.qiap.core.designsystem.component.SealState
import app.qiap.core.designsystem.component.TwoToneHeadline
import app.qiap.core.designsystem.theme.QiapSpacing
import app.qiap.core.designsystem.theme.QiapTheme
import app.qiap.core.designsystem.theme.tabular

// Sample week until history is persisted.
private val SampleWeek = listOf(
    SealState.Earned, SealState.Earned, SealState.Missed, SealState.Earned,
    SealState.Fallback, SealState.Earned, SealState.Earned,
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HistoryScreen() {
    QiapScreen(bottomClearance = QiapSpacing.section) {
        TwoToneHeadline("Your seals.", "Sample data.", Modifier.padding(top = QiapSpacing.xs))
        QiapCard {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(QiapSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(QiapSpacing.sm),
            ) {
                SampleWeek.forEach { SealStamp(size = 56.dp, state = it) }
            }
        }
        QiapCard(size = CardSize.Small) {
            QiapText("Streak", style = QiapTheme.type.caption, color = QiapTheme.colors.ink3)
            QiapText("2 days", style = QiapTheme.type.h3.tabular())
        }
    }
}
