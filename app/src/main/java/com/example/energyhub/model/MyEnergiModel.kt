package com.example.energyhub.model

import com.etfrogers.myenergiklient.Eddi
import com.etfrogers.myenergiklient.MyEnergiClient
import com.etfrogers.myenergiklient.MyEnergiSystem
import com.etfrogers.myenergiklient.Zappi

class MyEnergiModel(username: String,
                    password: String,
                    invalidSerials: List<String> = listOf()
): BaseModel() {

    private val client: MyEnergiClient = MyEnergiClient (username, password, invalidSerials)
    override suspend fun refresh(): MyEnergiSystem {
        return client.getCurrentStatus()
    }
}

val MyEnergiSystem.immersionPower: Float
    get() = powers["Eddi"]?.toFloat() ?: 0f

val MyEnergiSystem.carPower: Float
    get() = powers["Zappi"]?.toFloat() ?: 0f

val MyEnergiSystem.zappi: Zappi
    get() = if (zappis.size == 1) zappis[0]
    else Zappi(pStatusCode = "A")

val MyEnergiSystem.eddi: Eddi
    get() = if (eddis.size == 1) eddis[0]
    else Eddi()
