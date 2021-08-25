package util.io

import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyMMdd--hh-mm-ss")
  .withLocale(Locale.US)
  .withZone(ZoneId.systemDefault())

private fun getRootDir() = System.getProperty("user.dir")

val assetDir get() = "${getRootDir()}/assets"
val imageDir get() = "$assetDir/images"
val fontDir get() = "$assetDir/fonts"

val noImageSelectedFilepath get() = "$imageDir/no-image-selected.png"

private fun getDateString(time: Instant = Instant.now()): String = dateTimeFormatter.format(time)

fun getPresetDir(baseSketchName: String): File {
  val theDir = File("${getRootDir()}/presets/$baseSketchName")
  if (!theDir.exists()) theDir.mkdirs()

  return theDir
}

fun getPresetFilepath(baseSketchName: String, presetName: String): String =
  "${getPresetDir(baseSketchName).path}/$presetName.json"

private fun getOutputPath(baseSketchName: String, subDir: String = ""): String {
  val root = System.getProperty("user.dir")
  val theDir = File("$root/svgs/$baseSketchName/$subDir")
  if (!theDir.exists()) theDir.mkdirs()
  return theDir.absolutePath
}

private fun getSVGFilepath(baseSketchName: String) = getOutputPath(baseSketchName)

fun getTempFilepath(baseSketchName: String) =
  getOutputPath(baseSketchName, "temp")

private fun getSVGFilename(fileSuffix: String = "", time: Instant) =
  "${getDateString(time)}--$fileSuffix.svg"

private fun getTempFilename(fileSuffix: String = "", layerNum: Int, time: Instant) =
  "$${getDateString(time)}--$fileSuffix--layer-${layerNum}.svg"

fun getSVGNameAndPath(baseSketchName: String, fileSuffix: String, time: Instant = Instant.now()) =
  "${getSVGFilepath(baseSketchName)}/${getSVGFilename(fileSuffix, time)}"

fun getTempFileNameAndPath(
  baseSketchName: String,
  fileSuffix: String,
  layerNum: Int,
  time: Instant = Instant.now()
) = "${getTempFilepath(baseSketchName)}/${getTempFilename(fileSuffix, layerNum, time)}"

fun getFontFilePath(
  family: String,
  weight: String,
  type: String = "ttf",
) = "$fontDir/$family-$weight.$type"
