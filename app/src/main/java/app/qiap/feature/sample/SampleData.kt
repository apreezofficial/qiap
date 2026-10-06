package app.qiap.feature.sample

import androidx.compose.runtime.Immutable
import app.qiap.core.designsystem.pictogram.PictogramMotion
import app.qiap.core.designsystem.pictogram.Pictograms

/**
 * Placeholder content so the UI shell can be judged with realistic data. Everything here gets
 * replaced: alarms by the alarm store, exercises by `exercise/ExerciseCatalog`, history by the
 * session log. Nothing in this package may be read by the alarm engine.
 */
@Immutable
data class SampleExercise(
    val name: String,
    val category: String,
    val difficulty: Int,
    val unit: String,
    val jumping: Boolean,
    val needsFloor: Boolean,
    val motion: PictogramMotion,
)

@Immutable
data class SampleAlarm(
    val hour: Int,
    val minute: Int,
    val label: String,
    val days: List<Boolean>,
    val exercise: SampleExercise,
    val target: Int,
    val on: Boolean,
)

object SampleData {
    val categories = listOf("All", "Lower", "Upper", "Core", "Cardio", "Mobility")

    val squat = SampleExercise("Squat", "Lower", 1, "squats", jumping = false, needsFloor = false, Pictograms.Squat)
    val pushUp = SampleExercise("Push-up", "Upper", 2, "push-ups", jumping = false, needsFloor = true, Pictograms.PushUp)
    val lunge = SampleExercise("Lunge", "Lower", 2, "lunges", jumping = false, needsFloor = false, Pictograms.Lunge)
    val jack = SampleExercise("Jumping jack", "Cardio", 1, "jacks", jumping = true, needsFloor = false, Pictograms.JumpingJack)
    val plank = SampleExercise("Plank", "Core", 2, "seconds", jumping = false, needsFloor = true, Pictograms.Plank)
    val bridge = SampleExercise("Glute bridge", "Core", 1, "bridges", jumping = false, needsFloor = true, Pictograms.GluteBridge)
    val knees = SampleExercise("High knees", "Cardio", 2, "knees", jumping = true, needsFloor = false, Pictograms.HighKnees)
    val climber = SampleExercise("Mountain climber", "Core", 3, "climbers", jumping = false, needsFloor = true, Pictograms.MountainClimber)
    val reach = SampleExercise("Side reach", "Mobility", 1, "reaches", jumping = false, needsFloor = false, Pictograms.SideReach)

    val exercises = listOf(squat, pushUp, lunge, jack, plank, bridge, knees, climber, reach)

    val alarms = listOf(
        SampleAlarm(6, 30, "Weekdays", weekdays(1, 1, 1, 1, 1, 0, 0), squat, 12, on = true),
        SampleAlarm(8, 15, "Weekend, gently", weekdays(0, 0, 0, 0, 0, 1, 1), reach, 10, on = true),
        SampleAlarm(5, 45, "Gym days", weekdays(1, 0, 1, 0, 1, 0, 0), jack, 30, on = false),
    )

    private fun weekdays(vararg d: Int) = d.map { it == 1 }
}
