package app.qiap.core.designsystem.component

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.qiap.core.designsystem.icon.QiapIcons
import app.qiap.core.designsystem.theme.QiapRadius
import app.qiap.core.designsystem.theme.QiapShadow
import app.qiap.core.designsystem.theme.QiapSize
import app.qiap.core.designsystem.theme.QiapSpacing
import app.qiap.core.designsystem.theme.QiapTheme
import app.qiap.core.designsystem.theme.qiapShadow
import app.qiap.core.designsystem.theme.qiapTween

enum class CardSize { Small, Large }

enum class CardTone {
    /** White card with hairline + soft shadow. Default. */
    Surface,
    /** Muted grey fill, no shadow (stats, empty states). */
    Muted,
    /** Deeper sky (how-it-works, tips, FAQ). */
    Sky,
    /** Inverted ink fill for the one hero stat on a screen (History streak). */
    Ink,
}

/** The one card. Small = radius 16 / padding 16, Large = radius 24 / padding 24. */
@Composable
fun QiapCard(
    modifier: Modifier = Modifier,
    size: CardSize = CardSize.Large,
    tone: CardTone = CardTone.Surface,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(if (size == CardSize.Large) QiapSpacing.lg else QiapSpacing.md),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(QiapSpacing.xs),
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = QiapTheme.colors
    val shape = if (size == CardSize.Large) QiapRadius.cardLarge else QiapRadius.cardSmall
    val surface = when (tone) {
        CardTone.Surface -> Modifier
            .qiapShadow(QiapShadow.soft, shape)
            .background(colors.surface, shape)
            .border(QiapSize.hairline, colors.border, shape)
        CardTone.Muted -> Modifier.background(colors.surfaceMuted, shape)
        CardTone.Sky -> Modifier.background(colors.bgWash2, shape)
        CardTone.Ink -> Modifier.qiapShadow(QiapShadow.soft, shape).background(colors.ink, shape)
    }
    val contentColor = if (tone == CardTone.Ink) colors.onInk else colors.ink
    CompositionLocalProvider(LocalContentColor provides contentColor) {
        Column(
            modifier = modifier
                .then(surface)
                .clip(shape)
                .let { if (onClick != null) it.clickable(onClick = onClick) else it }
                .padding(contentPadding),
            verticalArrangement = verticalArrangement,
            content = content,
        )
    }
}

/** Square tile, radius 12, centered icon. 48dp by default (design.md §6); 40dp for in-row use. */
@Composable
fun IconTile(
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = QiapSize.iconTile,
    background: Color = QiapTheme.colors.surfaceMuted,
    tint: Color = QiapTheme.colors.ink,
    onClick: (() -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(QiapRadius.tile)
            .background(background)
            .let { if (onClick != null) it.clickable(role = Role.Button, onClick = onClick) else it },
        contentAlignment = Alignment.Center,
    ) {
        QiapIcon(icon, contentDescription, tint = tint)
    }
}

/** Small pill above a heading: sky star + label ("Our features"). */
@Composable
fun SectionBadge(label: String, modifier: Modifier = Modifier) {
    val colors = QiapTheme.colors
    CompositionLocalProvider(LocalContentColor provides colors.ink) {
        Row(
            modifier = modifier
                .background(colors.bgWash, QiapRadius.pill)
                .padding(horizontal = QiapSpacing.sm, vertical = QiapSpacing.xxs),
            horizontalArrangement = Arrangement.spacedBy(QiapSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            QiapIcon(QiapIcons.Star, contentDescription = null, tint = colors.sky, size = 14.dp)
            QiapText(label, style = QiapTheme.type.label)
        }
    }
}

/** Settings-style row: optional leading tile, title + subtitle, optional trailing control. */
@Composable
fun ListRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .let { if (onClick != null) it.clip(QiapRadius.cardSmall).clickable(onClick = onClick) else it }
            .padding(vertical = QiapSpacing.xs),
        horizontalArrangement = Arrangement.spacedBy(QiapSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leading?.invoke()
        Column(Modifier.weight(1f)) {
            QiapText(title, style = QiapTheme.type.title)
            if (subtitle != null) QiapText(subtitle, style = QiapTheme.type.caption, color = QiapTheme.colors.ink3)
        }
        trailing?.invoke(this)
    }
}

/** 1dp hairline between rows. */
@Composable
fun HairlineDivider(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().height(QiapSize.hairline).background(QiapTheme.colors.border))
}

/** Seven round day markers (M T W T F S S); active days filled ink. */
@Composable
fun DayDots(days: List<Boolean>, modifier: Modifier = Modifier) {
    val colors = QiapTheme.colors
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(QiapSpacing.xxs)) {
        DayLetters.forEachIndexed { i, letter ->
            val on = days.getOrElse(i) { false }
            Box(
                Modifier.size(22.dp).background(if (on) colors.ink else colors.surfaceMuted, QiapRadius.pill),
                contentAlignment = Alignment.Center,
            ) {
                QiapText(
                    letter,
                    style = QiapTheme.type.caption.copy(fontSize = 10.sp, lineHeight = 10.sp),
                    color = if (on) colors.onInk else colors.ink3,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

internal val DayLetters = listOf("M", "T", "W", "T", "F", "S", "S")

/** Onboarding progress: dots, the current one stretched into a short ink bar. */
@Composable
fun StepDots(count: Int, current: Int, modifier: Modifier = Modifier) {
    val colors = QiapTheme.colors
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(count) { i ->
            val w by animateDpAsState(if (i == current) 22.dp else 6.dp, qiapTween(), label = "stepDot")
            Box(Modifier.width(w).height(6.dp).background(if (i == current) colors.ink else colors.border, QiapRadius.pill))
        }
    }
}
