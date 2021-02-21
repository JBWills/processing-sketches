package sketches

import controls.ControlField.Companion.doubleField
import controls.ControlField.Companion.doublePairField
import controls.ControlField.Companion.intField
import controls.ControlGroup
import controls.ControlGroupable
import controls.PropFields
import controls.Props
import controls.controls
import coordinate.Arc
import coordinate.Circ
import coordinate.Deg
import sketches.ConcentricLayersSketch.GlobalValues
import sketches.ConcentricLayersSketch.TabValues
import sketches.base.LayeredCanvasSketch
import util.and
import util.atAmountAlong
import util.mapIf
import util.negToPos
import util.zeroTo
import kotlin.math.max
import kotlin.math.min

const val MAX_CIRCLES = 8

class ConcentricLayersSketch : LayeredCanvasSketch<TabValues, GlobalValues>(
  "ConcentricLayersSketch",
  maxLayers = MAX_CIRCLES
) {
  val trueNumLayers get() = arcsPerLayer.size
  val trueNumCircles get() = if (arcsPerLayer.size <= 1) 0 else arcsPerLayer[1].size

  init {
    numLayers.set(5)
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
        max(0.0,
          min(
            360.0,
            c.startLength + circleIndex * c.angleLengthDelta)),
        circle
      )
    }
  }

  override fun drawSetup(drawInfo: DrawInfo) {
    arcsPerLayer =
      (0 until numLayers.get()).map { getArcsForLayer(it, drawInfo.allTabValues[it], drawInfo.globalValues) }
  }

  override fun drawOnce(
    layerInfo: LayerInfo,
  ) = getArcsForLayer(layerInfo.layerIndex, layerInfo.tabValues, layerInfo.globalValues)
    .flatMapIndexed { circleIndex, layerArc ->
      layerArc.minusAll(
        getArcsAtIndex(circleIndex, layerInfo.layerIndex + 1)
          .mapIf({ !it.isSizeZero }) { it.expandPixels(layerInfo.globalValues.spacing) }
      )
    }.forEach { arc ->
      arc
        .walk(1.0)
        .draw(boundRect)
    }

  inner class TabValues(
    val startAngleDelta: Double = 5.0,
    val angleLengthDelta: Double = 0.0,
    val startAngle: Double = 0.0,
    val startLength: Double = 0.0,
    val startCircle: Int = 0,
    val endCircle: Int = 0,
  )

  inner class GlobalValues(
    val numCircles: Int = 50,
    val startRad: Double = 5.0,
    val endRad: Double = 500.0,
    val spacing: Double = 5.0,
  )

  override fun initProps(): Props<TabValues, GlobalValues> =
    object : Props<TabValues, GlobalValues>(maxLayers) {
      override fun globalControls(): PropFields<GlobalValues> =
        object : PropFields<GlobalValues>() {
          private val defaults = GlobalValues()
          val numCirclesField = intField(defaults::numCircles, 1..100)

          val startAndEndRadField =
            doublePairField(defaults::startRad, defaults::endRad,
              zeroTo(500) and zeroTo(1000))

          val spacingField = doubleField(defaults::spacing, zeroTo(360))

          override fun toControls(): List<ControlGroupable> = controls(
            numCirclesField,
            startAndEndRadField,
            spacingField
          )

          override fun toValues(): GlobalValues = GlobalValues(
            numCircles = numCirclesField.get(),
            startRad = startAndEndRadField.get().x,
            endRad = startAndEndRadField.get().y,
            spacing = spacingField.get()
          )
        }

      override fun tabControls(tabIndex: Int): PropFields<TabValues> =
        object : PropFields<TabValues>() {
          private val defaults = TabValues()

          private val startAngleDelta =
            doubleField(defaults::startAngleDelta, negToPos(180))
          private val angleLengthDelta = doubleField(defaults::angleLengthDelta, negToPos(16))
          private val startAngle = doubleField(defaults::startAngle, negToPos(360))
          private val startLength = doubleField(defaults::startLength, zeroTo(360))
          private val startCircle = intField(defaults::startCircle, 0..100)
          private val endCircle = intField(defaults::endCircle, 0..100)

          override fun toControls(): List<ControlGroupable> = controls(
            ControlGroup(startCircle, endCircle),
            ControlGroup(startAngle, startAngleDelta),
            ControlGroup(startLength, angleLengthDelta),
          )

          override fun toValues() = TabValues(
            startAngleDelta = startAngleDelta.get(),
            angleLengthDelta = angleLengthDelta.get(),
            startAngle = startAngle.get(),
            startLength = startLength.get(),
            startCircle = startCircle.get(),
            endCircle = endCircle.get(),
          )
        }
    }
}

fun main() = ConcentricLayersSketch().run()
