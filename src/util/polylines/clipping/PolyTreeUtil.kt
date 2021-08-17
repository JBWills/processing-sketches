package util.polylines.clipping

import de.lighti.clipper.PolyNode
import util.polylines.length

fun PolyNode.print(paddingLevel: Int = 0) {
  val paddingString = "|  ".repeat(paddingLevel)
  val line = polygon.toPolyLine()
  println("${paddingString}Node: length: ${line.length.toInt()}, start: ${line.firstOrNull()} end ${line.lastOrNull()}")
  childs.forEach { it.print(paddingLevel + 1) }
}
