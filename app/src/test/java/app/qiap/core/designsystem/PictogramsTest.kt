package app.qiap.core.designsystem

import app.qiap.core.designsystem.pictogram.Joint
import app.qiap.core.designsystem.pictogram.Pictograms
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PictogramsTest {

    @Test
    fun everyPoseHasAllJointsInsideTheGrid() {
        for (m in Pictograms.all) {
            for (pose in listOf(m.from, m.to)) {
                assertEquals("${m.name} joint count", Joint.COUNT * 2, pose.size)
                assertTrue("${m.name} out of 0..100", pose.all { it in 0f..100f })
            }
        }
    }

    @Test
    fun everyMotionActuallyMovesAndLoopsAtAHumanPace() {
        for (m in Pictograms.all) {
            val travel = m.from.indices.maxOf { kotlin.math.abs(m.from[it] - m.to[it]) }
            assertTrue("${m.name} barely moves ($travel)", travel >= 3f)
            assertTrue("${m.name} period ${m.periodMs}", m.periodMs in 600..4000)
        }
    }

    @Test
    fun namesAreUnique() {
        assertEquals(Pictograms.all.size, Pictograms.all.map { it.name }.toSet().size)
    }
}
