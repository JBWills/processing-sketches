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
import geomerativefork.src.RShape
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

  var unionShape: RShape? = null
  override fun drawOnce(layer: Int) {
    if (layer == 0) return

    (0..10).forEach { idx ->
      var s = Circ(center, idx * 25)
        .warped(Noise(noise, offset = noise.offset + (1000 * idx)))
        .toRShape()

      if (unionShape == null) {
        unionShape = RShape(s)
      } else {
        unionShape?.let {
          val sDiffed = s.diff(it)
          unionShape = it.union(s)
          s = sDiffed
        }
      }

      s.draw()
    }

    unionShape = null
  }

  override fun getControlsForLayer(index: Int): Array<ControlGroupable> =
    arrayOf(ControlGroup(listOf()))
}

fun main() = BaseSketch.run(GeomerativeSketch())
