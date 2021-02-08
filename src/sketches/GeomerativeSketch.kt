package sketches

import BaseSketch
import controls.ControlGroup
import controls.ControlGroupable
import controls.noiseControls
import coordinate.Circ
import coordinate.Point
import fastnoise.FastNoise.NoiseType.Perlin
import fastnoise.Noise
import fastnoise.Noise.Companion.warped
import fastnoise.NoiseQuality.High
import sketches.base.LayeredCanvasSketch
import util.geomutil.toRShape

class GeomerativeSketch : LayeredCanvasSketch("GeomerativeSketch") {

  private var noise: Noise = Noise(
    seed = 100,
    noiseType = Perlin,
    quality = High,
    scale = 0.15,
    offset = Point.Zero,
    strength = Point(0, 0)
  )

  override fun getGlobalControls(): Array<ControlGroupable> = arrayOf(*noiseControls(::noise))

  init {
    numLayers.set(1)
  }

  override fun drawOnce(layer: Int) {
    if (layer == 0) return

    Circ(center, 100).warped(noise).toRShape().draw()
  }

  override fun getControlsForLayer(index: Int): Array<ControlGroupable> =
    arrayOf(ControlGroup(listOf()))
}

fun main() = BaseSketch.run(GeomerativeSketch())
