package com.example.energyhub.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposePath
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
    //    def __init__(self, *args, **kwargs):
//        super().__init__()
//
//    o_x = NumericProperty(0)
//    o_y = NumericProperty(0)
//    #    to_x = NumericProperty(0)
//    #    to_y = NumericProperty(0)
//    #    to_pos = ReferenceListProperty(to_x,to_y)
//    angle = NumericProperty(0)
//    distance = NumericProperty(0)
//
//    #    def get_angle(self):
//    #        return ((math.atan2(self.to_x - self.o_x, self.o_y - self.to_y)/piby180)+630.0 ) % 360.0
//    #
//    #    def set_angle(self,angle):
//    #        self.to_x = self.o_x + (math.cos(angle * piby180) * self.distance)
//    #        self.to_y = self.o_y + (math.sin(angle * piby180) * self.distance)
//    #
//    #    def get_distance(self):
//    #        absx = abs(self.to_x-self.o_x)
//    #        absy = abs(self.to_y-self.o_y)
//    #        return math.sqrt((absx*absx)+(absy*absy))
//    #
//    #    def set_distance(self, distance):
//    #        self.to_x = self.o_x + ((math.cos(self.angle * piby180) * distance))
//    #        self.to_y = self.o_y + ((math.sin(self.angle * piby180) * distance))
//    #
//    ##    angle = AliasProperty(
//    #                          get_angle,
//    #                          set_angle,
//    #                          bind=['o_x','o_y','to_x', 'to_y']
//    #                         )
//    #    distance = AliasProperty(
//    #                          get_distance,
//    #                          set_distance,
//    #                          bind=['o_x','o_y','to_x', 'to_y']
//    #                         )
    val endX: Dp
        get() = startX + (cos(angle * PI_BY_180) * length)
    val endY: Dp
        get() = startY + (sin(angle * PI_BY_180) * length)


//    def set_to_x(self, to_x):
//        self.angle = ((math.atan2(to_x - self.o_x, self.o_y - self.to_y) / piby180) + 630.0) % 360.0
//        absx = abs(to_x - self.o_x)
//        absy = abs(self.to_y - self.o_y)
//        self.distance = math.sqrt((absx * absx) + (absy * absy))
//
//    def set_to_y(self, to_y):
//        self.angle = ((math.atan2(self.to_x - self.o_x, self.o_y - to_y) / piby180) + 630.0) % 360.0
//        absx = abs(self.to_x - self.o_x)
//        absy = abs(to_y - self.o_y)
//        self.distance = math.sqrt((absx * absx) + (absy * absy))

    val width: Dp
        get() = abs(startX.value - endX.value).dp
    val height: Dp
        get() = abs(startY.value - endY.value).dp

//    to_x = AliasProperty(
//        get_to_x,
//        set_to_x,
//        bind=['o_x', 'o_y', 'angle', 'distance']
//    )
//
//    to_y = AliasProperty(
//        get_to_y,
//        set_to_y,
//        bind=['o_x', 'o_y', 'angle', 'distance']
//    )
}
//def move_point(x, y, angle, distance):
//    return (
//        x + (math.cos(angle * piby180) * distance),
//        y + (math.sin(angle * piby180) * distance)
//    )
//
//
//# ------------ Arrow -------------- #
//
//class Arrow(Widget, KVector):
//    head_size = NumericProperty(cm(0.5))
//    head_angle = NumericProperty(90.0)
//    shaft_width = NumericProperty(cm(0.05))
//    fletching_radius = NumericProperty(cm(0.1))
//    main_color = ListProperty([1, 1, 1, 0.7])
//    outline_color = ListProperty([0, 0, 0, 0.7])
//    outline_width = NumericProperty(cm(0.01))
//    distortions = ListProperty([])
//    arrow_at_midpoint = BooleanProperty(False)
//    reverse_arrow = BooleanProperty(False)
//
//    def __init__(self, *args, **kwargs):
//        Widget.__init__(self, *args, **kwargs)
//        KVector.__init__(self, *args, **kwargs)
//
//        with self.canvas:
//            self.icolor = Color(rgba=self.main_color)
//            self.head = Mesh(mode='triangle_fan', indices=[0, 1, 2])
//            self.shaft = Line(width=self.shaft_width)
//            self.fletching = Ellipse()
//
//            self.ocolor = Color(rgba=self.outline_color)
//            self.head_outline = Line(width=self.outline_width)
//            self.shaft_outline_left = Line(width=self.outline_width)
//            self.shaft_outline_right = Line(width=self.outline_width)
//            self.fletching_outline = Line()
//
//        self.bind(
//            o_x=self.update_dims,
//            o_y=self.update_dims,
//            to_x=self.update_dims,
//            to_y=self.update_dims,
//            head_size=self.update_dims,
//            head_angle=self.update_dims,
//            shaft_width=self.update_shaft_width,
//            outline_color=self.update_outline_color,
//            main_color=self.update_color,
//            outline_width=self.update_outline_width,
//            distortions=self.update_dims,
//            arrow_at_midpoint=self.update_dims,
//            reverse_arrow=self.update_dims,
//        )
//        self.update_dims()
//        self.update_shaft_width()
//        self.update_color()
//
//    def update_shaft_width(self, *args):
//        self.shaft.width = self.shaft_width
//
//    def update_outline_width(self, *args):
//        self.shaft_outline_right.width = self.outline_width
//        self.shaft_outline_left.width = self.outline_width
//        self.head_outline.width = self.outline_width
//
//    def update_outline_color(self, *args):
//        self.ocolor.rgba = self.outline_color
//
//    def update_color(self, *args):
//        self.icolor.rgba = self.main_color
//
//    def create_distortions(self, x1, y1, x2, y2):
//        """
//        Add points for a bezier curve distorted by a fraction of the line length.
//        A distortion of [0.5] means that there will be one bezier point added in the middle of the line
//        and that point will be displaced perpendicularly by 0.5 * self.distance.
//        A distortion of [0.3, -0.5] means that there will be 2 points added, at 1/3 and 2/3 of the line
//        and those points will be displaced perpendicularly by 0.3 * self.distance and -0.5 * self.distance
//        meaning that the arrow will bend in both directions.
//        """
//        if not self.distortions:
//            return [x1, y1, x2, y2]
//
//        angle_perpendicular = self.angle + 90.0
//        points = len(self.distortions)
//
//        segments = [x1, y1]
//
//        # For 3 points, we have: x = (3x1 + x2) / 4, (2x1 + 2x2) / 4, (x1 + 3x2) / 4
//        for i, distortion in enumerate(self.distortions):
//            xpos = ((points - i) * x1 + (i + 1) * x2) / (points + 1)
//            ypos = ((points - i) * y1 + (i + 1) * y2) / (points + 1)
//            segments.extend(move_point(xpos, ypos, angle_perpendicular, self.distance * distortion))
//
//        segments.extend([x2, y2])
//
//        return segments
//
//    def update_dims(self, *args):
//        shaft_x1, shaft_y1 = move_point(self.o_x, self.o_y, self.angle, self.fletching_radius / math.sqrt(2))
//        if self.arrow_at_midpoint:
//            shaft_x2, shaft_y2 = self.to_x, self.to_y
//        else:
//            shaft_x2, shaft_y2 = move_point(self.to_x, self.to_y, self.angle,
//                                            - math.cos(self.head_angle / 2.0 * piby180) * self.head_size)
//
//        if not self.distortions:
//            self.shaft.points = [shaft_x1, shaft_y1, shaft_x2, shaft_y2]
//        else:
//            self.shaft.bezier = self.create_distortions(shaft_x1, shaft_y1, shaft_x2, shaft_y2)
//
//        shaft_ol_x1, shaft_ol_y1 = move_point(shaft_x1, shaft_y1, self.angle - 90, self.shaft_width / 0.6)
//        shaft_ol_x2, shaft_ol_y2 = move_point(shaft_x2, shaft_y2, self.angle - 90, self.shaft_width / 0.6)
//
//        shaft_or_x1, shaft_or_y1 = move_point(shaft_x1, shaft_y1, self.angle + 90, self.shaft_width / 0.6)
//        shaft_or_x2, shaft_or_y2 = move_point(shaft_x2, shaft_y2, self.angle + 90, self.shaft_width / 0.6)
//
//        if not self.distortions:
//            self.shaft_outline_left.points = [shaft_ol_x1, shaft_ol_y1, shaft_ol_x2, shaft_ol_y2]
//            self.shaft_outline_right.points = [shaft_or_x1, shaft_or_y1, shaft_or_x2, shaft_or_y2]
//        else:
//            self.shaft_outline_left.bezier = self.create_distortions(shaft_ol_x1, shaft_ol_y1, shaft_ol_x2,
//                                                                     shaft_ol_y2)
//            self.shaft_outline_right.bezier = self.create_distortions(shaft_or_x1, shaft_or_y1, shaft_or_x2,
//                                                                      shaft_or_y2)
//
//        head_x_tip, head_y_tip = self.to_x, self.to_y
//        head_x1, head_y1 = move_point(self.to_x, self.to_y, self.angle + (180 - self.head_angle / 2.0), self.head_size)
//        head_x2, head_y2 = move_point(self.to_x, self.to_y, self.angle - (180 - self.head_angle / 2.0), self.head_size)
//
//        if self.reverse_arrow:
//            head_length = math.cos((self.head_angle/2)*piby180) * self.head_size
//            head_x_tip, head_y_tip = move_point(head_x_tip, head_y_tip, self.angle+180, head_length)
//            head_x1, head_y1 = move_point(head_x1, head_y1, self.angle, head_length)
//            head_x2, head_y2 = move_point(head_x2, head_y2, self.angle, head_length)
//
//        if self.arrow_at_midpoint:
//            head_x_tip, head_y_tip = move_point(head_x_tip, head_y_tip,
//                                                self.angle+180, self.distance / 2)
//            head_x1, head_y1 = move_point(head_x1, head_y1,
//                                          self.angle + 180, self.distance / 2)
//            head_x2, head_y2 = move_point(head_x2, head_y2,
//                                          self.angle + 180, self.distance / 2)
//
//        self.head.vertices = [
//            head_x_tip,
//            head_y_tip,
//            0,
//            0,
//            head_x1,
//            head_y1,
//            0,
//            0,
//            head_x2,
//            head_y2,
//            0,
//            0,
//        ]
//
//        self.head_outline.points = [
//            self.to_x,
//            self.to_y,
//            head_x1,
//            head_y1,
//            head_x2,
//            head_y2,
//            self.to_x,
//            self.to_y
//        ]
//
//        self.fletching.pos = move_point(self.o_x,
//                                        self.o_y,
//                                        225,
//                                        self.fletching_radius)
//
//        self.fletching.size = [self.fletching_radius * math.sqrt(2)] * 2
//        self.fletching_outline.ellipse = (
//            self.fletching.pos[0],
//            self.fletching.pos[1],
//            self.fletching_radius * math.sqrt(2),
//            self.fletching_radius * math.sqrt(2),
//        )

@Composable
fun Arrow(
    angle: Float,
    length: Dp,
    modifier: Modifier = Modifier,
    head_size: Dp = 10.dp,
    head_angle: Float = 90.0f,
    shaft_width: Dp = 10.dp,
//    fletching_radius: Dp = 5.dp,
    main_color: Color = Color.Blue,
    outline_color: Color = Color.Black,
    outline_width: Dp = 1.dp,
//    distortions = ListProperty([])
    arrow_at_midpoint: Boolean = false,
    reverse_arrow: Boolean = false,
){

    ConstraintLayout(modifier=modifier) {
        val (line, head) = createRefs()

        ArrowLine(
            angle = angle,
            length = length,
            width = shaft_width,
            modifier = Modifier.constrainAs(line) {
                top.linkTo(parent.top)
                start.linkTo(parent.start)
            },
        )

        ArrowHead(
            angle = 10f,
            headSize = 10.dp,
            modifier = Modifier
                .constrainAs(head){

            })
    }
}

//    def __init__(self, *args, **kwargs):
//        Widget.__init__(self, *args, **kwargs)
//        KVector.__init__(self, *args, **kwargs)
//
//        with self.canvas:
//            self.icolor = Color(rgba=self.main_color)
//            self.head = Mesh(mode='triangle_fan', indices=[0, 1, 2])
//            self.shaft = Line(width=self.shaft_width)
//            self.fletching = Ellipse()
//
//            self.ocolor = Color(rgba=self.outline_color)
//            self.head_outline = Line(width=self.outline_width)
//            self.shaft_outline_left = Line(width=self.outline_width)
//            self.shaft_outline_right = Line(width=self.outline_width)
//            self.fletching_outline = Line()
//
//        self.bind(
//            o_x=self.update_dims,
//            o_y=self.update_dims,
//            to_x=self.update_dims,
//            to_y=self.update_dims,
//            head_size=self.update_dims,
//            head_angle=self.update_dims,
//            shaft_width=self.update_shaft_width,
//            outline_color=self.update_outline_color,
//            main_color=self.update_color,
//            outline_width=self.update_outline_width,
//            distortions=self.update_dims,
//            arrow_at_midpoint=self.update_dims,
//            reverse_arrow=self.update_dims,
//        )
//        self.update_dims()
//        self.update_shaft_width()
//        self.update_color()
//
//    def update_shaft_width(self, *args):
//        self.shaft.width = self.shaft_width
//
//    def update_outline_width(self, *args):
//        self.shaft_outline_right.width = self.outline_width
//        self.shaft_outline_left.width = self.outline_width
//        self.head_outline.width = self.outline_width
//
//    def update_outline_color(self, *args):
//        self.ocolor.rgba = self.outline_color
//
//    def update_color(self, *args):
//        self.icolor.rgba = self.main_color
//
//    def create_distortions(self, x1, y1, x2, y2):
//        """
//        Add points for a bezier curve distorted by a fraction of the line length.
//        A distortion of [0.5] means that there will be one bezier point added in the middle of the line
//        and that point will be displaced perpendicularly by 0.5 * self.distance.
//        A distortion of [0.3, -0.5] means that there will be 2 points added, at 1/3 and 2/3 of the line
//        and those points will be displaced perpendicularly by 0.3 * self.distance and -0.5 * self.distance
//        meaning that the arrow will bend in both directions.
//        """
//        if not self.distortions:
//            return [x1, y1, x2, y2]
//
//        angle_perpendicular = self.angle + 90.0
//        points = len(self.distortions)
//
//        segments = [x1, y1]
//
//        # For 3 points, we have: x = (3x1 + x2) / 4, (2x1 + 2x2) / 4, (x1 + 3x2) / 4
//        for i, distortion in enumerate(self.distortions):
//            xpos = ((points - i) * x1 + (i + 1) * x2) / (points + 1)
//            ypos = ((points - i) * y1 + (i + 1) * y2) / (points + 1)
//            segments.extend(move_point(xpos, ypos, angle_perpendicular, self.distance * distortion))
//
//        segments.extend([x2, y2])
//
//        return segments
//
//    def update_dims(self, *args):
//        shaft_x1, shaft_y1 = move_point(self.o_x, self.o_y, self.angle, self.fletching_radius / math.sqrt(2))
//        if self.arrow_at_midpoint:
//            shaft_x2, shaft_y2 = self.to_x, self.to_y
//        else:
//            shaft_x2, shaft_y2 = move_point(self.to_x, self.to_y, self.angle,
//                                            - math.cos(self.head_angle / 2.0 * piby180) * self.head_size)
//
//        if not self.distortions:
//            self.shaft.points = [shaft_x1, shaft_y1, shaft_x2, shaft_y2]
//        else:
//            self.shaft.bezier = self.create_distortions(shaft_x1, shaft_y1, shaft_x2, shaft_y2)
//
//        shaft_ol_x1, shaft_ol_y1 = move_point(shaft_x1, shaft_y1, self.angle - 90, self.shaft_width / 0.6)
//        shaft_ol_x2, shaft_ol_y2 = move_point(shaft_x2, shaft_y2, self.angle - 90, self.shaft_width / 0.6)
//
//        shaft_or_x1, shaft_or_y1 = move_point(shaft_x1, shaft_y1, self.angle + 90, self.shaft_width / 0.6)
//        shaft_or_x2, shaft_or_y2 = move_point(shaft_x2, shaft_y2, self.angle + 90, self.shaft_width / 0.6)
//
//        if not self.distortions:
//            self.shaft_outline_left.points = [shaft_ol_x1, shaft_ol_y1, shaft_ol_x2, shaft_ol_y2]
//            self.shaft_outline_right.points = [shaft_or_x1, shaft_or_y1, shaft_or_x2, shaft_or_y2]
//        else:
//            self.shaft_outline_left.bezier = self.create_distortions(shaft_ol_x1, shaft_ol_y1, shaft_ol_x2,
//                                                                     shaft_ol_y2)
//            self.shaft_outline_right.bezier = self.create_distortions(shaft_or_x1, shaft_or_y1, shaft_or_x2,
//                                                                      shaft_or_y2)
//
//        head_x_tip, head_y_tip = self.to_x, self.to_y
//        head_x1, head_y1 = move_point(self.to_x, self.to_y, self.angle + (180 - self.head_angle / 2.0), self.head_size)
//        head_x2, head_y2 = move_point(self.to_x, self.to_y, self.angle - (180 - self.head_angle / 2.0), self.head_size)
//
//        if self.reverse_arrow:
//            head_length = math.cos((self.head_angle/2)*piby180) * self.head_size
//            head_x_tip, head_y_tip = move_point(head_x_tip, head_y_tip, self.angle+180, head_length)
//            head_x1, head_y1 = move_point(head_x1, head_y1, self.angle, head_length)
//            head_x2, head_y2 = move_point(head_x2, head_y2, self.angle, head_length)
//
//        if self.arrow_at_midpoint:
//            head_x_tip, head_y_tip = move_point(head_x_tip, head_y_tip,
//                                                self.angle+180, self.distance / 2)
//            head_x1, head_y1 = move_point(head_x1, head_y1,
//                                          self.angle + 180, self.distance / 2)
//            head_x2, head_y2 = move_point(head_x2, head_y2,
//                                          self.angle + 180, self.distance / 2)
//
//        self.head.vertices = [
//            head_x_tip,
//            head_y_tip,
//            0,
//            0,
//            head_x1,
//            head_y1,
//            0,
//            0,
//            head_x2,
//            head_y2,
//            0,
//            0,
//        ]
//
//        self.head_outline.points = [
//            self.to_x,
//            self.to_y,
//            head_x1,
//            head_y1,
//            head_x2,
//            head_y2,
//            self.to_x,
//            self.to_y
//        ]
//
//        self.fletching.pos = move_point(self.o_x,
//                                        self.o_y,
//                                        225,
//                                        self.fletching_radius)
//
//        self.fletching.size = [self.fletching_radius * math.sqrt(2)] * 2
//        self.fletching_outline.ellipse = (
//            self.fletching.pos[0],
//            self.fletching.pos[1],
//            self.fletching_radius * math.sqrt(2),
//            self.fletching_radius * math.sqrt(2),
//        )

@Composable
internal fun ArrowLine(
    angle: Float,
    length: Dp,
    modifier: Modifier = Modifier,
    width: Dp = 5.dp,
    startX: Dp = 0.dp,
    startY: Dp = 0.dp,
) {

    val vector = KVector(angle = angle, length = length, startX = startX, startY = startY)
    Canvas(
        modifier = modifier
            .width(length)
            .height(length)
    ) {
        drawLine(
            start = Offset(x = vector.startX.value, y = vector.startY.value),
            end = Offset(x = vector.endX.value, y = vector.endY.value),
            color = Color.Blue,
            strokeWidth = width.value,
        )
    }
}

@Composable
internal fun ArrowHead(
    angle: Float,
    headSize: Dp,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier
        .width(headSize)
        .height(headSize)
    ) {
        val roundedPolygon = RoundedPolygon(
            numVertices = 3,
            radius = size.minDimension / 2,
            centerX = size.width / 2,
            centerY = size.height / 2,
            rounding = CornerRounding(
                size.minDimension / 10f,
                smoothing = 0.1f
            )
        )
        val roundedPolygonPath = roundedPolygon.toPath().asComposePath()
        drawPath(roundedPolygonPath, color = Color.Black)
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
                modifier = Modifier.constrainAs(arrow){
                    top.linkTo(middle)
                    start.linkTo(center)
                }
            )
        }
    }
}
