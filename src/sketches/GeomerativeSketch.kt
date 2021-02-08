package sketches

import BaseSketch
import controls.ControlGroup
import controls.ControlGroupable
import geomerativefork.src.RShape
import sketches.base.LayeredCanvasSketch

class GeomerativeSketch : LayeredCanvasSketch("GeomerativeSketch") {

  init {
    numLayers.set(1)
  }

  override fun drawOnce(layer: Int) {
    if (layer == 0) return

    // problem is these all create LINETOs
    RShape.createRectangle(center.toRPoint() - 150, 300f, 300f)
      .intersection(RShape.createCircle(center.toRPoint() - 150, 50))
      .draw(this)

    RShape.createRectangle(center.toRPoint() - 50, 100f, 100f)
      .draw(this)

    RShape.createRectangle(center.toRPoint() - 15, 30f, 30f)
      .draw(this)
  }

  override fun getControlsForLayer(index: Int): Array<ControlGroupable> =
    arrayOf(ControlGroup(listOf()))
}

fun main() = BaseSketch.run(GeomerativeSketch())
