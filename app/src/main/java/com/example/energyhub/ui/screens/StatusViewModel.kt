package com.example.energyhub.ui.screens
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.etfrogers.ecoforestklient.EcoforestStatus
import com.etfrogers.ecoforestklient.UnitValue
import com.etfrogers.myenergiklient.MyEnergiSystem
import com.example.energyhub.model.EcoForestModel
import com.example.energyhub.model.MyEnergiModel
import com.example.energyhub.model.Resource
import com.example.energyhub.model.SolarEdgeModel
import com.example.energyhub.model.SolarStatus
import com.example.energyhub.model.carPower
import com.example.energyhub.model.emptySystem
import com.example.energyhub.model.immersionPower
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal const val TAG = "StatusViewModel"

class StatusViewModel(
    private val solarModel: SolarEdgeModel,
    private val heatPumpModel: EcoForestModel,
    private val diverterModel: MyEnergiModel,
): ViewModel() {
    private val _uiState = MutableStateFlow(StatusUiState())
    val uiState: StateFlow<StatusUiState> = _uiState.asStateFlow()
    var isRefreshing by mutableStateOf( false)
        private set

    init {
        Log.d(TAG, "Initialising...")
        refresh()
    }

    fun refresh(){
        Log.d(TAG, "Starting refresh")
        isRefreshing = true
        viewModelScope.launch(context = Dispatchers.IO) {
            try {
                val solarStatus = async { solarModel.refresh() }
                val heatPumpStatus = async { heatPumpModel.refresh() }
                val diverterStatus = async { diverterModel.refresh() }
                _uiState.update { currentState ->
                    currentState.copy(
                        solarResource = solarStatus.await(),
                        heatPumpResource = heatPumpStatus.await(),
                        diverterResource = diverterStatus.await(),
                    )
                }
            } finally {
                Log.d(TAG, "Finishing refresh")
                isRefreshing = false
            }
        }
//        refreshSolar()
//        refreshHeatPump()
    }

    private fun refreshSolar(): Job {
        return viewModelScope.launch(context = Dispatchers.IO) {
            val status = solarModel.refresh()
            _uiState.update { currentState ->
                currentState.copy(
                    solarResource = status
                )
            }
        }
    }

    private fun refreshHeatPump() {
        viewModelScope.launch(context = Dispatchers.IO) {
            val status = heatPumpModel.refresh()
//            _uiState.value.heatPump = status
            _uiState.update { currentState ->
                currentState.copy(
                    heatPumpResource = status
                )
            }
        }
    }

    fun clearErrors(){
        _uiState.update { currentState ->
            currentState.errors = listOf()
            currentState
        }
    }
}

val EcoforestStatus.dhwPower: UnitValue<Float>
    get() {
        var value = 0f
        if (isDHWDemand) {
            value = electricalPower.value.toFloat()
        }
        return UnitValue(value, electricalPower.unit)
    }

val EcoforestStatus.heatingPower: UnitValue<Float>
    get() {
        var value = 0f
        if (!isDHWDemand && isHeatingDemand) {
            value = electricalPower.value.toFloat()
        }
        return UnitValue(value, electricalPower.unit)
    }


data class StatusUiState(
    val solarResource: Resource<SolarStatus> = Resource.Success(SolarStatus()),
    val heatPumpResource: Resource<EcoforestStatus> = Resource.Success(EcoforestStatus()),
    val diverterResource: Resource<MyEnergiSystem> = Resource.Success(MyEnergiSystem()),
) {
    val solar = dataOrEmpty(solarResource, ::SolarStatus)
    val heatPump = dataOrEmpty(heatPumpResource, ::EcoforestStatus)
    val diverter = dataOrEmpty(diverterResource, ::emptySystem)

    var errors = listOf(
        solarResource,
        heatPumpResource,
        diverterResource).mapNotNull {
            if (it is Resource.Error) it.error else null
        }

    val remainingPower: Float
        get() = solar.load - heatPump.dhwPower.value - bottomArmsPower

    val bottomArmsPower: Float
        get() = diverter.immersionPower + diverter.carPower + heatPump.heatingPower.value

    private fun <T> dataOrEmpty(resource: Resource<T>, emptyMaker:()->T): T {
        return if (resource is Resource.Success){
            resource.data
        } else {
            emptyMaker()
        }
    }
}

