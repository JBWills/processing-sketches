package controls

import controlP5.ControlP5
import coordinate.Point
import util.DoubleRange
import util.Position
import util.Size
import util.buttonWith
import util.range
import util.slider2dWith
import util.sliderWith
import util.toggleWith

sealed class Control(
  val applyToControl: (c: ControlP5, pos: Position, size: Size) -> Unit,
) {
  class Button(
    text: String,
    handleClick: () -> Unit,
  ) : Control(
    buttonWith(text) {
      onClick { handleClick() }
    }
  )

  class Toggle(
    text: String,
    handleToggled: (Boolean) -> Unit,
  ) : Control(
    toggleWith(text) {
      onChange { handleToggled(booleanValue) }
    }
  )

  class Slider(
    text: String,
    range: DoubleRange = 0.0..1.0,
    defaultValue: Double? = null,
    handleChange: (Double) -> Unit,
  ) : Control(
    sliderWith(text) {
      range(range)

      this.defaultValue = defaultValue?.toFloat() ?: range.start.toFloat()
      onChange { handleChange(value.toDouble()) }
    }
  )

  class Slider2d(
    text: String,
    rangeX: DoubleRange = 0.0..1.0,
    rangeY: DoubleRange = 0.0..1.0,
    defaultValue: Point? = null,
    handleChange: (Point) -> Unit
  ) : Control(
    slider2dWith(text) {
      range(rangeX, rangeY)
      val defaultPoint = defaultValue ?: Point(rangeX.start, rangeY.start)

      cursorX = defaultPoint.x.toFloat()
      cursorY = defaultPoint.y.toFloat()

      onChange { handleChange(Point(cursorX, cursorY)) }
    }
  )
}