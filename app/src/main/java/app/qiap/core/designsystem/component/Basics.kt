package app.qiap.core.designsystem.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import app.qiap.core.designsystem.icon.QiapIcons
import app.qiap.core.designsystem.theme.QiapSpacing
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import app.qiap.core.designsystem.theme.LocalWashEnabled
import app.qiap.core.designsystem.theme.QiapSize
import app.qiap.core.designsystem.theme.QiapTheme

/** All text in the app goes through here. [color] defaults to the surrounding content color. */
@Composable
fun QiapText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = QiapTheme.type.body,
    color: Color = LocalContentColor.current,
    textAlign: TextAlign = TextAlign.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
) {
    BasicText(
        text = text,
        modifier = modifier,
        style = style.merge(color = color, textAlign = textAlign),
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
    )
}

/** Two-tone heading (design.md §2): [lead] in ink, [accent] in ink2. */
@Composable
fun TwoToneHeadline(
    lead: String,
    accent: String,
    modifier: Modifier = Modifier,
    style: TextStyle = QiapTheme.type.h2,
    textAlign: TextAlign = TextAlign.Unspecified,
) {
    val colors = QiapTheme.colors
    BasicText(
        text = buildAnnotatedString {
            withStyle(SpanStyle(color = colors.ink)) { append(lead) }
            append(' ')
            withStyle(SpanStyle(color = colors.ink2)) { append(accent) }
        },
        modifier = modifier,
        style = style.merge(textAlign = textAlign),
    )
}

/** Tinted icon from [app.qiap.core.designsystem.icon.QiapIcons]. */
@Composable
fun QiapIcon(
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    size: Dp = QiapSize.icon,
) {
    Image(
        imageVector = icon,
        contentDescription = contentDescription,
        modifier = modifier.size(size),
        colorFilter = ColorFilter.tint(tint),
    )
}

/**
 * The single soft background effect (design.md §9): a pale sky arch rising from the bottom edge.
 * Brush is built once per size; turn it off globally with [LocalWashEnabled].
 */
@Composable
fun Modifier.skyWash(): Modifier {
    if (!LocalWashEnabled.current) return this
    val colors = QiapTheme.colors
    return drawWithCache {
        val brush = Brush.radialGradient(
            0f to colors.bgWash2,
            0.55f to colors.bgWash,
            1f to colors.bgWash.copy(alpha = 0f),
            center = Offset(size.width / 2f, size.height),
            radius = maxOf(size.width * 0.85f, 1f),
        )
        onDrawBehind { drawRect(brush) }
    }
}

/** Logo: sunrise glyph + lowercase "qiap" in Inter Medium, tight tracking (design.md §1). */
@Composable
fun QiapWordmark(modifier: Modifier = Modifier, color: Color = QiapTheme.colors.ink) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(QiapSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        QiapIcon(QiapIcons.SunriseGlyph, contentDescription = null, tint = color, size = 24.dp)
        QiapText(
            "qiap",
            style = QiapTheme.type.h3.copy(fontWeight = FontWeight.Medium, letterSpacing = (-0.04).em),
            color = color,
        )
    }
}
