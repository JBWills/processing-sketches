package sketches

import BaseSketch
import controls.ControlGroup
import coordinate.Point
import coordinate.Spiral
import sketches.base.CanvasSketch
import util.propertySliderPair
import util.propertyToggle

class TestCanvasSketch : CanvasSketch("TestCanvasSketch") {

  var boundBoxCenter = Point(0.5, 0.5)
  var boundBoxScale = Point(0.6, 0.8)
  var drawBoundRect = true

  init {
    markDirty()
  }

  override fun getControls(): List<ControlGroup> = listOf(
    *super.getControls().toTypedArray(),
    ControlGroup(
      *propertySliderPair(::boundBoxCenter)
    ),
    ControlGroup(
      *propertySliderPair(::boundBoxScale)
    ),
    ControlGroup(
      propertyToggle(::drawBoundRect)
    )
  )

  override fun drawOnce() {
    noFill()
    val boundRect = paper.toBoundRect().scale(boundBoxScale, newCenter = boundBoxCenter * Point(sizeX, sizeY))
    if (drawBoundRect) rect(boundRect)

    Spiral(
      originFunc = { t, percent, deg -> boundRect.center },
      lengthFunc = { t, percent, deg -> t * 20 },
      rotationsRange = 0.0..100.0)
      .walk(0.01)
      .draw(boundRect)
  }
}

fun main() = BaseSketch.run(TestCanvasSketch())