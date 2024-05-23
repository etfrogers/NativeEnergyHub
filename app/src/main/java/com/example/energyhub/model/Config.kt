package com.example.energyhub.model

import com.charleskorn.kaml.Yaml
import com.etfrogers.ecoforestklient.EcoForestConfig
import com.etfrogers.ksolaredge.SolarEdgeConfig
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.InputStream

@Serializable
data class ConfigData (
    @SerialName("solar-edge") val solarEdgeConfig: SolarEdgeConfig,
    @SerialName("ecoforest") val ecoForestConfig: EcoForestConfig,
)

lateinit var Config: ConfigData

fun loadConfig(configFile: InputStream) {
    val text = configFile.bufferedReader().use { it.readText() }
    Config = Yaml.default.decodeFromString(ConfigData.serializer(), text)
}

