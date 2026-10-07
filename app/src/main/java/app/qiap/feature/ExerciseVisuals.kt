package app.qiap.feature

import app.qiap.core.designsystem.pictogram.PictogramMotion
import app.qiap.core.designsystem.pictogram.Pictograms
import app.qiap.exercise.ExerciseCatalog

/**
 * Pictogram for a catalog exercise id. Exercises share art where the motion matches (every
 * push-up variation uses the push-up); anything without its own falls back to the squat.
 */
fun pictogramFor(exerciseId: String): PictogramMotion {
    val key = ExerciseCatalog.byId(exerciseId)?.pictogram ?: exerciseId
    return when (key) {
        "squat" -> Pictograms.Squat
        "pushup" -> Pictograms.PushUp
        "lunge" -> Pictograms.Lunge
        "jack" -> Pictograms.JumpingJack
        "plank" -> Pictograms.Plank
        "bridge" -> Pictograms.GluteBridge
        "high-knees" -> Pictograms.HighKnees
        "climber" -> Pictograms.MountainClimber
        "side-bend" -> Pictograms.SideReach
        "situp" -> Pictograms.SitUp
        else -> Pictograms.Squat
    }
}
