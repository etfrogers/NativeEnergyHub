package com.example.energyhub.ui.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.energyhub.model.SystemModel
import com.example.energyhub.ui.screens.CurrentStatusScreen
import com.example.energyhub.ui.screens.HistoryScreen
import com.example.energyhub.ui.screens.PlanningScreen
import com.example.energyhub.ui.screens.StatusViewModel


@Composable
fun Navigation(
    navController: NavHostController
) {
    NavHost(navController, startDestination = NavigationItem.CurrentStatus.route) {
        composable(NavigationItem.CurrentStatus.route) {
            CurrentStatusScreen(
                StatusViewModel(
                    solarModel = SystemModel.solarEdgeModel,
                    heatPumpModel = SystemModel.ecoForestModel,
                    diverterModel = SystemModel.myEnergiModel,
                )
            )
        }
        composable(NavigationItem.History.route) {
            HistoryScreen()
        }
        composable(NavigationItem.Planning.route) {
            PlanningScreen()
        }
    }
}