package com.example.energyhub.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.energyhub.model.Config
import com.example.energyhub.model.EcoForestModel
import com.example.energyhub.model.MyEnergiModel
import com.example.energyhub.model.Resource
import com.example.energyhub.model.SolarEdgeModel
import com.example.energyhub.model.SolarHistory
import com.example.energyhub.model.dataOrEmpty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.todayIn

class HistoryViewModel(
    private val solarModel: SolarEdgeModel,
    private val heatPumpModel: EcoForestModel,
    private val diverterModel: MyEnergiModel,
    ): ViewModel() {
    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()
    var date by mutableStateOf(Clock.System.todayIn(Config.location.timezone))

    init {
        getHistory()
    }

    private fun getHistory(){
        // read from this thread to avoid reading in from IO thread
        val date = date
        viewModelScope.launch(context = Dispatchers.IO) {
            try {
                val j1 = refreshSolarHistory(date)
//                val j2 = refreshHeatPump()
//                val j3 = refreshDiverter()
                j1.join()
//                j2.join()
//                j3.join()
            } finally {
//                Log.d(TAG, "Finishing refresh")
//                isRefreshing = false
            }
        }
    }

    private fun refreshSolarHistory(date: LocalDate): Job {
        return viewModelScope.launch(context = Dispatchers.IO) {
            val status = solarModel.getHistoryForDate(date)
            _uiState.update { currentState ->
                @Suppress("UNCHECKED_CAST")
                currentState.copy(
                    solarResource = status as Resource<SolarHistory>,
//                    isSolarStale = status is Resource.Error,
                )
            }
        }
    }
}

data class HistoryUiState(
    val solarResource: Resource<SolarHistory> = Resource.Success(SolarHistory())
){
    val solar: SolarHistory = dataOrEmpty(solarResource, ::SolarHistory)
}