package controls.props

import controls.panels.ControlTab
import util.iterators.flattenArray
import util.iterators.mapArray
import util.map

/**
 * Props for a [LayeredCanvasSketch].
 */
class LayerAndGlobalProps<TabValues : PropData<TabValues>, GlobalValues : PropData<GlobalValues>>(
  maxLayers: Int,
  defaultGlobal: GlobalValues,
  layerToDefaultTab: (Int) -> TabValues,
) {
  var globalBackingField = defaultGlobal.clone()

  private val layersBackingField: MutableList<TabValues> = (0 until maxLayers)
    .map { layerToDefaultTab(it).clone() }
    .toMutableList()

  private val global: TabProp<GlobalValues> by lazy {
    tabProp(::globalBackingField) { it.bind() }
  }

  private val tabs: List<TabProp<TabValues>> by lazy {
    maxLayers.map { tabIndex ->
      tabProp(layersBackingField, tabIndex) { it.bind() }
    }
  }

  val globalValues: GlobalValues
    get() = global.get()
  val tabValues: List<TabValues>
    get() = tabs.map { it.get() }

  val globalControlTabs: Array<ControlTab>
    get() = global.toTabs().toTypedArray()
  private val controlTabsByLayer: Array<Array<ControlTab>>
    get() = tabs.mapArray { it.toTabs().toTypedArray() }

  val layerControlTabs: Array<ControlTab>
    get() = controlTabsByLayer.flattenArray()

  fun cloneValues(): Pair<GlobalValues, List<TabValues>> =
    globalValues.clone() to tabValues.map { it.clone() }

  companion object {
    fun <TabValues : PropData<TabValues>, GlobalValues : PropData<GlobalValues>> props(
      maxLayers: Int,
      defaultGlobal: GlobalValues,
      layerToDefaultTab: (Int) -> TabValues,
    ) = LayerAndGlobalProps(maxLayers, defaultGlobal, layerToDefaultTab)
  }
}
