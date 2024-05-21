package com.example.energyhub.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.etfrogers.ecoforestklient.EcoforestStatus
import com.etfrogers.ecoforestklient.UnitValue
import com.example.energyhub.R
import com.example.energyhub.model.BatteryChargeState
import com.example.energyhub.model.EcoState
import com.example.energyhub.model.SolarStatus
import com.example.energyhub.model.SystemModel
import com.example.energyhub.ui.LabelledArrow
import com.example.energyhub.ui.PercentLabel
import com.example.energyhub.ui.PowerArrow
import com.example.energyhub.ui.PowerLabel
import com.example.energyhub.ui.UnitLabel
import com.example.energyhub.ui.theme.EnergyHubTheme

@Composable
fun CurrentStatusScreen(
    statusViewModel: StatusViewModel = StatusViewModel(
        solarModel = SystemModel.solarEdgeModel,
        heatPumpModel = SystemModel.ecoForestModel,
        )
){
    val statusUiState = statusViewModel.uiState.collectAsState()
    var firstRun by remember { mutableStateOf(true) }
    if (firstRun) {
        statusViewModel.refresh()
        firstRun = false
    }
    CurrentStatusLayout(statusUiState.value)
}

@Composable
fun CurrentStatusLayout (
    statusUiState: StatusUiState
) {
    Surface(modifier = Modifier.fillMaxSize()) {

        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
        ) {
            val armLength = 90.dp
            val center = createGuidelineFromStart(0.5f)
            val (solarLabel, solar, solarArrow, battery, batteryLabel, batteryArrow) = createRefs()
            val (grid, gridArrow, home, homeArrow) = createRefs()
            val (loadArrow, load) = createRefs()
            val (bottomArmsArrow, fromHomeArrow) = createRefs()
            val (heatingArrow, heating, heatingLabel, dhwArrow, dhw, dhwLabel) = createRefs()
            val (immersion, immersionArrow, car, carArrow) = createRefs()
            val ecoColor = colorResource(id = R.color.eco)
            val nonEcoColor = colorResource(id = R.color.non_eco)
            val loadColor = when (statusUiState.solar.loadStatus) {
                EcoState.ECO -> ecoColor
                EcoState.GRID -> nonEcoColor
                EcoState.MIXED -> colorResource(id = R.color.mixed)
            }
            val colorMatrix = if (isSystemInDarkTheme()) {
             floatArrayOf(
                -1f, 0f, 0f, 0f, 255f,
                0f, -1f, 0f, 0f, 255f,
                0f, 0f, -1f, 0f, 255f,
                0f, 0f, 0f, .5f, 0f
            ) } else {floatArrayOf(
                    1f, 0f, 0f, 0f, 0f,
                    0f, 1f, 0f, 0f, 0f,
                    0f, 0f, 1f, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f
                )}
            PowerLabel(
                power = statusUiState.solar.solarProduction,
                modifier = Modifier.constrainAs(solarLabel) {
                    top.linkTo(parent.top, margin = 12.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
            )
            val colorFilter = ColorFilter.colorMatrix(ColorMatrix(colorMatrix))
            Image(
                painter = painterResource(id = R.drawable.solar_panel),
                contentDescription = "Solar Panels",
                colorFilter = colorFilter,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .height(60.dp)
                    .width(150.dp)
                    .constrainAs(solar) {
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        top.linkTo(solarLabel.bottom, margin = 8.dp)
                    }
            )
            PowerArrow(
                angle = 90f,
                length = 75.dp,
                activeColor = colorResource(id = R.color.eco),
                power = statusUiState.solar.solarProduction,
                modifier = Modifier.constrainAs(solarArrow) {
                    start.linkTo(center)
                    top.linkTo(solar.bottom)
                }
            )
            Battery(
                chargeLevel = statusUiState.solar.batteryChargeState,
                charge = statusUiState.solar.batteryLevel,
                widthDp = 40.dp,
                heightDp = 100.dp,
                modifier = Modifier.constrainAs(battery) {
                    start.linkTo(parent.start)
                    end.linkTo(batteryArrow.start)
                    top.linkTo(batteryArrow.bottom)
                    bottom.linkTo(batteryArrow.bottom)
                }
            )
            PercentLabel(
                value = statusUiState.solar.batteryLevel.toFloat(),
                modifier = Modifier.constrainAs(batteryLabel) {
                    start.linkTo(battery.start)
                    end.linkTo(battery.end)
                    bottom.linkTo(battery.top, margin = 16.dp)
                })
            LabelledArrow(
                angle = 0f,
                length = armLength,
                power = statusUiState.solar.batteryProduction,
                activeColor = if (statusUiState.solar.solarProduction > 0)
                    ecoColor
                else nonEcoColor,
                reverseArrow = statusUiState.solar.isBatteryCharging,
                modifier = Modifier.constrainAs(batteryArrow) {
                    bottom.linkTo(solarArrow.bottom)
                    end.linkTo(solarArrow.start)
                }
            )
            LabelledArrow(
                angle = 0f,
                length = armLength,
                power = statusUiState.solar.gridPower,
                activeColor = if (statusUiState.solar.isGridExporting) ecoColor else nonEcoColor,
                reverseArrow = !statusUiState.solar.isGridExporting,
                modifier = Modifier.constrainAs(gridArrow) {
                    bottom.linkTo(solarArrow.bottom)
                    start.linkTo(solarArrow.start)
                },
            )
            Image(
                painter = painterResource(id = R.drawable.pylon256),
                contentDescription = "grid",
                colorFilter = colorFilter,
                modifier = Modifier
                    .height(80.dp)
                    .constrainAs(grid) {
                        end.linkTo(parent.end)
                        start.linkTo(gridArrow.end)
                        top.linkTo(batteryArrow.bottom)
                        bottom.linkTo(batteryArrow.bottom)
                    }
            )
            LabelledArrow(
                angle = 90f,
                length = 70.dp,
                power = statusUiState.solar.load,
                activeColor = loadColor,
                modifier = Modifier.constrainAs(homeArrow) {
                    start.linkTo(solarArrow.start)
                    top.linkTo(solarArrow.bottom)
                }
            )
            Image(
                painter = painterResource(id = R.drawable.house),
                contentDescription = "home",
                colorFilter = colorFilter,
                modifier = Modifier
                    .height(80.dp)
                    .constrainAs(home) {
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        top.linkTo(homeArrow.bottom)
                    }
            )
            PowerArrow(
                angle = 90f,
                length = 70.dp,
                power = statusUiState.solar.load,
                activeColor = loadColor,
                modifier = Modifier.constrainAs(fromHomeArrow){
                    start.linkTo(homeArrow.start)
                    top.linkTo(home.bottom)
                }
            )
            LabelledArrow(
                angle = 0f,
                length = 80.dp,
                power = statusUiState.remainingPower,
                activeColor = loadColor,
                modifier = Modifier.constrainAs(loadArrow){
                    bottom.linkTo(fromHomeArrow.bottom)
                    start.linkTo(fromHomeArrow.end)
                }
            )
            Image(
                painter = painterResource(id = R.drawable.light_bulb_2_256),
                contentDescription = "Light bulb",
                colorFilter = colorFilter,
                modifier = Modifier.constrainAs(load){
                    top.linkTo(loadArrow.bottom)
                    bottom.linkTo(loadArrow.bottom)
                    start.linkTo(loadArrow.end)
                    end.linkTo(parent.end)
                }
            )
            LabelledArrow(
                angle = 180f,
                length = armLength,
                power = statusUiState.heatPump.dhwPower.value,
                activeColor = loadColor,
                modifier = Modifier.constrainAs(dhwArrow){
                    bottom.linkTo(fromHomeArrow.bottom)
                    end.linkTo(fromHomeArrow.end)
                }
            )
            Image(
                painter = painterResource(id = R.drawable.shower),
                contentDescription = "Hot Water",
                colorFilter = colorFilter,
                modifier = Modifier
                    .width(70.dp)
                    .constrainAs(dhw) {
                        top.linkTo(dhwArrow.bottom)
                        bottom.linkTo(dhwArrow.bottom)
                        end.linkTo(dhwArrow.start)
                        start.linkTo(parent.start)
                    }
            )
            UnitLabel(
                value = statusUiState.heatPump.dhwActualTemp,
                setPoint = statusUiState.heatPump.dhwSetpoint,
                offset = statusUiState.heatPump.dhwOffset,
                decimalPlaces = 1,
                modifier = Modifier.constrainAs(dhwLabel){
                    start.linkTo(dhw.start)
                    end.linkTo(dhw.end, margin = (-24).dp)
                    top.linkTo(dhw.bottom)
                }
            )
            PowerArrow(
                angle = 90f,
                length = 110.dp,
                power = statusUiState.bottomArmsPower,
                activeColor = loadColor,
                modifier = Modifier.constrainAs(bottomArmsArrow){
                    top.linkTo(fromHomeArrow.bottom)
                    start.linkTo(fromHomeArrow.start)
                }
            )
            LabelledArrow(
                angle = 180f,
                length = armLength,
                power = statusUiState.heatPump.heatingPower.value,
                activeColor = loadColor,
                modifier = Modifier.constrainAs(heatingArrow){
                    bottom.linkTo(bottomArmsArrow.bottom)
                    end.linkTo(bottomArmsArrow.end)
                }
            )
            Image(
                painter = painterResource(id = R.drawable.radiator),
                contentDescription = "Heating",
                alpha = if(statusUiState.heatPump.isHeatingOn) 1f else .5f,
                colorFilter = colorFilter,
                modifier = Modifier
                    .width(70.dp)
                    .constrainAs(heating) {
                        top.linkTo(heatingArrow.bottom)
                        bottom.linkTo(heatingArrow.bottom)
                        end.linkTo(heatingArrow.start)
                        start.linkTo(parent.start)
                    }
            )
            UnitLabel(
                value = statusUiState.heatPump.heatingBufferActualTemp,
                setPoint = statusUiState.heatPump.heatingBufferSetpoint,
                offset = statusUiState.heatPump.heatingBufferOffset,
                decimalPlaces = 1,
                modifier = Modifier.constrainAs(heatingLabel){
                    start.linkTo(heating.start)
                    end.linkTo(heating.end, margin = (-24).dp)
                    top.linkTo(heating.bottom)
                }
            )
        }
    }
}

@Composable
fun Battery(
    chargeLevel: BatteryChargeState,
    charge: Int,
    widthDp: Dp,
    heightDp: Dp,
    modifier: Modifier = Modifier
) {
    val batteryColor = when (chargeLevel) {
        BatteryChargeState.HIGH -> colorResource(id = R.color.eco)
        BatteryChargeState.MEDIUM -> colorResource(id = R.color.non_eco)
        BatteryChargeState.LOW -> Color.Red
    }
    val edgeColor = MaterialTheme.colorScheme.outline
    Canvas(
        modifier = modifier
            .width(widthDp)
            .height(heightDp)
    ) {
        val width = (widthDp.value * density)
        val height = heightDp.value * density
        val chargeFraction = charge / 100f
        val batteryHeight = (height * 0.95 * chargeFraction).toFloat()
        drawRect(
            color = batteryColor,
            size = Size(
                width,
                batteryHeight
            ),
            topLeft = Offset(0f, (0.05f + 0.95f * (1f - chargeFraction)) * height)
        )
        drawRoundRect(
            color = edgeColor,
            size = Size(width, 0.95f * height),
            style = Stroke(width = 4f * density),
            cornerRadius = CornerRadius(10f, 10f),
            topLeft = Offset(0f, 0.05f * height)
        )
        drawRoundRect(
            color = edgeColor,
            size = Size(0.4f * width, 0.05f * height),
            cornerRadius = CornerRadius(10f, 10f),
            topLeft = Offset(0.3f * width, 0f)
        )
    }
}

@Preview(showBackground = false)
@Composable
fun PreviewCurrentStatus() {
    EnergyHubTheme(darkTheme = false) {
        CurrentStatusLayout(
            StatusUiState(
                SolarStatus(
                    solarProduction = 3000f,
                    batteryLevel = 30,
//                    batteryChargeState = BatteryChargeState.LOW,
                    batteryProduction = 1000f,
                    isGridExporting = true,
                    gridPower = 500f,
                    load = 1500f,
//                    loadStatus = EcoState.ECO,
                ),
                EcoforestStatus(
                    dhwActualTemp = UnitValue(53.4f, "ºC"),
                    dhwOffset = UnitValue(4f, "ºC"),
                    dhwSetpoint = UnitValue(46f, "ºC")
                )
            )
        )
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