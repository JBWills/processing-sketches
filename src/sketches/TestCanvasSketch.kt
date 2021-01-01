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
import util.randomColor
import util.squared
import util.toRadians
import kotlin.math.sin

class TestCanvasSketch : CanvasSketch("TestCanvasSketch") {

  var boundBoxCenter = Point(0.5, 0.5)
  var boundBoxScale = Point(0.6, 0.8)
  var startAngle = 0.0
  var angleLength = 1.0
  var drawBoundRect = true
  var startPoint = Point.Zero
  var endPoint = Point.Zero

  var sinMinAmplitude = 0.0
  var sinAmplitude = 140.0
  var sinFreq = 10.0

  var numStars = 3

  init {
    markDirty()
  }

  override fun getControls(): List<ControlGroup> = listOf(
    *super.getControls().toTypedArray(),
    ControlGroup(
      propertyToggle(::drawBoundRect)
    ),
    ControlGroup(
      *propertySliderPair(::boundBoxCenter)
    ),
    ControlGroup(
      *propertySliderPair(::boundBoxScale)
    ),
    ControlGroup(propertySlider(::startAngle), propertySlider(::angleLength, 0.0..1000.0)),
    ControlGroup(property2DSlider(::startPoint, -1000.0..1000.0, -1000.0..1000.0), property2DSlider(::endPoint, -1000.0..1000.0, -1000.0..1000.0)),
    ControlGroup(propertySlider(::sinMinAmplitude, 0.0..50.0), propertySlider(::sinAmplitude, 0.0..400.0), propertySlider(::sinFreq, 0.1..100.0), propertySlider(::numStars, 1..10))
  )

  override fun drawOnce() {
    noFill()
    val boundRect = paper.toBoundRect().scale(boundBoxScale, newCenter = boundBoxCenter * Point(sizeX, sizeY))
    if (drawBoundRect) rect(boundRect)

    val startToEndLine = Segment(center + startPoint, center + endPoint)

    for (i in 1..numStars) {
      val starSpacing = 1 / (sinFreq.toInt().toDouble() * numStars)

      val currStartAngle = startAngle + (starSpacing * (i - 1))
      println(currStartAngle)
      if (i != 1) {
        stroke(randomColor())
      }

      Spiral(
        originFunc = { t, percent, deg -> startToEndLine.getPointAtPercent(percent) },
        lengthFunc = { t, percent, deg ->
          (t).squared()
          val rad = ((t - currStartAngle) * 360).toRadians()
          (sin(sinFreq.toInt() * rad) + sinMinAmplitude) * sinAmplitude
        },
        rotationsRange = currStartAngle..(angleLength + currStartAngle))
        .walk(0.0001)
        .draw(boundRect)
    }

  }
}

fun main() = BaseSketch.run(TestCanvasSketch())