package sketches.base

import LayerConfig
import controls.ControlTab
import controls.Props
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
import util.print.*
import util.print.StrokeWeight.Thick
import java.awt.Color
import java.awt.Color.*
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

  private val props: Props<TabValues, GlobalValues> by lazy {
    val defaultPresetFile = File(getPresetPath(svgBaseFileName, "default"))
    var presetData: DrawInfo? = null
    if (defaultPresetFile.exists()) {
      println("Loading preset from: /presets/$svgBaseFilename/default.json")
      presetData = deserializeDrawInfo(globalSerializer, layerSerializer, defaultPresetFile.readText())
    }

    if (presetData == null)
      println("No preset data found. Falling back to default.")

    Props(
      this,
      maxLayers,
      presetData?.globalValues ?: defaultGlobal,
      { i ->
        presetData?.allTabValues?.getOrNull(i) ?: layerToDefaultTab(i)
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
      CANVAS_TAB_NAME,
      *super.getControls(),
      intProp(::numLayers, range = 0..maxLayers),
      nullableEnumProp(::weightOverride, StrokeWeight.values()),
      button("Save Preset") { savePreset() }
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
      File(getPresetPath(baseSketchName = svgBaseFileName, "default"))
        .save(s)
    } catch (e: Exception) {
      println("Could not save preset. Error message: ${e.message}")
    }
  }

  private fun getClonedProps() = props.letWith { globalValues.clone() to tabValues.map { it.clone() } }

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

    setActiveTab(GLOBAL_CONFIG_TAB_NAME)
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
        JsonObject(hashMapOf(
          SERIAL_KEY_GLOBAL to Json.encodeToJsonElement(globalSerializer, globalValues),
          SERIAL_KEY_LAYERS to Json.encodeToJsonElement(ListSerializer(layerSerializer), allTabValues),
        ))
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
    const val GLOBAL_CONFIG_TAB_NAME = "global config"
    const val CANVAS_TAB_NAME = "canvas"
    const val MAX_LAYERS = 10
  }
}
