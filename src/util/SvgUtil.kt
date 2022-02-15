package util

import coordinate.Point
import processing.core.PApplet
import processing.core.PStyle
import processing.data.XML
import util.io.getSVGNameAndPath
import util.io.getTempFileNameAndPath
import util.io.getTempFilepath
import util.layers.LayerSVGConfig
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
  block: (onNewLayer: (LayerSVGConfig) -> Unit) -> Unit
) {
  val time: Instant = Instant.now()
  val resultFile = getSVGNameAndPath(baseSketchName, fileSuffix, time)
  val tempFolder = getTempFilepath(baseSketchName)

  // Create an empty SVG file and load it
  recordSvg(resultFile) {}
  val xml = XML(File(resultFile))

  // Set inkscape namespace, so we can add inkscape layer definitions.
  xml.set(
    "xmlns:inkscape" to "http://www.inkscape.org/namespaces/inkscape",
  )


  var prevStyle: PStyle? = graphics?.style

  fun setup(tempFileName: String) {
    beginRecord(PApplet.SVG, tempFileName)
    if (prevStyle != null) {
      pushStyle()
      prevStyle?.let { recorder.applyStyle(it) }
    }
  }

  fun tearDown(index: Int, andWrite: Boolean, tempFileName: String, layerName: String) {
    prevStyle = recorder?.style ?: graphics?.style
    endRecord()
    if (!andWrite) return
    xml.addChild(
      XML("g").also { layerChild ->
        layerChild.setString("inkscape:groupmode", "layer")
        layerChild.setString("inkscape:label", "$index.${layerName}")
        layerChild.setString("id", "$index.${layerName}")
        XML(File(tempFileName))
          .children
          .filter { child -> child.name == "g" }
          .forEach { layerChild.addChild(it) }
      },
    )
  }

  setup(getTempFileNameAndPath(baseSketchName, fileSuffix, 0, time))

  var nextLayerName: String? = null

  var index = 0

  fun onBlockCalled(config: LayerSVGConfig) {
    val layerName = config.layerName ?: nextLayerName ?: ""
    tearDown(
      index,
      true,
      getTempFileNameAndPath(baseSketchName, fileSuffix, index, time),
      layerName,
    )
    setup(getTempFileNameAndPath(baseSketchName, fileSuffix, index + 1, time))
    nextLayerName = config.nextLayerName

    index++
  }

  block { config -> onBlockCalled(config) }

  tearDown(-1, false, "", "")

  File(tempFolder).deleteRecursively()

  // Set this at the end to override processing size values. the reason for this is if your screen
  // is too small, processing will record an invalid paper size which screws up your drawings.
  xml.setSizePx(sketchSize)

  xml.save(File(resultFile))
}
