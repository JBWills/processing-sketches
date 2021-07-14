package appletExtensions

import FastNoiseLite
import FastNoiseLite.NoiseType
import FastNoiseLite.NoiseType.Perlin
import appletExtensions.draw.arc
import appletExtensions.draw.circle
import appletExtensions.draw.drawPoint
import appletExtensions.draw.drawPoints
import appletExtensions.draw.line
import appletExtensions.draw.rect
import appletExtensions.draw.shape
import appletExtensions.draw.shapes
import coordinate.Arc
import coordinate.BoundRect
import coordinate.Circ
import coordinate.ContinuousMaskedShape
import coordinate.Line
import coordinate.Point
import coordinate.Segment
import fastnoise.Noise
import geomerativefork.src.RPath
import geomerativefork.src.util.boundMin
import interfaces.shape.Maskable
import processing.core.PApplet
import util.atAmountAlong
import util.iterators.addNotNull
import util.iterators.forEach2D
import util.iterators.mapWithNext
import util.lerp
import util.pointsAndLines.polyLine.forEachSegment
import util.pointsAndLines.polyLine.normalizeForPrint
import util.pointsAndLines.polyLine.toPolyLine
import util.randomColor
import java.awt.Color

/**
 * Todo: Why is this not just a bunch of top level extension functions?
 */
open class PAppletExt : PApplet() {

  val windowBounds: BoundRect get() = BoundRect(Point.Zero, width, height)

  val NOISE = getNoise(Perlin)

  fun background(c: Color) = background(c.rgb)
  fun stroke(c: Color) = stroke(c.rgb)
  fun fill(c: Color) = fill(c.rgb)
  fun setSurfaceSize(p: Point) = surface.setSize(p.xi, p.yi)

  fun getNoise(type: NoiseType = Perlin): FastNoiseLite =
    FastNoiseLite().also { it.SetNoiseType(type) }

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

  fun point(p: Point) = point(p.x, p.y)
  fun point(x: Number, y: Number) = point(x.toFloat(), y.toFloat())

  fun debugCirc(p: Point) = circle(p.xf, p.yf, 5f)
  fun Point.drawDebugCirc() = debugCirc(this)

  fun noiseXY(p: Point) = Point(NOISE.GetNoise(p.xf, p.yf, 0f), NOISE.GetNoise(p.xf, p.yf, 100f))
  fun noiseXY(x: Number, y: Number) = noiseXY(Point(x, y))


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
  fun List<List<Point>>.draw(
    bound: BoundRect,
    boundInside: Boolean = true,
    randomColors: Boolean = false
  ) = forEach {
    if (randomColors) {
      pushStyle()
      stroke(randomColor())
    }
    shape(it, bound, boundInside)

    if (randomColors) popStyle()
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

  @JvmName("drawManyPolyLines")
  fun List<List<List<Point>>>.draw(debug: Boolean = false) = shapes(this.flatten(), debug)
  fun RPath.drawLine() = points.map { Point(it.x, it.y) }.draw()
  fun Circ.draw() = circle(this)
  fun Segment.draw() = line(this)
  fun Line.draw(bounds: BoundRect) = bounds.getBoundSegment(this)?.let { line(it) }
  fun BoundRect.draw() = rect(this)
  fun Arc.draw() = arc(this)
  fun Maskable.draw() = draw(this@PAppletExt)
  fun Point.draw(radius: Number = 2) = drawPoint(this, radius)
  fun Iterable<Point>.drawPoints(radius: Number = 2) = drawPoints(this, radius)
}
