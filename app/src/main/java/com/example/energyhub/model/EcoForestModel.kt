package com.example.energyhub.model

import com.etfrogers.ecoforestklient.EcoForestClient
import com.etfrogers.ecoforestklient.EcoforestStatus

class EcoForestModel(server: String, port: String, serialNumber: String, authKey: String,): BaseModel() {
    private val client: EcoForestClient =
        EcoForestClient(server = server, port = port, authKey = authKey, serialNumber = serialNumber,
            debugSSL = true
        )

    fun refresh(): EcoforestStatus{
        return client.getCurrentStatus()
    }
}