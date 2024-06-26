package com.example.energyhub.ui.screens

import android.graphics.Typeface
import android.text.Layout
import androidx.annotation.ColorRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.energyhub.R
import com.example.energyhub.model.OffsetDateTime
import com.example.energyhub.model.toFractionalHours
import com.example.energyhub.ui.theme.EnergyHubTheme
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisGuidelineComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberEndAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineSpec
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.component.fixed
import com.patrykandpatrick.vico.compose.common.component.rememberLayeredComponent
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.of
import com.patrykandpatrick.vico.compose.common.shader.color
import com.patrykandpatrick.vico.compose.common.shape.markerCornered
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawContext
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasureContext
import com.patrykandpatrick.vico.core.cartesian.HorizontalDimensions
import com.patrykandpatrick.vico.core.cartesian.HorizontalLayout
import com.patrykandpatrick.vico.core.cartesian.Insets
import com.patrykandpatrick.vico.core.cartesian.Zoom
import com.patrykandpatrick.vico.core.cartesian.axis.AxisItemPlacer
import com.patrykandpatrick.vico.core.cartesian.axis.AxisPosition
import com.patrykandpatrick.vico.core.cartesian.data.AxisValueOverrider
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.ChartValues
import com.patrykandpatrick.vico.core.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.core.common.Dimensions
import com.patrykandpatrick.vico.core.common.component.TextComponent
import com.patrykandpatrick.vico.core.common.shader.DynamicShader
import com.patrykandpatrick.vico.core.common.shape.Corner
import com.patrykandpatrick.vico.core.common.shape.Shape
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import kotlin.math.ceil
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
    dayOfMonth(); char('/'); monthNumber(); char('/'); year();
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
            IconButton(onClick = onNext) {
                Image(
                    imageVector = Icons.AutoMirrored.Default.ArrowForward,
                    contentDescription = "Previous"
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
                        onClick = onCancel
                    ) {
                        Text(text = "OK")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { onNewDate(pickerState.selectedDateMillis) }
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
    Column(modifier = modifier) {
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
        TotalsChart(uiState)
        BatteryChart(uiState)//, Modifier.fillMaxWidth())
    }
}

@Composable
internal fun BatteryChart(uiState: HistoryUiState, modifier: Modifier = Modifier) {
    val modelProducer = remember { CartesianChartModelProducer.build() }
//    LaunchedEffect(Unit) {
//        withContext(Dispatchers.Default) {
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
//        }
//    }

    val marker = rememberMarker()
    CartesianChartHost(
        chart =
        rememberCartesianChart(
            rememberLineCartesianLayer(
                listOf(rememberLineSpec(DynamicShader.color(colorResource(id = R.color.battery)))),
                axisValueOverrider = AxisValueOverrider.fixed(
                    minX = 0f, maxX = 24f, minY = 0f, maxY = 100f)
            ),
            startAxis = rememberStartAxis(),
            bottomAxis = rememberBottomAxis(
                guideline = null,
                itemPlacer = HorizontalAxisItemPlacer(
                    spacing = 12,
                    shiftExtremeTicks = true
                )
            ),
            persistentMarkers = mapOf(
                Clock.System.now().toFractionalHours(uiState.timezone)
                        to marker),

        ),
        modelProducer = modelProducer,
        modifier = modifier.fillMaxWidth(),
        marker = marker,
        zoomState = rememberVicoZoomState(zoomEnabled = false, initialZoom = Zoom.Content),
    )
}

internal class HorizontalAxisItemPlacer(
    private val spacing: Int,
    private val offset: Int = 0,
    private val shiftExtremeTicks: Boolean,
    private val addExtremeLabelPadding: Boolean = false,
) : AxisItemPlacer.Horizontal {
    private val CartesianMeasureContext.addExtremeLabelPadding
        get() =
            this@HorizontalAxisItemPlacer.addExtremeLabelPadding &&
                    horizontalLayout is HorizontalLayout.FullWidth

    private val ChartValues.measuredLabelValues
        get() = buildList {
            add(minX)
            if (xLength < xStep) return@buildList
            add(minX + xStep * floor(xLength / xStep))
            if (xLength >= 2 * xStep) add(minX + xStep * round((xLength/2) / xStep))
        }

    override fun getShiftExtremeTicks(context: CartesianDrawContext): Boolean = shiftExtremeTicks

    override fun getFirstLabelValue(context: CartesianMeasureContext, maxLabelWidth: Float) =
        if (context.addExtremeLabelPadding)
            context.chartValues.minX + offset * context.chartValues.xStep
        else null

    override fun getLastLabelValue(context: CartesianMeasureContext, maxLabelWidth: Float) =
        if (context.addExtremeLabelPadding) {
            with(context.chartValues) { maxX - (xLength - xStep * offset) % (xStep * spacing) }
        } else {
            null
        }

    override fun getLabelValues(
        context: CartesianDrawContext,
        visibleXRange: ClosedFloatingPointRange<Float>,
        fullXRange: ClosedFloatingPointRange<Float>,
        maxLabelWidth: Float,
    ): List<Float> {
        with(context) {
            val dynamicSpacing =
                spacing *
                        if (this.addExtremeLabelPadding) {
                            ceil(maxLabelWidth / (horizontalDimensions.xSpacing * spacing)).toInt()
                        } else {
                            1
                        }
            val remainder =
                ((visibleXRange.start - chartValues.minX) / chartValues.xStep - offset) % dynamicSpacing
            val firstValue =
                visibleXRange.start + (dynamicSpacing - remainder) % dynamicSpacing * chartValues.xStep
            val minXOffset = chartValues.minX % chartValues.xStep
            val values = mutableListOf<Float>()
            var multiplier = -LABEL_OVERFLOW_SIZE
            var hasEndOverflow = false
            while (true) {
                var potentialValue = firstValue + multiplier++ * dynamicSpacing * chartValues.xStep
                potentialValue =
                    chartValues.xStep * round((potentialValue - minXOffset) / chartValues.xStep) + minXOffset
                if (potentialValue < chartValues.minX || potentialValue == fullXRange.start) continue
                if (potentialValue > chartValues.maxX || potentialValue == fullXRange.endInclusive) break
                values += potentialValue
                if (
                    potentialValue > visibleXRange.endInclusive &&
                    hasEndOverflow.also { hasEndOverflow = true }
                )
                    break
            }
            return values
        }
    }

    override fun getWidthMeasurementLabelValues(
        context: CartesianMeasureContext,
        horizontalDimensions: HorizontalDimensions,
        fullXRange: ClosedFloatingPointRange<Float>,
    ) = if (context.addExtremeLabelPadding) context.chartValues.measuredLabelValues else emptyList()

    override fun getHeightMeasurementLabelValues(
        context: CartesianMeasureContext,
        horizontalDimensions: HorizontalDimensions,
        fullXRange: ClosedFloatingPointRange<Float>,
        maxLabelWidth: Float,
    ) = context.chartValues.measuredLabelValues

    override fun getLineValues(
        context: CartesianDrawContext,
        visibleXRange: ClosedFloatingPointRange<Float>,
        fullXRange: ClosedFloatingPointRange<Float>,
        maxLabelWidth: Float,
    ): List<Float>? {
        return getLabelValues(context,visibleXRange, fullXRange, maxLabelWidth)}
//        with(context) {
//            when (horizontalLayout) {
//                is HorizontalLayout.Segmented -> {
//                    val remainder = (visibleXRange.start - fullXRange.start) % chartValues.xStep
//                    val firstValue = visibleXRange.start + (chartValues.xStep - remainder) % chartValues.xStep
//                    var multiplier = -TICK_OVERFLOW_SIZE
//                    val values = mutableListOf<Float>()
//                    while (true) {
//                        val potentialValue = firstValue + multiplier++ * chartValues.xStep
//                        if (potentialValue < fullXRange.start) continue
//                        if (potentialValue > fullXRange.endInclusive) break
//                        values += potentialValue
//                        if (potentialValue > visibleXRange.endInclusive) break
//                    }
//                    values
//                }
//                is HorizontalLayout.FullWidth -> null
//            }
//        }

    override fun getStartHorizontalAxisInset(
        context: CartesianMeasureContext,
        horizontalDimensions: HorizontalDimensions,
        tickThickness: Float,
        maxLabelWidth: Float,
    ): Float {
        val tickSpace = if (shiftExtremeTicks) tickThickness else (tickThickness/2)
        return when (context.horizontalLayout) {
            is HorizontalLayout.Segmented -> tickSpace
            is HorizontalLayout.FullWidth ->
                (tickSpace - horizontalDimensions.unscalableStartPadding).coerceAtLeast(0f)
        }
    }

    override fun getEndHorizontalAxisInset(
        context: CartesianMeasureContext,
        horizontalDimensions: HorizontalDimensions,
        tickThickness: Float,
        maxLabelWidth: Float,
    ): Float {
        val tickSpace = if (shiftExtremeTicks) tickThickness else (tickThickness/2)
        return when (context.horizontalLayout) {
            is HorizontalLayout.Segmented -> tickSpace
            is HorizontalLayout.FullWidth ->
                (tickSpace - horizontalDimensions.unscalableEndPadding).coerceAtLeast(0f)
        }
    }

    private companion object {
        const val LABEL_OVERFLOW_SIZE = 2
        const val TICK_OVERFLOW_SIZE = 1
    }
}


@Composable
internal fun rememberMarker(
    labelPosition: DefaultCartesianMarker.LabelPosition = DefaultCartesianMarker.LabelPosition.Top,
    showIndicator: Boolean = true,
): CartesianMarker {
    val labelBackgroundShape = Shape.markerCornered(Corner.FullyRounded)
    val labelBackground =
        rememberShapeComponent(labelBackgroundShape, MaterialTheme.colorScheme.surface)
            .setShadow(
                radius = LABEL_BACKGROUND_SHADOW_RADIUS_DP,
                dy = LABEL_BACKGROUND_SHADOW_DY_DP,
                applyElevationOverlay = true,
            )
    val label =
        rememberTextComponent(
            color = MaterialTheme.colorScheme.onSurface,
            background = labelBackground,
            padding = Dimensions.of(8.dp, 4.dp),
            typeface = Typeface.MONOSPACE,
            textAlignment = Layout.Alignment.ALIGN_CENTER,
            minWidth = TextComponent.MinWidth.fixed(40.dp),
        )
    val indicatorFrontComponent =
        rememberShapeComponent(Shape.Pill, MaterialTheme.colorScheme.surface)
    val indicatorCenterComponent = rememberShapeComponent(Shape.Pill)
    val indicatorRearComponent = rememberShapeComponent(Shape.Pill)
    val indicator =
        rememberLayeredComponent(
            rear = indicatorRearComponent,
            front =
            rememberLayeredComponent(
                rear = indicatorCenterComponent,
                front = indicatorFrontComponent,
                padding = Dimensions.of(5.dp),
            ),
            padding = Dimensions.of(10.dp),
        )
    val guideline = rememberAxisGuidelineComponent()
    return remember(label, labelPosition, indicator, showIndicator, guideline) {
        object :
            DefaultCartesianMarker(
                label = label,
                labelPosition = labelPosition,
                indicator = if (showIndicator) indicator else null,
                indicatorSizeDp = 36f,
                setIndicatorColor =
                if (showIndicator) {
                    { color ->
                        indicatorRearComponent.color = Color(color).copy(alpha = .15f).toArgb()
                        indicatorCenterComponent.color = color
                        indicatorCenterComponent.setShadow(radius = 12f, color = color)
                    }
                } else {
                    null
                },
                guideline = guideline,
            ) {
            override fun getInsets(
                context: CartesianMeasureContext,
                outInsets: Insets,
                horizontalDimensions: HorizontalDimensions,
            ) {
                with(context) {
                    super.getInsets(context, outInsets, horizontalDimensions)
                    val baseShadowInsetDp =
                        CLIPPING_FREE_SHADOW_RADIUS_MULTIPLIER * LABEL_BACKGROUND_SHADOW_RADIUS_DP
                    outInsets.top += (baseShadowInsetDp - LABEL_BACKGROUND_SHADOW_DY_DP).pixels
                    outInsets.bottom += (baseShadowInsetDp + LABEL_BACKGROUND_SHADOW_DY_DP).pixels
                }
            }
        }
    }
}

private const val LABEL_BACKGROUND_SHADOW_RADIUS_DP = 4f
private const val LABEL_BACKGROUND_SHADOW_DY_DP = 2f
private const val CLIPPING_FREE_SHADOW_RADIUS_MULTIPLIER = 1.4f



@Composable
internal fun TotalsChart(
    uiState: HistoryUiState,
    modifier: Modifier = Modifier
) {
    val modelProducer = remember { CartesianChartModelProducer.build() }
//    LaunchedEffect("TotalsChartUpdate") {
//        withContext(Dispatchers.Default) {
//            while (isActive) {
                modelProducer.tryRunTransaction {
                    /* Learn more:
                    https://patrykandpatrick.com/vico/wiki/cartesian-charts/layers/column-layer#data. */
                    columnSeries {
                        singleSeries(
                            0,
                            uiState.totalSelfConsumptionEnergy,
                            uiState.netBatteryChargeFromSolar,
                            uiState.totalExport)
                    }
                    columnSeries {
                        singleSeries(
                            1,
                        uiState.totalSelfConsumptionEnergy,
                        uiState.totalBatteryDischargeEnergy,
                        uiState.totalImport)

                    /* Learn more:
                    https://patrykandpatrick.com/vico/wiki/cartesian-charts/layers/line-layer#data. */
                }
                columnSeries {
                    singleSeries(
                        2,
                        uiState.zappiEnergy,
                        uiState.eddiEnergy,
                        uiState.dhwEnergy,
                        uiState.heatingEnergy,
                        uiState.legionnairesEnergy,
                        uiState.combinedHPEnergy,
                        uiState.netBatteryChargeFromGrid,
                        uiState.remainingEnergy,
                    )
                }
//                delay(Defaults.TRANSACTION_INTERVAL_MS)
            }
//        }
//    }

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
            )
        ),
        modelProducer = modelProducer,
        modifier = modifier.fillMaxWidth(),
        marker = rememberMarker(),
        runInitialAnimation = false,
        zoomState = rememberVicoZoomState(zoomEnabled = false),
    )
}

@Composable
fun totalCartesianColumnLayer(@ColorRes colorIds: List<Int>): ColumnCartesianLayer {
    return rememberColumnCartesianLayer(
        columnProvider =
        ColumnCartesianLayer.ColumnProvider.series(
            colorIds.map { color ->
                rememberLineComponent(
                    color = colorResource(id = color),
                    thickness = 40.dp,
                    shape = Shape.Rectangle)
            }
        ),
        mergeMode = { ColumnCartesianLayer.MergeMode.Stacked },
        verticalAxisPosition = AxisPosition.Vertical.Start,
    )
}

fun ColumnCartesianLayerModel.BuilderScope.singleSeries(x: Int, vararg y: Number){
    y.forEach {
        series(listOf(x), listOf(it))
    }
}

private fun frac(x: Float) = x - floor(x)
@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun PreviewHistory() {
    EnergyHubTheme(darkTheme = false) {
        val hrs = (0..<24*4).map{ it / 4f}

        ChartLayout(HistoryUiState(
            timestamps = hrs.map {
                    OffsetDateTime(
                        LocalDateTime(2024, 6, 1,
                            floor(it).toInt(), round(frac(it)*60).toInt(), 0),
                        TimeZone.UTC)
                                     },
            batteryPercentage = hrs.map { it*100f/24f },
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