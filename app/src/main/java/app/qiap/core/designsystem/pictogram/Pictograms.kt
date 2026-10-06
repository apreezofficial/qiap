package app.qiap.core.designsystem.pictogram

import androidx.compose.runtime.Immutable

/**
 * An exercise illustration as two keyframe poses the renderer eases between (design.md §9:
 * pictograms come from the skeleton renderer, so they stay vector and on-brand).
 *
 * Each pose holds 11 joints on a 100×100 grid as x,y pairs, in [Joint] order.
 * These are illustrations only; rep-counting thresholds live in `exercise/ExerciseCatalog`.
 */
@Immutable
class PictogramMotion(
    val name: String,
    val periodMs: Int,
    val from: FloatArray,
    val to: FloatArray,
)

object Joint {
    const val HEAD = 0
    const val NECK = 1
    const val HIP = 2
    const val ELBOW_L = 3
    const val HAND_L = 4
    const val ELBOW_R = 5
    const val HAND_R = 6
    const val KNEE_L = 7
    const val FOOT_L = 8
    const val KNEE_R = 9
    const val FOOT_R = 10
    const val COUNT = 11
}

object Pictograms {
    val Squat = PictogramMotion(
        "Squat", 2400,
        pose(50, 13, 50, 25, 50, 53, 53, 39, 56, 51, 53, 39, 56, 51, 51, 71, 50, 90, 51, 71, 50, 90),
        pose(60, 34, 56, 44, 38, 63, 68, 44, 82, 43, 68, 44, 82, 43, 60, 67, 52, 90, 60, 67, 52, 90),
    )
    val PushUp = PictogramMotion(
        "Push-up", 2000,
        pose(84, 50, 77, 55, 46, 63, 76, 68, 76, 84, 76, 68, 76, 84, 30, 68, 12, 76, 30, 68, 12, 76),
        pose(86, 68, 78, 72, 46, 73, 64, 68, 76, 84, 64, 68, 76, 84, 30, 74, 12, 78, 30, 74, 12, 78),
    )
    val Lunge = PictogramMotion(
        "Lunge", 2600,
        pose(50, 13, 50, 25, 50, 53, 52, 39, 52, 52, 48, 39, 48, 52, 52, 71, 54, 90, 48, 71, 46, 90),
        pose(50, 28, 50, 40, 50, 64, 52, 52, 52, 65, 48, 52, 48, 65, 66, 66, 66, 90, 38, 82, 22, 90),
    )
    val JumpingJack = PictogramMotion(
        "Jumping jack", 1000,
        pose(50, 13, 50, 25, 50, 54, 43, 38, 41, 52, 57, 38, 59, 52, 47, 72, 46, 90, 53, 72, 54, 90),
        pose(50, 9, 50, 21, 50, 50, 38, 14, 32, 2, 62, 14, 68, 2, 42, 68, 33, 86, 58, 68, 67, 86),
    )
    val Plank = PictogramMotion(
        "Plank", 3000,
        pose(84, 58, 77, 62, 46, 64, 76, 76, 90, 78, 76, 76, 90, 78, 30, 68, 12, 74, 30, 68, 12, 74),
        pose(84, 57, 77, 61, 46, 60, 76, 76, 90, 78, 76, 76, 90, 78, 30, 66, 12, 74, 30, 66, 12, 74),
    )
    val GluteBridge = PictogramMotion(
        "Glute bridge", 2400,
        pose(14, 80, 24, 80, 52, 82, 34, 88, 46, 90, 34, 88, 46, 90, 68, 64, 78, 88, 68, 64, 78, 88),
        pose(14, 80, 24, 78, 54, 60, 34, 88, 46, 90, 34, 88, 46, 90, 72, 56, 78, 88, 72, 56, 78, 88),
    )
    val HighKnees = PictogramMotion(
        "High knees", 800,
        pose(50, 13, 50, 25, 50, 53, 44, 36, 42, 24, 57, 40, 60, 52, 42, 62, 46, 78, 53, 72, 54, 90),
        pose(50, 13, 50, 25, 50, 53, 56, 36, 58, 24, 43, 40, 40, 52, 47, 72, 46, 90, 58, 62, 54, 78),
    )
    val MountainClimber = PictogramMotion(
        "Mountain climber", 900,
        pose(84, 48, 77, 53, 46, 60, 76, 66, 76, 84, 76, 66, 76, 84, 62, 70, 56, 84, 30, 68, 12, 76),
        pose(84, 49, 77, 54, 46, 61, 76, 66, 76, 84, 76, 66, 76, 84, 34, 68, 14, 76, 60, 72, 54, 86),
    )
    val SideReach = PictogramMotion(
        "Side reach", 3000,
        pose(50, 13, 50, 25, 50, 53, 42, 14, 44, 2, 58, 14, 56, 2, 48, 72, 46, 90, 52, 72, 54, 90),
        pose(40, 15, 44, 26, 50, 53, 30, 18, 22, 8, 38, 14, 30, 4, 48, 72, 46, 90, 52, 72, 54, 90),
    )

    val all: List<PictogramMotion> = listOf(
        Squat, PushUp, Lunge, JumpingJack, Plank, GluteBridge, HighKnees, MountainClimber, SideReach,
    )

    private fun pose(vararg xy: Int): FloatArray {
        require(xy.size == Joint.COUNT * 2) { "pose needs ${Joint.COUNT} x,y pairs, got ${xy.size / 2}" }
        return FloatArray(xy.size) { xy[it].toFloat() }
    }
}
