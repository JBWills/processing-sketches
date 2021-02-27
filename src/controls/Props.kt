package controls

import BaseSketch
import interfaces.Bindable
import util.mapArray

/**
 * Props for a sketch.
 */
class Props<TabValues : Bindable, GlobalValues : Bindable>(
  private val sketch: BaseSketch,
  maxLayers: Int,
  var defaultGlobal: GlobalValues,
  layerToDefaultTab: (Int) -> TabValues,
) {
  private val defaultTabs: List<TabValues> = (0 until maxLayers).map(layerToDefaultTab)
  private fun globalControls(): ControlProp<GlobalValues> =
    sketch.prop(::defaultGlobal) {
      it.bindSketch(this)
    }

  private fun tabControls(tabIndex: Int): ControlProp<TabValues> =
    sketch.prop(defaultTabs.toMutableList(), tabIndex) {
      it.bindSketch(sketch)
    }

  private val global: ControlProp<GlobalValues> by lazy { globalControls() }
  private val tabs: List<ControlProp<TabValues>> by lazy {
    (0 until maxLayers).map { tabControls(it) }
  }

  val globalValues: GlobalValues get() = global.get()
  val tabValues: List<TabValues> get() = tabs.map { it.get() }

  val globalControls: Array<ControlGroupable> get() = global.toControlGroups()
  val tabControls: Array<Array<ControlGroupable>>
    get() = tabs.mapArray { it.toControlGroups() }
}
