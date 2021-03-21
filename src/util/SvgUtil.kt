package util

import processing.core.PApplet
import processing.data.XML
import util.io.getSVGNameAndPath
import util.io.getTempFileNameAndPath
import util.io.getTempFilepath
import java.io.File
import java.time.Instant

fun PApplet.recordSvg(filename: String, block: () -> Unit) {
  beginRecord(PApplet.SVG, filename)
  block()
  endRecord()
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
    xml.addChild(XML("g").also { layerGroup ->
      layerGroup.setString("inkscape:groupmode", "layer")
      layerGroup.setString("inkscape:label", layerIndex.toString())
      layerGroup.setString("id", resultFile)
      XML(File(tempFileName))
        .children
        .filter { child -> child.name == "g" }
        .forEach { layerGroup.addChild(it) }
    })
  }

  File(tempFolder).deleteRecursively()

  xml.save(File(resultFile))
}
