package sketches

import BaseSketch
import appletExtensions.withStroke
import controls.ControlTab.Companion.tab
import controls.doubleProp
import controls.noiseProp
import controls.props.PropData
import fastnoise.Noise
import kotlinx.serialization.Serializable
import sketches.base.LayeredCanvasSketch
import util.ZeroToOne
import util.lightened
import util.noiseContour
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
    val (noise, threshold) = values.tabValues
    val contour = noise.noiseContour(boundRect, threshold)
    contour.forEachIndexed { i, p ->
      withStroke(Color.black.lightened(i / contour.size.toFloat())) {
        p.draw()
      }
    }
  }
}

@Serializable
data class BlobLayerData(
  var noise: Noise = Noise.DEFAULT,
  var threshold: Double = 0.5,
) : PropData<BlobLayerData> {
  override fun BaseSketch.bind() = listOf(
    tab(
      "L",
      noiseProp(::noise, showStrengthSliders = false),
      doubleProp(::threshold, ZeroToOne),
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
