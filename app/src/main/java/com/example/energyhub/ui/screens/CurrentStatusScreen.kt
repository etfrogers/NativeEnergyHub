package com.example.energyhub.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.example.energyhub.ui.theme.EnergyHubTheme

@Composable
fun CurrentStatusScreen () {
    Column {
        CenterText(text = "Current Status")
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
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = text, fontSize = 32.sp)
    }
}