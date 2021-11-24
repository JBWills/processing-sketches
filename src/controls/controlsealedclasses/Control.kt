package controls.controlsealedclasses

import BaseSketch
import controlP5.ControlP5
import controlP5.Controller
import controlP5.Tab
import controls.panels.ControlItem
import controls.panels.ControlPanel
import controls.panels.ControlStyle
import controls.panels.Panelable
import coordinate.BoundRect
import util.positionAndSize
import util.style

val DEFAULT_RANGE = 0.0..1.0

/**
 * A control binds data to ControlP5 Sliders, toggles, etc.
 *
 * @param T The ControlP5 Controller type this Control binds to
 * @property name The visible name for the control (also used to generate IDs for the control)
 *  This name must be unique within a given [ControlPanel], but a control can have the same name
 *  as another control as long as it's in a different [ControlPanel] or [ControlTab]
 * @property createFunc
 * @property block
 */
sealed class Control<T : Controller<T>>(
  var name: String,
  val createFunc: ControlP5.(id: String) -> T,
  val block: T.(BaseSketch, ControlStyle) -> Unit,
) : Panelable {
  override fun toControlPanel(): ControlPanel = ControlItem(control = this)

  var ref: T? = null

  var refValue: Float?
    get() = ref?.value
    set(value) {
      if (value != null && value != ref?.value) ref?.value = value
    }

  fun applyToControl(
    sketch: BaseSketch,
    controlP5: ControlP5,
    tab: Tab,
    panel: ControlPanel,
    bound: BoundRect
  ) {
    ref = controlP5.createFunc(panel.id).apply {
      label = this@Control.name
      moveTo(tab)
      positionAndSize(bound)
      style(panel.styleFromParents)

      block(sketch, panel.styleFromParents)
    }
  }
}
