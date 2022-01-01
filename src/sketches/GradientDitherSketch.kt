package sketches

import appletExtensions.withStyle
import controls.controlsealedclasses.Button.Companion.button
import controls.controlsealedclasses.Slider.Companion.slider
import controls.controlsealedclasses.Slider2D.Companion.slider2D
import controls.controlsealedclasses.Toggle.Companion.toggle
import controls.panels.TabStyle.Companion.toTabStyle
import controls.panels.TabsBuilder.Companion.tabs
import controls.props.PropData
import controls.props.types.PenProp
import coordinate.Point
import kotlinx.serialization.Serializable
import sketches.base.SimpleCanvasSketch
import util.print.Pen
import util.randItem

/**
 * Starter sketch that uses all the latest bells and whistles.
 *
 * Copy and paste this to create a new sketch.
 */
class GradientDitherSketch : SimpleCanvasSketch<GradientDitherData>(
  "GradientDither",
  GradientDitherData(),
) {
  override suspend fun SequenceScope<Unit>.drawLayers(drawInfo: DrawInfo) {
    val (points, radius, drawAsScribbles) = drawInfo.dataValues.pointData

    points.forEach {
      withStyle(it.penProp.style) {
        boundRect.pointAt(it.center).draw(radius)
      }
    }
  }
}

@Serializable
data class GradientPoint(
  var center: Point = Point(0.5, 0.5),
  var penProp: PenProp = PenProp(pen = Pen.GellyColors.randItem(), filterByWeight = true)
  // consider adding intensity field here
) {
  constructor(p: GradientPoint) : this(p.center.copy(), p.penProp.clone())
}


@Serializable
data class PointData(
  val points: MutableList<GradientPoint> = mutableListOf(),
  var radius: Double = 1.0,
  var drawAsScribbles: Boolean = false
)

private const val GlobalTabName = "Global"

@Serializable
data class GradientDitherData(
  val pointData: PointData = PointData(),
) : PropData<GradientDitherData> {
  val points get() = pointData.points
  override fun bind() = tabs {
    fun getPointTabName(index: Int) = "p$index"
    tab(GlobalTabName) {
      row {
        button("Add New Point") {
          points.add(GradientPoint())
          updateControls(newTabName = getPointTabName(points.size - 1))
        }
        button("Clear Points") {
          points.clear()
          updateControls()
        }
      }

      row {
        toggle(pointData::drawAsScribbles)
        slider(pointData::radius, 0..10)
      }
    }

    tabs(
      points,
      getTabName = { index, _ -> getPointTabName(index) },
      getTabStyle = { index, gradientPoint -> gradientPoint.penProp.pen.toTabStyle() },
    ) { index, gradientPoint ->
      row {
        heightRatio = 0.5
        button("Refresh Controls Panel") { updateControls() }
      }
      row {
        button("Clone") {
          points.add(GradientPoint(gradientPoint))
          updateControls(newTabName = getPointTabName(points.size - 1))
        }

        button("Delete") {
          points.removeAt(index)
          val newIndex = if (points.size > index) index else index - 1
          val newTabName =
            if (points.indices.contains(newIndex)) getPointTabName(newIndex)
            else GlobalTabName
          updateControls(newTabName = newTabName)
        }
      }

      row {
        panel(gradientPoint::penProp)
      }

      slider2D(gradientPoint::center, -2..2).withHeight(2)
    }
  }

  override fun clone() = copy()

  override fun toSerializer() = serializer()
}

fun main() = GradientDitherSketch().run()
