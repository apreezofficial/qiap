package app.qiap.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
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
import app.qiap.core.designsystem.icon.QiapIcons
import app.qiap.core.designsystem.theme.QiapSpacing
import app.qiap.core.designsystem.theme.QiapTheme
import app.qiap.core.designsystem.theme.QiapThemeVariant
import app.qiap.feature.editor.EditorScreen
import app.qiap.feature.gallery.DesignGalleryScreen
import app.qiap.feature.history.HistoryScreen
import app.qiap.feature.home.HomeScreen
import app.qiap.feature.library.LibraryScreen
import app.qiap.feature.ringing.RingingScreen
import app.qiap.feature.settings.SettingsScreen
import app.qiap.feature.workout.WorkoutScreen
import kotlinx.serialization.Serializable

@Serializable data object HomeRoute : NavKey
@Serializable data object EditorRoute : NavKey
@Serializable data object LibraryRoute : NavKey
@Serializable data object RingingRoute : NavKey
@Serializable data object WorkoutRoute : NavKey
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

    Box(Modifier.fillMaxSize()) {
        NavDisplay(
            backStack = backStack,
            onBack = { if (backStack.size > 1) backStack.removeLastOrNull() },
            entryProvider = entryProvider {
                entry<HomeRoute> {
                    QiapTheme {
                        HomeScreen(
                            onNewAlarm = { backStack.add(EditorRoute) },
                            onPreviewRinging = { backStack.add(RingingRoute) },
                            onOpenSettings = { backStack.add(SettingsRoute) },
                        )
                    }
                }
                entry<EditorRoute> { QiapTheme { EditorScreen(onBack = backStack::pop) } }
                entry<LibraryRoute> { QiapTheme { LibraryScreen() } }
                entry<HistoryRoute> { QiapTheme { HistoryScreen() } }
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
                        // Workout replaces Ringing: there is no way back to a silenced alarm.
                        RingingScreen(onStartWorkout = { backStack.replaceTop(WorkoutRoute) })
                    }
                }
                entry<WorkoutRoute> {
                    QiapTheme(QiapThemeVariant.Night) { WorkoutScreen(onFinish = backStack::pop) }
                }
                // Constant-false in release, so R8 drops the gallery entirely.
                if (BuildConfig.DEBUG) {
                    entry<GalleryRoute> { QiapTheme { DesignGalleryScreen(onBack = backStack::pop) } }
                }
            },
        )

        val top = backStack.lastOrNull()
        val tabIndex = TopLevelRoutes.indexOf(top)
        if (tabIndex >= 0) {
            QiapTheme {
                FloatingNavPill(
                    items = NavItems,
                    selectedIndex = tabIndex,
                    onSelect = { i -> backStack.switchTab(TopLevelRoutes[i]) },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(bottom = QiapSpacing.md),
                )
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
    if (lastOrNull() == route) return
    clear()
    add(route)
}
