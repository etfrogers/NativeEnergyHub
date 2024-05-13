package com.example.energyhub.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.example.energyhub.model.SystemModel
import com.example.energyhub.ui.theme.EnergyHubTheme

@Composable
fun CurrentStatusScreen (
    statusViewModel: StatusViewModel = StatusViewModel(SystemModel.solarEdgeModel)
) {
    val statusUiState = statusViewModel.uiState.collectAsState()
    statusViewModel.refresh()
    Column {
        CenterText(text = "Current Status")
        CenterText(text = "")
        CenterText(text = statusUiState.value.powerFlow.toString())
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewCurrentStatus() {
    EnergyHubTheme {
        CurrentStatusScreen()
    }
}

@Composable
fun CenterText(text: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = text, fontSize = 32.sp)
    }
}