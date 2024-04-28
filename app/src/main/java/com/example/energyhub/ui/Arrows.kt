package com.example.energyhub.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.toPath
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

    val width: Dp
        get() = abs(startX.value - endX.value).dp
    val height: Dp
        get() = abs(startY.value - endY.value).dp
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
    val head = RoundedPolygon(
        numVertices = 3,
        radius = headSize.value / 2,
        centerX = if (arrowAtMidpoint) 0f else -headSize.value /2,
        centerY = 0f,
    )
    val headPath = head.toPath().asComposePath()
    Canvas(
        modifier = modifier
            .width(length)
            .height(length)
    ) {
        drawLine(
            start = Offset(x = vector.startX.value, y = vector.startY.value),
            end = Offset(x = vector.endX.value, y = vector.endY.value),
            color = color,
            strokeWidth = shaftWidth.value,
        )

        // values if the arrow is reversed.
        var translationX = 0f
        var translationY = 0f


        if (arrowAtMidpoint) {
            translationX = vector.endX.value / 2
            translationY = vector.endY.value / 2
        } else {
            if (!reverseArrow) {
                translationX = vector.endX.value
                translationY = vector.endY.value
            }
        }
        translate(
            left=translationX,
            top=translationY
        ) {
            var rotationAngle = angle
            if (reverseArrow)  {
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


@Preview(showBackground = true)
@Composable
fun ArrowPreview() {
    EnergyHubTheme {
        ConstraintLayout {
            // Create references for the composables to constrain
            val (arrow) = createRefs()
            val center = createGuidelineFromStart(0.5f)
            val middle = createGuidelineFromTop(0.5f)
            Arrow(
                angle = 225f,
                length = 200.dp,
                color = Color.Green,
                modifier = Modifier.constrainAs(arrow){
                    top.linkTo(middle)
                    start.linkTo(center)
                }
            )
            Arrow(
                angle = 180f,
                length = 200.dp,
                color = Color.Red,
                reverseArrow = true,
                modifier = Modifier.constrainAs(arrow){
                    top.linkTo(middle)
                    start.linkTo(center)
                }
            )
            Arrow(
                angle = 0f,
                length = 200.dp,
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
                length = 200.dp,
                color = Color.Blue,
                reverseArrow = false,
                arrowAtMidpoint = true,
                modifier = Modifier.constrainAs(arrow){
                    top.linkTo(middle)
                    start.linkTo(center)
                }
            )
        }
    }
}
