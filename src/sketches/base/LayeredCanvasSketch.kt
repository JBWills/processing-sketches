package sketches.base

import LayerConfig
import controls.ControlTab
import controls.Props
import controls.Props.Companion.props
import controls.dropdownList
import controls.intProp
import controls.nullableEnumProp
import geomerativefork.src.util.mapArray
import interfaces.Bindable
import interfaces.Copyable
import interfaces.KSerializable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import util.button
import util.io.getPresetPath
import util.io.save
import util.iterators.flattenArray
import util.iterators.timesArray
import util.letWith
import util.limit
import util.print.Orientation
import util.print.Paper
import util.print.Pen
import util.print.StrokeWeight
import util.print.StrokeWeight.Thick
import util.print.Style
import java.awt.Color
import java.awt.Color.BLUE
import java.awt.Color.CYAN
import java.awt.Color.DARK_GRAY
import java.awt.Color.GREEN
import java.awt.Color.LIGHT_GRAY
import java.awt.Color.MAGENTA
import java.awt.Color.ORANGE
import java.awt.Color.RED
import java.awt.Color.YELLOW
import java.io.File

abstract class LayeredCanvasSketch<TabValues, GlobalValues>(
  svgBaseFilename: String,
  defaultGlobal: GlobalValues,
  layerToDefaultTab: (Int) -> TabValues,
  canvas: Paper = Paper.SquareBlack,
  orientation: Orientation = Orientation.Landscape,
  val maxLayers: Int = MAX_LAYERS,
) : CanvasSketch(
  svgBaseFilename,
  canvas,
  orientation,
) where
TabValues : Bindable,
TabValues : Copyable<TabValues>,
TabValues : KSerializable<TabValues>,
GlobalValues : Bindable,
GlobalValues : Copyable<GlobalValues>,
GlobalValues : KSerializable<GlobalValues> {

  private val defaultProps = props(
    maxLayers,
    defaultGlobal,
    layerToDefaultTab
  )

  private var currentPreset: String = DEFAULT_PRESET_NAME

  private var props: Props<TabValues, GlobalValues> = defaultProps

  private fun updatePropsFromPreset() {
    val presetFile = File(getPresetPath(svgBaseFileName, currentPreset))
    var presetData: DrawInfo? = null
    if (presetFile.exists()) {
      println("Loading preset from: /presets/$svgBaseFileName/$currentPreset.json")
      presetData = deserializeDrawInfo(globalSerializer, layerSerializer, presetFile.readText())
    }

    if (presetData == null) {
      println("No preset data found. Not updating props.")
      return
    }

    props = props(
      maxLayers,
      presetData.globalValues,
      { i ->
        presetData.allTabValues.getOrNull(i) ?: props.tabValues[i]
      }
    )
  }

  private val globalSerializer: KSerializer<GlobalValues> = defaultGlobal.toSerializer()
  private val layerSerializer: KSerializer<TabValues> = layerToDefaultTab(0).toSerializer()

  private var weightOverride: StrokeWeight? = null

  private var frozenValues: DrawInfo? = null

  var numLayers: Int = maxLayers

  private fun coloredLayer(c: Color) = LayerConfig(Style(Thick, c))

  override fun getLayers(): List<LayerConfig> = listOf(
    coloredLayer(BLUE),
    coloredLayer(RED),
    coloredLayer(GREEN),
    coloredLayer(LIGHT_GRAY),
    coloredLayer(DARK_GRAY),
    coloredLayer(ORANGE),
    coloredLayer(YELLOW),
    coloredLayer(CYAN),
    coloredLayer(Color(GRAY)),
    coloredLayer(MAGENTA),
  )
    .limit(numLayers)
    .map { LayerConfig(it.style.applyOverrides(Style(weightOverride))) }
    .plus(LayerConfig(Pen.ThickGellyWhite.style))

  override fun getControlTabs(): Array<ControlTab> = arrayOf(
    ControlTab(
      PRESETS_TAB_NAME,
      button("Save as $DEFAULT_PRESET_NAME") { savePreset() },
      dropdownList("Load Preset", listOf(DEFAULT_PRESET_NAME), ::currentPreset) {
        updatePropsFromPreset()
        updateControls()
      }
    ),
    ControlTab(
      CANVAS_TAB_NAME,
      *super.getControls(),
      intProp(::numLayers, range = 0..maxLayers),
      nullableEnumProp(::weightOverride, StrokeWeight.values()),
    ),
    *props.globalControlTabs,
    *timesArray(maxLayers) { index ->
      props.layerControlTabs[index]
        .mapArray { tab ->
          ControlTab(
            name = "${tab.name}-${index + 1}",
            tab.controlSections
          )
        }
    }.flattenArray(),
  )

  private fun savePreset() {
    val (global, tab) = getClonedProps()
    val s = DrawInfo(global, tab).serialize(globalSerializer, layerSerializer)

    if (s == null) {
      println("Could not save preset.")
      return
    }

    try {
      File(getPresetPath(baseSketchName = svgBaseFileName, DEFAULT_PRESET_NAME))
        .save(s)
    } catch (e: Exception) {
      println("Could not save preset. Error message: ${e.message}")
    }
  }

  private fun getClonedProps() =
    props.letWith { globalValues.clone() to tabValues.map { it.clone() } }

  abstract fun drawOnce(values: LayerInfo)

  open fun drawSetup(layerInfo: DrawInfo) {}

  final override fun drawSetup() {
    super.drawSetup()

    val (global, tab) = getClonedProps()
    frozenValues = DrawInfo(global, tab).also { drawSetup(it) }
  }

  final override fun drawOnce(layer: Int) {
    val frozenValues = frozenValues ?: return
    drawOnce(LayerInfo(layer, frozenValues.globalValues, frozenValues.allTabValues))
  }

  final override fun setup() {
    super.setup()

    updatePropsFromPreset()
    setActiveTab(props.globalControlTabs.firstOrNull()?.name ?: CANVAS_TAB_NAME)
  }

  open inner class DrawInfo(
    val globalValues: GlobalValues,
    val allTabValues: List<TabValues>,
  ) {
    override fun toString(): String =
      "DrawInfo(globalValues=$globalValues, allTabValues=$allTabValues)"

    fun serialize(
      globalSerializer: KSerializer<GlobalValues>,
      layerSerializer: KSerializer<TabValues>,
    ): String? = try {

      Json.encodeToString(
        JsonObject.serializer(),
        JsonObject(
          hashMapOf(
            SERIAL_KEY_GLOBAL to Json.encodeToJsonElement(globalSerializer, globalValues),
            SERIAL_KEY_LAYERS to Json.encodeToJsonElement(
              ListSerializer(layerSerializer),
              allTabValues
            ),
          )
        )
      )
    } catch (e: Exception) {
      println("Failed to serialize $this.\nError message: ${e.message}")
      null
    }
  }

  fun deserializeDrawInfo(
    globalSerializer: KSerializer<GlobalValues>,
    layerSerializer: KSerializer<TabValues>,
    fileContents: String,
  ): DrawInfo? = try {
    val element2 = Json.parseToJsonElement(fileContents).jsonObject
    val globalElement = element2[SERIAL_KEY_GLOBAL]
      ?: throw Exception("Json doesn't contain $SERIAL_KEY_GLOBAL key.")
    val layersList = element2[SERIAL_KEY_LAYERS]
      ?: throw Exception("Json doesn't contain $SERIAL_KEY_LAYERS key.")

    val globalValues = Json.decodeFromJsonElement(globalSerializer, globalElement)
    val layers = Json.decodeFromJsonElement(ListSerializer(layerSerializer), layersList)

    DrawInfo(globalValues, layers)
  } catch (e: Exception) {
    println("Error deserializing. Got message: ${e.message}. Json Text:\n\n$fileContents")
    null
  }

  inner class LayerInfo(
    val layerIndex: Int,
    globalValues: GlobalValues,
    allTabValues: List<TabValues>,
  ) : DrawInfo(globalValues, allTabValues) {
    val tabValues = allTabValues[layerIndex]
  }

  companion object {
    private const val SERIAL_KEY_GLOBAL = "global"
    private const val SERIAL_KEY_LAYERS = "layers"
    const val DEFAULT_PRESET_NAME = "default"
    const val CANVAS_TAB_NAME = "canvas"
    const val PRESETS_TAB_NAME = "presets"
    const val MAX_LAYERS = 3
  }
}
