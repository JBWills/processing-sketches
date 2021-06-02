package sketches.base

import LayerConfig
import controls.panels.ControlTab
import controls.panels.ControlTab.Companion.tab
import controls.panels.panelext.button
import controls.panels.panelext.dropdown
import controls.panels.panelext.intSlider
import controls.panels.panelext.textInput
import controls.props.LayerAndGlobalProps
import controls.props.LayerAndGlobalProps.Companion.props
import controls.props.PropData
import controls.props.types.CanvasProp
import controls.props.types.CanvasProp.Companion.updateCanvas
import controls.utils.deletePresetFile
import controls.utils.loadPresets
import controls.utils.savePresetToFile
import kotlinx.serialization.KSerializer
import util.constants.getLayerColors
import util.iterators.mapArrayIndexed
import util.print.Pen
import util.print.StrokeWeight
import util.print.StrokeWeight.Thick
import util.print.Style
import java.awt.Color

abstract class LayeredCanvasSketch<GlobalValues : PropData<GlobalValues>, TabValues : PropData<TabValues>>(
  svgBaseFilename: String,
  defaultGlobal: GlobalValues,
  layerToDefaultTab: (Int) -> TabValues,
  val maxLayers: Int = MAX_LAYERS,
) : CanvasSketch(svgBaseFilename, CanvasProp()) {
  private val globalSerializer: KSerializer<GlobalValues> = defaultGlobal.toSerializer()
  private val layerSerializer: KSerializer<TabValues> = layerToDefaultTab(0).toSerializer()

  private val fallbackProps = props(maxLayers, CanvasProp(), defaultGlobal, layerToDefaultTab)

  private var presets: Map<String, LayerAndGlobalProps<TabValues, GlobalValues>> =
    loadPresets(maxLayers, globalSerializer, layerSerializer)

  private var layerAndGlobalProps: LayerAndGlobalProps<TabValues, GlobalValues> =
    presets.getOrDefault(DEFAULT_PRESET_NAME, fallbackProps)

  /* controlled props */
  private var currentPreset: String = DEFAULT_PRESET_NAME
  private var weightOverride: StrokeWeight? = null
  var numLayers: Int = maxLayers
  /* end of controlled props */

  // Values that are locked during draw (so they don't change in the middle of a draw step)
  private var frozenValues: DrawInfo? = null

  override var canvasProps
    get() = layerAndGlobalProps.canvasValues
    set(value) {
      layerAndGlobalProps = LayerAndGlobalProps(layerAndGlobalProps, canvasData = value)
    }

  final override fun setup() {
    super.setup()

    setActiveTab(layerAndGlobalProps.globalControlTabs.firstOrNull()?.name ?: CANVAS_TAB_NAME)

    onSwitchCanvas(DEFAULT_PRESET_NAME)
  }

  abstract fun drawOnce(values: LayerInfo)

  open fun drawSetup(layerInfo: DrawInfo) {}
  open fun drawInteractive(layerInfo: DrawInfo) {}

  override fun getLayers(): List<LayerConfig> =
    getLayerColors(numLayers)
      .map { LayerConfig(Style(Thick, it).applyOverrides(Style(weightOverride))) }
      .plus(LayerConfig(Pen.ThickGellyWhite.style))

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

      dropdown("Load Preset", presets.keys.sorted(), ::currentPreset) { onSwitchCanvas(it) }
    },
    tab(CANVAS_TAB_NAME) {
      +layerAndGlobalProps.canvasControls
      intSlider(::numLayers, range = 0..maxLayers)
      dropdown(::weightOverride, StrokeWeight.values())
    },
    *layerAndGlobalProps.globalControlTabs,
    *layerAndGlobalProps.layerControlTabs.mapArrayIndexed { index, tab ->
      tab.withName("${tab.name}-${index + 1}")
    },
  )

  private fun onSwitchCanvas(newPresetName: String) {
    val newPreset = presets[newPresetName] ?: return
    layerAndGlobalProps = newPreset
    updateCanvas(canvasProps)
    updateControls()
    markDirty()
  }

  final override fun markDirty() {
    super.markDirty()
    val (canvas, global, tab) = layerAndGlobalProps.cloneValues()
    frozenValues = DrawInfo(canvas, global, tab).also { drawSetup(it) }
  }

  final override fun drawInteractive() {
    super.drawInteractive()
    frozenValues?.let { drawInteractive(it) }
  }

  final override fun drawSetup() {
    super.drawSetup()
    frozenValues?.let { drawSetup(it) }
  }

  final override fun drawOnce(layer: Int) {
    val frozenValues = frozenValues ?: return
    drawOnce(LayerInfo(layer, frozenValues.globalValues, frozenValues.allTabValues))
  }

  private fun savePreset(presetName: String) {
    savePresetToFile(svgBaseFileName, presetName, layerAndGlobalProps)
    refreshPresets()
  }

  private fun deletePreset(presetName: String) {
    if (presetName == DEFAULT_PRESET_NAME) return
    deletePresetFile(presetName)
    currentPreset = DEFAULT_PRESET_NAME
    refreshPresets()
  }

  private fun refreshPresets() {
    presets = loadPresets(maxLayers, globalSerializer, layerSerializer)
    updateControls()
  }

  private fun coloredLayer(c: Color) = LayerConfig(Style(Thick, c))

  open inner class DrawInfo(
    val canvasValues: CanvasProp,
    val globalValues: GlobalValues,
    val allTabValues: List<TabValues>,
  ) {
    override fun toString(): String =
      "DrawInfo(globalValues=$globalValues, allTabValues=$allTabValues)"
  }

  inner class LayerInfo(
    val layerIndex: Int,
    globalValues: GlobalValues,
    allTabValues: List<TabValues>,
  ) : DrawInfo(canvasProps, globalValues, allTabValues) {
    val tabValues = allTabValues[layerIndex]
  }

  companion object {
    const val DEFAULT_PRESET_NAME = "default"
    const val CANVAS_TAB_NAME = "canvas"
    const val PRESETS_TAB_NAME = "presets"
    const val MAX_LAYERS = 3
  }
}
