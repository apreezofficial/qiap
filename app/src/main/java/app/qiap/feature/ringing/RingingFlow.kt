package app.qiap.feature.ringing

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.qiap.QiapApp
import app.qiap.alarm.AlarmService
import app.qiap.alarm.HistoryStats
import app.qiap.alarm.Outcome
import app.qiap.alarm.RingRequest
import app.qiap.core.designsystem.theme.QiapTheme
import app.qiap.core.designsystem.theme.QiapThemeVariant
import app.qiap.exercise.ExerciseCatalog
import app.qiap.feature.success.SuccessScreen
import app.qiap.feature.workout.WorkoutScreen
import java.time.LocalDate

private enum class Step { Ringing, Workout, Success }

/**
 * The real alarm flow inside [app.qiap.alarm.RingingActivity]: Ringing → Workout → Success.
 * Back does nothing until the alarm is done: there is no way out except the reps or the
 * deliberate hold-to-skip fallback.
 */
@Composable
fun RingingFlow(onClose: () -> Unit) {
    val context = LocalContext.current
    val container = remember { (context.applicationContext as QiapApp).container }
    val ring by AlarmService.state.collectAsStateWithLifecycle()
    var step by rememberSaveable { mutableStateOf(Step.Ringing) }
    var reps by rememberSaveable { mutableIntStateOf(0) }
    var seconds by rememberSaveable { mutableIntStateOf(0) }

    // The service clears its state when the alarm ends; keep the request for the success screen.
    val holder = remember { arrayOfNulls<RingRequest>(1) }
    ring?.let { holder[0] = it.request }
    val request = holder[0]

    if (request == null || (ring == null && step != Step.Success)) {
        // Opened with nothing ringing (stale notification, alarm already ended): just leave.
        LaunchedEffect(Unit) { onClose() }
        return
    }
    BackHandler(enabled = step != Step.Success) { /* the alarm isn't done */ }

    val spec = ExerciseCatalog.byId(request.exerciseId) ?: ExerciseCatalog.Squat
    when (step) {
        Step.Ringing -> QiapTheme(QiapThemeVariant.Ringing) {
            RingingScreen(
                hour = request.hour,
                minute = request.minute,
                label = request.label,
                exerciseUnit = spec.unit,
                target = request.target,
                exerciseId = spec.id,
                onStartWorkout = {
                    AlarmService.workoutStarted(context)
                    step = Step.Workout
                },
                onFallback = {
                    AlarmService.finish(context, Outcome.FALLBACK, reps = 0, seconds = 0)
                    onClose()
                },
            )
        }
        Step.Workout -> QiapTheme(QiapThemeVariant.Night) {
            WorkoutScreen(exercise = spec, target = request.target, onComplete = { r, s ->
                reps = r
                seconds = s
                AlarmService.finish(context, Outcome.EARNED, r, s)
                step = Step.Success
            })
        }
        Step.Success -> QiapTheme {
            val entries by container.historyStore.entries.collectAsStateWithLifecycle()
            val stats = remember(entries) { HistoryStats.from(entries, LocalDate.now()) }
            SuccessScreen(reps = reps, seconds = seconds, streak = stats.streak, best = stats.bestStreak, onDone = onClose)
        }
    }
}
