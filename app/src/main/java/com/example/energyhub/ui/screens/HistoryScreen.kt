package com.example.energyhub.ui.screens

import android.graphics.Typeface
import android.text.Layout
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasureContext
import com.patrykandpatrick.vico.core.cartesian.HorizontalDimensions
import com.patrykandpatrick.vico.core.cartesian.Insets
import com.patrykandpatrick.vico.core.cartesian.axis.AxisPosition
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlin.random.Random


@Composable
fun HistoryScreen (
    modifier: Modifier = Modifier,
    historyViewModel: HistoryViewModel = viewModel(factory = ViewModelFactory.HistoryFactory)
) {
    val uiState by historyViewModel.uiState.collectAsState()
    Column {
        CenterText(text = "Total Generation ${uiState.solar.totalConsumption}")
        ChartLayout()
    }
}

@Composable
fun ChartLayout(modifier: Modifier = Modifier){
    Column {
        Chart1(modifier)
        Chart8(modifier)
    }
}

@Composable
internal fun Chart1(modifier: Modifier) {
    val modelProducer = remember { CartesianChartModelProducer.build() }
    LaunchedEffect(Unit) {
        withContext(Dispatchers.Default) {
            modelProducer.tryRunTransaction {
                /* Learn more:
                https://patrykandpatrick.com/vico/wiki/cartesian-charts/layers/line-layer#data. */
                lineSeries { series(x, x.map { Random.nextFloat() * 15 }) }
            }
        }
    }
    ComposeChart1(modelProducer, modifier)
}

@Composable
private fun ComposeChart1(modelProducer: CartesianChartModelProducer, modifier: Modifier) {
    val marker = rememberMarker()
    CartesianChartHost(
        chart =
        rememberCartesianChart(
            rememberLineCartesianLayer(
                listOf(rememberLineSpec(DynamicShader.color(Color(0xffa485e0))))
            ),
            startAxis = rememberStartAxis(),
            bottomAxis = rememberBottomAxis(guideline = null),
            persistentMarkers = mapOf(PERSISTENT_MARKER_X to marker),
        ),
        modelProducer = modelProducer,
        modifier = modifier,
        marker = marker,
        zoomState = rememberVicoZoomState(zoomEnabled = false),
    )
}

private const val PERSISTENT_MARKER_X = 7f

private val x = (1..50).toList()

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
internal fun Chart8(modifier: Modifier) {
    val modelProducer = remember { CartesianChartModelProducer.build() }
    LaunchedEffect(Unit) {
        withContext(Dispatchers.Default) {
            while (isActive) {
                modelProducer.tryRunTransaction {
                    /* Learn more:
                    https://patrykandpatrick.com/vico/wiki/cartesian-charts/layers/column-layer#data. */
                    columnSeries {
                        repeat(Defaults.MULTI_SERIES_COUNT) {
                            series(
                                List(Defaults.ENTRY_COUNT) {
                                    Defaults.COLUMN_LAYER_MIN_Y +
                                            Random.nextFloat() * Defaults.COLUMN_LAYER_RELATIVE_MAX_Y
                                }
                            )
                        }
                    }
                    /* Learn more:
                    https://patrykandpatrick.com/vico/wiki/cartesian-charts/layers/line-layer#data. */
                    lineSeries { series(List(Defaults.ENTRY_COUNT) { Random.nextFloat() * Defaults.MAX_Y }) }
                }
                delay(Defaults.TRANSACTION_INTERVAL_MS)
            }
        }
    }
    ComposeChart8(modelProducer, modifier)
}

@Composable
private fun ComposeChart8(modelProducer: CartesianChartModelProducer, modifier: Modifier) {
    CartesianChartHost(
        chart =
        rememberCartesianChart(
            rememberColumnCartesianLayer(
                columnProvider =
                ColumnCartesianLayer.ColumnProvider.series(
                    columnChartColors.map { color ->
                        rememberLineComponent(color = color, thickness = 8.dp, shape = Shape.rounded(40))
                    }
                ),
                mergeMode = { ColumnCartesianLayer.MergeMode.Stacked },
                verticalAxisPosition = AxisPosition.Vertical.Start,
            ),
            rememberLineCartesianLayer(
                lines = listOf(rememberLineSpec(shader = DynamicShader.color(color4))),
                verticalAxisPosition = AxisPosition.Vertical.End,
            ),
            startAxis = rememberStartAxis(guideline = null),
            endAxis = rememberEndAxis(guideline = null),
            bottomAxis = rememberBottomAxis(),
        ),
        modelProducer = modelProducer,
        modifier = modifier,
        marker = rememberMarker(),
        runInitialAnimation = false,
        zoomState = rememberVicoZoomState(zoomEnabled = false),
    )
}

private val color1 = Color(0xffa55a5a)
private val color2 = Color(0xffd3756b)
private val color3 = Color(0xfff09b7d)
private val color4 = Color(0xffffc3a1)
private val columnChartColors = listOf(color1, color2, color3)

object Defaults {
    const val TRANSACTION_INTERVAL_MS = 2000L
    const val MULTI_SERIES_COUNT = 3
    const val ENTRY_COUNT = 50
    const val MAX_Y = 20
    const val COLUMN_LAYER_MIN_Y = 2
    const val COLUMN_LAYER_RELATIVE_MAX_Y = MAX_Y - COLUMN_LAYER_MIN_Y
}
@Preview(showBackground = true)
@Composable
fun PreviewHistory() {
    EnergyHubTheme(darkTheme = false) {
        ChartLayout()
    }
}