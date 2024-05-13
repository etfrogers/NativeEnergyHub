package com.example.energyhub.model

object SystemModel {
    lateinit var solarEdgeModel: SolarEdgeModel

    fun build(config: Config) {
        solarEdgeModel = SolarEdgeModel(config.solarEdgeConfig.siteID, config.solarEdgeConfig.apiKey)
    }
}