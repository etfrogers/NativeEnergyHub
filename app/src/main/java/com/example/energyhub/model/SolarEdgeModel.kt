package com.example.energyhub.model

import com.etfrogers.ksolaredge.SolarEdgeApi
import com.etfrogers.ksolaredge.serialisers.Connection
import com.etfrogers.ksolaredge.serialisers.StorageData
import com.etfrogers.ksolaredge.serialisers.Telemetry
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

enum class BatteryChargeState {
    HIGH, MEDIUM, LOW
}

class SolarStatus (
    var solarProduction: Float = 0f,
    var batteryProduction: Float = 0f,
    var gridPower: Float = 0f,
    var isGridExporting: Boolean = false,
    var batteryLevel: Int = 0,
    var load: Float = 0f,
    var batteryState: String = "Charging",
){
    val batteryChargeState: BatteryChargeState
        get() = when {
            batteryLevel > 80 -> BatteryChargeState.HIGH
            batteryLevel > 40 -> BatteryChargeState.MEDIUM
            else -> BatteryChargeState.LOW
        }

    val isBatteryCharging: Boolean
        get() = batteryState == "Charging"

    val loadStatus: EcoState
        get() = getLoadStatusFun()

    private fun getLoadStatusFun(): EcoState {
        val usingGrid = gridPower > 0 && !isGridExporting
        val generatingSolar = solarProduction > 0
        val usingBattery = batteryProduction > 0 && batteryState != "Charging"
        val ecoGeneration = generatingSolar || usingBattery

        return when {
            ecoGeneration && !usingGrid -> EcoState.ECO
            ecoGeneration -> EcoState.MIXED
            else -> EcoState.GRID
        }
    }
}

class SolarEdgeModel(siteID: String, apiKey: String) : BaseModel<SolarStatus>() {
    private val client = SolarEdgeApi(siteID, apiKey)

//    @popup_on_error('SolarEdge', cleanup_function=BaseModel._finish_refresh)
    override suspend fun refreshUnsafe(): SolarStatus {
        val powerFlowData = client.getPowerFlow()
        val conversionFactor = 1000
        if (powerFlowData.unit != "kW") {
            throw NotImplementedError()
        }
        return SolarStatus(
            batteryProduction = powerFlowData.storage.currentPower * conversionFactor,
            batteryLevel = powerFlowData.storage.chargeLevel,
            batteryState = powerFlowData.storage.status,
            solarProduction = powerFlowData.pv.currentPower * conversionFactor,
            gridPower = powerFlowData.grid.currentPower * conversionFactor,
            load = powerFlowData.load.currentPower * conversionFactor,
            isGridExporting = Connection(from = "LOAD", to = "Grid") in powerFlowData.connections,
        )
    }

    suspend fun getBatteryHistoryForDate(date: LocalDate): StorageData {
        return client.getBatteryHistoryForDay(date)
    }

    override suspend fun getHistoryForDateUnsafe(date: LocalDate): SolarHistory {
        val energyData = client.getEnergyForDay(date)
        assert(energyData.unit == "Wh")
        assert(energyData.timeUnit == "DAY")
        val energyMeters = energyData.meters
        assert(energyMeters.timestamps.size == 1)
        assert(energyMeters.consumption!!.size == 1)
        assert(energyMeters.feedIn!!.size == 1)
        assert(energyMeters.purchased!!.size == 1)
        assert(energyMeters.production!!.size == 1)


        val powerData = client.getPowerHistoryForDay(date)
        assert(powerData.unit == "W")
        assert(powerData.timeUnit == "QUARTER_OF_AN_HOUR")
        val powerMeters = powerData.meters
        val n = powerMeters.timestamps.size
        return SolarHistory(
            timestamps = powerMeters.timestamps,
            import = nullToZero(powerMeters.purchased, n),
            consumption = nullToZero(powerMeters.consumption, n),
            generation = nullToZero(powerMeters.production, n),
            export = nullToZero(powerMeters.feedIn, n),
            totalExport = energyMeters.feedIn!![0]!!,
            totalConsumption = energyMeters.consumption!![0]!!,
            totalGeneration = energyMeters.production!![0]!!,
            totalImport = energyMeters.purchased!![0]!!,
            )
    }
}

fun nullToZero(list: List<Float?>?, length: Int): List<Float>{
    return list?.map { it ?: 0f } ?: List(length) { 0f }
}

class SolarHistory(
    timestamps: List<LocalDateTime> = listOf(),
    val export: List<Float> = listOf(),
    val consumption: List<Float> = listOf(),
    val generation: List<Float> = listOf(),
    val import: List<Float> = listOf(),
    val totalExport: Float = 0f,
    val totalImport: Float = 0f,
    val totalConsumption: Float = 0f,
    val totalGeneration: Float = 0f,
): HistoryData(timestamps)

val Telemetry.totalChargeFromGrid: Float
    get() = chargeEnergyFromGrid.sum()
val Telemetry.totalDischarge: Float
    get() = integratePowers(dischargePower, timestamps)
val Telemetry.totalChargeFromSolar: Float
    get() = integratePowers(chargePowerFromSolar, timestamps)

/*
from datetime import datetime
from typing import Dict

import numpy as np
from kivy.clock import mainthread
from kivy.properties import NumericProperty, BooleanProperty, StringProperty, AliasProperty

from .model import BaseModel
from energyhub.utils import popup_on_error, TimestampArray
from solaredge import SolarEdgeClient


class SolarEdgeModel(BaseModel):

    solar_production = NumericProperty(0)
    battery_production = NumericProperty(4)
    grid_power = NumericProperty(0)
    grid_exporting = BooleanProperty(False)
    battery_level = NumericProperty(0.5)
    load = NumericProperty(0.5)
    battery_state = StringProperty('Charging')

    def __init__(self, api_key, site_id, timezone, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.api_key = api_key
        self.site_id = site_id
        self.timezone = timezone

    @popup_on_error('Error initialising SolarEdge')
    def _connect(self):
        self.connection = SolarEdgeClient(self.api_key,
                                          self.site_id,
                                          self.timezone)

    @popup_on_error('SolarEdge', cleanup_function=BaseModel._finish_refresh)
    def _refresh(self):
        power_flow_data = self.connection.get_power_flow()
        self.update_properties(power_flow_data)

    @mainthread
    def _update_properties(self, power_flow_data):
        if power_flow_data['unit'] == 'kW':
            conversion_factor = 1000
        else:
            raise NotImplementedError
        self.battery_production = power_flow_data['STORAGE']['currentPower'] * conversion_factor
        self.battery_level = power_flow_data['STORAGE']['chargeLevel']
        self.battery_state = power_flow_data['STORAGE']['status']
        self.solar_production = power_flow_data['PV']['currentPower'] * conversion_factor
        self.grid_power = power_flow_data['GRID']['currentPower'] * conversion_factor
        self.load = power_flow_data['LOAD']['currentPower'] * conversion_factor
        self.grid_exporting = {'from': 'LOAD', 'to': 'Grid'} in power_flow_data['connections']

    def _get_history_for_date(self, date: datetime.date) -> (np.ndarray, Dict[str, np.ndarray]):
        data = self.connection.get_power_history_for_day(date)
        data['export'] = data.pop('FeedIn')
        timestamps = data.pop('timestamps').view(TimestampArray)
        energy_data = self.connection.get_energy_for_day(date)
        energy_data['export'] = energy_data.pop('FeedIn')
        energy_data = {k+'_energy': v for k, v in energy_data.items()}
        data.update(energy_data)
        data = {k.lower(): v for k, v in data.items()}
        return timestamps, data

    def get_battery_history_for_date(self, date: datetime.date) -> (np.ndarray, Dict[str, np.ndarray]):
        self._run_in_model_thread(self._get_battery_history_for_date, date)

    def _get_battery_history_for_date(self, date: datetime.date) -> (np.ndarray, Dict[str, np.ndarray]):
        data = self.connection.get_battery_history_for_day(date)
        timestamps = data.pop('timestamps').view(TimestampArray)
        return timestamps, data

    def _get_battery_color(self):
        if self.battery_level > 80:
            return 0, 1, 0, 1
        elif self.battery_level > 40:
            return 1, .5, 0, 1
        else:
            return 1, 0, 0, 1

    def _get_load_color(self):
        using_grid = self.grid_power > 0 and not self.grid_exporting
        generating_solar = self.solar_production > 0
        using_battery = self.battery_production > 0 and self.battery_state != 'Charging'
        eco_generation = generating_solar or using_battery
        if eco_generation and not using_grid:
            return 0, 0.8, 0, 1
        elif eco_generation:
            return 0.8, 0.8, 0, 1
        else:
            return 1, 0.5, 0, 1

    battery_color = AliasProperty(
        _get_battery_color,
        bind=['battery_level']
    )

    load_color = AliasProperty(
        _get_load_color,
        bind=['solar_production', 'grid_exporting', 'grid_power', 'battery_production', 'battery_state']
    )

 */