package controls.utils

import BaseSketch
import controls.props.PropData
import controls.props.SketchProps
import controls.props.SketchProps.Companion.props
import controls.props.types.CanvasProp
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import util.io.decode
import util.io.getAllFilesInPath
import util.io.getPresetDir
import util.io.getPresetFilepath
import util.io.jsonStringOf
import util.io.save
import util.io.toJsonElement
import java.io.File

private const val SERIAL_KEY_CANVAS = "canvas"
private const val SERIAL_KEY_GLOBAL = "data"

/**
 * Load the given preset file. This will throw an exception if the preset file doesn't exist.
 *
 * @param maxLayers The number of layers in the sketch TODO: delete this param requirement
 * @param sketchBaseName The name of the sketch (this is used as a subfolder to store the presets)
 * @param presetName The name of the specific preset (this is used for the json filename)
 */
fun <Data : PropData<Data>> loadPreset(
  sketchBaseName: String,
  presetName: String,
  dataSerializer: KSerializer<Data>,
): SketchProps<Data> {
  val presetFile = File(getPresetFilepath(sketchBaseName, presetName))
  var presetData: Pair<CanvasProp?, Data>? = null
  if (presetFile.exists()) {
    presetData = deserializeDrawInfo(dataSerializer, presetFile.readText())
  }

  if (presetData == null) throw Exception("No preset data found")

  return props(presetData.first, presetData.second)
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
fun <Data : PropData<Data>> savePresetToFile(
  sketchBaseName: String,
  presetName: String,
  props: SketchProps<Data>,
) {
  val s = serializeDrawInfo(props)

  if (s == null) {
    println("Could not save preset. Error in serialization.")
    return
  }

  try {
    File(getPresetFilepath(baseSketchName = sketchBaseName, presetName))
      .save(s)
  } catch (e: Exception) {
    println("Could not save preset. Error message: ${e.message}")
  }
}

fun <Data : PropData<Data>> BaseSketch.loadPresets(
  dataSerializer: KSerializer<Data>
): Map<String, SketchProps<Data>> =
  getAllFilesInPath(
    getPresetDir(svgBaseFileName).path,
    recursive = false,
  )
    .mapNotNull { file ->
      val fileName = file.nameWithoutExtension
      val preset = loadPresetOrNull(
        presetName = fileName,
        dataSerializer,
      )
      preset?.let { fileName to it }
    }.toMap()

private fun <Data : PropData<Data>> BaseSketch.loadPresetOrNull(
  presetName: String,
  dataSerializer: KSerializer<Data>,
) = try {
  loadPreset(
    svgBaseFileName,
    presetName,
    dataSerializer,
  )
} catch (e: Exception) {
  println("Failed to load preset: $presetName. Got error: ${e.message}")
  null
}

private fun <Data : PropData<Data>> serializeDrawInfo(
  props: SketchProps<Data>,
): String? = try {
  jsonStringOf(
    SERIAL_KEY_CANVAS to props.canvasValues.toJsonElement(),
    SERIAL_KEY_GLOBAL to props.data.toJsonElement(),
  )
} catch (e: Exception) {
  println("Failed to serialize.\nError message: ${e.message}")
  null
}

private fun <Data> deserializeDrawInfo(
  dataSerializer: KSerializer<Data>,
  fileContents: String,
): Pair<CanvasProp?, Data>? = try {
  val element = Json.parseToJsonElement(fileContents).jsonObject
  val dataElement = element[SERIAL_KEY_GLOBAL]
    ?: throw Exception("Json doesn't contain $SERIAL_KEY_GLOBAL key.")

  val dataValues = decode(dataSerializer, dataElement)
  val canvasProp: CanvasProp? = element[SERIAL_KEY_CANVAS]?.decode(CanvasProp().toSerializer())

  Pair(canvasProp, dataValues)
} catch (e: Exception) {
  println("Error deserializing. Got message: ${e.message}. Json Text:\n\n$fileContents")
  null
}

