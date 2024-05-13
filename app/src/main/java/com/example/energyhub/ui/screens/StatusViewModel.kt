package com.example.energyhub.ui.screens
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.etfrogers.ksolaredge.serialisers.SitePowerFlow
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
            val powers = seModel.getCurrentPowerFlow()
            _uiState.update { currentState ->
                currentState.copy(powerFlow = powers)
            }
        }
    }
}

data class StatusUiState(
    val powerFlow: SitePowerFlow? = null
)

