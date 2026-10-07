package app.qiap.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import app.qiap.BuildConfig
import app.qiap.core.designsystem.component.FloatingNavPill
import app.qiap.core.designsystem.component.NavPillItem
import app.qiap.core.designsystem.component.PillButton
import app.qiap.core.designsystem.icon.QiapIcons
import app.qiap.core.designsystem.theme.QiapSpacing
import app.qiap.core.designsystem.theme.QiapTheme
import app.qiap.core.designsystem.theme.QiapThemeVariant
import app.qiap.core.designsystem.theme.qiapTween
import app.qiap.exercise.ExerciseCatalog
import app.qiap.feature.editor.EditorScreen
import app.qiap.feature.gallery.DesignGalleryScreen
import app.qiap.feature.history.HistoryScreen
import app.qiap.feature.home.HomeScreen
import app.qiap.feature.library.LibraryScreen
import app.qiap.feature.ringing.RingingScreen
import app.qiap.feature.settings.SettingsScreen
import app.qiap.feature.success.SuccessScreen
import app.qiap.feature.workout.WorkoutScreen
import kotlinx.serialization.Serializable

@Serializable data object HomeRoute : NavKey
/** [alarmId] null = new alarm. */
@Serializable data class EditorRoute(val alarmId: Int? = null) : NavKey
@Serializable data object LibraryRoute : NavKey
@Serializable data object RingingRoute : NavKey
/** [exerciseId] is the exercise to practise ("Try it now" in the library, or the ringing preview). */
@Serializable data class WorkoutRoute(val exerciseId: String = "squat") : NavKey
@Serializable data object SuccessRoute : NavKey
@Serializable data object HistoryRoute : NavKey
@Serializable data object SettingsRoute : NavKey
@Serializable data object GalleryRoute : NavKey

/** Screens that show the floating nav pill, in pill order. */
internal val TopLevelRoutes: List<NavKey> = listOf(HomeRoute, LibraryRoute, HistoryRoute)

private val NavItems = listOf(
    NavPillItem(QiapIcons.Alarm, "Alarms"),
    NavPillItem(QiapIcons.Library, "Library"),
    NavPillItem(QiapIcons.History, "History"),
)

@Composable
fun QiapNavHost() {
    val backStack = rememberNavBackStack(HomeRoute)
    // A screen overlay (exercise sheet) hides the nav so it never floats over the sheet.
    var overlayOpen by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        NavDisplay(
            backStack = backStack,
            onBack = { if (backStack.size > 1) backStack.removeLastOrNull() },
            entryProvider = entryProvider {
                entry<HomeRoute> {
                    QiapTheme {
                        HomeScreen(
                            onEditAlarm = { id -> backStack.add(EditorRoute(id)) },
                            onPreviewRinging = { backStack.add(RingingRoute) },
                            onOpenSettings = { backStack.add(SettingsRoute) },
                        )
                    }
                }
                entry<EditorRoute> { route ->
                    QiapTheme {
                        EditorScreen(route.alarmId, onDone = backStack::pop, onSeeAllExercises = { backStack.switchTab(LibraryRoute) })
                    }
                }
                entry<LibraryRoute> {
                    QiapTheme {
                        LibraryScreen(onTryIt = { id -> backStack.add(WorkoutRoute(id)) }, onOverlayChange = { overlayOpen = it })
                    }
                }
                entry<HistoryRoute> { QiapTheme { HistoryScreen(onOverlayChange = { overlayOpen = it }) } }
                entry<SettingsRoute> {
                    QiapTheme {
                        SettingsScreen(
                            onBack = backStack::pop,
                            onOpenGallery = if (BuildConfig.DEBUG) ({ backStack.add(GalleryRoute) }) else null,
                        )
                    }
                }
                entry<RingingRoute> {
                    QiapTheme(QiapThemeVariant.Ringing) {
                        // In-app preview only (no sound, nothing logged). Real alarms ring in RingingActivity.
                        RingingScreen(
                            hour = 6, minute = 30, label = "Preview", exerciseId = "squat", exerciseUnit = "squats", target = 12,
                            onStartWorkout = { backStack.replaceTop(WorkoutRoute()) },
                            onFallback = backStack::pop,
                        )
                    }
                }
                entry<WorkoutRoute> { route ->
                    val spec = ExerciseCatalog.byId(route.exerciseId) ?: ExerciseCatalog.Squat
                    QiapTheme(QiapThemeVariant.Night) {
                        WorkoutScreen(
                            exercise = spec,
                            target = spec.defaultTarget,
                            onComplete = { _, _ -> backStack.replaceTop(SuccessRoute) },
                        )
                    }
                }
                entry<SuccessRoute> {
                    QiapTheme { SuccessScreen(reps = 12, seconds = 48, streak = 1, best = 1, onDone = { backStack.switchTab(HomeRoute) }) }
                }
                // Constant-false in release, so R8 drops the gallery entirely.
                if (BuildConfig.DEBUG) {
                    entry<GalleryRoute> { QiapTheme { DesignGalleryScreen(onBack = backStack::pop) } }
                }
            },
        )

        val top = backStack.lastOrNull()
        val tabIndex = TopLevelRoutes.indexOf(top)
        AnimatedVisibility(
            visible = tabIndex >= 0 && !(overlayOpen && (top == LibraryRoute || top == HistoryRoute)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(bottom = QiapSpacing.md),
            enter = fadeIn(qiapTween()) + slideInVertically(qiapTween()) { it / 2 },
            exit = fadeOut(qiapTween()) + slideOutVertically(qiapTween()) { it / 2 },
        ) {
            QiapTheme {
                Row(horizontalArrangement = Arrangement.spacedBy(QiapSpacing.xs), verticalAlignment = Alignment.CenterVertically) {
                    FloatingNavPill(
                        items = NavItems,
                        selectedIndex = tabIndex.coerceAtLeast(0),
                        onSelect = { i -> backStack.switchTab(TopLevelRoutes[i]) },
                    )
                    if (top == HomeRoute) {
                        PillButton("New", onClick = { backStack.add(EditorRoute()) }, leadingIcon = QiapIcons.Plus)
                    }
                }
            }
        }
    }
}

private fun NavBackStack<NavKey>.pop() {
    if (size > 1) removeAt(lastIndex)
}

private fun NavBackStack<NavKey>.replaceTop(route: NavKey) {
    set(lastIndex, route)
}

/** Tabs don't stack: switching resets to just that tab. */
private fun NavBackStack<NavKey>.switchTab(route: NavKey) {
    if (size == 1 && lastOrNull() == route) return
    clear()
    add(route)
}
