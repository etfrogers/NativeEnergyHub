package com.example.energyhub.ui.screens

import android.graphics.Typeface
import android.text.Layout
import androidx.annotation.ColorRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisGuidelineComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
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
import com.patrykandpatrick.vico.core.cartesian.Zoom
import com.patrykandpatrick.vico.core.cartesian.axis.AxisPosition
import com.patrykandpatrick.vico.core.cartesian.data.AxisValueOverrider
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.ChartValues
import com.patrykandpatrick.vico.core.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.core.cartesian.data.LineCartesianLayerModel
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.core.common.Dimensions
import com.patrykandpatrick.vico.core.common.component.TextComponent
import com.patrykandpatrick.vico.core.common.shader.DynamicShader
import com.patrykandpatrick.vico.core.common.shape.Corner
import com.patrykandpatrick.vico.core.common.shape.Shape
import kotlinx.datetime.TimeZone
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.round
import com.example.energyhub.model.minusAssign
import com.example.energyhub.model.sum
import com.patrykandpatrick.vico.compose.common.shader.BrushShader
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer

internal class HorizontalAxisItemPlacer(
    private val spacing: Int,
    private val offset: Int = 0,
    private val shiftExtremeTicks: Boolean,
    private val addExtremeLabelPadding: Boolean = false,
) : HorizontalAxis.ItemPlacer {
    private val CartesianMeasureContext.addExtremeLabelPadding
        get() =
            this@HorizontalAxisItemPlacer.addExtremeLabelPadding &&
                    horizontalLayout is HorizontalLayout.FullWidth

    private val ChartValues.measuredLabelValues
        get() = buildList {
            add(minX)
            if (xLength < xStep) return@buildList
            add(minX + xStep * floor(xLength / xStep))
            if (xLength >= 2 * xStep) add(minX + xStep * round((xLength / 2) / xStep))
        }

    override fun getShiftExtremeLines(context: CartesianDrawContext): Boolean = shiftExtremeTicks

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
    ): List<Float> {
        return getLabelValues(context,visibleXRange, fullXRange, maxLabelWidth)}

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
        rememberShapeComponent(shape = labelBackgroundShape, color = MaterialTheme.colorScheme.surface)
            .setShadow(
                radius = LABEL_BACKGROUND_SHADOW_RADIUS_DP,
                dy = LABEL_BACKGROUND_SHADOW_DY_DP,
//                applyElevationOverlay = true,
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
        rememberShapeComponent(
            shape = Shape.Pill,
            color = MaterialTheme.colorScheme.surface)
    val indicatorCenterComponent = rememberShapeComponent(shape = Shape.Pill)
    val indicatorRearComponent = rememberShapeComponent(shape = Shape.Pill)
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
//            override fun updateInsets(
//                context: CartesianMeasureContext,
//                outInsets: Insets,
//                horizontalDimensions: HorizontalDimensions,
//            ) {
//                with(context) {
//                    super.getInsets(context, outInsets, horizontalDimensions)
//                    val baseShadowInsetDp =
//                        CLIPPING_FREE_SHADOW_RADIUS_MULTIPLIER * LABEL_BACKGROUND_SHADOW_RADIUS_DP
//                    outInsets.top += (baseShadowInsetDp - LABEL_BACKGROUND_SHADOW_DY_DP).pixels
//                    outInsets.bottom += (baseShadowInsetDp + LABEL_BACKGROUND_SHADOW_DY_DP).pixels
//                }
//            }
        }
    }
}

private const val LABEL_BACKGROUND_SHADOW_RADIUS_DP = 4f
private const val LABEL_BACKGROUND_SHADOW_DY_DP = 2f
private const val CLIPPING_FREE_SHADOW_RADIUS_MULTIPLIER = 1.4f

@Composable
fun totalCartesianColumnLayer(@ColorRes colorIds: List<Int>): ColumnCartesianLayer {
    return rememberColumnCartesianLayer(
        columnProvider =
        ColumnCartesianLayer.ColumnProvider.series(
            colorIds.map { color ->
                rememberLineComponent(
                    color = colorResource(id = color),
                    thickness = 40.dp,
                    shape = Shape.Rectangle
                )
            }
        ),
        mergeMode = { ColumnCartesianLayer.MergeMode.Stacked },
        verticalAxisPosition = AxisPosition.Vertical.Start,
    )
}

internal fun ColumnCartesianLayerModel.BuilderScope.singleSeries(x: Int, vararg y: Number){
    y.forEach {
        series(listOf(x), listOf(it))
    }
}

internal fun LineCartesianLayerModel.BuilderScope.stackedSeries(x: List<Number>, vararg y: List<Number>){
    val topLine = y.toList().sum().toMutableList()
    y.forEach {
        series(x, topLine)
        topLine -= it
    }
}

@Composable
fun DailyChart(
    @ColorRes colors: List<Int>,
    timezone: TimeZone,
    modelProducer: CartesianChartModelProducer,
    modifier: Modifier = Modifier,
    maxY: Float? = null,
    solidColor: Boolean = true
){
    val marker = rememberMarker()
    CartesianChartHost(
        chart =
        rememberCartesianChart(
            rememberLineCartesianLayer(
                LineCartesianLayer.LineProvider.series(
                    colors.map {
                        colorResource(id = it).let { color ->
                            val bg = if (solidColor){
                                DynamicShader.color(color)
                            } else {
                                BrushShader(Brush.verticalGradient(
                                    listOf(color.copy(alpha = 0.5f), color.copy(alpha = 0f)))
                                )
                            }
                            rememberLine(
                                shader = DynamicShader.color(color),
                                backgroundShader = bg,
                            )
                        }
                               },
                ),
                axisValueOverrider = AxisValueOverrider.fixed(
                    minX = 0f, maxX = 24f, minY = 0f, maxY = maxY)
            ),
            startAxis = rememberStartAxis(),
            bottomAxis = rememberBottomAxis(
                guideline = null,
                itemPlacer = HorizontalAxisItemPlacer(
                    spacing = 12,
                    shiftExtremeTicks = true
                )
            ),
//            persistentMarkers = mapOf(
//                Clock.System.now().toFractionalHours(timezone)
//                        to marker),
            marker = marker,
            ),
        modelProducer = modelProducer,
        modifier = modifier.fillMaxWidth(),
        zoomState = rememberVicoZoomState(zoomEnabled = false, initialZoom = Zoom.Content),
    )
}