package sketches

import BaseSketch
import controls.ControlGroup
import controls.ControlGroupable
import controls.controls
import controls.doubleProp
import controls.intProp
import coordinate.Arc
import coordinate.Circ
import coordinate.Deg
import geomerativefork.src.util.bound
import interfaces.Bindable
import sketches.base.LayeredCanvasSketch
import util.atAmountAlong
import util.mapIf
import util.negToPos
import util.zeroTo

const val MAX_CIRCLES = 8

class ConcentricLayersSketch : LayeredCanvasSketch<TabValues, GlobalValues>(
  "ConcentricLayersSketch",
  GlobalValues(),
  { TabValues() },
  maxLayers = MAX_CIRCLES
) {
  val trueNumLayers get() = arcsPerLayer.size
  val trueNumCircles get() = if (arcsPerLayer.size <= 1) 0 else arcsPerLayer[1].size

  init {
    numLayers = 5
  }

  var arcsPerLayer: List<List<Arc>> = listOf()

  private fun getArcsAtIndex(circleIndex: Int, startLayer: Int): List<Arc> =
    (startLayer until trueNumLayers).map { layer -> arcsPerLayer[layer][circleIndex] }

  private fun getCircle(circleIndex: Int, globalValues: GlobalValues): Circ {
    val (startRad, endRad) = globalValues.startRad to globalValues.endRad
    val rad = (startRad..endRad).atAmountAlong(circleIndex / trueNumCircles.toDouble())
    return Circ(center, rad)
  }

  private fun getArcsForLayer(index: Int, c: TabValues, g: GlobalValues): List<Arc> {
    if (index == 0) return listOf()

    return (0 until g.numCircles).map { circleIndex ->
      val circle = getCircle(circleIndex, g)
      return@map if (index == 1) Arc(circle)
      else if (circleIndex < c.startCircle || circleIndex > c.endCircle)
        Arc(Deg(0), 0.0, circle)
      else Arc(
        Deg(c.startAngle + circleIndex * c.startAngleDelta),
        (c.startLength + circleIndex * c.angleLengthDelta)
          .bound(0.0, 360.0),
        circle
      )
    }
  }

  override fun drawSetup(layerInfo: DrawInfo) {
    arcsPerLayer =
      (0 until numLayers).map {
        getArcsForLayer(
          it,
          layerInfo.allTabValues[it],
          layerInfo.globalValues)
      }
  }

  override fun drawOnce(
    values: LayerInfo,
  ) = getArcsForLayer(values.layerIndex, values.tabValues, values.globalValues)
    .flatMapIndexed { circleIndex, layerArc ->
      layerArc.minusAll(
        getArcsAtIndex(circleIndex, values.layerIndex + 1)
          .mapIf({ !it.isSizeZero }) { it.expandPixels(values.globalValues.spacing) }
      )
    }.forEach { arc ->
      arc
        .walk(1.0)
        .draw(boundRect)
    }
}

data class TabValues(
  var startAngleDelta: Double = 5.0,
  var angleLengthDelta: Double = 0.0,
  var startAngle: Double = 0.0,
  var startLength: Double = 0.0,
  var startCircle: Int = 0,
  var endCircle: Int = 0,
) : Bindable {
  override fun bind(s: BaseSketch): List<ControlGroupable> = controls(
    s.doubleProp(::startAngleDelta, negToPos(180)),
    s.doubleProp(::angleLengthDelta, negToPos(16)),
    s.doubleProp(::startAngle, negToPos(360)),
    s.doubleProp(::startLength, zeroTo(360)),
    s.intProp(::startCircle, 0..100),
    s.intProp(::endCircle, 0..100),
  )
}

data class GlobalValues(
  var numCircles: Int = 50,
  var startRad: Double = 5.0,
  var endRad: Double = 500.0,
  var spacing: Double = 5.0,
) : Bindable {
  override fun bind(s: BaseSketch): List<ControlGroupable> = controls(
    s.intProp(::numCircles, 1..100),
    ControlGroup(
      s.doubleProp(::startRad, zeroTo(500)),
      s.doubleProp(::endRad, zeroTo(1000)),
    ),
    s.doubleProp(::spacing, zeroTo(360)),
  )
}

fun main() = ConcentricLayersSketch().run()
