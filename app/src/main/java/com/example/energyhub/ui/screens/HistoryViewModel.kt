package com.example.energyhub.ui.screens

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.etfrogers.ksolaredge.serialisers.Battery
import com.etfrogers.ksolaredge.serialisers.Telemetry
import com.example.energyhub.model.BatteryHistory
import com.example.energyhub.model.Config
import com.example.energyhub.model.EcoForestModel
import com.example.energyhub.model.ErrorType
import com.example.energyhub.model.HeatPumpDay
import com.example.energyhub.model.MyEnergiHistory
import com.example.energyhub.model.MyEnergiModel
import com.example.energyhub.model.OffsetDateTime
import com.example.energyhub.model.Resource
import com.example.energyhub.model.SolarEdgeModel
import com.example.energyhub.model.SolarHistory
import com.example.energyhub.model.div
import com.example.energyhub.model.mapInPlace
import com.example.energyhub.model.plus
import com.example.energyhub.model.minus
import com.example.energyhub.model.toHours
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import kotlin.time.Duration.Companion.days

const val DISPLAY_SCALE_FACTOR = 1000

class HistoryViewModel(
    private val solarModel: SolarEdgeModel,
    private val heatPumpModel: EcoForestModel,
    private val diverterModel: MyEnergiModel,
    private val timezone: TimeZone,
    ): ViewModel() {
    private var date by mutableStateOf(Clock.System.todayIn(Config.location.timezone))
    private val _uiState = MutableStateFlow(
        HistoryUiState(
            timezone=timezone,
            date = date,
        ))
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        getHistory()
    }

    @Suppress("PropertyName")
    @OptIn(ExperimentalMaterial3Api::class)
    val DatesInThePast = object: SelectableDates {

        override fun isSelectableYear(year: Int): Boolean {
            return year <= Clock.System.todayIn(timezone).year
        }

        override fun isSelectableDate(utcTimeMillis: Long): Boolean {
            return utcTimeMillis <= Clock.System.now().plus(1.days).toLocalDateTime(timezone).toInstant(timezone).toEpochMilliseconds()
        }
    }

    fun convertMillisToLocalDate(millis: Long) : LocalDate {
        return Instant
            .fromEpochMilliseconds(millis)
            .toLocalDateTime(timezone)
            .date
    }


    fun setDateTo(newDate: LocalDate?){
        if (newDate == null)
            return
        date = newDate
        getHistory()
    }

    fun incrementDate(){
        setDateTo(date - DatePeriod(days = 1))
    }

    fun decrementDate(){
        setDateTo(date + DatePeriod(days = 1))
    }

    fun goToToday(){
        setDateTo(Clock.System.todayIn(timezone))
    }

    private fun getHistory() {
        // read from this thread to avoid reading in from IO thread
        val date = date
        var solarResource: Resource<SolarHistory>
        var heatPumpResource: Resource<HeatPumpDay>
        var batteryResource: Resource<BatteryHistory>
        var diverterResource: Resource<MyEnergiHistory>
        viewModelScope.launch(context = Dispatchers.IO) {
            val solarDeferred = getSolarHistory(date)
            val hpDeferred = getHeatPumpHistory(date)
            val batteryDeferred = getBatteryHistory(date)
            val diverterDeferred = getDiverterHistory(date)

            solarResource = solarDeferred.await()
            heatPumpResource = hpDeferred.await()
            batteryResource = batteryDeferred.await()
            diverterResource = diverterDeferred.await()
            buildStatus(solarResource, heatPumpResource, batteryResource, diverterResource)
        }
    }

    @Suppress("ConvertArgumentToSet")
    private fun buildStatus(
        solarResource: Resource<SolarHistory>,
        heatPumpResource: Resource<HeatPumpDay>,
        batteryResource: Resource<BatteryHistory>,
        diverterResource: Resource<MyEnergiHistory>,
    ) {
        val errors = listOf(
            solarResource,
            heatPumpResource,
            batteryResource,
            diverterResource
        ).mapNotNull {
            if (it is Resource.Error) it.error else null
        }
        if (solarResource is Resource.Error) {
            _uiState.update { currentState ->
                currentState.copy(
                    errors = errors,
                )
            }
            return
        }
        val solar = (solarResource as Resource.Success<SolarHistory>).data
        val heatPump = (heatPumpResource as? Resource.Success<HeatPumpDay>)?.data ?: HeatPumpDay()
        val battery = (batteryResource as? Resource.Success<BatteryHistory>)?.data ?: BatteryHistory()
        val diverter = (diverterResource as? Resource.Success<MyEnergiHistory>)?.data ?: MyEnergiHistory()
        val generationPower = solar.generation
        val consumptionPower = solar.consumption
        val exportPower = solar.export
        val importPower = solar.import

        val refTimestamps = solar.timestamps

        val carChargePower = normaliseToTimestamps(
            refTimestamps, diverter.zappi.timestamps, diverter.zappi.totalPower)
        val immersionPower = normaliseToTimestamps(
            refTimestamps, diverter.eddi.timestamps, diverter.eddi.totalPower)
        val dhwPower = normaliseToTimestamps(refTimestamps, heatPump.timestamps, heatPump.dhwPower)
        val heatingPower = normaliseToTimestamps(
            refTimestamps, heatPump.timestamps,
            heatPump.heatingPower
        )
        val legionnairesPower = normaliseToTimestamps(
            refTimestamps, heatPump.timestamps,
            heatPump.legionnairesPower
        )
        val combinedPower = normaliseToTimestamps(
            refTimestamps, heatPump.timestamps,
            heatPump.combinedPower
        )
        val unknownHeatPumpPower = normaliseToTimestamps(
            refTimestamps, heatPump.timestamps,
            heatPump.unknownPower
        )
        val batteryGridCharging = normaliseToTimestamps(
            refTimestamps, battery.timestamps,
            battery.chargePowerFromGrid
        )
        val batterySolarCharging = normaliseToTimestamps(
            refTimestamps, battery.timestamps,
            battery.chargePowerFromSolar
        )
        val batteryDischarging = normaliseToTimestamps(
            refTimestamps, battery.timestamps,
            battery.dischargePower
        )
        val batteryStateFull = normaliseToTimestamps(
            refTimestamps, battery.timestamps,
            battery.chargePercentage
        )
        val batteryState = batteryStateFull.zip(refTimestamps).mapNotNull {
            (value, ts) ->  if (ts <= battery.timestamps.last()) value else null}


        val otherConsumption = consumptionPower - (carChargePower + immersionPower +
                dhwPower + heatingPower
                + legionnairesPower + combinedPower + unknownHeatPumpPower
                + batteryGridCharging)

        val solarProduction = generationPower + batterySolarCharging - batteryDischarging
        val solarConsumption = solarProduction - (exportPower + batterySolarCharging)

        val netBatteryChargeFromGrid = battery.storedEnergy.last() - battery.storedEnergy.first()
        var totalConsumption = if (battery.totalChargeFromGrid > 0f) {
            solar.totalConsumption + netBatteryChargeFromGrid
        }
        else {
            solar.totalConsumption
        }
        val batteryDischargeEnergy = battery.totalDischarge
        val solarProductionEnergy = solar.totalGeneration
        val exportEnergy = solar.totalExport
        val batterySolarChargingEnergy = battery.totalChargeFromSolar
        val solarConsumptionEnergy = (solarProductionEnergy
                                    - (exportEnergy + batterySolarChargingEnergy))

        val remainingEnergy = (solar.totalConsumption
                            - (heatPump.heatingEnergy
                               + heatPump.dhwEnergy
                               + heatPump.legionnairesEnergy
                               + heatPump.combinedEnergy
                               + heatPump.unknownEnergy
                               + diverter.zappi.totalEnergy
                               + diverter.eddi.totalEnergy
                               )
                            )

        _uiState.update { currentState ->
            currentState.copy(
                timestamps = refTimestamps,
                totalSelfConsumptionEnergy = solarConsumptionEnergy / DISPLAY_SCALE_FACTOR,
                netBatteryChargeFromSolar = batterySolarChargingEnergy / DISPLAY_SCALE_FACTOR,
                netBatteryChargeFromGrid = netBatteryChargeFromGrid / DISPLAY_SCALE_FACTOR,
                totalExport = solar.totalExport / DISPLAY_SCALE_FACTOR,
                batteryPercentage = batteryState,
                totalBatteryDischargeEnergy = batteryDischargeEnergy / DISPLAY_SCALE_FACTOR,
                totalImport = solar.totalImport / DISPLAY_SCALE_FACTOR,
                dhwEnergy = heatPump.dhwEnergy / DISPLAY_SCALE_FACTOR,
                heatingEnergy = heatPump.heatingEnergy / DISPLAY_SCALE_FACTOR,
                legionnairesEnergy = heatPump.legionnairesEnergy / DISPLAY_SCALE_FACTOR,
                combinedHPEnergy = heatPump.combinedEnergy / DISPLAY_SCALE_FACTOR,
                remainingEnergy = remainingEnergy / DISPLAY_SCALE_FACTOR,
                timezone = timezone,
            )
        }

    }

    private fun getSolarHistory(date: LocalDate): Deferred<Resource<SolarHistory>> {
        return viewModelScope.async(context = Dispatchers.IO) {
            solarModel.getHistoryForDate(date)
        }
    }

    private fun getHeatPumpHistory(date: LocalDate): Deferred<Resource<HeatPumpDay>> {
        return viewModelScope.async(context = Dispatchers.IO) {
            heatPumpModel.getHistoryForDate(date)
        }
    }

    private fun getBatteryHistory(date: LocalDate): Deferred<Resource<BatteryHistory>> {
        return viewModelScope.async(context = Dispatchers.IO) {
            solarModel.getBatteryHistoryForDate(date)
        }
    }

    private fun getDiverterHistory(date: LocalDate): Deferred<Resource<MyEnergiHistory>> {
        return viewModelScope.async(context = Dispatchers.IO) {
            diverterModel.getHistoryForDate(date)
        }
    }

    fun clearErrors(){
        _uiState.update { currentState ->
            currentState.errors = listOf()
            currentState
        }
    }
}

enum class NormalisationMode {
    MIDPOINT,
    PRECEDING,
    FOLLOWING
}

private fun normaliseToTimestamps(
    refTimestamps: List<OffsetDateTime>,
    dataTimestamps: List<OffsetDateTime>,
    data: List<Float>,
    mode: NormalisationMode = NormalisationMode.PRECEDING,
    ): List<Float> {
    val refHours = refTimestamps.toHours()
    val dataHours = dataTimestamps.toHours()
    val binEdges = when (mode) {
        NormalisationMode.MIDPOINT -> refHours.drop(1).zip(refHours.dropLast(1)).map { (z, e) -> (z+e)/2 }
        NormalisationMode.PRECEDING -> refHours.drop(1)
        NormalisationMode.FOLLOWING -> refHours.dropLast(1)
    }
    // 0 and max(data) are implicit bin_edges in np.digitize
    val binIndices = digitize(dataHours, binEdges)
    val countsPerBin = bincount(binIndices, minLength=refTimestamps.size)
    val totalDataInBin = bincount(binIndices, weights=data, minLength=refTimestamps.size)
    val meanDataInBin = (totalDataInBin / countsPerBin).toMutableList()
    meanDataInBin.mapInPlace { if(it.isNaN()) 0f else it }
    return meanDataInBin
}

fun digitize(data: List<Float>, binEdges: List<Float>): List<Int> {
    val indices = Array(data.size) {0}
    var edgeIndex = 0
    for (i in data.indices){
        while (edgeIndex < binEdges.size && data[i] > binEdges[edgeIndex])
        {
            edgeIndex++
        }
        indices[i] = edgeIndex
    }
    return indices.toList()
}

private fun bincount(binIndices: List<Int>, minLength: Int? = null): List<Int> {

    val counts = Array(minLength?: binIndices.max()) { 0 }
    binIndices.forEach { counts[it] += 1 }
    return counts.toList()
}

private fun bincount(binIndices: List<Int>, weights: List<Float>,  minLength: Int? = null): List<Float> {
    val counts = Array(minLength?: binIndices.max()) { 0f }
    binIndices.forEachIndexed { origIndex, binIndex -> counts[binIndex] += weights[origIndex] }
    return counts.toList()
}

@Suppress("FunctionName")
fun EmptyBattery() = Battery("", 0f, "", 0, Telemetry())

data class HistoryUiState(
    val timestamps: List<OffsetDateTime> = listOf(),
    val totalSelfConsumptionEnergy: Float = 0f,
    val netBatteryChargeFromSolar: Float = 0f,
    val totalBatteryDischargeEnergy: Float = 0f,
    val netBatteryChargeFromGrid: Float = 0f,
    val totalExport: Float = 0f,
    val totalImport: Float = 0f,

    val zappiEnergy: Float = 0f,
    val eddiEnergy: Float = 0f,

    val dhwEnergy: Float = 0f,
    val heatingEnergy: Float = 0f,
    val legionnairesEnergy: Float = 0f,
    val combinedHPEnergy: Float = 0f,
    val remainingEnergy: Float = 0f,


    val batteryPercentage: List<Float> = listOf(),

    var errors: List<ErrorType> = listOf(),

    val timezone: TimeZone,
    val date: LocalDate,
//    val solarResource: Resource<SolarHistory> = Resource.Success(SolarHistory()),
//    val batteryResource: Resource<Battery> = Resource.Success(EmptyBattery())
){
    val fractionalHours: List<Float> by lazy { timestamps.toHours() }

//    val solar: SolarHistory = dataOrEmpty(solarResource, ::SolarHistory)
//    val battery: Battery = dataOrEmpty(batteryResource, ::EmptyBattery)
}