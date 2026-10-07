package app.qiap.feature.library

import android.content.Context
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.qiap.core.designsystem.component.CardTone
import app.qiap.core.designsystem.component.Chip
import app.qiap.core.designsystem.component.ChipTone
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
import app.qiap.exercise.Category
import app.qiap.exercise.ExerciseCatalog
import app.qiap.exercise.ExerciseSpec
import app.qiap.feature.home.NavClearance
import app.qiap.feature.pictogramFor

/** Repeating bento rhythm: one wide tile, then pairs that alternate sky / muted / dark. */
private val TileStyles = listOf(
    ExerciseTileStyle.Sky, ExerciseTileStyle.Muted, ExerciseTileStyle.Dark,
    ExerciseTileStyle.Sky, ExerciseTileStyle.Muted, ExerciseTileStyle.Dark,
)

private val Tabs = listOf("All") + Category.entries.map { it.label }

/** Only this many tiles animate at once; the rest show a still frame so 52 tiles stay smooth. */
private const val ANIMATED_TILES = 10

/**
 * Exercise library (design.md §7): search, category tabs, filter chips, bento of ExerciseTiles,
 * detail sheet with a demo loop and "Try it now". [onOverlayChange] lets the shell hide the nav
 * pill while the sheet is up. [onTryIt] gets the exercise id to practise.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LibraryScreen(onTryIt: (String) -> Unit, onOverlayChange: (Boolean) -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("library", Context.MODE_PRIVATE) }
    var category by rememberSaveable { mutableIntStateOf(0) }
    var query by rememberSaveable { mutableStateOf("") }
    var noJumping by rememberSaveable { mutableStateOf(false) }
    var noFloor by rememberSaveable { mutableStateOf(false) }
    var onlyFavorites by rememberSaveable { mutableStateOf(false) }
    val favorites = remember { mutableStateListOf<String>().apply { addAll(prefs.getStringSet("favorites", emptySet()).orEmpty()) } }
    var open by remember { mutableStateOf<ExerciseSpec?>(null) }
    // Keep the last exercise around so the sheet has content while it slides out. Not state:
    // it only changes together with `open`, which already triggers recomposition.
    val lastOpened = remember { arrayOfNulls<ExerciseSpec>(1) }
    open?.let { lastOpened[0] = it }
    LaunchedEffect(open) { onOverlayChange(open != null) }

    val list = ExerciseCatalog.all.filter {
        (category == 0 || it.category.label == Tabs[category]) &&
            (query.isBlank() || it.name.contains(query.trim(), ignoreCase = true)) &&
            !(noJumping && it.jumping) && !(noFloor && it.needsFloor) &&
            !(onlyFavorites && it.id !in favorites)
    }

    Box(Modifier.fillMaxSize()) {
        QiapScreen(bottomClearance = NavClearance) {
            SectionBadge("${ExerciseCatalog.all.size} exercises", Modifier.padding(top = QiapSpacing.xs))
            TwoToneHeadline("Pick your", "wake-up weapon.")
            SearchPill("Search squats, planks…", onQueryChange = { query = it })
            SegmentedTabs(Tabs, category, { category = it })
            Row(horizontalArrangement = Arrangement.spacedBy(QiapSpacing.xs)) {
                Chip("No jumping", selected = noJumping, onClick = { noJumping = !noJumping })
                Chip("No floor needed", selected = noFloor, onClick = { noFloor = !noFloor })
                Chip("Favorites", selected = onlyFavorites, leadingIcon = QiapIcons.Star, onClick = { onlyFavorites = !onlyFavorites })
            }

            if (list.isEmpty()) {
                QiapCard(tone = CardTone.Muted, modifier = Modifier.fillMaxWidth()) {
                    QiapText("Nothing fits those filters.", style = QiapTheme.type.title)
                    QiapText("Your neighbours thank you.", style = QiapTheme.type.bodySmall, color = QiapTheme.colors.ink2)
                }
            } else {
                val toggleFav: (ExerciseSpec) -> Unit = { ex ->
                    if (!favorites.remove(ex.id)) favorites.add(ex.id)
                    prefs.edit().putStringSet("favorites", favorites.toSet()).apply()
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
            lastOpened[0]?.let { ex -> ExerciseDetail(ex, onClose = { open = null }, onTryIt = { open = null; onTryIt(ex.id) }) }
        }
    }
}

@Composable
private fun Tile(
    ex: ExerciseSpec,
    index: Int,
    big: Boolean,
    favorites: List<String>,
    onFavorite: (ExerciseSpec) -> Unit,
    modifier: Modifier = Modifier,
    onOpen: (ExerciseSpec) -> Unit,
) {
    ExerciseTile(
        name = ex.name,
        category = ex.category.label,
        difficulty = ex.difficulty,
        motion = pictogramFor(ex.id),
        favorite = ex.id in favorites,
        onFavorite = { onFavorite(ex) },
        onClick = { onOpen(ex) },
        modifier = modifier.fillMaxWidth(),
        style = if (big) ExerciseTileStyle.Sky else TileStyles[(index - 1) % TileStyles.size],
        big = big,
        phase = index * 0.21f,
        animate = index < ANIMATED_TILES,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExerciseDetail(ex: ExerciseSpec, onClose: () -> Unit, onTryIt: () -> Unit) {
    val colors = QiapTheme.colors
    val motion = pictogramFor(ex.id)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            QiapText(ex.name, style = QiapTheme.type.h3)
            QiapText(
                "${ex.category.label} · ${listOf("Easy", "Medium", "Hard")[ex.difficulty - 1]} · ${if (ex.jumping) "Jumping" else "Quiet"}",
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
        Pictogram(motion, Modifier.size(120.dp).alpha(0.18f), phase = 0.5f)
        Pictogram(motion, Modifier.size(180.dp))
    }
    if (ex.tip.isNotBlank()) QiapText(ex.tip, style = QiapTheme.type.body, color = colors.ink2)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(QiapSpacing.xs), verticalArrangement = Arrangement.spacedBy(QiapSpacing.xs)) {
        Chip(ex.view.hint, leadingIcon = QiapIcons.Camera)
        Chip("Aim for ${ex.defaultTarget} ${ex.unit}", leadingIcon = QiapIcons.Timer)
        if (ex.needsFloor) Chip("Needs floor space", tone = ChipTone.Muted)
        if (ex.provisional) Chip("Beta: counting still being tuned", tone = ChipTone.Saffron)
    }
    PillButton("Try it now", onClick = onTryIt, leadingIcon = QiapIcons.Play, modifier = Modifier.fillMaxWidth())
}
