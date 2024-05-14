package com.example.energyhub.ui.screens
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.etfrogers.ksolaredge.serialisers.SitePowerFlow
import com.example.energyhub.R
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
            seModel.refresh()
            _uiState.update { currentState ->
                currentState.copy(
                    solarProduction = seModel.status.solarProduction,
                    batteryProduction = seModel.status.batteryProduction,
                    gridPower = seModel.status.gridPower,
                    isGridExporting = seModel.status.isGridExporting,
                    batteryLevel = seModel.status.batteryLevel,
                    load = seModel.status.load,
                    isBatteryCharging = seModel.status.isBatteryCharging,
                    batteryChargeState = seModel.status.batteryChargeState,
                    loadStatus = seModel.status.loadStatus,
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

