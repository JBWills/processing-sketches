package appletExtensions

import FastNoiseLite
import FastNoiseLite.NoiseType
import FastNoiseLite.NoiseType.Perlin
import coordinate.Arc
import coordinate.BoundRect
import coordinate.Circ
import coordinate.ContinuousMaskedShape
import coordinate.Deg
import coordinate.Line
import coordinate.Point
import coordinate.Segment
import fastnoise.Noise
import geomerativefork.src.RPath
import geomerativefork.src.RPoint
import geomerativefork.src.util.boundMin
import processing.core.PApplet
import util.atAmountAlong
import util.iterators.addNotNull
import util.iterators.forEach2D
import util.iterators.mapWithNext
import util.lerp
import util.pointsAndLines.polyLine.forEachSegment
import util.pointsAndLines.polyLine.normalizeForPrint
import util.pointsAndLines.polyLine.toPolyLine
import util.toRadians
import java.awt.Color

/**
 * Todo: Why is this not just a bunch of top level extension functions?
 */
open class PAppletExt : PApplet() {

  val NOISE = getNoise(Perlin)

  fun background(c: Color) = background(c.rgb)
  fun stroke(c: Color) = stroke(c.rgb)
  fun fill(c: Color) = fill(c.rgb)
  fun setSurfaceSize(p: Point) = surface.setSize(p.xi, p.yi)

  fun getNoise(type: NoiseType = Perlin): FastNoiseLite =
    FastNoiseLite().also { it.SetNoiseType(type) }

  fun line(l: Segment) = line(l.p1, l.p2)
  fun line(l: Line, length: Number, centered: Boolean = true) {
    val lineExtender = (length.toDouble() / 2)

    if (centered) {
      line(l.getPointAtDist(-lineExtender), l.getPointAtDist(lineExtender))
    } else {
      line(l.origin, l.getPointAtDist(lineExtender * 2))
    }
  }

  fun Noise.color(
    bound: BoundRect,
    startColor: Color = Color.WHITE,
    endColor: Color = Color.BLACK
  ) = toValueMatrix(bound).forEach2D { rowIndex, colIndex, item ->
    withStroke(Color((startColor.rgb..endColor.rgb).atAmountAlong(item).toInt())) {
      point(rowIndex + bound.left.toFloat(), colIndex + bound.right.toFloat())
    }
  }

  fun strokeWeight(weight: Number) = strokeWeight(weight.toFloat())

  fun circle(x: Number, y: Number, r: Number) =
    ellipse(x.toFloat(), y.toFloat(), r.toFloat(), r.toFloat())

  fun point(p: Point) = point(p.x, p.y)
  fun point(x: Number, y: Number) = point(x.toFloat(), y.toFloat())

  fun line(p1: Point, p2: Point) = line(p1.xf, p1.yf, p2.xf, p2.yf)
  fun rect(r: BoundRect) =
    rect(r.left.toFloat(), r.top.toFloat(), r.width.toFloat(), r.height.toFloat())

  fun circle(c: Circ) = circle(c.origin.xf, c.origin.yf, c.diameter.toFloat())
  fun debugCirc(p: Point) = circle(p.xf, p.yf, 5f)
  fun Point.drawDebugCirc() = debugCirc(this)

  fun noiseXY(p: Point) = Point(NOISE.GetNoise(p.xf, p.yf, 0f), NOISE.GetNoise(p.xf, p.yf, 100f))
  fun noiseXY(x: Number, y: Number) = noiseXY(Point(x, y))

  fun arc(a: Arc) {
    if (a.lengthClockwise >= 360.0) {
      circle(a)
      return
    }

    arc(a, a.startDeg, a.endDeg)
  }

  fun arcFlipped(a: Arc) {
    if (a.lengthClockwise >= 360.0) {
      circle(a)
      return
    }

    // Need to flip the arc and switch the start and end points because the arc Processing util draws counterclockwise,
    // but our arcs store start and end degrees in clockwise notation.
    val aFlipped = a.flippedVertically()
    arc(a, aFlipped.endDeg, aFlipped.startDeg)
  }

  private fun arc(c: Circ, startDeg: Deg, endDeg: Deg) {
    val endDegValue = if (endDeg.value < startDeg.value) {
      endDeg.value + 360.0
    } else endDeg.value


    arc(
      c.origin.xf, c.origin.yf, c.diameter.toFloat(), c.diameter.toFloat(),
      startDeg.rad.toFloat(), endDegValue.toRadians().toFloat(),
    )
  }

  fun Point.drawPoint(radius: Number = 2) {
    if (radius.toDouble() > 0) circle(Circ(this, radius))
  }

  fun Point.drawSquare(size: Number = 2, rotation: Deg = Deg(0)) {
    if (size.toDouble() > 0)
      BoundRect.centeredRect(this, Point(size, size))
        .toRPath()
        .apply { rotate(rotation.rad.toFloat()) }.draw()
  }

  fun Iterable<Point>.drawPoints(radius: Int = 2) = forEach { it.drawPoint(radius) }

  fun arcs(arcs: Iterable<Arc>) = arcs.forEach { a -> arc(a) }

  fun boundArc(arc: Arc, bound: BoundRect) = arcs(arc.clipInsideRect(bound))

  fun vertex(p: Point) = vertex(p.xf, p.yf)

  fun shape(vertices: List<Point>) {
    beginShape()
    vertices.normalizeForPrint().forEach { vertex ->
      vertex(vertex)
    }
    endShape()
  }

  private fun shapes(lines: List<List<Point>>, debug: Boolean = false) =
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

  fun shape(vertices: Array<RPoint>) {
    beginShape()
    vertices.forEach { vertex(it.x, it.y) }
    endShape()
  }

  fun shapeSegments(segments: List<Segment>) {
    beginShape()
    segments.forEachIndexed { index, segment ->
      vertex(segment.p1)
      if (index == segments.size - 1) {
        vertex(segment.p2)
      }
    }
    endShape()
  }

  fun List<Point>.toSegments(): List<Segment> = mapWithNext { curr, next -> Segment(curr, next) }
  fun List<Segment>.toVertices(): List<Point> = map { it.p1 }.addNotNull(lastOrNull()?.p2)

  fun getBoundLines(
    unboundLine: List<Point>, bound: BoundRect, boundInside: Boolean,
  ): List<List<Point>> =
    ContinuousMaskedShape(unboundLine, bound).toBoundPoints(boundInside)

  fun shape(vertices: List<Point>, bound: BoundRect, boundInside: Boolean = true) {
    getBoundLines(vertices, bound, boundInside).map { shapeList ->
      shape(shapeList)
    }
  }

  fun shape(path: RPath, bound: BoundRect, boundInside: Boolean = true) {
    getBoundLines(
      path.points.map { Point(it.x, it.y) }.normalizeForPrint(),
      bound,
      boundInside,
    ).map { shapeList ->
      shape(shapeList)
    }
  }

  fun List<Point>.draw(bound: BoundRect, boundInside: Boolean = true) {
    shape(this, bound, boundInside)
  }

  @JvmName("drawSegments")
  fun List<Segment>.draw(bound: BoundRect, boundInside: Boolean = true) = forEach {
    shape(listOf(it.p1, it.p2), bound, boundInside)
  }

  @JvmName("drawLines")
  fun List<List<Point>>.draw(bound: BoundRect, boundInside: Boolean = true) = forEach {
    shape(it, bound, boundInside)
  }

  /**
   * Note that this will break your SVG output into a million separate lines, only do it during
   * testing!
   */
  fun List<List<Point>>.drawDebug() {
    val totalSize = sumBy { (it.size - 1).boundMin(0) }
    var currIndex = 0

    val colors = listOf(Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.BLUE)

    forEach { line ->
      beginShape()
      line.forEachSegment { segment ->
        withStroke(colors.lerp(currIndex.toDouble() / (totalSize - 1))) {
          segment.draw()
        }
        currIndex += 1
      }
      endShape()
    }
  }

  fun List<Segment>.drawAsSegments() = map { it.draw() }
  fun List<Segment>.drawAsLine() = toPolyLine().draw()
  fun List<Point>.draw() = shape(this)

  @JvmName("drawPolyLines")
  fun List<List<Point>>.draw(debug: Boolean = false) = shapes(this, debug)
  fun RPath.drawLine() = points.map { Point(it.x, it.y) }.draw()
  fun Circ.draw() = circle(this)
  fun Segment.draw() = line(this)
  fun Line.draw(bounds: BoundRect) = bounds.getBoundSegment(this)?.let { line(it) }
  fun BoundRect.draw() = rect(this)
  fun Arc.draw() = arc(this)
}
