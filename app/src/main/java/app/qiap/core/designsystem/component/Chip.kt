package app.qiap.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import app.qiap.core.designsystem.theme.QiapRadius
import app.qiap.core.designsystem.theme.QiapSize
import app.qiap.core.designsystem.theme.QiapSpacing
import app.qiap.core.designsystem.theme.QiapTheme
import app.qiap.core.designsystem.theme.qiapTween

/**
 * Small pill: categories, feature tags, day picker. Plain = white + hairline.
 * [selected] fills it with ink (same language as the active billing toggle in design.md §6).
 */
@Composable
fun Chip(
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val colors = QiapTheme.colors
    val shape = QiapRadius.pill
    val fill by animateColorAsState(if (selected) colors.ink else colors.surface, qiapTween(), label = "chipFill")
    val content by animateColorAsState(if (selected) colors.onInk else colors.ink, qiapTween(), label = "chipText")
    val clickable = if (onClick != null) {
        Modifier.clickable(role = Role.Checkbox, onClick = onClick)
    } else {
        Modifier
    }

    CompositionLocalProvider(LocalContentColor provides content) {
        Row(
            modifier = modifier
                .clip(shape)
                .background(fill, shape)
                .border(QiapSize.hairline, if (selected) fill else colors.border, shape)
                .then(clickable)
                .padding(horizontal = QiapSpacing.sm, vertical = QiapSpacing.xs),
            horizontalArrangement = Arrangement.spacedBy(QiapSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (leadingIcon != null) QiapIcon(leadingIcon, contentDescription = null, size = 16.dp)
            QiapText(label, style = QiapTheme.type.label)
        }
    }
}
