package sketches

import PointInterpolator1D
import PointInterpolator1D.CubicSpline2D
import appletExtensions.withFill
import appletExtensions.withStroke
import controls.controlsealedclasses.Button.Companion.button
import controls.controlsealedclasses.Dropdown.Companion.dropdown
import controls.controlsealedclasses.Slider.Companion.slider
import controls.controlsealedclasses.Toggle.Companion.toggle
import controls.panels.ControlStyle.Companion.Yellow
import controls.panels.TabsBuilder.Companion.singleTab
import controls.props.PropData
import coordinate.Point
import coordinate.Segment
import kotlinx.serialization.Serializable
import sketches.base.SimpleCanvasSketch
import util.base.letWith
import util.interpolation.InterpolationFunction1D
import util.interpolation.Interpolator1DType
import util.interpolation.Interpolator1DType.CubicSpline1DType
import util.iterators.meanByOrNull
import util.layers.LayerSVGConfig
import util.numbers.ifNan
import util.polylines.PolyLine
import util.polylines.iterators.walk
import java.awt.Color

/**
 * Starter sketch that uses all the latest bells and whistles.
 *
 * Copy and paste this to create a new sketch.
 */
class InterpolationTestSketch : SimpleCanvasSketch<InterpolationTestData>(
  "InterpolationTest",
  InterpolationTestData(),
) {

  override fun mouseClicked(p: Point) {
    super.mouseClicked(p)
    modifyPropsDirectly { props ->
      if (props.data.appendToEnd) {
        props.data.points = props.data.points + p
      } else {
        props.data.points = p + props.data.points
      }

      return@modifyPropsDirectly true
    }
  }

  override fun drawLayers(drawInfo: DrawInfo, onNextLayer: (LayerSVGConfig) -> Unit) {
    val (points, _, forceEquidistantPoints, equidistantPointsWidth, interpolation, interpolation2D, interpolationSampleStep) = drawInfo.dataValues

    val pointsMapped = if (forceEquidistantPoints) {
      val baseLine =
        Segment(
          Point(0, 0),
          Point(boundRect.width * equidistantPointsWidth, 0),
        ).plus(boundRect.topLeft)
          .plus(
            Point(
              (boundRect.width * (1 - equidistantPointsWidth)) / 2,
              boundRect.height / 2,
            ),
          )
          .also { it.drawSegment() }
      val avgY = points.meanByOrNull { it.y } ?: 0.0
      points.mapIndexed { index, point ->
        val percent = (index.toDouble() / (points.size - 1)).ifNan { 0.5 }
        baseLine.getPointAtPercent(percent).addY(point.y - avgY)
      }
    } else {
      points
    }

    fun getTextLocation(index: Int): Pair<Float, Float> =
      boundRect.topLeft
        .addY(-index * 15)
        .letWith { xf to yf }

    withStroke(Color.RED) {
      pointsMapped.draw(boundRect)
      pointsMapped.drawPoints(3)
      val (x, y) = getTextLocation(3)
      withFill(Color.RED) { text("Red: original", x, y) }
    }

    fun drawInterpolatedLine(interpolator: InterpolationFunction1D) {
      if (points.size > 3) {
        interpolator.setData(pointsMapped)
        points.walk(interpolationSampleStep) {
          Point(it.x, interpolator.interpolate(it.x))
        }.draw(boundRect)
      }
    }

    withStroke(Color.GREEN) {
      drawInterpolatedLine(interpolation.create())

      val (x, y) = getTextLocation(2)
      withFill(Color.GREEN) {
        text("Green: 1d interpolated with: ${interpolation.name}", x, y)
      }
    }

    withStroke(Color.YELLOW) {
      drawInterpolatedLine(interpolation2D)

      val (x, y) = getTextLocation(1)
      withFill(Color.YELLOW) {
        text("Yellow: 2d interpolated with: ${interpolation2D.name}", x, y)
      }
    }
  }
}

@Serializable
data class InterpolationTestData(
  var points: PolyLine = listOf(),
  var appendToEnd: Boolean = true,
  var forceEquidistantPoints: Boolean = true,
  var equidistantPointsWidth: Double = 1.0,
  var interpolation: Interpolator1DType = CubicSpline1DType,
  var interpolation2D: PointInterpolator1D = CubicSpline2D,
  var interpolationSampleStep: Double = 1.0,
) : PropData<InterpolationTestData> {
  override fun bind() = singleTab("Global") {
    row(style = Yellow) {
      button("Clear Points") { points = listOf(); markDirty() }
      button("Delete start") {
        if (points.isNotEmpty()) {
          points = points.slice(1 until points.size)
          markDirty()
        }
      }
      button("Delete end") {
        if (points.isNotEmpty()) {
          points = points.slice(0 until (points.size - 1))
          markDirty()
        }
      }
    }

    row {
      toggle(::appendToEnd)
      toggle(::forceEquidistantPoints)
    }

    slider(::equidistantPointsWidth, 0..1)

    row {
      heightRatio = 3
      dropdown(::interpolation)
      dropdown(::interpolation2D)
    }
  }

  override fun clone() = copy()

  override fun toSerializer() = serializer()
}

fun main() = InterpolationTestSketch().run()
