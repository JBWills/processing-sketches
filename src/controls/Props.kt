package controls

import BaseSketch
import interfaces.Bindable
import util.iterators.mapArray

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
  private fun globalControls(): TabProp<GlobalValues> =
    sketch.tabProp(::defaultGlobal) { currValues ->
      currValues.bindSketch(this)
    }

  private fun tabControls(tabIndex: Int): TabProp<TabValues> =
    sketch.tabProp(defaultTabs.toMutableList(), tabIndex) { currValues ->
      currValues.bindSketch(sketch)
    }

  private val global: TabProp<GlobalValues> by lazy { globalControls() }
  private val tabs: List<TabProp<TabValues>> by lazy {
    (0 until maxLayers).map { tabControls(it) }
  }

  val globalValues: GlobalValues
    get() = global.get()
  val tabValues: List<TabValues>
    get() = tabs.map { it.get() }

  val globalControlTabs: Array<ControlTab>
    get() = global.toTabs().toTypedArray()
  val layerControlTabs: Array<Array<ControlTab>>
    get() = tabs.mapArray { it.toTabs().toTypedArray() }
}
