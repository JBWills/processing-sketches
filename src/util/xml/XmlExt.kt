package util.xml

import coordinate.Point
import processing.data.XML

fun XML.set(vararg pairs: Pair<String, String>): XML = apply {
  pairs.forEach { (key, value) ->
    setString(key, value.trimEnd('/'))
  }
}

fun XML.setSizePx(size: Point): XML = apply {
  val (width, height) = size.toPair { it.toInt().toString() }
  set("width" to width, "height" to height)
}
