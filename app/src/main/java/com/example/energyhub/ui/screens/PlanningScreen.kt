package com.example.energyhub.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.toPath
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.energyhub.R
import com.example.energyhub.ui.theme.EnergyHubTheme
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.max

@Composable
fun PlanningScreen (
    modifier: Modifier = Modifier,
    viewModel: PlanningViewModel = viewModel(factory = ViewModelFactory.PlanningFactory)
) {
    val uiState by viewModel.uiState.collectAsState()

    PlanningLayout(
        uiState = uiState,
        onSetWork = viewModel::setWorkBoolean,
        modifier = modifier
    )
}

@Composable
fun PlanningLayout(
    uiState: PlanningUiState,
    onSetWork: (Boolean, LocalDate) -> Unit,
    modifier: Modifier = Modifier
){
    Column(modifier = modifier.fillMaxWidth()) {
        AgilePrices()
        DayList(uiState.days,
            uiState = uiState,
            onSetWork = onSetWork)
    }
}

@Composable
fun AgilePrices(
    modifier: Modifier = Modifier
){

}

@Composable
fun DayList(
    days: List<DayState>,
    uiState: PlanningUiState,
    onSetWork: (Boolean, LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
    ) {
        days.forEach {
            item { DayLine(day=it, onSetWork = onSetWork) }
        }
    }
}

fun RoundedPolygon.getBounds() = calculateBounds().let { Rect(it[0], it[1], it[2], it[3]) }
class ArcClipShape(
    private val radius: Float,
    private val startAngle: Float,
    private val endAngle: Float,
    private var matrix: Matrix = Matrix()
) : Shape {
    var path = Path()
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val baseRadius = radius
        path.rewind()
//        path = arc.toPath().asComposePath()
//        path.addRect(Rect(0f, 0f, radius, radius))
        path.moveTo(baseRadius, baseRadius)
//        path.lineTo(radius*2, radius)
//        path.lineTo(radius*2, radius*2)
        path.addArc(
            startAngleDegrees = startAngle,
            sweepAngleDegrees = endAngle-startAngle,
            oval = Rect(0f, 0f, baseRadius*2, baseRadius*2)
        )
        path.lineTo(baseRadius, baseRadius)

//        matrix.reset()
//        val bounds = polygon.getBounds()
//        val maxDimension = radius//max(bounds.width, bounds.height)
//        matrix.scale(size.width / maxDimension, size.height / maxDimension)
//        matrix.translate(-bounds.left, -bounds.top)
//        Log.i("PLANNING", matrix.toString())
//        if (radius > 0) {
//            path.transform(matrix)
//        }
        return Outline.Generic(path)
    }
}

class RoundedPolygonShape(
    private val polygon: RoundedPolygon,
    private var matrix: Matrix = Matrix()
) : Shape {
    private var path = Path()
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        path.rewind()
        path = polygon.toPath().asComposePath()
        matrix.reset()
        val bounds = polygon.getBounds()
        val maxDimension = max(bounds.width, bounds.height)
        matrix.scale(size.width / maxDimension, size.height / maxDimension)
        matrix.translate(-bounds.left, -bounds.top)

        path.transform(matrix)
        return Outline.Generic(path)
    }
}

val YELLOW = ColorFilter.colorMatrix(
    ColorMatrix(
        floatArrayOf(
            1f, 0f, 0f, 0f, 240f,
            0f, 1f, 0f, 0f, 230f,
            0f, 0f, 1f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        )
    )
)


@Composable
fun FractionallyColoredImage(
    @DrawableRes res: Int,
    fraction: Float,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
){
    val painter = painterResource(res)

    //initial height set at 0.dp
    var componentHeight by remember { mutableFloatStateOf(0f) }
    var componentWidth by remember { mutableFloatStateOf(0f) }

    val clip = //remember(componentWidth, componentHeight) {
        ArcClipShape(
            radius = (componentHeight + componentWidth) / 4,
            0f,
            360*fraction,
        )
//    }
    Box(
        modifier = modifier
        .padding(all = 3.dp)
    ) {
        Image( //black image in background
            painter,
            contentDescription = contentDescription,
            alpha = 0.5f,
            modifier = Modifier,
        )
        Image( // clipped yellow image in foreground
            painter,
            colorFilter = YELLOW,
            contentDescription = contentDescription,
            modifier = Modifier
                .graphicsLayer {
                    this.shape = clip
                    this.clip = true
                }
                .onGloballyPositioned {
                    componentHeight = it.size.height.toFloat()
                    componentWidth = it.size.width.toFloat()
                }

        )
    }

}

@Composable
fun DayLine(
    day: DayState,
    onSetWork: (Boolean, LocalDate) -> Unit,
    modifier: Modifier = Modifier
){
//    val context = LocalContext.current.applicationContext.resources.assets.locales
    val locale = Locale("en_UK")
    val rowHeight = 50.dp
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        Text(
            text = "${day.date.dayOfWeek.getDisplayName(TextStyle.SHORT_STANDALONE, locale)} ${day.date.dayOfMonth}",
            style = MaterialTheme.typography.displaySmall,
        )
        FractionallyColoredImage(
            res = R.drawable.sun,
            fraction = day.sunshineFraction,
            contentDescription = "Amount of sunlight",
            modifier = Modifier.height(rowHeight)
        )
//        FractionallyColoredImage(
//            res = R.drawable.three_way_arrow,
//            fraction = 0.5f,
//            contentDescription = "Windiness",
//            modifier = Modifier.height(rowHeight)
//            )
        Switch(
            checked = day.isGoingToWork,
            onCheckedChange = { onSetWork(it, day.date) },
        )

    }
}

@Preview(showBackground = true)
@Composable
fun PreviewPlanning() {
    EnergyHubTheme(darkTheme = false) {
        PlanningLayout(
            uiState = PlanningUiState(
                timezone = TimeZone.of("Europe/London"),
                days = listOf(
                    DayState(LocalDate(24, 7, 27), false, 0.3f),
                    DayState(LocalDate(24, 7, 28), true, 0.5f),
                    DayState(LocalDate(24, 7, 29), false, 0.8f),
                )
            ),
            onSetWork = {_,_ -> },
        )
    }
}