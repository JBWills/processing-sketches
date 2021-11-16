package controls.props

import controls.panels.ControlPanel
import controls.panels.ControlTab
import controls.props.types.CanvasProp
import util.iterators.flattenArray
import util.iterators.mapArray
import util.numbers.map
import util.tuple.Pair3

/**
 * Props for a [LayeredCanvasSketch].
 */
class LayerAndGlobalProps<TabValues : PropData<TabValues>, GlobalValues : PropData<GlobalValues>>(
  private val maxLayers: Int,
  private val canvasData: CanvasProp?,
  private val defaultGlobal: GlobalValues,
  private val layerToDefaultTab: (Int) -> TabValues,
) {

  constructor(
    l: LayerAndGlobalProps<TabValues, GlobalValues>,
    maxLayers: Int? = null,
    canvasData: CanvasProp? = null,
    defaultGlobal: GlobalValues? = null,
  ) : this(
    maxLayers ?: l.maxLayers,
    canvasData ?: l.canvasData?.clone(),
    defaultGlobal ?: l.defaultGlobal.clone(),
    {
      l.layersBackingField[it].clone()
    },
  )

  var globalBackingField = defaultGlobal.clone()
  var canvasBackingField = canvasData?.clone() ?: CanvasProp()

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

  private val canvas: TabProp<CanvasProp> by lazy {
    tabProp(::canvasBackingField) { it.bind() }
  }

  val globalValues: GlobalValues
    get() = global.get()
  val tabValues: List<TabValues>
    get() = tabs.map { it.get() }
  val canvasValues: CanvasProp
    get() = canvas.get()

  val canvasControls: ControlPanel
    get() = canvasValues.asControlPanel()
  val globalControlTabs: Array<ControlTab>
    get() = global.toTabs().toTypedArray()
  private val controlTabsByLayer: Array<Array<ControlTab>>
    get() = tabs.mapArray { it.toTabs().toTypedArray() }

  val layerControlTabs: Array<ControlTab>
    get() = controlTabsByLayer.flattenArray()

  fun cloneValues(): Pair3<CanvasProp, GlobalValues, List<TabValues>> = Pair3(
    canvasValues.clone(),
    globalValues.clone(),
    tabValues.map { it.clone() },
  )

  companion object {
    fun <TabValues : PropData<TabValues>, GlobalValues : PropData<GlobalValues>> props(
      maxLayers: Int,
      canvasData: CanvasProp?,
      defaultGlobal: GlobalValues,
      layerToDefaultTab: (Int) -> TabValues,
    ) = LayerAndGlobalProps(maxLayers, canvasData, defaultGlobal, layerToDefaultTab)
  }
}
