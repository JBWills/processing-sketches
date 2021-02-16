package sketches

import controls.Control.Button
import controls.ControlField.Companion.doubleField
import controls.ControlField.Companion.intField
import controls.ControlField.Companion.pointField
import controls.ControlGroup
import controls.ControlGroupable
import coordinate.Point
import coordinate.Segment
import coordinate.Spiral
import sketches.base.LayeredCanvasSketch
import util.ZeroToOne
import util.pow
import util.times

class TestCanvasSketch : LayeredCanvasSketch("TestCanvasSketch") {

  val START_END_POINT_RANGE = (Point.NegativeToPositive..Point.NegativeToPositive) * 1000

  private val tabs: List<TabControls> = (1..MAX_LAYERS).map { TabControls() }

  inner class TabControls {
    var startAngle = doubleField("startAngle")
    var angleLength = doubleField("angleLength",
      startVal = 1.0,
      range = ZeroToOne * 1000)
    var startPoint = pointField("startPoint",
      range = START_END_POINT_RANGE)
    var endPoint = pointField("endPoint",
      range = START_END_POINT_RANGE)
    var sinMinAmplitude = doubleField("sinMinAmplitude",
      range = 0.0..10.0)
    var sinAmplitude = doubleField("sinAmplitude",
      startVal = 100.0,
      range = 0.0..400.0)
    var sinFreq = doubleField("sinFreq",
      startVal = 4.0,
      range = 0.1..100.0)
    var numStars = intField("numStars",
      startVal = 1,
      range = 1..10)

    fun copyValuesFrom(other: TabControls) {
      startAngle.set(other.startAngle.get())
      angleLength.set(other.angleLength.get())
      startPoint.set(other.startPoint.get())
      endPoint.set(other.endPoint.get())
      sinMinAmplitude.set(other.sinMinAmplitude.get())
      sinAmplitude.set(other.sinAmplitude.get())
      sinFreq.set(other.sinFreq.get())
      numStars.set(other.numStars.get())
    }
  }

  private fun getCopyLayerButton(
    currentTab: TabControls,
    tabToClone: Int,
  ) = Button("copy L-${tabToClone + 1}") {
    currentTab.copyValuesFrom(tabs[tabToClone])
    updateControls()
    markDirty()
  }

  override fun getControlsForLayer(index: Int): Array<ControlGroupable> {
    val controls = tabs[index]

    return arrayOf(
      ControlGroup(times(4) {
        getCopyLayerButton(controls,
          it)
      },
        0.5),
      ControlGroup(controls.startAngle,
        controls.angleLength),
      ControlGroup(controls.startPoint,
        controls.endPoint),
      ControlGroup(controls.sinMinAmplitude,
        controls.sinAmplitude,
        controls.sinFreq,
        controls.numStars)
    )
  }

  init {
    markDirty()
  }

  override fun drawOnce(layer: Int) {
    val t = tabs[layer - 1]

    val startToEndLine = Segment(center + t.startPoint.get(),
      center + t.endPoint.get())

    for (i in 1..t.numStars.get()) {
      val starSpacing = 1 / (t.sinFreq.get().toInt().toDouble() * t.numStars.get())

      val currStartAngle = t.startAngle.get() + (starSpacing * (i - 1))

      Spiral(
        originFunc = { time, percent, deg -> startToEndLine.getPointAtPercent(percent) },
        lengthFunc = { time, percent, deg ->
          val tadjusted = time - currStartAngle
//          val rad = ((time - currStartAngle) * 360).toRadians()
//          (sin(t.sinFreq.get().toInt() * rad) + t.sinMinAmplitude.get()) * t.sinAmplitude.get()
          if (tadjusted < t.angleLength.get() / 2) {
            0.5 * (t.angleLength.get() - tadjusted).pow(1.2)
          } else {
            0.5 * tadjusted.pow(1.2)
          }
        },
        rotationsRange = currStartAngle..(t.angleLength.get() + currStartAngle))
        .walk(0.001)
        .draw(boundRect)
    }

  }
}

fun main() = TestCanvasSketch().run()
