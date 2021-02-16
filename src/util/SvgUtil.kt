package util

import processing.core.PApplet
import processing.data.XML
import java.io.File

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
  pathToSVGFolder: String,
  baseFilename: String,
  numLayers: Int,
  drawLayer: (Int) -> Unit,
) {
  val resultFile = "$pathToSVGFolder/$baseFilename.svg"

  val tempFolder = "$pathToSVGFolder/temp"
  File(tempFolder).mkdirs()

  // Create an empty SVG file and load it
  recordSvg(resultFile) {}
  val xml = XML(File(resultFile))

  // Set inkscape namespace so we can add inkscape layer defs.
  xml.setString("xmlns:inkscape", "http://www.inkscape.org/namespaces/inkscape/")

  numLayers.times { layerIndex ->
    val tempFileName = "$tempFolder/$baseFilename--layer-$layerIndex.svg"
    recordSvg(tempFileName) { drawLayer(layerIndex) }
    xml.addChild(XML("g").also { layerGroup ->
      layerGroup.setString("inkscape:groupmode", "layer")
      layerGroup.setString("inkscape:label", baseFilename)
      layerGroup.setString("id", baseFilename)
      XML(File(tempFileName))
        .children
        .filter { child -> child.name == "g" }
        .forEach { layerGroup.addChild(it) }
    })
  }

  File(tempFolder).deleteRecursively()

  xml.save(File(resultFile))
}
