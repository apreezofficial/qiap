package app.qiap.alarm

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OemGuideTest {

    @Test
    fun knownBrandsGetTheirOwnGuide() {
        assertEquals("Tecno / Infinix / itel", OemGuides.forManufacturer("TECNO").brand)
        assertEquals("Tecno / Infinix / itel", OemGuides.forManufacturer("Infinix").brand)
        assertEquals("Xiaomi / Redmi / Poco", OemGuides.forManufacturer("Xiaomi").brand)
        assertEquals("Samsung", OemGuides.forManufacturer("samsung").brand)
        assertEquals("Oppo / Realme / OnePlus", OemGuides.forManufacturer("realme").brand)
        assertEquals("Huawei / Honor", OemGuides.forManufacturer("HUAWEI").brand)
    }

    @Test
    fun unknownOrMissingBrandsGetAGeneralGuide() {
        assertEquals("your phone", OemGuides.forManufacturer("Google").brand)
        assertEquals("your phone", OemGuides.forManufacturer(null).brand)
        assertEquals("your phone", OemGuides.forManufacturer("").brand)
    }

    @Test
    fun everyGuideHasAtLeastTwoSteps() {
        for (m in listOf("tecno", "xiaomi", "oppo", "vivo", "samsung", "huawei", "google")) {
            assertTrue(m, OemGuides.forManufacturer(m).steps.size >= 2)
        }
    }
}
