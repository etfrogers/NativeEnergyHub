package com.example.energyhub.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.energyhub.ui.theme.EnergyHubTheme

@Composable
fun HistoryScreen (
    modifier: Modifier = Modifier,
    historyViewModel: HistoryViewModel = viewModel(factory = ViewModelFactory.HistoryFactory)
) {
    val uiState by historyViewModel.uiState.collectAsState()
    Column {
        CenterText(text = "Total Generation ${uiState.solar.totalConsumption}")
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewHistory() {
    EnergyHubTheme {
        HistoryScreen()
    }
}