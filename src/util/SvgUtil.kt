package util

import processing.core.PApplet
import processing.core.PStyle
import processing.data.XML
import util.io.getSVGNameAndPath
import util.io.getTempFileNameAndPath
import util.io.getTempFilepath
import util.numbers.times
import util.print.applyStyle
import java.io.File
import java.time.Instant

fun PApplet.recordSvg(filename: String, block: () -> Unit) {
  beginRecord(PApplet.SVG, filename)
  block()
  endRecord()
}

fun PApplet.withLayeredSvg(
  baseSketchName: String,
  fileSuffix: String,
  drawBlock: () -> Unit
) {
  val time: Instant = Instant.now()
  val resultFile = getSVGNameAndPath(baseSketchName, fileSuffix, time)
  val tempFolder = getTempFilepath(baseSketchName)

  // Create an empty SVG file and load it
  recordSvg(resultFile) {}
  val xml = XML(File(resultFile))

  // Set inkscape namespace, so we can add inkscape layer definitions.
  xml.setString("xmlns:inkscape", "http://www.inkscape.org/namespaces/inkscape/")

  drawBlock()

  File(tempFolder).deleteRecursively()

  xml.save(File(resultFile))
}

fun PApplet.drawLayeredSvg(
  baseSketchName: String,
  fileSuffix: String,
  drawLayerSequence: Sequence<Unit>,
) {
  val time: Instant = Instant.now()
  val resultFile = getSVGNameAndPath(baseSketchName, fileSuffix, time)
  val tempFolder = getTempFilepath(baseSketchName)

  // Create an empty SVG file and load it
  recordSvg(resultFile) {}
  val xml = XML(File(resultFile))

  // Set inkscape namespace, so we can add inkscape layer definitions.
  xml.setString("xmlns:inkscape", "http://www.inkscape.org/namespaces/inkscape/")

  var prevStyle: PStyle? = graphics?.style

  fun setup(tempFileName: String) {
    beginRecord(PApplet.SVG, tempFileName)
    if (prevStyle != null) {
      pushStyle()
      prevStyle?.let { recorder.applyStyle(it) }
    }
  }

  fun tearDown(index: Int, andWrite: Boolean, tempFileName: String) {
    prevStyle = recorder?.style ?: graphics?.style
    endRecord()
    if (!andWrite) return
    xml.addChild(
      XML("g").also { layerGroup ->
        layerGroup.setString("inkscape:groupmode", "layer")
        layerGroup.setString("inkscape:label", index.toString())
        layerGroup.setString("id", resultFile)
        XML(File(tempFileName))
          .children
          .filter { child -> child.name == "g" }
          .forEach { layerGroup.addChild(it) }
      },
    )
  }

  setup(getTempFileNameAndPath(baseSketchName, fileSuffix, 0, time))

  drawLayerSequence.forEachIndexed { index, _ ->
    tearDown(index, true, getTempFileNameAndPath(baseSketchName, fileSuffix, index, time))
    setup(getTempFileNameAndPath(baseSketchName, fileSuffix, index + 1, time))
  }

  tearDown(-1, false, "")

  File(tempFolder).deleteRecursively()

  xml.save(File(resultFile))
}

/**
 * Take multiple layers and combine them into a single SVG, using
 * Inscape's grouping labels.
 */
fun PApplet.combineDrawLayersIntoSVG(
  baseSketchName: String,
  fileSuffix: String,
  numLayers: Int,
  drawLayer: (Int) -> Unit,
) {
  val time: Instant = Instant.now()
  val resultFile = getSVGNameAndPath(baseSketchName, fileSuffix, time)

  val tempFolder = getTempFilepath(baseSketchName)

  // Create an empty SVG file and load it
  recordSvg(resultFile) {}
  val xml = XML(File(resultFile))

  // Set inkscape namespace so we can add inkscape layer defs.
  xml.setString("xmlns:inkscape", "http://www.inkscape.org/namespaces/inkscape/")

  numLayers.times { layerIndex ->
    val tempFileName = getTempFileNameAndPath(baseSketchName, fileSuffix, layerIndex, time)
    recordSvg(tempFileName) { drawLayer(layerIndex) }
    xml.addChild(
      XML("g").also { layerGroup ->
        layerGroup.setString("inkscape:groupmode", "layer")
        layerGroup.setString("inkscape:label", layerIndex.toString())
        layerGroup.setString("id", resultFile)
        XML(File(tempFileName))
          .children
          .filter { child -> child.name == "g" }
          .forEach { layerGroup.addChild(it) }
      },
    )
  }

  File(tempFolder).deleteRecursively()

  xml.save(File(resultFile))
}
