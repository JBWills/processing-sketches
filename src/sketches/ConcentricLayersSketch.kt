package sketches

import controls.panels.TabsBuilder.Companion.layerTab
import controls.panels.TabsBuilder.Companion.singleTab
import controls.controlsealedclasses.Slider.Companion.slider
import controls.props.PropData
import coordinate.Arc
import coordinate.Circ
import coordinate.Deg
import kotlinx.serialization.Serializable
import sketches.base.LayeredCanvasSketch
import util.atAmountAlong
import util.base.negToPos
import util.base.zeroTo
import util.iterators.mapIf
import util.numbers.bound

const val MAX_CIRCLES = 8

class ConcentricLayersSketch :
  LayeredCanvasSketch<CircleGlobalData, CircleLayerData>(
    "ConcentricLayersSketch",
    CircleGlobalData(),
    { CircleLayerData() },
    maxLayers = MAX_CIRCLES,
  ) {
  val trueNumLayers get() = arcsPerLayer.size
  val trueNumCircles get() = if (arcsPerLayer.size <= 1) 0 else arcsPerLayer[1].size

  init {
    numLayers = 5
  }

  var arcsPerLayer: List<List<Arc>> = listOf()

  private fun getArcsAtIndex(circleIndex: Int, startLayer: Int): List<Arc> =
    (startLayer until trueNumLayers).map { layer -> arcsPerLayer[layer][circleIndex] }

  private fun getCircle(
    circleIndex: Int,
    circleGlobalData: CircleGlobalData,
  ): Circ {
    val (startRad, endRad) = circleGlobalData.startRad to circleGlobalData.endRad
    val rad =
      (startRad..endRad).atAmountAlong(circleIndex / trueNumCircles.toDouble())
    return Circ(center, rad)
  }

  private fun getArcsForLayer(
    index: Int,
    c: CircleLayerData,
    g: CircleGlobalData,
  ): List<Arc> {
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
        circle,
      )
    }
  }

  override fun drawSetup(layerInfo: DrawInfo) {
    arcsPerLayer =
      (0 until numLayers).map {
        getArcsForLayer(
          it,
          layerInfo.allTabValues[it],
          layerInfo.globalValues,
        )
      }
  }

  override fun drawOnce(
    layerInfo: LayerInfo,
  ) = getArcsForLayer(
    layerInfo.layerIndex,
    layerInfo.tabValues,
    layerInfo.globalValues,
  )
    .flatMapIndexed { circleIndex, layerArc ->
      layerArc.minusAll(
        getArcsAtIndex(circleIndex, layerInfo.layerIndex + 1)
          .mapIf({ !it.isSizeZero }) { it.expandPixels(layerInfo.globalValues.spacing) },
      )
    }.forEach {
      it
        .walk(1.0)
        .draw(boundRect)
    }
}

@Serializable
data class CircleLayerData(
  var startAngleDelta: Double = 5.0,
  var angleLengthDelta: Double = 0.0,
  var startAngle: Double = 0.0,
  var startLength: Double = 0.0,
  var startCircle: Int = 0,
  var endCircle: Int = 0,
) : PropData<CircleLayerData> {
  override fun bind() = layerTab {
    slider(::startAngleDelta, negToPos(180))
    slider(::angleLengthDelta, negToPos(16))
    slider(::startAngle, negToPos(360))
    slider(::startLength, zeroTo(360))
    slider(::startCircle, 0..100)
    slider(::endCircle, 0..100)
  }

  override fun clone() = copy()
  override fun toSerializer() = serializer()
}

@Serializable
data class CircleGlobalData(
  var numCircles: Int = 50,
  var startRad: Double = 5.0,
  var endRad: Double = 500.0,
  var spacing: Double = 5.0,
) : PropData<CircleGlobalData> {
  override fun bind() = singleTab("Global") {
    slider(::numCircles, 1..100)

    row {
      slider(::startRad, zeroTo(500))
      slider(::endRad, zeroTo(1000))
    }

    slider(::spacing, zeroTo(360))
  }

  override fun clone() = copy()

  override fun toSerializer() = serializer()
}

fun main() = ConcentricLayersSketch().run()
