package util

import coordinate.Point
import processing.core.PApplet
import processing.core.PStyle
import processing.data.XML
import util.io.getSVGNameAndPath
import util.io.getTempFileNameAndPath
import util.io.getTempFilepath
import util.print.applyStyle
import util.xml.set
import util.xml.setSizePx
import java.io.File
import java.time.Instant

fun PApplet.recordSvg(filename: String, block: () -> Unit) {
  beginRecord(PApplet.SVG, filename)
  block()
  endRecord()
}

fun PApplet.drawLayeredSvg(
  baseSketchName: String,
  fileSuffix: String,
  sketchSize: Point,
  drawLayerSequence: Sequence<Unit>,
) {
  val time: Instant = Instant.now()
  val resultFile = getSVGNameAndPath(baseSketchName, fileSuffix, time)
  val tempFolder = getTempFilepath(baseSketchName)

  // Create an empty SVG file and load it
  recordSvg(resultFile) {}
  val xml = XML(File(resultFile))

  // Set inkscape namespace, so we can add inkscape layer definitions.
  xml.set(
    "xmlns:ns" to "http://www.inkscape.org/namespaces/inkscape/",
    "xmlns:inkscape" to "http://www.inkscape.org/namespaces/inkscape/",
  )

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
      XML("g").also { layerChild ->
        layerChild.setString("inkscape:groupmode", "layer")
        layerChild.setString("inkscape:label", "$index.layer")
        layerChild.setString("id", "layer$index")
        XML(File(tempFileName))
          .children
          .filter { child -> child.name == "g" }
          .forEach { layerChild.addChild(it) }
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

  // Set this at the end to override processing size values. the reason for this is if your screen
  // is too small, processing will record an invalid paper size which screws up your drawings.
  xml.setSizePx(sketchSize)

  File(resultFile).let { f ->
    xml.save(f)
    removeInkscapeNamespaceHack(f)
  }
}

/**
 * This is a hack to remove the xmlns:inkscape definition from the top of the file. Why? Because
 * inkscape only shows layers if this is not defined.
 *
 * But then why was the namespace added in the first place? Because the XML class we're using
 * requires correct input or else it crashes, so the only way we can do inkscape:groupMode, etc.
 * we need to set the definition, add those values, then call this hack to remove the definition in
 * the finished file (outside the XML parser which would complain).
 *
 * @param file the file to find-and-replace in
 */
fun removeInkscapeNamespaceHack(f: File) {
  val lines = f.readLines().toMutableList()
  val inkDefIndex = lines.indexOfFirst { it.contains("xmlns:inkscape") }
  lines[inkDefIndex] = lines[inkDefIndex].replace(
    "xmlns:inkscape=\"http://www.inkscape.org/namespaces/inkscape/\"",
    "",
  )

  f.writeText(lines.joinToString("\n"))
}
