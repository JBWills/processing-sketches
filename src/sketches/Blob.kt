package sketches

import BaseSketch
import appletExtensions.withStroke
import controls.ControlGroup.Companion.group
import controls.ControlTab.Companion.tab
import controls.doubleProp
import controls.intProp
import controls.noiseProp
import controls.props.PropData
import fastnoise.Noise
import kotlinx.serialization.Serializable
import sketches.base.LayeredCanvasSketch
import util.ZeroToOne
import util.algorithms.contouring.getNoiseContour
import util.algorithms.contouring.mergeSegments
import util.darkened
import util.percentAlong
import util.step
import java.awt.Color

/**
 * Starter sketch that uses all of the latest bells and whistles.
 *
 * Copy and paste this to create a new sketch.
 */
class Blob : LayeredCanvasSketch<BlobData, BlobLayerData>(
  "Blob",
  BlobData(),
  { BlobLayerData() }
) {
  init {
    numLayers = 1
  }

  override fun drawSetup(layerInfo: DrawInfo) {}

  override fun drawOnce(values: LayerInfo) {
    val (noise, gridStep, thresholdStart, thresholdEnd, numThresholds) = values.tabValues

    val contourSegs = getNoiseContour(
      if (numThresholds == 1)
        listOf(thresholdStart)
      else (thresholdStart..thresholdEnd step ((thresholdEnd - thresholdStart) / numThresholds)).map { it },
      boundRect,
      gridStep,
      noise,
    )

    contourSegs.forEach { threshold, v ->
      v.mergeSegments()
        .forEachIndexed { i, line ->
          val percent = (thresholdStart..thresholdEnd).percentAlong(threshold)
          val color = Color.BLUE.darkened(percent.toFloat())
          withStroke(color) { line.draw() }
        }
    }

  }
}

@Serializable
data class BlobLayerData(
  var noise: Noise = Noise.DEFAULT.with(
    scale = 1.07
  ),
  var gridStep: Double = 1.0,
  var thresholdStart: Double = 0.4,
  var thresholdEnd: Double = 0.9,
  var numThresholds: Int = 1,
) : PropData<BlobLayerData> {
  override fun BaseSketch.bind() = listOf(
    tab(
      "L",
      doubleProp(::gridStep, 1.0..30.0),
      noiseProp(::noise, showStrengthSliders = false),
      group(
        doubleProp(::thresholdStart, ZeroToOne),
        doubleProp(::thresholdEnd, ZeroToOne),
      ),
      intProp(::numThresholds, 1..10)
    )
  )

  override fun clone() = copy()
  override fun toSerializer() = serializer()
}

@Serializable
data class BlobData(
  val ExampleGlobalProp: Double = 0.0,
) : PropData<BlobData> {
  override fun BaseSketch.bind() = listOf(
    tab(
      "Global",
    )
  )

  override fun clone() = copy()

  override fun toSerializer() = serializer()
}

fun main() = Blob().run()
