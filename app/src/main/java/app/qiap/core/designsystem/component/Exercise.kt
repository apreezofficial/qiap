package app.qiap.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.qiap.core.designsystem.icon.QiapIcons
import app.qiap.core.designsystem.pictogram.Pictogram
import app.qiap.core.designsystem.pictogram.PictogramMotion
import app.qiap.core.designsystem.theme.QiapRadius
import app.qiap.core.designsystem.theme.QiapShadow
import app.qiap.core.designsystem.theme.QiapSpacing
import app.qiap.core.designsystem.theme.QiapTheme
import app.qiap.core.designsystem.theme.qiapShadow
import app.qiap.core.designsystem.theme.qiapTween
import kotlin.math.max

enum class ExerciseTileStyle {
    /** Sky wash rising from the bottom edge. */
    Sky,
    /** Flat muted grey. */
    Muted,
    /** Dark tile, light figure. Mixed in to give the bento rhythm. */
    Dark,
}

/** Three dots, [level] of them filled. */
@Composable
fun DifficultyDots(level: Int, modifier: Modifier = Modifier, color: Color = LocalContentColor.current) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        repeat(3) { i ->
            Box(Modifier.size(5.dp).background(color.copy(alpha = if (i < level) 1f else 0.25f), QiapRadius.pill))
        }
    }
}

/**
 * Bento tile for the exercise library (design.md §6 ExerciseTile): animated pictogram, name,
 * category + difficulty, favourite star. [big] spans the full row.
 */
@Composable
fun ExerciseTile(
    name: String,
    category: String,
    difficulty: Int,
    motion: PictogramMotion,
    favorite: Boolean,
    onFavorite: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: ExerciseTileStyle = ExerciseTileStyle.Sky,
    big: Boolean = false,
    phase: Float = 0f,
) {
    val colors = QiapTheme.colors
    val dark = style == ExerciseTileStyle.Dark
    // "Dark" stays dark in both themes, so it can't use ink (which is light at Night).
    val darkBg = if (colors.isNight) colors.surfaceMuted else colors.ink
    val darkContent = if (colors.isNight) colors.ink else colors.onInk
    val content = if (dark) darkContent else colors.ink
    val wash1 = colors.bgWash2
    val wash2 = colors.bgWash
    val muted = colors.surfaceMuted

    CompositionLocalProvider(LocalContentColor provides content) {
        Box(
            modifier
                .heightIn(min = if (big) 210.dp else 172.dp)
                .clip(QiapRadius.cardLarge)
                .drawWithCache {
                    val brush = when (style) {
                        ExerciseTileStyle.Sky -> Brush.radialGradient(
                            listOf(wash1, wash2), center = Offset(size.width / 2f, size.height),
                            radius = max(size.width, size.height),
                        )
                        ExerciseTileStyle.Muted -> Brush.linearGradient(listOf(muted, muted))
                        ExerciseTileStyle.Dark -> Brush.linearGradient(listOf(darkBg, darkBg))
                    }
                    onDrawBehind { drawRect(brush) }
                }
                .clickable(role = Role.Button, onClick = onClick)
                .padding(QiapSpacing.md),
        ) {
            Column(
                Modifier.fillMaxWidth(if (big) 0.6f else 1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                QiapText(
                    name,
                    style = QiapTheme.type.title.copy(fontSize = if (big) 22.sp else 16.sp, lineHeight = if (big) 24.sp else 19.sp),
                    modifier = Modifier.padding(end = if (big) 0.dp else 28.dp),
                )
                Row(
                    Modifier.alpha(0.7f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    QiapText(category, style = QiapTheme.type.caption)
                    DifficultyDots(difficulty)
                }
            }
            Pictogram(
                motion,
                Modifier.align(Alignment.BottomEnd).size(if (big) 150.dp else 104.dp),
                phase = phase,
            )
            FavoriteButton(favorite, onFavorite, dark, Modifier.align(Alignment.TopEnd).padding(start = 0.dp))
        }
    }
}

@Composable
private fun FavoriteButton(on: Boolean, onToggle: () -> Unit, dark: Boolean, modifier: Modifier) {
    val colors = QiapTheme.colors
    val star by animateColorAsState(if (on) colors.saffron else LocalContentColor.current, qiapTween(), label = "fav")
    Box(
        modifier
            .size(32.dp)
            .clip(QiapRadius.pill)
            .background(if (dark) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.7f))
            .clickable(role = Role.Checkbox, onClick = onToggle),
        contentAlignment = Alignment.Center,
    ) {
        QiapIcon(QiapIcons.Star, if (on) "Remove favourite" else "Add favourite", tint = if (dark || on) star else Color(0xFF111114), size = 16.dp)
    }
}

/** Compact exercise choice for the alarm editor's horizontal picker. */
@Composable
fun ExercisePickerItem(
    name: String,
    motion: PictogramMotion,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    phase: Float = 0f,
) {
    val colors = QiapTheme.colors
    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier
            .size(92.dp, 112.dp)
            .then(if (selected) Modifier.qiapShadow(QiapShadow.soft, shape) else Modifier)
            .clip(shape)
            .background(if (selected) colors.surface else colors.surfaceMuted)
            .border(2.dp, if (selected) colors.ink else Color.Transparent, shape)
            .clickable(role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.Bottom),
    ) {
        Pictogram(motion, Modifier.size(58.dp), phase = phase)
        QiapText(name, style = QiapTheme.type.caption.copy(lineHeight = 14.sp), textAlign = TextAlign.Center, maxLines = 2)
    }
}
