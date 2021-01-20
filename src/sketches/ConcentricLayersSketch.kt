package sketches

import BaseSketch
import controls.ControlField.Companion.doubleField
import controls.ControlField.Companion.doublePairField
import controls.ControlField.Companion.intField
import controls.ControlGroup
import controls.ControlGroupable
import coordinate.Arc
import coordinate.Circ
import coordinate.Deg
import sketches.base.EmptyConfig
import sketches.base.LayeredCanvasSketch
import util.RangeWithCurrent.Companion.at
import util.and
import util.atAmountAlong
import util.mapIf
import util.negToPos
import util.zeroTo
import kotlin.math.max
import kotlin.math.min

class ConcentricLayersSketch : LayeredCanvasSketch("ConcentricLayersSketch") {
  inner class TabControls {
    val startAngleDelta =
      doubleField("startAngleDelta", negToPos(180) at 5)

    val angleLengthDelta = doubleField(
      "angleLengthDelta",
      negToPos(16) at 0
    )

    val startAngle = doubleField("startAngle", negToPos(360) at 0)

    val startLength = doubleField("startLength", zeroTo(360) at 0)

    val startCircle = intField("startCircle", 0..100 at 0)
    val endCircle = intField("endCircle", 0..100 at 100)
  }

  private val tabs: List<TabControls> = (1..MAX_LAYERS).map { TabControls() }

  val numCirclesField = intField("numCircles", 1..100 at 50)
  val numCircles get() = numCirclesField.get()

  val startAndEndRadField =
    doublePairField("startEndRad", (zeroTo(500) at 5) and (zeroTo(1000) at 500))
  val startAndEndRad get() = startAndEndRadField.get()

  val spacing = doubleField("spacing", zeroTo(360) at 5)

  val trueNumLayers get() = arcsPerLayer.size
  val trueNumCircles get() = if (arcsPerLayer.size <= 1) 0 else arcsPerLayer[1].size

  init {
    numLayers.set(5)
  }

  override fun getControlsForLayer(index: Int): Array<ControlGroupable> {
    val controls = tabs[index]

    return arrayOf(
      ControlGroup(controls.startCircle, controls.endCircle),
      ControlGroup(controls.startAngle, controls.startAngleDelta),
      ControlGroup(controls.startLength, controls.angleLengthDelta),
    )
  }

  override fun getMaxLayers(): Int = 8

  override fun getGlobalControls(): Array<ControlGroupable> = arrayOf(
    numCirclesField,
    startAndEndRadField,
    spacing
  )

  var arcsPerLayer: List<List<Arc>> = listOf()

  private fun getArcsAtIndex(circleIndex: Int, startLayer: Int): List<Arc> =
    (startLayer until trueNumLayers).map { layer -> arcsPerLayer[layer][circleIndex] }

  private fun getCircle(circleIndex: Int): Circ {
    val (startRad, endRad) = startAndEndRad
    val rad = (startRad..endRad).atAmountAlong(circleIndex / trueNumCircles.toDouble())
    return Circ(center, rad)
  }

  private fun getArcsForLayer(index: Int, c: TabControls, numCirclesFrozen: Int): List<Arc> {
    if (index == 0) return listOf()

    return (0 until numCirclesFrozen).map { circleIndex ->

      val circle = getCircle(circleIndex)
      return@map if (index == 1) Arc(circle)
      else if (circleIndex < c.startCircle.get() || circleIndex > c.endCircle.get())
        Arc(Deg(0), 0.0, circle)
      else Arc(
        Deg(c.startAngle.get() + circleIndex * c.startAngleDelta.get()),
        max(0.0,
          min(
            360.0,
            c.startLength.get() + circleIndex * c.angleLengthDelta.get())),
        circle
      )
    }
  }

  override fun drawSetup(sketchConfig: EmptyConfig) {
    val numCirclesFrozen = numCircles
    arcsPerLayer = (0 until numLayers.get()).map { getArcsForLayer(it, tabs[it], numCirclesFrozen) }
  }

  override fun drawOnce(layer: Int) {
    getArcsForLayer(layer, tabs[layer], trueNumCircles)
      .forEachIndexed { circleIndex, layerArc ->
        layerArc
          .minusAll(
            getArcsAtIndex(circleIndex, layer + 1)
              .mapIf({ !it.isSizeZero }) { it.expandPixels(spacing.get()) }
          )
          .forEach { arc ->
            arc
              .walk(1.0)
              .draw(boundRect)
          }
      }
  }
}

fun main() = BaseSketch.run(ConcentricLayersSketch())