package com.example.energyhub.model

import com.etfrogers.ecoforestklient.EcoForestConfig
import com.etfrogers.ksolaredge.SolarEdgeConfig
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Config(
    @SerialName("solar-edge") val solarEdgeConfig: SolarEdgeConfig,
    @SerialName("ecoforest") val ecoForestConfig: EcoForestConfig,

)