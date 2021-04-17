package controls.utils

import BaseSketch
import controls.props.LayerAndGlobalProps
import controls.props.LayerAndGlobalProps.Companion.props
import controls.props.PropData
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import util.io.decode
import util.io.getAllFilesInPath
import util.io.getPresetDir
import util.io.getPresetPath
import util.io.jsonStringOf
import util.io.save
import util.io.toJsonArray
import util.io.toJsonElement
import java.io.File

private const val SERIAL_KEY_GLOBAL = "global"
private const val SERIAL_KEY_LAYERS = "layers"

/**
 * Load the given preset file. This will throw an exception if the preset file doesn't exist.
 *
 * @param maxLayers The number of layers in the sketch TODO: delete this param requirement
 * @param sketchBaseName The name of the sketch (this is used as a subfolder to store the presets)
 * @param presetName The name of the specific preset (this is used for the json filename)
 */
fun <GlobalValues : PropData<GlobalValues>, TabValues : PropData<TabValues>> BaseSketch.loadPreset(
  maxLayers: Int,
  sketchBaseName: String,
  presetName: String,
  globalSerializer: KSerializer<GlobalValues>,
  layerSerializer: KSerializer<TabValues>,
): LayerAndGlobalProps<TabValues, GlobalValues> {
  val presetFile = File(getPresetPath(sketchBaseName, presetName))
  var presetData: Pair<GlobalValues, List<TabValues>>? = null
  if (presetFile.exists()) {
    presetData = deserializeDrawInfo(globalSerializer, layerSerializer, presetFile.readText())
  }

  if (presetData == null) throw Exception("No preset data found")

  return props(maxLayers, presetData.first) { i -> presetData.second[i] }
}

/**
 * Serialize props into a preset json file.
 *
 * This will override an existing preset with the same sketchBaseName/presetName
 *
 * @param sketchBaseName The name of the sketch (this is used as a subfolder to store the presets)
 * @param presetName The name of the specific preset (this is used for the json filename)
 * @param layerAndGlobalProps The props to serialize into a preset.
 */
fun <GlobalValues : PropData<GlobalValues>, TabValues : PropData<TabValues>> savePresetToFile(
  sketchBaseName: String,
  presetName: String,
  layerAndGlobalProps: LayerAndGlobalProps<GlobalValues, TabValues>,
) {
  val s = serializeDrawInfo(layerAndGlobalProps)

  if (s == null) {
    println("Could not save preset. Error in serialization.")
    return
  }

  try {
    File(getPresetPath(baseSketchName = sketchBaseName, presetName))
      .save(s)
  } catch (e: Exception) {
    println("Could not save preset. Error message: ${e.message}")
  }
}

fun BaseSketch.deletePresetFile(presetName: String) {
  val f = File(getPresetPath(svgBaseFileName, presetName))

  if (f.exists()) f.delete()
}

fun <GlobalValues : PropData<GlobalValues>, TabValues : PropData<TabValues>> BaseSketch.loadPresets(
  maxLayers: Int,
  globalSerializer: KSerializer<GlobalValues>,
  layerSerializer: KSerializer<TabValues>,
): Map<String, LayerAndGlobalProps<TabValues, GlobalValues>> =
  getAllFilesInPath(
    getPresetDir(svgBaseFileName).path,
    recursive = false,
  )
    .mapNotNull { file ->
      val fileName = file.nameWithoutExtension
      val preset = loadPresetOrNull(
        maxLayers,
        presetName = fileName,
        globalSerializer,
        layerSerializer,
      )
      preset?.let { fileName to it }
    }.toMap()

private fun <GlobalValues : PropData<GlobalValues>, TabValues : PropData<TabValues>> BaseSketch.loadPresetOrNull(
  maxLayers: Int,
  presetName: String,
  globalSerializer: KSerializer<GlobalValues>,
  layerSerializer: KSerializer<TabValues>,
) = try {
  loadPreset(
    maxLayers,
    svgBaseFileName,
    presetName,
    globalSerializer,
    layerSerializer,
  )
} catch (e: Exception) {
  println("Failed to load preset: $presetName. Got error: ${e.message}")
  null
}

private fun <GlobalValues : PropData<GlobalValues>, TabValues : PropData<TabValues>> serializeDrawInfo(
  layerAndGlobalProps: LayerAndGlobalProps<TabValues, GlobalValues>,
): String? = try {
  jsonStringOf(
    SERIAL_KEY_GLOBAL to layerAndGlobalProps.globalValues.toJsonElement(),
    SERIAL_KEY_LAYERS to layerAndGlobalProps.tabValues.toJsonArray(),
  )
} catch (e: Exception) {
  println("Failed to serialize.\nError message: ${e.message}")
  null
}

private fun <GlobalValues, TabValues> deserializeDrawInfo(
  globalSerializer: KSerializer<GlobalValues>,
  layerSerializer: KSerializer<TabValues>,
  fileContents: String,
): Pair<GlobalValues, List<TabValues>>? = try {
  val element = Json.parseToJsonElement(fileContents).jsonObject
  val globalElement = element[SERIAL_KEY_GLOBAL]
    ?: throw Exception("Json doesn't contain $SERIAL_KEY_GLOBAL key.")
  val layersList = element[SERIAL_KEY_LAYERS]
    ?: throw Exception("Json doesn't contain $SERIAL_KEY_LAYERS key.")

  val globalValues = decode(globalSerializer, globalElement)
  val layers = decode(ListSerializer(layerSerializer), layersList)

  globalValues to layers
} catch (e: Exception) {
  println("Error deserializing. Got message: ${e.message}. Json Text:\n\n$fileContents")
  null
}

