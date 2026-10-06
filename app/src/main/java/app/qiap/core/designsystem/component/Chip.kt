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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import app.qiap.core.designsystem.theme.QiapColors
import app.qiap.core.designsystem.theme.QiapRadius
import app.qiap.core.designsystem.theme.QiapSize
import app.qiap.core.designsystem.theme.QiapSpacing
import app.qiap.core.designsystem.theme.QiapTheme
import app.qiap.core.designsystem.theme.qiapTween

enum class ChipTone {
    /** White + hairline. Default. */
    Surface,
    /** Muted grey, no hairline (exercise chips on cards). */
    Muted,
    /** Saffron tint: streaks, "Fix", highlight chips. */
    Saffron,
    /** Jade tint: success / all-good summaries. */
    Jade,
}

/** Tinted chip fills, shared with anything else that wants a soft saffron/jade wash. */
fun QiapColors.saffronTint(): Color = lerp(surface, saffron, 0.24f)
fun QiapColors.jadeTint(): Color = lerp(surface, jade, 0.18f)

/**
 * Small pill: categories, feature tags, day picker. [selected] fills it with ink (same language
 * as the active billing toggle in design.md §6). [leading] takes any small composable, e.g. a
 * [app.qiap.core.designsystem.pictogram.Pictogram]; [leadingIcon] is the common icon case.
 */
@Composable
fun Chip(
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    tone: ChipTone = ChipTone.Surface,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    leading: (@Composable () -> Unit)? = null,
) {
    val colors = QiapTheme.colors
    val shape = QiapRadius.pill
    val idle = when (tone) {
        ChipTone.Surface -> colors.surface
        ChipTone.Muted -> colors.surfaceMuted
        ChipTone.Saffron -> colors.saffronTint()
        ChipTone.Jade -> colors.jadeTint()
    }
    val fill by animateColorAsState(if (selected) colors.ink else idle, qiapTween(), label = "chipFill")
    val content by animateColorAsState(if (selected) colors.onInk else colors.ink, qiapTween(), label = "chipText")
    val hairline = if (tone == ChipTone.Surface && !selected) colors.border else Color.Transparent

    CompositionLocalProvider(LocalContentColor provides content) {
        Row(
            modifier = modifier
                .clip(shape)
                .background(fill, shape)
                .border(QiapSize.hairline, hairline, shape)
                .let { if (onClick != null) it.clickable(role = Role.Button, onClick = onClick) else it }
                .padding(horizontal = QiapSpacing.sm, vertical = QiapSpacing.xs),
            horizontalArrangement = Arrangement.spacedBy(QiapSpacing.xxs, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (leadingIcon != null) QiapIcon(leadingIcon, contentDescription = null, size = 16.dp)
            leading?.invoke()
            QiapText(label, style = QiapTheme.type.label, maxLines = 1)
        }
    }
}
