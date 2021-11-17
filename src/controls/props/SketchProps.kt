package controls.props

import controls.panels.ControlPanel
import controls.panels.ControlTab
import controls.props.types.CanvasProp

/**
 * Props for a [SimpleCanvasSketch].
 */
class SketchProps<Data : PropData<Data>>(
  private val canvasData: CanvasProp?,
  private val defaultData: Data,
) {

  constructor(
    l: SketchProps<Data>,
    canvasData: CanvasProp? = null,
    defaultData: Data? = null,
  ) : this(
    canvasData ?: l.canvasData?.clone(),
    defaultData ?: l.defaultData.clone(),
  )

  private var dataBackingField = defaultData.clone()
  var canvasBackingField = canvasData?.clone() ?: CanvasProp()

  private val dataProp: TabProp<Data> by lazy {
    tabProp(::dataBackingField) { it.bind() }
  }

  private val canvasProp: TabProp<CanvasProp> by lazy {
    tabProp(::canvasBackingField) { it.bind() }
  }

  val data: Data
    get() = dataProp.get()
  val canvasValues: CanvasProp
    get() = canvasProp.get()

  val canvasControls: ControlPanel
    get() = canvasValues.asControlPanel()
  val dataControlTabs: Array<ControlTab>
    get() = dataProp.toTabs().toTypedArray()

  fun cloneValues(): Pair<CanvasProp, Data> = Pair(
    canvasValues.clone(),
    data.clone(),
  )

  companion object {
    fun <Data : PropData<Data>> props(
      canvasData: CanvasProp?,
      defaultGlobal: Data,
    ) = SketchProps(canvasData, defaultGlobal)
  }
}
