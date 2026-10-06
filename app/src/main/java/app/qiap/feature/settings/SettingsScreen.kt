package app.qiap.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.qiap.core.designsystem.component.CardSize
import app.qiap.core.designsystem.component.QiapCard
import app.qiap.core.designsystem.component.QiapIcon
import app.qiap.core.designsystem.component.QiapScreen
import app.qiap.core.designsystem.component.QiapText
import app.qiap.core.designsystem.icon.QiapIcons
import app.qiap.core.designsystem.theme.QiapTheme

/** Phase 0 placeholder. [onOpenGallery] is null in release builds. */
@Composable
fun SettingsScreen(onBack: () -> Unit, onOpenGallery: (() -> Unit)?) {
    QiapScreen(title = "Settings", onBack = onBack) {
        QiapCard(size = CardSize.Small) {
            QiapText("Permissions & onboarding", style = QiapTheme.type.h3)
            QiapText("Arrives with the alarm engine.", color = QiapTheme.colors.ink2)
        }
        if (onOpenGallery != null) {
            QiapCard(size = CardSize.Small, onClick = onOpenGallery) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    QiapText("Design gallery (debug)", style = QiapTheme.type.h3)
                    QiapIcon(QiapIcons.ChevronRight, contentDescription = null)
                }
            }
        }
    }
}
