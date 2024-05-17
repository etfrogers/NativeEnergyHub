package com.example.energyhub.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.toPath
import com.example.energyhub.R
import com.example.energyhub.ui.theme.EnergyHubTheme
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

const val PI_BY_180 = PI / 180.0
//
//
//# ------------------ KVector -------------------- #
//
//
private class KVector(
    var startX: Dp = 0.dp,
    var startY: Dp = 0.dp,
    var angle: Float = 0f,
    var length: Dp = 100.dp
) {

    val endX: Dp
        get() = startX + (cos(angle * PI_BY_180) * length)
    val endY: Dp
        get() = startY + (sin(angle * PI_BY_180) * length)

}

@Composable
fun Arrow(
    angle: Float,
    length: Dp,
    modifier: Modifier = Modifier,
    headSize: Dp = 100.dp,
    headRatio: Float = 0.7f,
    shaftWidth: Dp = 10.dp,
    color: Color = Color.Blue,
    arrowAtMidpoint: Boolean = false,
    reverseArrow: Boolean = false,
){
    val vector = KVector(angle = angle, length = length)
    val width = abs(vector.endX.value - vector.startX.value).dp
    val height = abs(vector.endY.value - vector.startY.value).dp
    Canvas(
        modifier = modifier
            .width(width)
            .height(height)
            .background(color = Color.LightGray)
    ) {
        val endX = vector.endX.value * density
        val endY = vector.endY.value * density
        drawLine(
            start = Offset(x = vector.startX.value, y = vector.startY.value),
            end = Offset(x = endX, y = endY),
            color = color,
            strokeWidth = shaftWidth.value,
        )

        if (headSize > 0.dp) {
            val head = RoundedPolygon(
                numVertices = 3,
                radius = headSize.value / 2,
                centerX = if (arrowAtMidpoint) 0f else -headSize.value /2,
                centerY = 0f,
            )
            val headPath = head.toPath().asComposePath()

            // values if the arrow is reversed.
            var translationX = 0f
            var translationY = 0f


            if (arrowAtMidpoint) {
                translationX = endX / 2
                translationY = endY / 2
            } else {
                if (!reverseArrow) {
                    translationX = endX
                    translationY = endY
                }
            }
            translate(
                left = translationX,
                top = translationY
            ) {
                var rotationAngle = angle
                if (reverseArrow) {
                    rotationAngle += 180
                }
                rotate(rotationAngle, Offset(0f, 0f)) {
                    scale(1.0f, headRatio, Offset(0f, 0f)) {
                        drawPath(headPath, color = color)
                    }
                }
            }
        }
    }
}


@Composable
fun PowerArrow(
    angle: Float,
    length: Dp,
    power: Float,
    modifier: Modifier = Modifier,
    reverseArrow: Boolean = false,
    activeColor: Color = colorResource(id = R.color.neutral),
){
    Arrow(
        angle = angle,
        length = length,
        modifier = modifier,
        arrowAtMidpoint = true,
        reverseArrow = reverseArrow,
        headSize = calculateArrowSize(power),
        color = if (power == 0f) colorResource(id = R.color.inactive)  else activeColor
        )
}

internal fun calculateArrowSize(power: Float): Dp {
    return if (power == 0f) {
        0.dp
    } else {
        (50 + (0.007 * power)).dp
    }
}

@Composable
fun PowerLabel(
    power: Float,
    modifier: Modifier = Modifier,
    unit: String = "kW",
    conversionFactor: Float = 1/1000f,
    decimalPlaces: Int = 2,
    vertical: Boolean = false,
){
    val labelSep = (if (vertical) '\n' else ' ')
    val unitLabel = labelSep + unit
    val valueString =  "%.${decimalPlaces}f".format(power*conversionFactor)
    Text (
        text = "$valueString$unitLabel",
        style = typography.labelLarge,
        textAlign = TextAlign.Center,
        modifier = modifier,
    )
}

@Composable
fun PercentLabel(value: Float){
    PowerLabel(
        power = value,
        unit = "%",
        conversionFactor = 1f,
        decimalPlaces = 0)
}

@Composable
fun LabelledArrow(
    angle: Float,
    length: Dp,
    power: Float,
    activeColor: Color,
    modifier: Modifier = Modifier,
    reverseArrow: Boolean = false,
) {

    val isVertical = angle !in arrayOf(0f, 180f)
//    is_stale: None
    if (isVertical) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PowerArrow(
                angle = angle,
                length = length,
                power = power,
                activeColor = activeColor,
                reverseArrow = reverseArrow,
                modifier = Modifier.padding(end = 12.dp)
            )
            PowerLabel(
                power = power,
                vertical = isVertical,
            )
        }
    } else {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PowerLabel(
                power = power,
                vertical = isVertical,
            )
            PowerArrow(
                angle = angle,
                length = length,
                power = power,
                activeColor = activeColor,
                reverseArrow = reverseArrow,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ArrowPreview() {
    EnergyHubTheme {
        ConstraintLayout (modifier = Modifier.fillMaxSize()){
            // Create references for the composables to constrain
            val (arrow, arrow2, arrow3, arrow4, arrow5, arrow6, arrow7, arrow8) = createRefs()
            val center = createGuidelineFromStart(0.5f)
            val middle = createGuidelineFromTop(0.5f)
            Arrow(
                angle = 225f,
                length = 100.dp,
                color = Color.Green,
                modifier = Modifier.constrainAs(arrow){
                    top.linkTo(middle)
                    start.linkTo(center)
                }
            )
            Arrow(
                angle = 180f,
                length = 100.dp,
                color = Color.Red,
                reverseArrow = true,
                modifier = Modifier.constrainAs(arrow){
                    top.linkTo(middle)
                    start.linkTo(center)
                }
            )
            Arrow(
                angle = 0f,
                length = 100.dp,
                color = Color.Gray,
                reverseArrow = true,
                arrowAtMidpoint = true,
                modifier = Modifier.constrainAs(arrow){
                    top.linkTo(middle)
                    start.linkTo(center)
                }
            )
            Arrow(
                angle = 90f,
                length = 100.dp,
                color = Color.Blue,
                reverseArrow = false,
                arrowAtMidpoint = true,
                modifier = Modifier.constrainAs(arrow){
                    top.linkTo(middle)
                    start.linkTo(center)
                }
            )
            PowerArrow(
                angle = 90f,
                length = 100.dp,
                activeColor = colorResource(id = R.color.eco),
                power = 0f,
                modifier = Modifier.constrainAs(arrow2){
                    top.linkTo(middle)
                    start.linkTo(parent.start, margin = 25.dp)
                }
            )
            PowerArrow(
                angle = 90f,
                length = 100.dp,
                activeColor = colorResource(id = R.color.eco),
                power = 100f,
                modifier = Modifier.constrainAs(arrow3){
                    top.linkTo(middle)
                    start.linkTo(parent.start, margin = 50.dp)
                }
            )
            PowerArrow(
                angle = 90f,
                length = 100.dp,
                activeColor = colorResource(id = R.color.eco),
                power = 1000f,
                modifier = Modifier.constrainAs(arrow4){
                    top.linkTo(middle)
                    start.linkTo(parent.start, margin = 75.dp)
                }
            )
            PowerArrow(
                angle = 90f,
                length = 100.dp,
                activeColor = colorResource(id = R.color.eco),
                power = 10000f,
                modifier = Modifier.constrainAs(arrow5){
                    top.linkTo(middle)
                    start.linkTo(parent.start, margin = 100.dp)
                }
            )
            LabelledArrow(
                angle = 90f,
                length = 100.dp,
                activeColor = colorResource(id = R.color.non_eco),
                power = 300f,
                modifier = Modifier.constrainAs(arrow6){
                    top.linkTo(middle, margin = 110.dp)
                    start.linkTo(parent.start, margin = 25.dp)
                }
            )
            LabelledArrow(
                angle = 90f,
                length = 100.dp,
                activeColor = colorResource(id = R.color.non_eco),
                power = 1000f,
                reverseArrow = true,
                modifier = Modifier.constrainAs(arrow7){
                    top.linkTo(middle, margin = 110.dp)
                    start.linkTo(arrow6.end, margin = 20.dp)
                }
            )
            LabelledArrow(
                angle = 0f,
                length = 100.dp,
                activeColor = colorResource(id = R.color.non_eco),
                power = 8000f,
                modifier = Modifier.constrainAs(arrow8){
                    top.linkTo(middle, margin = 210.dp)
                    start.linkTo(parent.start, margin = 25.dp)
                }
            )
        }
    }
}
