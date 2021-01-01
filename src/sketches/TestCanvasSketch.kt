package sketches

import BaseSketch
import controls.ControlGroup
import coordinate.Point
import coordinate.Segment
import coordinate.Spiral
import sketches.base.CanvasSketch
import util.property2DSlider
import util.propertySlider
import util.propertySliderPair
import util.propertyToggle
import util.squared

class TestCanvasSketch : CanvasSketch("TestCanvasSketch") {

  var boundBoxCenter = Point(0.5, 0.5)
  var boundBoxScale = Point(0.6, 0.8)
  var startAngle = 0.0
  var angleLength = 100.0
  var drawBoundRect = true
  var startPoint = Point.Zero
  var endPoint = Point.Zero

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
    ControlGroup(propertySlider(::startAngle), propertySlider(::angleLength, 0.0..1000.0)),
    ControlGroup(property2DSlider(::startPoint, -1000.0..1000.0, -1000.0..1000.0), property2DSlider(::endPoint, -1000.0..1000.0, -1000.0..1000.0)),
    ControlGroup(
      propertyToggle(::drawBoundRect)
    )
  )

  override fun drawOnce() {
    noFill()
    val boundRect = paper.toBoundRect().scale(boundBoxScale, newCenter = boundBoxCenter * Point(sizeX, sizeY))
    if (drawBoundRect) rect(boundRect)

    val startToEndLine = Segment(center + startPoint, center + endPoint)
    Spiral(
      originFunc = { t, percent, deg -> startToEndLine.getPointAtPercent(percent) },
      lengthFunc = { t, percent, deg ->
        (t - startAngle).squared()
      },
      rotationsRange = startAngle..(angleLength + startAngle))
      .walk(0.001)
      .draw(boundRect)
  }
}

fun main() = BaseSketch.run(TestCanvasSketch())