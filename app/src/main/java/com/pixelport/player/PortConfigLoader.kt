package com.pixelport.player

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class ButtonMapping(
    val id: String,
    val label: String,
    val key: String,
    val pos: String
)

data class ControlsConfig(
    val packageId: String,
    val profile: String,
    val opacity: Double,
    val leftInput: String,
    val buttons: List<ButtonMapping>
)

data class PortConfig(
    val appName: String,
    val packageId: String,
    val mainExecutable: String?,
    val detectedEngine: String,
    val runtimeTarget: String,
    val touchProfile: String,
    val orientation: String
)

object PortConfigLoader {
    private fun readAsset(context: Context, fileName: String): String {
        return context.assets.open(fileName).bufferedReader().use { it.readText() }
    }

    fun loadPortConfig(context: Context): PortConfig {
        val obj = JSONObject(readAsset(context, "port_config.json"))
        return PortConfig(
            appName = obj.optString("app_name", "PixelPort Game"),
            packageId = obj.optString("package_id", "com.pixelport.game"),
            mainExecutable = if (obj.isNull("main_executable")) null else obj.optString("main_executable"),
            detectedEngine = obj.optString("detected_engine", "Unknown"),
            runtimeTarget = obj.optString("runtime_target", "generic"),
            touchProfile = obj.optString("touch_profile", "rpg_classic"),
            orientation = obj.optString("orientation", "landscape")
        )
    }

    fun loadControlsConfig(context: Context): ControlsConfig {
        val obj = JSONObject(readAsset(context, "controls.json"))
        val buttonsArray: JSONArray = obj.optJSONArray("buttons") ?: JSONArray()
        val buttonList = mutableListOf<ButtonMapping>()
        for (i in 0 until buttonsArray.length()) {
            val b = buttonsArray.getJSONObject(i)
            buttonList.add(
                ButtonMapping(
                    id = b.optString("id", "btn_$i"),
                    label = b.optString("label", "BTN"),
                    key = b.optString("key", "KEY_Z"),
                    pos = b.optString("pos", "right_bottom")
                )
            )
        }
        return ControlsConfig(
            packageId = obj.optString("package_id", "com.pixelport.game"),
            profile = obj.optString("profile", "rpg_classic"),
            opacity = obj.optDouble("opacity", 0.65),
            leftInput = obj.optString("left_input", "dpad"),
            buttons = buttonList
        )
    }
}
