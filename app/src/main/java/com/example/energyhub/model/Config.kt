package com.example.energyhub.model

import com.charleskorn.kaml.Yaml
import com.etfrogers.ecoforestklient.EcoForestConfig
import com.etfrogers.ksolaredge.SolarEdgeConfig
import com.etfrogers.myenergiklient.MyEnergiConfig
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.InputStream

@Serializable
data class Preferences(
    @SerialName("color-mode") val colorMode: ColorMode,
)

@Serializable
data class ConfigData (
    @SerialName("solar-edge") val solarEdgeConfig: SolarEdgeConfig,
    @SerialName("ecoforest") val ecoForestConfig: EcoForestConfig,
    @SerialName("myenergi") val myEnergiConfig: MyEnergiConfig,
    @SerialName("app-preferences") val preferences: Preferences,
){
    fun isAppInDarkTheme(isSystemInDarkTheme: Boolean): Boolean {
        return when (preferences.colorMode) {
            ColorMode.DARK -> true
            ColorMode.LIGHT -> false
            ColorMode.AUTO -> isSystemInDarkTheme
        }
    }
}

lateinit var Config: ConfigData

fun loadConfig(configFile: InputStream) {
    val text = configFile.bufferedReader().use { it.readText() }
    Config = Yaml.default.decodeFromString(ConfigData.serializer(), text)
}

@Serializable
enum class ColorMode {
    @SerialName("light") LIGHT,
    @SerialName("dark") DARK,
    @SerialName("auto") AUTO,
}
