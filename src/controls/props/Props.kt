package controls.props

import BaseSketch
import controls.ControlTab
import controls.TabProp
import controls.tabProp
import interfaces.Bindable
import interfaces.Copyable
import interfaces.KSerializable
import util.iterators.flattenArray
import util.iterators.mapArray
import util.map

interface PropData<T> : Bindable, Copyable<T>, KSerializable<T>

/**
 * Props for a sketch.
 */
class Props<TabValues : PropData<TabValues>, GlobalValues : PropData<GlobalValues>>(
  private val sketch: BaseSketch,
  maxLayers: Int,
  defaultGlobal: GlobalValues,
  layerToDefaultTab: (Int) -> TabValues,
) {
  var globalBackingField = defaultGlobal.clone()

  private val layersBackingField: MutableList<TabValues> = (0 until maxLayers)
    .map { layerToDefaultTab(it).clone() }
    .toMutableList()

  private val global: TabProp<GlobalValues> by lazy {
    sketch.tabProp(::globalBackingField) { currValues ->
      currValues.bindSketch(this)
    }
  }

  private val tabs: List<TabProp<TabValues>> by lazy {
    maxLayers.map { tabIndex ->
      sketch.tabProp(layersBackingField, tabIndex) { currValues ->
        currValues.bindSketch(sketch)
      }
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
    fun <TabValues : PropData<TabValues>, GlobalValues : PropData<GlobalValues>> BaseSketch.props(
      maxLayers: Int,
      defaultGlobal: GlobalValues,
      layerToDefaultTab: (Int) -> TabValues,
    ) = Props(this, maxLayers, defaultGlobal, layerToDefaultTab)
  }
}
