package app.qiap.feature.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import app.qiap.core.designsystem.component.CardTone
import app.qiap.core.designsystem.component.Chip
import app.qiap.core.designsystem.component.QiapCard
import app.qiap.core.designsystem.component.QiapScreen
import app.qiap.core.designsystem.component.QiapText
import app.qiap.core.designsystem.component.SectionBadge
import app.qiap.core.designsystem.component.SegmentedTabs
import app.qiap.core.designsystem.component.TwoToneHeadline
import app.qiap.core.designsystem.theme.QiapSpacing
import app.qiap.core.designsystem.theme.QiapTheme

private val Categories = listOf("All", "Lower", "Upper", "Core", "Cardio", "Mobility")

/** Phase 0 placeholder for the exercise library bento grid. */
@Composable
fun LibraryScreen() {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    QiapScreen(bottomClearance = QiapSpacing.section) {
        SectionBadge("Library", Modifier.padding(top = QiapSpacing.xs))
        TwoToneHeadline("52 ways to", "get out of bed.")
        SegmentedTabs(Categories, selectedIndex = tab, onSelect = { tab = it })
        Row(horizontalArrangement = Arrangement.spacedBy(QiapSpacing.xs)) {
            Chip("No jumping")
            Chip("Needs floor")
        }
        QiapCard(tone = CardTone.Sky) {
            QiapText(Categories[tab], style = QiapTheme.type.h3)
            QiapText("Exercise tiles arrive with the exercise catalog.", color = QiapTheme.colors.ink2)
        }
    }
}
