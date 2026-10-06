package app.qiap.feature.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import app.qiap.core.designsystem.component.CardTone
import app.qiap.core.designsystem.component.Chip
import app.qiap.core.designsystem.component.ExerciseTile
import app.qiap.core.designsystem.component.ExerciseTileStyle
import app.qiap.core.designsystem.component.IconTile
import app.qiap.core.designsystem.component.PillButton
import app.qiap.core.designsystem.component.QiapCard
import app.qiap.core.designsystem.component.QiapScreen
import app.qiap.core.designsystem.component.QiapSheet
import app.qiap.core.designsystem.component.QiapText
import app.qiap.core.designsystem.component.SearchPill
import app.qiap.core.designsystem.component.SectionBadge
import app.qiap.core.designsystem.component.SegmentedTabs
import app.qiap.core.designsystem.component.TwoToneHeadline
import app.qiap.core.designsystem.component.skyWash
import app.qiap.core.designsystem.icon.QiapIcons
import app.qiap.core.designsystem.pictogram.Pictogram
import app.qiap.core.designsystem.theme.QiapRadius
import app.qiap.core.designsystem.theme.QiapSpacing
import app.qiap.core.designsystem.theme.QiapTheme
import app.qiap.feature.home.NavClearance
import app.qiap.feature.sample.SampleData
import app.qiap.feature.sample.SampleExercise

/** Repeating bento rhythm: one wide tile, then pairs that alternate sky / muted / dark. */
private val TileStyles = listOf(
    ExerciseTileStyle.Sky, ExerciseTileStyle.Muted, ExerciseTileStyle.Dark,
    ExerciseTileStyle.Sky, ExerciseTileStyle.Muted, ExerciseTileStyle.Dark,
)

/**
 * Exercise library (design.md §7): search, category tabs, filter chips, bento of ExerciseTiles,
 * detail sheet with a demo loop and "Try it now". [onOverlayChange] lets the shell hide the nav
 * pill while the sheet is up.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LibraryScreen(onTryIt: () -> Unit, onOverlayChange: (Boolean) -> Unit) {
    var category by rememberSaveable { mutableIntStateOf(0) }
    var noJumping by rememberSaveable { mutableStateOf(false) }
    var noFloor by rememberSaveable { mutableStateOf(false) }
    val favorites = remember { mutableStateListOf(SampleData.squat.name) }
    var open by remember { mutableStateOf<SampleExercise?>(null) }
    // Keep the last exercise around so the sheet has content while it slides out. Not state:
    // it only changes together with `open`, which already triggers recomposition.
    val lastOpened = remember { arrayOfNulls<SampleExercise>(1) }
    open?.let { lastOpened[0] = it }
    LaunchedEffect(open) { onOverlayChange(open != null) }

    val list = SampleData.exercises.filter {
        (category == 0 || it.category == SampleData.categories[category]) &&
            !(noJumping && it.jumping) && !(noFloor && it.needsFloor)
    }

    Box(Modifier.fillMaxSize()) {
        QiapScreen(bottomClearance = NavClearance) {
            SectionBadge("52 exercises", Modifier.padding(top = QiapSpacing.xs))
            TwoToneHeadline("Pick your", "wake-up weapon.")
            SearchPill("Search squats, planks…")
            SegmentedTabs(SampleData.categories, category, { category = it })
            Row(horizontalArrangement = Arrangement.spacedBy(QiapSpacing.xs)) {
                Chip("No jumping", selected = noJumping, onClick = { noJumping = !noJumping })
                Chip("No floor needed", selected = noFloor, onClick = { noFloor = !noFloor })
            }

            if (list.isEmpty()) {
                QiapCard(tone = CardTone.Muted, modifier = Modifier.fillMaxWidth()) {
                    QiapText("Nothing fits those filters.", style = QiapTheme.type.title)
                    QiapText("Your neighbours thank you.", style = QiapTheme.type.bodySmall, color = QiapTheme.colors.ink2)
                }
            } else {
                val toggleFav: (SampleExercise) -> Unit = { ex ->
                    if (!favorites.remove(ex.name)) favorites.add(ex.name)
                }
                Tile(list.first(), 0, big = true, favorites, toggleFav) { open = it }
                list.drop(1).chunked(2).forEachIndexed { row, pair ->
                    Row(horizontalArrangement = Arrangement.spacedBy(QiapSpacing.sm)) {
                        pair.forEachIndexed { col, ex ->
                            val i = 1 + row * 2 + col
                            Tile(ex, i, big = false, favorites, toggleFav, Modifier.weight(1f)) { open = it }
                        }
                        if (pair.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }

        QiapSheet(visible = open != null, onDismiss = { open = null }) {
            lastOpened[0]?.let { ex -> ExerciseDetail(ex, onClose = { open = null }, onTryIt = { open = null; onTryIt() }) }
        }
    }
}

@Composable
private fun Tile(
    ex: SampleExercise,
    index: Int,
    big: Boolean,
    favorites: List<String>,
    onFavorite: (SampleExercise) -> Unit,
    modifier: Modifier = Modifier,
    onOpen: (SampleExercise) -> Unit,
) {
    ExerciseTile(
        name = ex.name,
        category = ex.category,
        difficulty = ex.difficulty,
        motion = ex.motion,
        favorite = ex.name in favorites,
        onFavorite = { onFavorite(ex) },
        onClick = { onOpen(ex) },
        modifier = modifier.fillMaxWidth(),
        style = if (big) ExerciseTileStyle.Sky else TileStyles[(index - 1) % TileStyles.size],
        big = big,
        phase = index * 0.21f,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExerciseDetail(ex: SampleExercise, onClose: () -> Unit, onTryIt: () -> Unit) {
    val colors = QiapTheme.colors
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column {
            QiapText(ex.name, style = QiapTheme.type.h3)
            QiapText(
                "${ex.category} · ${listOf("Easy", "Medium", "Hard")[ex.difficulty - 1]} · ${if (ex.jumping) "Jumping" else "Quiet"}",
                style = QiapTheme.type.label,
                color = colors.ink2,
            )
        }
        IconTile(QiapIcons.Close, "Close", size = 40.dp, onClick = onClose)
    }
    Box(
        Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(QiapRadius.cardLarge)
            .background(colors.bgWash)
            .skyWash(),
        contentAlignment = Alignment.Center,
    ) {
        // Ghost runs half a cycle behind: shows the range of motion at a glance.
        Pictogram(ex.motion, Modifier.size(120.dp).alpha(0.18f), phase = 0.5f)
        Pictogram(ex.motion, Modifier.size(180.dp))
    }
    FlowRow(horizontalArrangement = Arrangement.spacedBy(QiapSpacing.xs), verticalArrangement = Arrangement.spacedBy(QiapSpacing.xs)) {
        Chip("Phone at hip height, 2 m away", leadingIcon = QiapIcons.Camera)
        Chip("~1 min", leadingIcon = QiapIcons.Timer)
    }
    PillButton("Try it now", onClick = onTryIt, leadingIcon = QiapIcons.Play, modifier = Modifier.fillMaxWidth())
}
