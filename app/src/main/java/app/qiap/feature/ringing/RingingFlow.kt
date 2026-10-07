package app.qiap.feature.ringing

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.qiap.QiapApp
import app.qiap.alarm.AlarmScheduler
import app.qiap.alarm.AlarmService
import app.qiap.alarm.HistoryEntry
import app.qiap.alarm.HistoryStats
import app.qiap.alarm.Outcome
import app.qiap.alarm.RingRequest
import app.qiap.alarm.RingRules
import app.qiap.core.designsystem.theme.QiapTheme
import app.qiap.core.designsystem.theme.QiapThemeVariant
import app.qiap.exercise.ExerciseCatalog
import app.qiap.exercise.ExerciseSpec
import app.qiap.feature.success.SuccessScreen
import app.qiap.feature.workout.WorkoutScreen
import java.time.LocalDate
import java.time.LocalTime

private enum class Step { Ringing, Workout, MiniSet, Fallback, Success }

/** The moves this ring asks for, in order: a routine, or just the one exercise. */
private fun movesFor(request: RingRequest): List<Pair<ExerciseSpec, Int>> {
    val routine = request.routine.mapNotNull { id -> ExerciseCatalog.byId(id)?.let { it to it.defaultTarget } }
    return routine.ifEmpty { listOf((ExerciseCatalog.byId(request.exerciseId) ?: ExerciseCatalog.Squat) to request.target) }
}

/**
 * The real alarm flow inside [app.qiap.alarm.RingingActivity]: Ringing → Workout (one or more
 * moves) → Success. Back does nothing until the alarm is done: there is no way out except the
 * reps, an earned snooze, or the emergency exit (a short sums challenge).
 */
@Composable
fun RingingFlow(onClose: () -> Unit) {
    val context = LocalContext.current
    val container = remember { (context.applicationContext as QiapApp).container }
    val ring by AlarmService.state.collectAsStateWithLifecycle()
    var step by rememberSaveable { mutableStateOf(Step.Ringing) }
    var moveIndex by rememberSaveable { mutableIntStateOf(0) }
    var reps by rememberSaveable { mutableIntStateOf(0) }
    var seconds by rememberSaveable { mutableIntStateOf(0) }
    var fallbackReason by rememberSaveable { mutableStateOf("") }
    var fromWorkout by rememberSaveable { mutableStateOf(false) }

    // Load the pose model while it rings so "Start workout" opens the camera instantly.
    DisposableEffect(Unit) {
        container.prewarmPoseEngine()
        onDispose { container.discardWarmPoseEngine() }
    }

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

    val moves = remember(request) { movesFor(request) }
    val (spec, target) = moves[moveIndex.coerceIn(0, moves.lastIndex)]

    when (step) {
        Step.Ringing -> QiapTheme(QiapThemeVariant.Ringing) {
            val first = moves.first()
            RingingScreen(
                hour = request.hour,
                minute = request.minute,
                label = request.label,
                exerciseUnit = first.first.unit,
                target = first.second,
                exerciseId = first.first.id,
                moveCount = moves.size,
                onStartWorkout = {
                    AlarmService.workoutStarted(context)
                    moveIndex = 0
                    step = Step.Workout
                },
                onFallback = {
                    fallbackReason = "Can't do the workout today? That's okay. Prove you're awake another way."
                    fromWorkout = false
                    step = Step.Fallback
                },
                snoozesLeft = request.snoozesLeft,
                snoozeNeedsMiniSet = request.snoozeMini,
                onSnooze = {
                    if (request.snoozeMini) {
                        moveIndex = 0
                        step = Step.MiniSet
                    } else {
                        AlarmService.snooze(context)
                        onClose()
                    }
                },
            )
        }
        Step.Workout -> QiapTheme(QiapThemeVariant.Night) {
            // key() gives each move of a routine a fresh camera session and rep counter.
            key(moveIndex) {
                WorkoutScreen(
                    exercise = spec,
                    target = target,
                    onComplete = { r, s ->
                        reps += r
                        seconds += s
                        if (moveIndex < moves.lastIndex) {
                            // Credit this move now; the service logs the last one when the ring ends.
                            if (request.alarmId != AlarmScheduler.TEST_ID) {
                                val now = LocalTime.now()
                                container.historyStore.record(
                                    HistoryEntry(
                                        alarmId = request.alarmId,
                                        epochDay = LocalDate.now().toEpochDay(),
                                        endedAtMs = System.currentTimeMillis(),
                                        alarmMinuteOfDay = request.hour * 60 + request.minute,
                                        endedMinuteOfDay = now.hour * 60 + now.minute,
                                        outcome = Outcome.EARNED,
                                        exerciseId = spec.id,
                                        reps = r,
                                        seconds = s,
                                    ),
                                )
                            }
                            moveIndex++
                        } else {
                            AlarmService.finish(context, Outcome.EARNED, reps, seconds, exerciseId = spec.id)
                            step = Step.Success
                        }
                    },
                    onGiveUp = { reason ->
                        fallbackReason = reason
                        fromWorkout = true
                        step = Step.Fallback
                    },
                )
            }
        }
        Step.MiniSet -> QiapTheme(QiapThemeVariant.Night) {
            // Earn the snooze: a short set of the first move. The alarm keeps ringing meanwhile.
            val (miniSpec, miniFull) = moves.first()
            WorkoutScreen(
                exercise = miniSpec,
                target = RingRules.miniSetTarget(miniSpec.isHold, miniFull),
                onComplete = { _, _ ->
                    AlarmService.snooze(context)
                    onClose()
                },
                onGiveUp = { step = Step.Ringing },
            )
        }
        Step.Fallback -> QiapTheme {
            FallbackScreen(
                reason = fallbackReason,
                onSolved = {
                    AlarmService.finish(context, Outcome.FALLBACK, reps = 0, seconds = 0)
                    onClose()
                },
                onBackToWorkout = if (fromWorkout) ({ step = Step.Workout }) else ({ step = Step.Ringing }),
            )
        }
        Step.Success -> QiapTheme {
            val entries by container.historyStore.entries.collectAsStateWithLifecycle()
            val stats = remember(entries) { HistoryStats.from(entries, LocalDate.now()) }
            SuccessScreen(reps = reps, seconds = seconds, streak = stats.streak, best = stats.bestStreak, onDone = onClose)
        }
    }
}
