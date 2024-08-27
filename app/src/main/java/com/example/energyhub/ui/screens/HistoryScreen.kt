package com.example.energyhub.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.energyhub.R
import com.example.energyhub.ui.theme.EnergyHubTheme
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberEndAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import kotlinx.datetime.toInstant
import kotlinx.datetime.todayIn
import kotlin.math.floor
import kotlin.math.round


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen (
    modifier: Modifier = Modifier,
    historyViewModel: HistoryViewModel = viewModel(factory = ViewModelFactory.HistoryFactory)
) {
    val uiState by historyViewModel.uiState.collectAsState()
    var showErrorLog by remember { mutableStateOf(false) }
    var showDateSelector by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    ) { contentPadding ->
        ErrorHost(
            showErrorLog,
            uiState.errors,
            onCloseLog = { showErrorLog = false },
            onShowMore = { showErrorLog = true },
            afterShow = { historyViewModel.clearErrors() },
            snackbarHostState = snackbarHostState,
        ) {
            ChartLayout(
                uiState,
                onPrev = historyViewModel::decrementDate,
                onNext = historyViewModel::incrementDate,
                onSelect = {showDateSelector = true},
                onCancel = {showDateSelector = false},
                onNewDate = {
                    historyViewModel.setDateTo(
                        it?.let { it1 -> historyViewModel.convertMillisToLocalDate(it1) })
                    showDateSelector = false
                    },
                onToday = historyViewModel::goToToday,
                showPicker = showDateSelector,
                validDates = historyViewModel.DatesInThePast,
                modifier = modifier.padding(contentPadding)
            )
        }
    }
}


internal val dateButtonFormat = LocalDate.Format {
    dayOfMonth(); char('/'); monthNumber(); char('/'); year()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateSelector(uiState: HistoryUiState,
                 onPrev: () -> Unit,
                 onNext: () -> Unit,
                 onSelect: () -> Unit,
                 onCancel: () -> Unit,
                 onToday: () -> Unit,
                 onNewDate: (Long? /*dateMillis*/) -> Unit,
                 showPicker: Boolean,
                 modifier: Modifier = Modifier,
                 validDates: SelectableDates,
){
    val pickerState = rememberDatePickerState(
        yearRange = 2021..Clock.System.todayIn(TimeZone.UTC).year,
        selectableDates = validDates,
    )

    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            ) {
            TextButton(onClick = { /*do nothing - placeholder only*/ }) {
                Text(text = "")
            }
            IconButton(onClick = onPrev) {
                Image(
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = "Previous"
                )
            }
            TextButton(
                onClick = onSelect,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = uiState.date.format(dateButtonFormat),
                    style = MaterialTheme.typography.labelLarge,
                    fontSize = TextUnit(20f, TextUnitType.Sp)
                )
            }
            IconButton(
                onClick = onNext,
                enabled = uiState.date < Clock.System.todayIn(TimeZone.UTC)
            ) {
                Image(
                    imageVector = Icons.AutoMirrored.Default.ArrowForward,
                    contentDescription = "Next"
                )
            }
            IconButton(
                onClick = onToday,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.last_page),
                    contentDescription = "Today",
                    contentScale = ContentScale.FillHeight
                )
            }
        }
        if (showPicker){
            DatePickerDialog(
                onDismissRequest = onCancel,
                confirmButton = {
                    Button(
                        onClick = { onNewDate(pickerState.selectedDateMillis) }
                    ) {
                        Text(text = "OK")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = onCancel
                    ) {
                        Text(text = "Cancel")
                    }
                }
            ) {
                DatePicker(
                    state = pickerState,
                    showModeToggle = true
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartLayout(
    uiState: HistoryUiState,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onSelect: () -> Unit,
    onCancel: () -> Unit,
    onToday: () -> Unit,
    onNewDate: (Long?) -> Unit,
    showPicker: Boolean,
    modifier: Modifier = Modifier,
    validDates: SelectableDates = DatePickerDefaults.AllDates,
){
    LazyColumn(modifier = modifier) {
        item {
            DateSelector(
                uiState = uiState,
                onNext = onNext,
                onPrev = onPrev,
                onToday = onToday,
                onSelect = onSelect,
                onNewDate = onNewDate,
                onCancel = onCancel,
                showPicker = showPicker,
                validDates = validDates,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {TotalsChart(uiState)}
        item {ConsumptionChart(uiState)}
        item {ConsumptionSourceChart(uiState)}
        item {GenerationDestinationChart(uiState)}
        item {BatteryChart(uiState)}
    }
}

@Composable
internal fun TotalsChart(
    uiState: HistoryUiState,
    modifier: Modifier = Modifier
) {
    val modelProducer = remember { CartesianChartModelProducer() }
    modelProducer.tryRunTransaction {
        /* Learn more:
        https://patrykandpatrick.com/vico/wiki/cartesian-charts/layers/column-layer#data. */
        columnSeries {
            singleSeries(
                0,
                uiState.totalSelfConsumptionEnergy,
                uiState.netBatteryChargeFromSolar,
                uiState.totalExport
            )
        }
        columnSeries {
            singleSeries(
                1,
                uiState.totalSelfConsumptionEnergy,
                uiState.totalBatteryDischargeEnergy,
                uiState.totalImport
            )
            /* Learn more:
            https://patrykandpatrick.com/vico/wiki/cartesian-charts/layers/line-layer#data. */
        }
        columnSeries {
            singleSeries(
                2,
                uiState.carEnergy,
                uiState.immersionEnergy,
                uiState.dhwEnergy,
                uiState.heatingEnergy,
                uiState.legionnairesEnergy,
                uiState.combinedHPEnergy,
                uiState.netBatteryChargeFromGrid,
                uiState.remainingEnergy,
            )
        }
    }

    CartesianChartHost(
        chart =
        rememberCartesianChart(

            totalCartesianColumnLayer(colorIds = listOf(
                R.color.consumption,
                R.color.battery,
                R.color.export)),
            totalCartesianColumnLayer(
                colorIds = listOf(R.color.solar,
                    R.color.battery,
                    R.color.importColor)
            ),
            totalCartesianColumnLayer(colorIds = listOf(
                R.color.car,
                R.color.immersion,
                R.color.DHW,
                R.color.heating,
                R.color.legionnaires,
                R.color.combined,
                R.color.battery,
                R.color.consumption)
            ),
            startAxis = rememberStartAxis(guideline = null),
            endAxis = rememberEndAxis(guideline = null),
            bottomAxis = rememberBottomAxis(
                valueFormatter = { _, _, _ -> "" }
            ),
            marker = rememberMarker(),
        ),
        modelProducer = modelProducer,
        modifier = modifier.fillMaxWidth(),
        runInitialAnimation = false,
        zoomState = rememberVicoZoomState(zoomEnabled = false),
    )
}

@Composable
internal fun ConsumptionChart(
    uiState: HistoryUiState,
    modifier: Modifier = Modifier
) {
    val modelProducer = remember { CartesianChartModelProducer() }
    modelProducer.tryRunTransaction {
        /* Learn more:
        https://patrykandpatrick.com/vico/wiki/cartesian-charts/layers/line-layer#data. */
        if (uiState.fractionalHours.isNotEmpty()) {
            lineSeries {
                stackedSeries(
                    uiState.fractionalHours,
                    uiState.carChargePower,
                    uiState.immersionPower,
                    uiState.dhwPower,
                    uiState.heatingPower,
                    uiState.legionnairesPower,
                    uiState.combinedPower,
                    uiState.unknownHeatPumpPower,
                    uiState.batteryGridCharging,
                    uiState.remainingLoad,)
            }
        }
    }
    DailyChart(
        colors = listOf(
            R.color.car,
            R.color.immersion,
            R.color.DHW,
            R.color.heating,
            R.color.legionnaires,
            R.color.combined,
            R.color.heatPumpUnknown,
            R.color.battery,
            R.color.consumption,
        ),
        timezone = uiState.timezone,
        modelProducer = modelProducer,
        modifier = modifier)
}

@Composable
internal fun BatteryChart(uiState: HistoryUiState, modifier: Modifier = Modifier) {
    val modelProducer = remember { CartesianChartModelProducer() }
    modelProducer.tryRunTransaction {
        /* Learn more:
        https://patrykandpatrick.com/vico/wiki/cartesian-charts/layers/line-layer#data. */
        if (uiState.fractionalHours.isNotEmpty()) {
            lineSeries {
                series(
                    uiState.fractionalHours,
                    uiState.batteryPercentage
                )
            }
        }
    }

    DailyChart(
        colors = listOf(R.color.battery),
        timezone = uiState.timezone,
        modelProducer = modelProducer,
        maxY = 100f,
        modifier = modifier,
        solidColor = false,
    )
}

@Composable
internal fun ConsumptionSourceChart(
    uiState: HistoryUiState,
    modifier: Modifier = Modifier
) {
    val modelProducer = remember { CartesianChartModelProducer() }
    modelProducer.tryRunTransaction {
        /* Learn more:
        https://patrykandpatrick.com/vico/wiki/cartesian-charts/layers/line-layer#data. */
        if (uiState.fractionalHours.isNotEmpty()) {
            lineSeries {
                stackedSeries(
                    uiState.fractionalHours,
                    uiState.solarConsumption,
                    uiState.batteryDischarging,
                    uiState.importPower,)
            }
        }
    }
    DailyChart(
        colors = listOf(
            R.color.solar,
            R.color.battery,
            R.color.importColor,
        ),
        timezone = uiState.timezone,
        modelProducer = modelProducer,
        modifier = modifier)
}

@Composable
internal fun GenerationDestinationChart(
    uiState: HistoryUiState,
    modifier: Modifier = Modifier
) {
    val modelProducer = remember { CartesianChartModelProducer() }
    modelProducer.tryRunTransaction {
        /* Learn more:
        https://patrykandpatrick.com/vico/wiki/cartesian-charts/layers/line-layer#data. */
        if (uiState.fractionalHours.isNotEmpty()) {
            lineSeries {
                stackedSeries(
                    uiState.fractionalHours,
                    uiState.solarConsumption,
                    uiState.batterySolarCharging,
                    uiState.exportPower,)
            }
        }
    }
    DailyChart(
        colors = listOf(
            R.color.solar,
            R.color.battery,
            R.color.export,
        ),
        timezone = uiState.timezone,
        modelProducer = modelProducer,
        modifier = modifier)
}


@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun PreviewHistory() {
    EnergyHubTheme(darkTheme = false) {
        fun frac(x: Float) = x - floor(x)
        val hrs = (0..<24*4).map{ it / 4f}
        val ones = (Array(hrs.size) { 1f }).toList()
        ChartLayout(HistoryUiState(
            timestamps = hrs.map {
                        LocalDateTime(
                            2024,
                            6,
                            1,
                            floor(it).toInt(),
                            round(frac(it)*60).toInt(),
                            0).toInstant(TimeZone.UTC)
                                 },
            batteryPercentage = hrs.map { it*100f/24f },
            carChargePower = ones,
            immersionPower = ones,
            dhwPower = ones,
            heatingPower = ones,
            legionnairesPower = ones,
            combinedPower = ones,
            unknownHeatPumpPower = ones,
            batteryGridCharging = ones,
            remainingLoad = ones,

            batteryDischarging = ones,

            solarConsumption = ones,
            exportPower = ones,
            importPower = ones,
            batterySolarCharging = ones,

            timezone = TimeZone.UTC,//TimeZone.of("Europe/London"),
            date = LocalDate(2024, 3, 1),
        ),
            onPrev = {},
            onToday = {},
            onSelect = {},
            onCancel = {},
            onNext = {},
            onNewDate = {},
            showPicker = false,
        )
    }
}