package controls.utils

import BaseSketch
import util.io.getPresetFilepath
import java.io.File

fun BaseSketch.deletePresetFile(presetName: String) {
  val f = File(getPresetFilepath(svgBaseFileName, presetName))

  if (f.exists()) f.delete()
}
