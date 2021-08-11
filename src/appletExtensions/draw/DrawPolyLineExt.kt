package appletExtensions.draw

import appletExtensions.stroke
import appletExtensions.withStrokeIf
import coordinate.Point
import coordinate.Segment
import geomerativefork.src.RPoint
import processing.core.PApplet
import processing.core.PGraphics
import util.polylines.PolyLine
import util.polylines.polyLine.normalizeForPrint
import java.awt.Color

fun PApplet.vertex(p: Point) = vertex(p.xf, p.yf)

fun PApplet.shape(vertices: PolyLine) {
  beginShape()
  vertices.normalizeForPrint().forEach { vertex ->
    vertex(vertex)
  }
  endShape()
}

fun PApplet.shapes(lines: List<PolyLine>, debug: Boolean = false) =
  lines.forEachIndexed { lineIndex, vertices ->
    withStrokeIf(debug, if (lineIndex % 2 == 0) Color.RED else Color.GREEN) {
      beginShape()
      vertices.forEach { vertex -> vertex(vertex) }
      endShape()
    }
  }

fun PApplet.shape(vertices: Array<RPoint>) {
  beginShape()
  vertices.forEach { vertex(it.x, it.y) }
  endShape()
}

fun PApplet.shapeSegments(segments: List<Segment>) {
  beginShape()
  segments.forEachIndexed { index, segment ->
    vertex(segment.p1)
    if (index == segments.size - 1) {
      vertex(segment.p2)
    }
  }
  endShape()
}

fun PGraphics.vertex(p: Point) = vertex(p.xf, p.yf)

fun PGraphics.shape(vertices: PolyLine) {
  beginShape()
  vertices.normalizeForPrint().forEach { vertex ->
    vertex(vertex)
  }
  endShape()
}

private fun PGraphics.shapes(lines: List<PolyLine>, debug: Boolean = false) =
  lines.forEachIndexed { lineIndex, vertices ->
    if (debug) {
      pushStyle()
      stroke(if (lineIndex % 2 == 0) Color.RED else Color.GREEN)
    }
    beginShape()
    vertices.forEach { vertex -> vertex(vertex) }
    endShape()
    if (debug) popStyle()
  }

fun PGraphics.shape(vertices: Array<RPoint>) {
  beginShape()
  vertices.forEach { vertex(it.x, it.y) }
  endShape()
}

fun PGraphics.shapeSegments(segments: List<Segment>) {
  beginShape()
  segments.forEachIndexed { index, segment ->
    vertex(segment.p1)
    if (index == segments.size - 1) {
      vertex(segment.p2)
    }
  }
  endShape()
}
