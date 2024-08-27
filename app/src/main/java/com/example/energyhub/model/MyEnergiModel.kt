package com.example.energyhub.model

import com.etfrogers.myenergiklient.DetailHistory
import com.etfrogers.myenergiklient.Eddi
import com.etfrogers.myenergiklient.HourData
import com.etfrogers.myenergiklient.MyEnergiClient
import com.etfrogers.myenergiklient.MyEnergiSystem
import com.etfrogers.myenergiklient.Zappi
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class MyEnergiModel(
    username: String,
    password: String,
    invalidSerials: List<String> = listOf(),
    val timezone: TimeZone,
): BaseModel<MyEnergiSystem, MyEnergiHistory>() {

    private lateinit var zappiSerial: String
    private lateinit var eddiSerial: String
    private val client: MyEnergiClient = MyEnergiClient(
        username,
        password,
        invalidSerials,
        timezone)

    override suspend fun refreshUnsafe(): MyEnergiSystem {
        val status = client.getCurrentStatus()
        zappiSerial = status.zappi.serialNumber
        eddiSerial = status.eddi.serialNumber
        return status
    }

    override suspend fun getHistoryForDateUnsafe(date: LocalDate): MyEnergiHistory {

        val zappiData = client.getMinuteData(zappiSerial, date)
        val zappiHourData = client.getHourData(zappiSerial, date)
        val eddiData = client.getMinuteData(eddiSerial, date)
        val eddiHourData = client.getHourData(eddiSerial, date)
        return MyEnergiHistory(
            zappi = MyEnergiDeviceHistory.fromMinuteHourData(zappiData, zappiHourData, timezone),
            eddi = MyEnergiDeviceHistory.fromMinuteHourData(eddiData, eddiHourData, timezone)
        )
    }
}

fun DetailHistory.meanVoltagePerHour(): Map<Int, Double> {
    return (0..24).associateWith { hour ->
        this.voltage.filterIndexed { i, _ ->
            this.timestamps[i].toLocalDateTime(TimeZone.UTC).hour == hour
        }.average()
    }
}

private data class EnergyData(
    val importEnergy: Float,
    val exportEnergy: Float,
    val divertedEnergy: Float,
    val boostEnergy: Float,
){
    val totalEnergy: Float
        get() = divertedEnergy + boostEnergy
}

private fun HourData.toEnergies(meanVoltagePerHour: Map<Int, Double>): EnergyData {
    fun toEnergy(data: List<Int>): Float {
        return data.sum().toFloat() / (60*60)
    }
    return EnergyData(
        importEnergy = toEnergy(importReading),
        exportEnergy = toEnergy(exportReading),
        divertedEnergy = toEnergy(divertReading),
        boostEnergy = toEnergy(boostReading),

    )
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

internal fun emptyZappi() = Zappi(pStatusCode = "A")

fun emptySystem() = MyEnergiSystem(zappis = listOf(emptyZappi()))


data class MyEnergiHistory(
    val zappi: MyEnergiDeviceHistory = MyEnergiDeviceHistory(),
    val eddi: MyEnergiDeviceHistory = MyEnergiDeviceHistory(),
)

data class MyEnergiDeviceHistory(
    val timestamps: List<Instant> = listOf(),
    val voltage: List<Float> = listOf(),
    val frequency: List<Float> = listOf(),
    val importPower: List<Float> = listOf(),
    val exportPower: List<Float> = listOf(),
    val divertPower: List<Float> = listOf(),
    val boostPower: List<Float> = listOf(),
    val totalPower: List<Float> = listOf(),
    val importEnergy: Float = 0f,
    val exportEnergy: Float = 0f,
    val divertedEnergy: Float = 0f,
    val boostEnergy: Float = 0f,
    val totalEnergy: Float = 0f,
){
    companion object {
        fun fromMinuteHourData(data: DetailHistory, hourData: HourData, timezone: TimeZone): MyEnergiDeviceHistory {
            val energyData = hourData.toEnergies(data.meanVoltagePerHour())
            return MyEnergiDeviceHistory(
                timestamps = data.timestamps,
                voltage = data.voltage,
                frequency = data.frequency,
                importPower = data.importPower,
                exportPower = data.exportPower,
                divertPower = data.divertPower,
                boostPower = data.divertPower,
                totalPower = data.totalPower,
                importEnergy = energyData.importEnergy,
                exportEnergy = energyData.exportEnergy,
                divertedEnergy = energyData.divertedEnergy,
                boostEnergy = energyData.boostEnergy,
                totalEnergy = energyData.totalEnergy,
            )
        }
    }
}