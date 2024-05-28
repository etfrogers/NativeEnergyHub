package com.example.energyhub.model

import com.etfrogers.ecoforestklient.EcoForestClient
import com.etfrogers.ecoforestklient.EcoforestStatus

class EcoForestModel(server: String, port: String, serialNumber: String, authKey: String,): BaseModel<EcoforestStatus>() {
    private val client: EcoForestClient =
        EcoForestClient(server = server, port = port, authKey = authKey, serialNumber = serialNumber,
            debugSSL = true
        )

    override suspend fun refreshUnsafe(): EcoforestStatus {
        return client.getCurrentStatus()
    }
}