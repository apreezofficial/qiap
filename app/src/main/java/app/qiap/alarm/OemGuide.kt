package app.qiap.alarm

/**
 * Step-by-step text for keeping Qiap alive on phones whose skins aggressively kill background
 * apps (details.md §5). Plain text, no screenshots; menu names vary by version, so the steps say
 * what to look for rather than promising exact paths. Pure Kotlin so it is unit-tested.
 */
class OemGuide(val brand: String, val steps: List<String>)

object OemGuides {
    private val lockInRecents = "Open recent apps, then lock Qiap (a padlock or a pull-down on its card) so it is not swiped away."

    private val generic = OemGuide(
        "your phone",
        listOf(
            "Settings > Apps > Qiap > Battery: choose Unrestricted (or No restrictions).",
            "If your phone has an Autostart or Auto-launch list, switch Qiap on.",
            lockInRecents,
        ),
    )

    private val guides: List<Pair<List<String>, OemGuide>> = listOf(
        listOf("tecno", "infinix", "itel", "transsion") to OemGuide(
            "Tecno / Infinix / itel",
            listOf(
                "Open Phone Master > App management > Autostart, and switch Qiap on.",
                "Settings > Battery > App battery management: set Qiap to No restrictions.",
                lockInRecents,
            ),
        ),
        listOf("xiaomi", "redmi", "poco") to OemGuide(
            "Xiaomi / Redmi / Poco",
            listOf(
                "Settings > Apps > Permissions > Autostart, and switch Qiap on.",
                "Settings > Apps > Manage apps > Qiap > Battery saver: choose No restrictions.",
                lockInRecents,
            ),
        ),
        listOf("oppo", "realme", "oneplus") to OemGuide(
            "Oppo / Realme / OnePlus",
            listOf(
                "Settings > Apps > App management > Qiap > Battery usage: allow background activity and auto launch.",
                "Settings > Battery: turn off any optimisation for Qiap.",
                lockInRecents,
            ),
        ),
        listOf("vivo", "iqoo") to OemGuide(
            "Vivo / iQOO",
            listOf(
                "iManager > App manager > Autostart, and switch Qiap on.",
                "Settings > Battery > Background power consumption: allow Qiap.",
                lockInRecents,
            ),
        ),
        listOf("samsung") to OemGuide(
            "Samsung",
            listOf(
                "Settings > Battery > Background usage limits: remove Qiap from Sleeping and Deep sleeping apps.",
                "Add Qiap to Never sleeping apps.",
                "Settings > Apps > Qiap > Battery: choose Unrestricted.",
            ),
        ),
        listOf("huawei", "honor") to OemGuide(
            "Huawei / Honor",
            listOf(
                "Settings > Apps > App launch > Qiap: choose Manage manually, then switch on all three options.",
                "Settings > Battery: set Qiap to not be optimised.",
                lockInRecents,
            ),
        ),
    )

    /** The guide for [manufacturer] (as in `Build.MANUFACTURER`), or a general one for unknown brands. */
    fun forManufacturer(manufacturer: String?): OemGuide {
        val m = manufacturer.orEmpty().lowercase()
        return guides.firstOrNull { (keys, _) -> keys.any { it in m } }?.second ?: generic
    }
}
