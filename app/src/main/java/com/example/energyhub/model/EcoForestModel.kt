package com.example.energyhub.model

import com.etfrogers.ecoforestklient.ChunkClass
import com.etfrogers.ecoforestklient.DayData
import com.etfrogers.ecoforestklient.EcoForestClient
import com.etfrogers.ecoforestklient.EcoforestStatus
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone

class EcoForestModel(
    server: String,
    port: String,
    serialNumber: String,
    authKey: String,
    val timezone: TimeZone,
    ): BaseModel<EcoforestStatus, HeatPumpDay>() {
    private val client: EcoForestClient =
        EcoForestClient(server = server, port = port, authKey = authKey, serialNumber = serialNumber,
            debugSSL = true
        )

    override suspend fun refreshUnsafe(): EcoforestStatus {
        return client.getCurrentStatus()
    }

    override suspend fun getHistoryForDateUnsafe(date: LocalDate): HeatPumpDay {
        return HeatPumpDay.fromDayData(client.getHistoryForDate(date), timezone)
    }
}

data class HeatPumpDay(
    val timestamps: List<Instant> = listOf(),
    val outdoorTemp: List<Float> = listOf(),
    val heatingPower: List<Float> = listOf(),
    val dhwPower: List<Float> = listOf(),
    val legionnairesPower: List<Float> = listOf(),
    val combinedPower: List<Float> = listOf(),
    val unknownPower: List<Float> = listOf(),
    val heatingEnergy: Float = 0f,
    val dhwEnergy: Float = 0f,
    val legionnairesEnergy: Float = 0f,
    val combinedEnergy: Float = 0f,
    val unknownEnergy: Float = 0f,
){
    companion object {
        fun fromDayData(data: DayData, timezone: TimeZone): HeatPumpDay {
            return HeatPumpDay(
                timestamps = data.timestamps,
                outdoorTemp = data.outdoorTemp,
                heatingPower = data.getPowerSeries(ChunkClass.heatingTypes()) * 1000,
                dhwPower = data.getPowerSeries(ChunkClass.dhwTypes()) * 1000,
                legionnairesPower = data.getPowerSeries(ChunkClass.legionnairesTypes()) * 1000,
                combinedPower = data.getPowerSeries(ChunkClass.combinedTypes()) * 1000,
                unknownPower = data.getPowerSeries(setOf(ChunkClass.UNKNOWN)) * 1000,
                heatingEnergy = data.consumedEnergyOfType(ChunkClass.heatingTypes()) * 1000,
                dhwEnergy = data.consumedEnergyOfType(ChunkClass.dhwTypes()) * 1000,
                legionnairesEnergy = data.consumedEnergyOfType(ChunkClass.legionnairesTypes()) * 1000,
                combinedEnergy = data.consumedEnergyOfType(ChunkClass.combinedTypes()) * 1000,
                unknownEnergy = data.consumedEnergyOfType(setOf(ChunkClass.UNKNOWN)) * 1000,
            )
        }
    }
}