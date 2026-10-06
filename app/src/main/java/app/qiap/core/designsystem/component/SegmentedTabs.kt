package app.qiap.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import app.qiap.core.designsystem.theme.QiapRadius
import app.qiap.core.designsystem.theme.QiapSpacing
import app.qiap.core.designsystem.theme.QiapTheme
import app.qiap.core.designsystem.theme.qiapTween

/**
 * Pill container with a sliding sky indicator (200 ms). With [fillWidth] the segments share the
 * width equally; otherwise each hugs its label and the row scrolls if it overflows (360dp phones).
 */
@Composable
fun SegmentedTabs(
    items: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    fillWidth: Boolean = false,
) {
    val colors = QiapTheme.colors
    // Left edge and width of each segment in px, filled on placement.
    val lefts = remember(items.size) { mutableStateListOf(*Array(items.size) { 0f }) }
    val widths = remember(items.size) { mutableStateListOf(*Array(items.size) { 0f }) }
    val index = selectedIndex.coerceIn(0, items.lastIndex)
    val left by animateFloatAsState(lefts[index], qiapTween(), label = "tabLeft")
    val width by animateFloatAsState(widths[index], qiapTween(), label = "tabWidth")
    val pad = QiapSpacing.xxs

    val container = modifier
        .clip(QiapRadius.pill)
        .background(colors.surfaceMuted, QiapRadius.pill)
        .let { if (fillWidth) it else it.horizontalScroll(rememberScrollState()) }
        .padding(pad)
        .drawBehind {
            if (width > 0f) {
                drawRoundRect(
                    color = colors.sky,
                    topLeft = Offset(left, 0f),
                    size = Size(width, size.height),
                    cornerRadius = CornerRadius(size.height / 2f),
                )
            }
        }

    Row(container, verticalAlignment = Alignment.CenterVertically) {
        items.forEachIndexed { i, label ->
            val textColor by animateColorAsState(
                if (i == index) colors.onAccent else colors.ink2, qiapTween(), label = "tabText",
            )
            Box(
                modifier = Modifier
                    .let { if (fillWidth) it.weight(1f) else it }
                    .onPlaced {
                        lefts[i] = it.positionInParent().x
                        widths[i] = it.size.width.toFloat()
                    }
                    .clip(QiapRadius.pill)
                    .clickable(role = Role.Tab) { onSelect(i) }
                    .padding(horizontal = QiapSpacing.md, vertical = QiapSpacing.xs),
                contentAlignment = Alignment.Center,
            ) {
                QiapText(
                    label,
                    style = QiapTheme.type.label,
                    color = textColor,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = if (fillWidth) Modifier.fillMaxWidth() else Modifier,
                )
            }
        }
    }
}
