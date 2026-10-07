package app.qiap.feature.workout

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.compose.CameraXViewfinder
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.qiap.BuildConfig
import app.qiap.QiapApp
import app.qiap.camera.PoseCamera
import app.qiap.core.designsystem.component.CardSize
import app.qiap.core.designsystem.component.Chip
import app.qiap.core.designsystem.component.ChipTone
import app.qiap.core.designsystem.component.FormFeedbackPill
import app.qiap.core.designsystem.component.FormState
import app.qiap.core.designsystem.component.QiapCard
import app.qiap.core.designsystem.component.QiapProgressBar
import app.qiap.core.designsystem.component.QiapText
import app.qiap.core.designsystem.component.RecordingDot
import app.qiap.core.designsystem.icon.QiapIcons
import app.qiap.core.designsystem.pictogram.Pictogram
import app.qiap.core.designsystem.pictogram.Pictograms
import app.qiap.core.designsystem.theme.QiapMotion
import app.qiap.core.designsystem.theme.QiapRadius
import app.qiap.core.designsystem.theme.QiapSpacing
import app.qiap.core.designsystem.theme.QiapTheme
import app.qiap.exercise.ExerciseCatalog
import app.qiap.exercise.ExerciseSpec
import app.qiap.feature.pictogramFor
import app.qiap.pose.PoseEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Workout (design.md §7), Night theme. With camera permission: live CameraX preview, MediaPipe
 * skeleton overlay and real rep counting. Without it (or if the pose engine fails to load): a
 * demo stand-in, so the flow is never a dead end. Nothing over the camera is glass or blurred.
 */
@Composable
fun WorkoutScreen(
    onComplete: (reps: Int, seconds: Int) -> Unit,
    exercise: ExerciseSpec = ExerciseCatalog.Squat,
    target: Int = 12,
    /**
     * Set for a real ringing alarm: the way out when the workout can't happen (no camera, no pose
     * model, 10 minutes of trying, or "I can't do this"). The reason is shown on the fallback screen.
     * Null in previews, where a demo stand-in keeps the flow alive instead.
     */
    onGiveUp: ((reason: String) -> Unit)? = null,
) {
    val context = LocalContext.current
    var granted by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    var asked by rememberSaveable { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        granted = it
        asked = true
    }
    LaunchedEffect(Unit) { if (!granted && !asked) launcher.launch(Manifest.permission.CAMERA) }

    val giveUp by rememberUpdatedState(onGiveUp)
    if (granted) {
        LiveWorkout(exercise, target, onComplete, onGiveUp)
    } else if (onGiveUp != null) {
        // A real alarm never falls back to the demo (its "+1 rep" would be a free pass).
        LaunchedEffect(asked) { if (asked) giveUp?.invoke("The camera is off, so I can't count reps.") }
        CameraStage { QiapText("Waiting for camera access…", style = QiapTheme.type.caption, color = QiapTheme.colors.ink3, modifier = Modifier.align(Alignment.Center)) }
    } else {
        DemoWorkout(
            exercise = exercise,
            target = target,
            onComplete = onComplete,
            notice = if (asked) "Camera is off, so this is a demo." else null,
            onAllowCamera = {
                // After a permanent denial the system dialog no longer shows: send them to settings.
                if (asked) {
                    context.startActivity(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null)),
                    )
                } else {
                    launcher.launch(Manifest.permission.CAMERA)
                }
            },
        )
    }
}

private sealed interface EngineState {
    data object Loading : EngineState
    data class Ready(val engine: PoseEngine) : EngineState
    data class Failed(val reason: String) : EngineState
}

@Composable
private fun LiveWorkout(exercise: ExerciseSpec, target: Int, onComplete: (Int, Int) -> Unit, onGiveUp: ((String) -> Unit)?) {
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    val container = remember { (context.applicationContext as QiapApp).container }
    val session = remember(exercise) { PoseSession(exercise) }
    var seconds by rememberSaveable { mutableIntStateOf(0) }
    LaunchedEffect(Unit) { while (true) { delay(1000); seconds++ } }
    val done by rememberUpdatedState(onComplete)
    val giveUp by rememberUpdatedState(onGiveUp)
    // Camera sessions are capped (details.md §3): after this long, offer the way out.
    LaunchedEffect(seconds >= WORKOUT_CAP_SECONDS) {
        if (seconds >= WORKOUT_CAP_SECONDS) giveUp?.invoke("That's 10 minutes of trying. No shame in it.")
    }

    // Model load + GPU init take a few hundred ms: off the main thread, closed on leave.
    val state by produceState<EngineState>(EngineState.Loading) {
        val engine = try {
            withContext(Dispatchers.Default + NonCancellable) { container.createPoseEngine() }
        } catch (e: RuntimeException) {
            value = EngineState.Failed(e.message ?: "Pose engine failed to load")
            return@produceState
        }
        try {
            ensureActive()
            engine.listener = session
            value = EngineState.Ready(engine)
            awaitCancellation()
        } finally {
            engine.close()
        }
    }

    LaunchedEffect(session.reps) {
        if (session.reps >= target) {
            delay(500)
            done(session.reps, seconds)
        }
    }

    when (val s = state) {
        is EngineState.Failed -> if (onGiveUp != null) {
            LaunchedEffect(Unit) { giveUp?.invoke("Rep counting couldn't start on this phone.") }
            CameraStage {}
        } else {
            DemoWorkout(exercise, target, onComplete, notice = "Rep counting couldn't start: ${s.reason}", onAllowCamera = null)
        }
        EngineState.Loading -> CameraStage { QiapText("Waking up the camera…", style = QiapTheme.type.caption, color = QiapTheme.colors.ink3, modifier = Modifier.align(Alignment.Center)) }
        is EngineState.Ready -> {
            val camera = remember(s.engine) { PoseCamera(context, s.engine) }
            LaunchedEffect(camera, owner) {
                try {
                    camera.run(owner)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    // Camera in use, disabled by policy, or no such camera: never crash a ringing alarm.
                    giveUp?.invoke("The camera wouldn't open.")
                }
            }
            val request by camera.surfaceRequest.collectAsStateWithLifecycle()
            val mirrored by camera.mirrored.collectAsStateWithLifecycle()

            CameraStage {
                request?.let {
                    CameraXViewfinder(surfaceRequest = it, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                }
                SkeletonOverlay(session, mirrored, Modifier.fillMaxSize())
                WorkoutHud(
                    exercise = exercise,
                    target = target,
                    reps = session.reps,
                    seconds = seconds,
                    cue = session.cue,
                    offForm = session.offForm,
                    stats = if (BuildConfig.DEBUG) "${session.fps} fps · ${session.inferenceMs} ms · ${s.engine.delegateName}" else null,
                    footer = { if (BuildConfig.DEBUG) FixtureRecorder(session) else QiapText(PRIVACY, style = QiapTheme.type.caption, color = QiapTheme.colors.ink3) },
                    onGiveUp = onGiveUp?.let { g -> { g("Injured, too dark, or just not today? Do this instead.") } },
                )
            }
        }
    }
}

private const val WORKOUT_CAP_SECONDS = 600
private const val PRIVACY = "Counting on-device. Nothing leaves your phone."

/** Debug only: record a landmark fixture for threshold tuning (adb pull …/files/fixtures). */
@Composable
private fun RowScope.FixtureRecorder(session: PoseSession) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var recording by remember { mutableStateOf(session.isRecording) }
    var saved by remember { mutableStateOf<String?>(null) }
    QiapText(saved ?: "Debug: record a fixture to tune thresholds", style = QiapTheme.type.caption, color = QiapTheme.colors.ink3, modifier = Modifier.weight(1f))
    Chip(if (recording) "Stop" else "Record", leadingIcon = if (recording) null else QiapIcons.Video, selected = recording, onClick = {
        if (!recording) {
            session.startRecording("${session.spec.id} fixture, ${android.os.Build.MODEL}, describe reps/depth here")
            recording = true
        } else {
            recording = false
            val text = session.stopRecording() ?: return@Chip
            scope.launch {
                val name = "${session.spec.id}-${System.currentTimeMillis()}.csv"
                withContext(Dispatchers.IO) {
                    val dir = File(context.getExternalFilesDir(null), "fixtures").apply { mkdirs() }
                    File(dir, name).writeText(text)
                }
                saved = "Saved fixtures/$name"
            }
        }
    })
}

/** Stand-in when there's no camera: a looping skeleton that counts one rep per cycle. */
@Composable
private fun DemoWorkout(
    exercise: ExerciseSpec,
    target: Int,
    onComplete: (Int, Int) -> Unit,
    notice: String?,
    onAllowCamera: (() -> Unit)?,
) {
    val colors = QiapTheme.colors
    var reps by rememberSaveable { mutableIntStateOf(0) }
    var seconds by rememberSaveable { mutableIntStateOf(0) }
    val done by rememberUpdatedState(onComplete)
    LaunchedEffect(Unit) {
        while (reps < target) {
            delay(pictogramFor(exercise.id).periodMs.toLong())
            if (reps < target) reps++
        }
        delay(500)
        done(reps, seconds)
    }
    LaunchedEffect(Unit) { while (true) { delay(1000); seconds++ } }
    val offForm = reps % 4 == 2

    CameraStage(standIn = true) {
        Pictogram(
            pictogramFor(exercise.id),
            Modifier.align(Alignment.TopCenter).padding(top = 120.dp).size(300.dp),
            color = colors.ink,
            showFloor = false,
            showJoints = true,
            offForm = offForm,
        )
        if (notice != null) {
            QiapCard(
                Modifier.align(Alignment.TopCenter).windowInsetsPadding(WindowInsets.safeDrawing).padding(top = 64.dp, start = QiapSpacing.md, end = QiapSpacing.md),
                size = CardSize.Small,
            ) {
                QiapText(notice, style = QiapTheme.type.bodySmall)
                if (onAllowCamera != null) Chip("Allow camera", leadingIcon = QiapIcons.Camera, tone = ChipTone.Saffron, onClick = onAllowCamera)
            }
        }
        WorkoutHud(
            exercise = exercise,
            target = target,
            reps = reps,
            seconds = seconds,
            cue = if (offForm) exercise.depthCue else null,
            offForm = offForm,
            stats = "Demo",
            footer = {
                QiapText(PRIVACY, style = QiapTheme.type.caption, color = colors.ink3, modifier = Modifier.weight(1f))
                Chip("+1 rep", onClick = { if (reps < target) reps++ })
            },
        )
    }
}

/** Full-screen dark stage. [standIn] paints a dim room where the camera preview would be. */
@Composable
private fun CameraStage(standIn: Boolean = false, content: @Composable BoxScope.() -> Unit) {
    Box(
        Modifier.fillMaxSize().background(QiapTheme.colors.bg).let { if (standIn) it.cameraStandIn() else it },
        content = content,
    )
}

/** Top: proof timer + exercise chip (+ debug stats). Bottom: cue, giant rep count, progress, footer. All opaque. */
@Composable
private fun BoxScope.WorkoutHud(
    exercise: ExerciseSpec,
    target: Int,
    reps: Int,
    seconds: Int,
    cue: String?,
    offForm: Boolean,
    stats: String?,
    footer: @Composable RowScope.() -> Unit,
    onGiveUp: (() -> Unit)? = null,
) {
    val colors = QiapTheme.colors
    val type = QiapTheme.type

    // Rep counter pops and flashes jade on every new rep.
    val bump = remember { Animatable(0f) }
    LaunchedEffect(reps) {
        if (reps == 0) return@LaunchedEffect
        bump.snapTo(1f)
        bump.animateTo(0f, tween(350, easing = QiapMotion.easing))
    }

    Column(
        Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.safeDrawing).padding(QiapSpacing.md),
        verticalArrangement = Arrangement.spacedBy(QiapSpacing.xs),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(
                Modifier.background(colors.surface, QiapRadius.pill).padding(horizontal = QiapSpacing.sm, vertical = QiapSpacing.xs),
                horizontalArrangement = Arrangement.spacedBy(QiapSpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RecordingDot()
                QiapText(
                    "Proof · ${seconds / 60}:${(seconds % 60).toString().padStart(2, '0')}",
                    style = type.numeric.copy(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.SemiBold),
                )
            }
            Chip(exercise.name, leading = { Pictogram(pictogramFor(exercise.id), Modifier.size(18.dp), showFloor = false) })
        }
        if (onGiveUp != null) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Chip("I can't do this", onClick = onGiveUp)
            }
        }
        if (stats != null) {
            QiapText(
                stats,
                style = type.numeric.copy(fontSize = 12.sp, lineHeight = 16.sp),
                color = colors.ink3,
                modifier = Modifier.background(colors.surface, QiapRadius.pill).padding(horizontal = QiapSpacing.sm, vertical = QiapSpacing.xxs),
            )
        }
    }

    Column(
        Modifier.align(Alignment.BottomStart).fillMaxWidth().windowInsetsPadding(WindowInsets.safeDrawing).padding(QiapSpacing.md),
        verticalArrangement = Arrangement.spacedBy(QiapSpacing.sm),
    ) {
        FormFeedbackPill(cue ?: "Good depth", if (cue != null || offForm) FormState.Off else FormState.Good)
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(QiapSpacing.xs)) {
            QiapText(
                "$reps",
                style = type.display.copy(fontSize = 128.sp, lineHeight = 116.sp, letterSpacing = (-0.05).em),
                color = lerp(colors.ink, colors.jade, bump.value),
                modifier = Modifier.graphicsLayer {
                    val s = 1f + 0.18f * bump.value
                    scaleX = s
                    scaleY = s
                    transformOrigin = TransformOrigin(0f, 1f)
                },
            )
            QiapText(
                "/ $target",
                style = type.displayCompact.copy(fontSize = 28.sp, lineHeight = 28.sp),
                color = colors.ink3,
                modifier = Modifier.padding(bottom = 14.dp),
            )
        }
        QiapProgressBar(reps / target.toFloat())
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(QiapSpacing.xs), content = footer)
    }
}

/** Dim room + floor line where the CameraX preview would go. Opaque, drawn once per size. */
private fun Modifier.cameraStandIn(): Modifier = drawWithCache {
    val glow = Brush.radialGradient(
        listOf(Color(0xFF2A2723), Color.Transparent),
        center = Offset(size.width / 2f, size.height * 0.3f),
        radius = size.width * 0.8f,
    )
    val room = Brush.verticalGradient(
        0f to Color(0xFF1B1A18), 0.62f to Color(0xFF0D0D0F), 0.621f to Color(0xFF141312), 1f to Color(0xFF0D0D0F),
    )
    onDrawBehind {
        drawRect(room)
        drawRect(glow)
    }
}
