package com.example.energyhub.model

import com.charleskorn.kaml.Yaml
import com.etfrogers.ecoforestklient.EcoForestConfig
import com.etfrogers.ksolaredge.SolarEdgeConfig
import com.etfrogers.myenergiklient.MyEnergiConfig
import kotlinx.datetime.TimeZone
import kotlinx.datetime.serializers.TimeZoneSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.InputStream

@Serializable
data class Preferences(
    @SerialName("color-mode") val colorMode: ColorMode,
)

@Serializable
data class Location(
    @SerialName("lat") val latitude: Float,
    @SerialName("lon") val longitude: Float,
    @Serializable(with=TimeZoneSerializer::class) val timezone: TimeZone
)

@Serializable
data class ConfigData (
    @SerialName("solar-edge") val solarEdgeConfig: SolarEdgeConfig,
    @SerialName("ecoforest") val ecoForestConfig: EcoForestConfig,
    @SerialName("myenergi") val myEnergiConfig: MyEnergiConfig,
    @SerialName("app-preferences") val preferences: Preferences,
    @SerialName("site-location") val location: Location,
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
