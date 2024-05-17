package com.example.energyhub.ui.screens
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.energyhub.model.BatteryChargeState
import com.example.energyhub.model.EcoState
import com.example.energyhub.model.SolarEdgeModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StatusViewModel(
    private val seModel: SolarEdgeModel
): ViewModel() {
    private val _uiState = MutableStateFlow(StatusUiState())
    val uiState: StateFlow<StatusUiState> = _uiState.asStateFlow()

    fun refresh(){
        getCurrentPower()
    }

    private fun getCurrentPower() {
        viewModelScope.launch {
            val status = seModel.refresh()
            _uiState.update { currentState ->
                currentState.copy(
                    solarProduction = status.solarProduction,
                    batteryProduction = status.batteryProduction,
                    gridPower = status.gridPower,
                    isGridExporting = status.isGridExporting,
                    batteryLevel = status.batteryLevel,
                    load = status.load,
                    isBatteryCharging = status.isBatteryCharging,
                    batteryChargeState = status.batteryChargeState,
                    loadStatus = status.loadStatus,
                )
            }
        }
    }
}

data class StatusUiState(
    var solarProduction: Float = 0f,
    var batteryProduction: Float = 0f,
    var gridPower: Float = 0f,
    var isGridExporting: Boolean = false,
    var batteryLevel: Int = 0,
    var load: Float = 0f,
    var isBatteryCharging: Boolean = false,
    var batteryChargeState: BatteryChargeState = BatteryChargeState.HIGH,
    var loadStatus: EcoState = EcoState.MIXED,
)

