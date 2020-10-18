package controls

import controlP5.ControlP5
import util.Position
import util.Size
import util.buttonWith
import util.range
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
    range: Pair<Float, Float> = 0f to 1f,
    defaultValue: Float? = null,
    handleChange: (Float) -> Unit,
  ) : Control(
    sliderWith(text) {
      range(range)

      this.defaultValue = defaultValue ?: range.first
      onChange { handleChange(value) }
    }
  )
}