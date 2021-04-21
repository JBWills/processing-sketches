package util.io

import BaseSketch
import arrow.core.memoize
import processing.core.PImage
import java.io.File

private val _loadImageMemo: (BaseSketch, String) -> PImage? = { sketch: BaseSketch, path: String ->
  if (!File(path).exists()) null
  else sketch.loadImage(path)
}.memoize()


fun BaseSketch.loadImageMemo(path: String): PImage? = _loadImageMemo(this, path)
