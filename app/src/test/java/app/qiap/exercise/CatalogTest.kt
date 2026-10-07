package app.qiap.exercise

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CatalogTest {

    @Test
    fun hasAllFiftyTwoExercisesWithUniqueIds() {
        assertEquals(52, ExerciseCatalog.all.size)
        assertEquals(52, ExerciseCatalog.all.map { it.id }.toSet().size)
        for (s in ExerciseCatalog.all) assertNotNull(s.id, ExerciseCatalog.byId(s.id))
    }

    @Test
    fun categoryCountsMatchTheSpec() {
        val counts = ExerciseCatalog.all.groupingBy { it.category }.eachCount()
        assertEquals(13, counts[Category.LOWER])
        assertEquals(9, counts[Category.UPPER])
        assertEquals(14, counts[Category.CORE])
        assertEquals(10, counts[Category.CARDIO])
        assertEquals(6, counts[Category.MOBILITY])
    }

    @Test
    fun holdsAreCountedInSeconds() {
        for (s in ExerciseCatalog.all.filter { it.isHold }) assertEquals(s.id, "seconds", s.unit)
        assertEquals(
            setOf("wall-sit", "plank", "side-plank", "chair-pose", "warrior-ii", "tree-pose", "downward-dog"),
            ExerciseCatalog.all.filter { it.isHold }.map { it.id }.toSet(),
        )
    }

    @Test
    fun presetsResolveAndFilterAsDescribed() {
        for (p in ExercisePools.all) assertTrue(p.id, p.members.isNotEmpty())
        assertEquals(5, ExercisePools.WakeUpLite.members.size)
        val quiet = ExercisePools.Quiet.members.map { it.id }
        for (id in listOf("jump-squat", "jack", "star-jump", "jump-rope", "burpee")) assertFalse(id, id in quiet)
        assertTrue(ExercisePools.NoFloor.members.none { it.needsFloor })
    }

    @Test
    fun everyExerciseStillCarriesProvisionalUntilFixturesBackIt() {
        // CLAUDE.md: thresholds are tuned from recorded fixtures. Until each exercise has a
        // passing fixture, it must stay marked provisional. Flip a flag only together with a fixture test.
        assertTrue(ExerciseCatalog.all.all { it.provisional })
    }

    @Test
    fun everyExerciseHasADefaultTargetAndATip() {
        for (s in ExerciseCatalog.all) {
            assertTrue(s.id, s.defaultTarget > 0)
            assertTrue("${s.id} needs a tip", s.tip.isNotBlank())
        }
    }
}
