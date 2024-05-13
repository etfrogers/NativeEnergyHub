package com.example.energyhub.model

import com.etfrogers.ksolaredge.SolarEdgeApi
import com.etfrogers.ksolaredge.SolarEdgeApiService
import com.etfrogers.ksolaredge.serialisers.SitePowerFlow

class SolarEdgeModel (siteID: String, apiKey: String) {
    private val service: SolarEdgeApiService = SolarEdgeApi(siteID, apiKey).retrofitService
    suspend fun getCurrentPowerFlow(): SitePowerFlow {
        return service.getPowerFlow()
    }
}
