package com.example.energyhub.ui.screens
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.etfrogers.ecoforestklient.EcoforestStatus
import com.etfrogers.ecoforestklient.UnitValue
import com.example.energyhub.model.EcoForestModel
import com.example.energyhub.model.SolarEdgeModel
import com.example.energyhub.model.SolarStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StatusViewModel(
    private val solarModel: SolarEdgeModel,
    private val heatPumpModel: EcoForestModel,
): ViewModel() {
    private val _uiState = MutableStateFlow(StatusUiState())
    val uiState: StateFlow<StatusUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh(){
        viewModelScope.launch(context = Dispatchers.IO) {
            val solarStatus = async { solarModel.refresh() }
            val heatPumpStatus = async { heatPumpModel.refresh() }
            _uiState.update { currentState ->
                currentState.copy(
                    solar = solarStatus.await(),
                    heatPump = heatPumpStatus.await(),
                )
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
                    solar = status
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
                    heatPump = status
                )
            }
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
    val solar: SolarStatus = SolarStatus(),
    val heatPump: EcoforestStatus = EcoforestStatus(),
) {
    val remainingPower: Float
        get() = solar.load

    val bottomArmsPower: Float
        get() = 0f
}

