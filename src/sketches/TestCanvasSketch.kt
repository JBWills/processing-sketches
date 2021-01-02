package sketches

import BaseSketch
import LayerConfig
import controls.ControlField.Companion.booleanField
import controls.ControlField.Companion.doubleField
import controls.ControlField.Companion.doublePairField
import controls.ControlField.Companion.intField
import controls.ControlField.Companion.pointField
import controls.ControlGroup
import controls.ControlTab
import coordinate.Point
import coordinate.Segment
import coordinate.Spiral
import sketches.base.CanvasSketch
import util.print.Pen
import util.squared
import util.toRadians
import kotlin.math.sin

class TestCanvasSketch : CanvasSketch("TestCanvasSketch") {

  val START_END_POINT_RANGE = Point(-1000, -1000)..Point(1000, 1000)

  var boundBoxCenter = doublePairField("boundBoxCenter", Point.Half)
  var boundBoxScale = doublePairField("boundBoxScale", Point(0.8, 0.8))
  var startAngle = doubleField("startAngle")
  var angleLength = doubleField("angleLength", startVal = 1.0, range = 0.0..1000.0)
  var drawBoundRect = booleanField("drawBoundRect", true)
  var startPoint = pointField("startPoint", range = START_END_POINT_RANGE)
  var endPoint = pointField("endPoint", range = START_END_POINT_RANGE)

  var sinMinAmplitude = doubleField("sinMinAmplitude", range = 0.0..10.0)
  var sinAmplitude = doubleField("sinAmplitude", startVal = 100.0, range = 0.0..400.0)
  var sinFreq = doubleField("sinFreq", startVal = 4.0, range = 0.1..100.0)
  var numStars = intField("numStars", startVal = 1, range = 1..10)

  init {
    markDirty()
  }

  override fun getControlTabs(): Array<ControlTab> = arrayOf(
    ControlTab("page setup", *super.getControls().toTypedArray()),
    ControlTab(
      "controls",
      ControlGroup(drawBoundRect, heightRatio = 0.5),
      boundBoxCenter, boundBoxScale,
      ControlGroup(startAngle, angleLength),
      ControlGroup(startPoint, endPoint),
      ControlGroup(sinMinAmplitude, sinAmplitude, sinFreq, numStars)
    )
  )

  override fun getLayers(): List<LayerConfig> = listOf(LayerConfig(Pen.WhiteGellyThick), LayerConfig(Pen.WhiteGellyThin))

  override fun drawOnce(layer: Int) {
    noFill()
    val boundRect = paper.toBoundRect().scale(boundBoxScale.get(), newCenter = boundBoxCenter.get() * Point(sizeX, sizeY))
    if (layer == 0) {
      rect(boundRect)
      return
    }

    val startToEndLine = Segment(center + startPoint.get(), center + endPoint.get())

    for (i in 1..numStars.get()) {
      val starSpacing = 1 / (sinFreq.get().toInt().toDouble() * numStars.get())

      val currStartAngle = startAngle.get() + (starSpacing * (i - 1))

      Spiral(
        originFunc = { t, percent, deg -> startToEndLine.getPointAtPercent(percent) },
        lengthFunc = { t, percent, deg ->
          (t).squared()
          val rad = ((t - currStartAngle) * 360).toRadians()
          (sin(sinFreq.get().toInt() * rad) + sinMinAmplitude.get()) * sinAmplitude.get()
        },
        rotationsRange = currStartAngle..(angleLength.get() + currStartAngle))
        .walk(0.0001)
        .draw(boundRect)
    }

  }
}

fun main() = BaseSketch.run(TestCanvasSketch())