package interfaces

import BaseSketch
import controls.ControlGroupable

interface Bindable {
  fun bind(s: BaseSketch): List<ControlGroupable>
}
