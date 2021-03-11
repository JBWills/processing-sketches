package sketches

import BaseSketch
import appletExtensions.getParallelLinesInBound
import controls.ControlTab.Companion.layerTab
import controls.ControlTab.Companion.tab
import controls.degProp
import controls.doublePairProp
import controls.doubleProp
import controls.enumProp
import controls.props.PropData
import coordinate.Circ
import coordinate.Deg
import coordinate.Point
import kotlinx.serialization.Serializable
import sketches.MoireShape.Circle
import sketches.MoireShape.Rectangle
import sketches.base.LayeredCanvasSketch
import util.atAmountAlong

enum class MoireShape {
  Rectangle,
  Circle,
}

/**
 * Create a Moire pattern interaction between two shapes
 */
class MoireLines : LayeredCanvasSketch<MoireLinesData, MoireLinesLayerData>(
  "MoireLines",
  MoireLinesData(),
  { MoireLinesLayerData() }
) {
  init {
    numLayers = 2
  }

  override fun drawSetup(layerInfo: DrawInfo) {}

  override fun drawOnce(values: LayerInfo) {
    val (baseData) = values.globalValues
    val (shape, lineDensity, lineAngle, lineOffset, shapeSize, shapeCenter) = values.tabValues

    val centerPoint = boundRect.size * shapeCenter

    val distanceBetweenLines = (50.0..0.5).atAmountAlong(lineDensity)

    when (shape) {
      Rectangle -> {
        val r = boundRect.scale(shapeSize, centerPoint)

        getParallelLinesInBound(
          r,
          lineAngle,
          distanceBetweenLines,
          lineOffset * distanceBetweenLines
        )
      }
      Circle -> {
        val c = Circ(centerPoint, shapeSize.x)
        getParallelLinesInBound(
          c.bounds,
          lineAngle,
          distanceBetweenLines,
          lineOffset * distanceBetweenLines
        ).mapNotNull { c.bound(it) }
      }
    }.draw(boundRect)
  }
}

@Serializable
data class MoireLinesLayerData(
  var shape: MoireShape = Rectangle,
  var lineDensity: Double = 0.5,
  var lineAngle: Deg = Deg.VERTICAL,
  var lineOffset: Double = 0.0,
  var shapeSize: Point = Point.One,
  var shapeCenter: Point = Point.Half,
) : PropData<MoireLinesLayerData> {
  override fun BaseSketch.bind() = layerTab(
    enumProp(::shape),
    doubleProp(::lineDensity, 0.0..1.0),
    degProp(::lineAngle, 0.0..90.0),
    doubleProp(::lineOffset, 0.0..1.0),
    doublePairProp(::shapeSize, -0.0..2.0),
    doublePairProp(::shapeCenter, -2.0..2.0),
  )

  override fun clone() = copy()
  override fun toSerializer() = serializer()
}

@Serializable
data class MoireLinesData(
  var lineDensity: Double = 0.5,
) : PropData<MoireLinesData> {
  override fun BaseSketch.bind() = listOf(
    tab(
      "Global",
    )
  )

  override fun clone() = copy()

  override fun toSerializer() = serializer()
}

fun main() = MoireLines().run()
