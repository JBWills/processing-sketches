package controls

import BaseSketch
import interfaces.Bindable
import util.mapArray

/**
 * Props for a sketch.
 */
abstract class Props<TabValues : Bindable, GlobalValues : Bindable>(
  private val sketch: BaseSketch,
  maxLayers: Int,
  var defaultGlobal: GlobalValues,
  layerToDefaultTab: (Int) -> TabValues,
) {
  private val defaultTabs: List<TabValues> = (0 until maxLayers).map(layerToDefaultTab)
  fun globalControls(): ControlProp<GlobalValues> = sketch.prop(this::defaultGlobal) {
    it.bind(sketch)
  }

  fun tabControls(tabIndex: Int): ControlProp<TabValues> = sketch.prop(defaultTabs.toMutableList(), tabIndex) {
    it.bind(sketch)
  }

  val global: ControlProp<GlobalValues> by lazy { globalControls() }
  val tabs: List<ControlProp<TabValues>> by lazy {
    (0 until maxLayers).map { tabControls(it) }
  }

  val globalValues: GlobalValues get() = global.get()
  val tabValues: List<TabValues> get() = tabs.map { it.get() }

  val globalControls: Array<ControlGroupable> get() = global.toControlGroups()
  val tabControls: Array<Array<ControlGroupable>>
    get() = tabs.mapArray { it.toControlGroups() }
}
