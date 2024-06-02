package com.example.energyhub.model

import com.etfrogers.ecoforestklient.EcoForestClient
import com.etfrogers.ecoforestklient.EcoforestStatus
import kotlinx.datetime.LocalDate

class EcoForestModel(server: String, port: String, serialNumber: String, authKey: String,): BaseModel<EcoforestStatus>() {
    private val client: EcoForestClient =
        EcoForestClient(server = server, port = port, authKey = authKey, serialNumber = serialNumber,
            debugSSL = true
        )

    override suspend fun refreshUnsafe(): EcoforestStatus {
        return client.getCurrentStatus()
    }

    override suspend fun getHistoryForDateUnsafe(date: LocalDate): HistoryData {
        TODO("Not yet implemented")
    }
}