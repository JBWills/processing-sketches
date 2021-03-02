package appletExtensions

import FastNoiseLite
import FastNoiseLite.NoiseType
import FastNoiseLite.NoiseType.Perlin
import coordinate.*
import geomerativefork.src.RPath
import geomerativefork.src.RPoint
import processing.core.PApplet
import util.addNotNull
import util.mapWithNext
import util.toRadians

/**
 * Todo: Why is this not just a bunch of top level extension functions?
 */
open class PAppletExt : PApplet() {

  val NOISE = getNoise(Perlin)

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

  fun strokeWeight(weight: Number) = strokeWeight(weight.toFloat())

  fun circle(x: Number, y: Number, r: Number) = ellipse(x.toFloat(), y.toFloat(), r.toFloat(), r.toFloat())

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


    arc(c.origin.xf, c.origin.yf, c.diameter.toFloat(), c.diameter.toFloat(),
      startDeg.rad.toFloat(), endDegValue.toRadians().toFloat())
  }

  fun Point.drawPoint(radius: Int = 2) = circle(Circ(this, radius))
  fun Iterable<Point>.drawPoints(radius: Int = 2) = forEach { it.drawPoint(radius) }

  fun arcs(arcs: Iterable<Arc>) = arcs.forEach { a -> arc(a) }

  fun boundArc(arc: Arc, bound: BoundRect) = arcs(arc.clipInsideRect(bound))

  fun vertex(p: Point) = vertex(p.xf, p.yf)

  fun shape(vertices: List<Point>) {
    beginShape()
    vertices.forEach { vertex(it) }
    endShape()
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
    getBoundLines(path.points.map { Point(it.x, it.y) }, bound, boundInside).map { shapeList ->
      shape(shapeList)
    }
  }

  fun List<Point>.draw(bound: BoundRect, boundInside: Boolean = true) {
    shape(this, bound, boundInside)
  }

  @JvmName("drawLines")
  fun List<List<Point>>.draw(bound: BoundRect, boundInside: Boolean = true) = forEach {
    shape(it, bound, boundInside)
  }

  fun List<Point>.draw() = shape(this)
  fun Circ.draw() = circle(this)
  fun BoundRect.draw() = rect(this)
  fun Arc.draw() = arc(this)
}
