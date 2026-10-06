package app.qiap.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import app.qiap.core.designsystem.icon.QiapIcons
import app.qiap.core.designsystem.theme.QiapRadius
import app.qiap.core.designsystem.theme.QiapShadow
import app.qiap.core.designsystem.theme.QiapSize
import app.qiap.core.designsystem.theme.QiapSpacing
import app.qiap.core.designsystem.theme.QiapTheme
import app.qiap.core.designsystem.theme.qiapShadow

enum class CardSize { Small, Large }

enum class CardTone {
    /** White card with hairline + soft shadow. Default. */
    Surface,
    /** Muted grey fill, no shadow (pricing header, newsletter). */
    Muted,
    /** Deeper sky (how-it-works, FAQ card). */
    Sky,
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
    }
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

/** 48dp square, radius 12, muted fill, centered icon. Store buttons and quick actions. */
@Composable
fun IconTile(
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val colors = QiapTheme.colors
    Box(
        modifier = modifier
            .size(QiapSize.iconTile)
            .clip(QiapRadius.tile)
            .background(colors.surfaceMuted)
            .let { if (onClick != null) it.clickable(role = Role.Button, onClick = onClick) else it },
        contentAlignment = Alignment.Center,
    ) {
        QiapIcon(icon, contentDescription, tint = colors.ink)
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
