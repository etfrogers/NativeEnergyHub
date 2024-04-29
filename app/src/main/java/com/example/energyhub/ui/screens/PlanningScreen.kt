package com.example.energyhub.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.energyhub.ui.theme.EnergyHubTheme

@Composable
fun PlanningScreen () {
    Column {
        CenterText(text = "Planning")
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewPlanning() {
    EnergyHubTheme {
        PlanningScreen()
    }
}