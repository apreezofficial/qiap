package app.qiap.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import app.qiap.core.designsystem.theme.QiapRadius
import app.qiap.core.designsystem.theme.QiapShadow
import app.qiap.core.designsystem.theme.QiapSize
import app.qiap.core.designsystem.theme.QiapSpacing
import app.qiap.core.designsystem.theme.QiapTheme
import app.qiap.core.designsystem.theme.qiapShadow
import app.qiap.core.designsystem.theme.qiapTween

@Immutable
data class NavPillItem(val icon: ImageVector, val label: String)

/** Floating bottom nav (design.md §7): white pill, icons only, active one sits in an ink circle. */
@Composable
fun FloatingNavPill(
    items: List<NavPillItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = QiapTheme.colors
    val shape = QiapRadius.pill
    Row(
        modifier = modifier
            .qiapShadow(QiapShadow.soft, shape)
            .background(colors.surface, shape)
            .border(QiapSize.hairline, colors.border, shape)
            .padding(QiapSpacing.xs),
        horizontalArrangement = Arrangement.spacedBy(QiapSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items.forEachIndexed { i, item ->
            val active = i == selectedIndex
            val fill by animateColorAsState(
                if (active) colors.ink else colors.surface, qiapTween(), label = "navFill",
            )
            val tint by animateColorAsState(
                if (active) colors.onInk else colors.ink2, qiapTween(), label = "navTint",
            )
            Box(
                modifier = Modifier
                    .size(QiapSize.navItem)
                    .clip(QiapRadius.pill)
                    .background(fill)
                    .selectable(selected = active, role = Role.Tab) { onSelect(i) },
                contentAlignment = Alignment.Center,
            ) {
                QiapIcon(item.icon, contentDescription = item.label, tint = tint)
            }
        }
    }
}
