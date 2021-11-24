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
import appletExtensions.draw.vertex
import coordinate.Arc
import coordinate.BoundRect
import coordinate.Circ
import coordinate.Line
import coordinate.Point
import coordinate.Segment
import coordinate.transforms.ShapeTransform
import de.lighti.clipper.Clipper.ClipType
import de.lighti.clipper.Clipper.ClipType.INTERSECTION
import fastnoise.Noise
import interfaces.shape.Maskable
import org.opencv.core.Mat
import processing.core.PApplet
import processing.core.PImage
import util.atAmountAlong
import util.base.lerp
import util.image.opencvMat.getTransformedMat
import util.image.opencvMat.toPImage
import util.image.pimage.scale
import util.iterators.addNotNull
import util.iterators.forEach2D
import util.iterators.mapWithNext
import util.numbers.boundMin
import util.polylines.PolyLine
import util.polylines.clipping.clip
import util.polylines.forEachSegment
import util.polylines.toPolyLine
import java.awt.Color

/**
 * Todo: Why is this not just a bunch of top level extension functions?
 */
open class PAppletExt : PApplet() {
  val windowBounds: BoundRect get() = BoundRect(Point.Zero, width, height)

  @Suppress("PropertyName")
  val Noise = getNoise(Perlin)

  fun background(c: Color) = background(c.rgb)
  fun stroke(c: Color) = stroke(c.rgb)
  fun fill(c: Color) = fill(c.rgb)
  fun setSurfaceSize(p: Point) = surface.setSize(p.xi, p.yi)

  @Suppress("SameParameterValue")
  private fun getNoise(type: NoiseType = Perlin): FastNoiseLite =
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

  fun noiseXY(p: Point) = Point(Noise.GetNoise(p.xf, p.yf, 0f), Noise.GetNoise(p.xf, p.yf, 100f))
  fun noiseXY(x: Number, y: Number) = noiseXY(Point(x, y))

  fun PolyLine.toSegments(): List<Segment> = mapWithNext { curr, next -> Segment(curr, next) }
  fun List<Segment>.toVertices(): PolyLine = map { it.p1 }.addNotNull(lastOrNull()?.p2)

  private fun PolyLine.shape() {
    beginShape()
    forEach { vertex(it) }
    endShape()
  }

  private fun List<PolyLine>.shapes() = map { it.shape() }

  private fun getBoundLine(
    unboundLines: PolyLine,
    bound: BoundRect,
    boundInside: Boolean,
  ): List<PolyLine> = getBoundLines(listOf(unboundLines), bound, boundInside)

  private fun getBoundLines(
    unboundLines: List<PolyLine>,
    bound: BoundRect,
    boundInside: Boolean,
  ): List<PolyLine> {
    val rectLine = bound.toPolyLine()
    val clipOperation = if (boundInside) INTERSECTION else ClipType.DIFFERENCE

    return unboundLines.clip(rectLine, clipOperation)
  }

  fun PolyLine.draw(bound: BoundRect? = null, boundInside: Boolean = true) =
    listOf(this).draw(bound, boundInside)

  @JvmName("drawPolyLines")
  fun List<PolyLine>.draw(bound: BoundRect? = null, boundInside: Boolean = true) {
    val lines = if (bound == null) this else getBoundLines(this, bound, boundInside)
    lines.shapes()
  }

  /**
   * Note that this will break your SVG output into a million separate lines, only do it during
   * testing!
   */
  fun List<PolyLine>.drawDebug() {
    val totalSize = sumOf { (it.size - 1).boundMin(0) }
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

  @JvmName("drawManyPolyLines")
  fun List<List<PolyLine>>.draw(bound: BoundRect? = null, boundInside: Boolean = true) =
    flatten().draw(bound, boundInside)

  @JvmName("drawManyPolyLinesList")
  fun List<List<List<PolyLine>>>.draw(bound: BoundRect, boundInside: Boolean = true) =
    flatten().flatten().draw(bound, boundInside)

  fun Circ.draw() = circle(this)
  fun Segment.draw() = line(this)
  fun Line.draw(bounds: BoundRect) = bounds.getBoundSegment(this)?.let { line(it) }
  fun BoundRect.draw() = rect(this)
  fun Arc.draw() = arc(this)
  fun Maskable.draw() = draw(this@PAppletExt)
  fun Point.draw(radius: Number = 2, color: Color? = null) = drawPoint(this, radius, color)
  fun Point.draw(radii: List<Number>, color: Color? = null) = radii.forEach {
    draw(it, color)
  }

  fun Iterable<Point>.drawPoints(radius: Number = 2) = drawPoints(this, radius)

  fun PImage.draw(topLeft: Point) = image(this, topLeft.xf, topLeft.yf)
  fun PImage.draw(bound: BoundRect) = scale(bound.size).draw(bound.topLeft)
  fun Mat.draw(offset: Point = Point.Zero) = toPImage().draw(offset)

  fun Mat.draw(matToScreenTransform: ShapeTransform, boundRect: BoundRect) {
    getTransformedMat(matToScreenTransform, boundRect)?.draw(boundRect.topLeft)
  }
}
