package sketches.base

import LayerConfig
import controls.panels.ControlTab
import controls.panels.ControlTab.Companion.tab
import controls.panels.panelext.button
import controls.panels.panelext.dropdown
import controls.panels.panelext.markDirtyIf
import controls.panels.panelext.textInput
import controls.props.PropData
import controls.props.SketchProps
import controls.props.SketchProps.Companion.props
import controls.props.types.CanvasProp
import controls.props.types.CanvasProp.Companion.updateCanvas
import controls.utils.deletePresetFile
import controls.utils.loadPresets
import controls.utils.savePresetToFile
import kotlinx.serialization.KSerializer
import util.print.Style

abstract class SimpleCanvasSketch<Data : PropData<Data>>(
  svgBaseFilename: String,
  defaultData: Data,
) : CanvasSketch(svgBaseFilename, CanvasProp()) {
  private val dataSerializer: KSerializer<Data> = defaultData.toSerializer()

  private val fallbackProps = props(CanvasProp(), defaultData)

  private var presets: Map<String, SketchProps<Data>> =
    loadPresets(dataSerializer)

  private var props: SketchProps<Data> =
    presets.getOrDefault(DEFAULT_PRESET_NAME, fallbackProps)

  /* controlled props */
  private var currentPreset: String = DEFAULT_PRESET_NAME
  /* end of controlled props */

  // Values that are locked during draw (so they don't change in the middle of a draw step)
  private var frozenValues: DrawInfo? = null

  override var canvasProps
    get() = props.canvasValues
    set(value) {
      props = SketchProps(props, canvasData = value)
    }

  /**
   * Update the props directly (as in, not through the control panel).
   * This is useful if you need to listen to mouse or other UI events.
   *
   * @param block block to modify the parent props in-place. Return true if the sketch should be marked dirty.
   */
  @Suppress("unused")
  fun modifyPropsDirectly(
    block: (mutableProps: SketchProps<Data>) -> Boolean
  ) = markDirtyIf(block(props))

  final override fun setup() {
    super.setup()

    setActiveTab(props.dataControlTabs.firstOrNull()?.name ?: CANVAS_TAB_NAME)

    onSwitchCanvas(DEFAULT_PRESET_NAME)
  }

  abstract suspend fun SequenceScope<Unit>.drawLayers(drawInfo: DrawInfo)

  open fun drawSetup(drawInfo: DrawInfo) {}
  open fun drawInteractive(layerInfo: DrawInfo) {}

  override fun getControlTabs(): Array<ControlTab> = arrayOf(
    tab(PRESETS_TAB_NAME) {
      row {
        button("Override $DEFAULT_PRESET_NAME preset") { savePreset(DEFAULT_PRESET_NAME) }

        if (currentPreset != DEFAULT_PRESET_NAME) {
          button("Override $currentPreset preset") { savePreset(currentPreset) }
          button("Delete $currentPreset preset") { deletePreset(currentPreset) }
        }
      }

      textInput(textFieldLabel = "New Preset Name", submitButtonLabel = "Save new preset") {
        savePreset(it)
      }

      dropdown(
        "Load Preset",
        presets.keys.sorted(),
        ::currentPreset,
        shouldMarkDirty = false,
      ) { onSwitchCanvas(it) }
    },
    tab(CANVAS_TAB_NAME) {
      +props.canvasControls
    },
    *props.dataControlTabs,
    // Have to add these, so we can select the last tab because of a bug in controlP5
    tab("_") { },
    tab("__") { },
  )

  private fun onSwitchCanvas(newPresetName: String) {
    val newPreset = presets[newPresetName] ?: return
    props = newPreset
    updateCanvas(canvasProps)
    updateControls()
    markDirty()
  }

  override fun getLayers(): List<LayerConfig> = listOf(LayerConfig(Style(color = strokeColor)))

  final override fun markDirty() {
    super.markDirty()
    val (canvas, data) = props.cloneValues()
    frozenValues = DrawInfo(canvas, data).also { drawSetup(it) }
  }

  final override fun drawInteractive() {
    super.drawInteractive()
    frozenValues?.let { drawInteractive(it) }
  }

  final override fun drawSetup() {
    super.drawSetup()
    frozenValues?.let { drawSetup(it) }
  }

  final override suspend fun SequenceScope<Unit>.drawOnce(layer: Int) {
    val frozenValues = frozenValues ?: return
    drawLayers(frozenValues)
  }

  private fun savePreset(presetName: String) {
    savePresetToFile(svgBaseFileName, presetName, props)
    refreshPresets()
  }

  private fun deletePreset(presetName: String) {
    if (presetName == DEFAULT_PRESET_NAME) return
    deletePresetFile(presetName)
    currentPreset = DEFAULT_PRESET_NAME
    refreshPresets()
  }

  private fun refreshPresets() {
    presets = loadPresets(dataSerializer)
    updateControls()
  }

  open inner class DrawInfo(
    @Suppress("MemberVisibilityCanBePrivate") val canvasValues: CanvasProp,
    @Suppress("MemberVisibilityCanBePrivate") val dataValues: Data,
  ) {
    override fun toString(): String =
      "DrawInfo(canvasValues=$canvasValues, dataValues=$dataValues)"
  }

  companion object {
    const val DEFAULT_PRESET_NAME = "default"
    const val CANVAS_TAB_NAME = "canvas"
    const val PRESETS_TAB_NAME = "presets"
  }
}
