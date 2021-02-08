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
    RShape.createCircle(center.xf, center.yf, 50f).draw(this)
    RShape.createEllipse(center.xf, center.yf, 100f, 200f).draw(this)
//    RShape.createRectangle(center.xf - 450, center.yf - 450, center.xf + 450, center.yf + 450)
//      .draw(this)
  }

  override fun getControlsForLayer(index: Int): Array<ControlGroupable> =
    arrayOf(ControlGroup(listOf()))
}

fun main() = BaseSketch.run(GeomerativeSketch())
