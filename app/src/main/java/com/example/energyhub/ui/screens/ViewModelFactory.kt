package com.example.energyhub.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.energyhub.model.SystemModel

object ViewModelFactory {
    val StatusFactory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            modelClass: Class<T>,
            extras: CreationExtras
        ): T {
            return StatusViewModel(
                solarModel = SystemModel.solarEdgeModel,
                heatPumpModel = SystemModel.ecoForestModel,
                diverterModel = SystemModel.myEnergiModel,
            ) as T
        }
    }

    val HistoryFactory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            modelClass: Class<T>,
            extras: CreationExtras
        ): T {
            return HistoryViewModel(
                solarModel = SystemModel.solarEdgeModel,
                heatPumpModel = SystemModel.ecoForestModel,
                diverterModel = SystemModel.myEnergiModel,
            ) as T
        }
    }
}