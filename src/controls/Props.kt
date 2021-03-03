package controls

import BaseSketch
import interfaces.Bindable
import interfaces.Copyable
import interfaces.KSerializable
import util.iterators.mapArray

/**
 * Props for a sketch.
 */
class Props<TabValues, GlobalValues>(
  private val sketch: BaseSketch,
  maxLayers: Int,
  defaultGlobal: GlobalValues,
  layerToDefaultTab: (Int) -> TabValues,
) where TabValues : Bindable,
        TabValues : Copyable<TabValues>,
        TabValues : KSerializable<TabValues>,
        GlobalValues : Bindable,
        GlobalValues : Copyable<GlobalValues>,
        GlobalValues : KSerializable<GlobalValues> {
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
    (0 until maxLayers).map { tabIndex ->
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
  val layerControlTabs: Array<Array<ControlTab>>
    get() = tabs.mapArray { it.toTabs().toTypedArray() }

  companion object {
    fun <T, G> BaseSketch.props(
      maxLayers: Int,
      defaultGlobal: G,
      layerToDefaultTab: (Int) -> T,
    ) where T : Bindable,
            T : Copyable<T>,
            T : KSerializable<T>,
            G : Bindable,
            G : Copyable<G>,
            G : KSerializable<G> = Props(this, maxLayers, defaultGlobal, layerToDefaultTab)
  }
}
