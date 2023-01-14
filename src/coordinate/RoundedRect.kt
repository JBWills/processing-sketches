package coordinate

import de.lighti.clipper.Clipper.ClipType.DIFFERENCE
import de.lighti.clipper.Clipper.ClipType.INTERSECTION
import interfaces.shape.Maskable
import interfaces.shape.Transformable
import interfaces.shape.Walkable
import util.base.step
import util.debugLog
import util.easing.Ease.Linear
import util.interpolation.interpolate
import util.polar.polarToPoint
import util.polylines.PolyLine
import util.polylines.clipping.clip
import util.polylines.iterators.walk
import util.polylines.rotate
import util.polylines.toSegment
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

const val DefaultSquircleStep: Double = 1.0

data class RoundedRect(val base: BoundRect, val r: Double, val step: Double = DefaultSquircleStep) :
  Walkable, Maskable,
  Transformable<RoundedRect> {

  val polyLine by lazy { toPolyLine() }

  override fun contains(p: Point): Boolean {
    return if (!base.contains(p)) {
      // the rect is guaranteed to be bigger than the squircle
      false
    } else if (base.width < r || base.height < r) {
      // if the radius is sufficiently large, it's just an ellipse
      // yeah this isn't quite right and will result in some points not being included in
      // very wide or tall squircles with large radii, but that's all the math I feel like doing rn.
      Circ(base.center, r).contains(p)
    } else if (base.shrink(r).contains(p)) {
      true
    } else {
      polyLine.contains(p)
    }
  }

  override fun intersection(line: Line, memoized: Boolean): List<Segment> =
    polyLine.clip(base.intersection(line).map { it.toPolyLine() }, INTERSECTION)
      .map { it.toSegment() }


  override fun intersection(segment: Segment, memoized: Boolean): List<Segment> =
    polyLine.clip(segment.toPolyLine(), INTERSECTION).map { it.toSegment() }

  override fun diff(segment: Segment, memoized: Boolean): List<Segment> =
    polyLine.clip(segment.toPolyLine(), DIFFERENCE).map { it.toSegment() }

  override fun intersection(polyLine: PolyLine, memoized: Boolean): List<PolyLine> =
    this.polyLine.clip(polyLine, INTERSECTION)

  override fun diff(polyLine: PolyLine, memoized: Boolean): List<PolyLine> =
    this.polyLine.clip(polyLine, DIFFERENCE)

  override fun rotated(deg: Deg, anchor: Point): PolyLine = polyLine.rotate(deg, anchor)

  override fun scaled(scale: Point, anchor: Point): RoundedRect =
    RoundedRect(base.scale(scale), r * scale.x, step)

  override fun translated(translate: Point): RoundedRect =
    RoundedRect(base.translated(translate), r, step)

  override fun walk(step: Double): List<Point> = polyLine.walk(step)

  override fun <T> walk(step: Double, block: (Point) -> T): List<T> = polyLine.walk(step, block)

  private fun toPolyLine(): PolyLine {
    if (r == 0.0) {
      return base.toPolyLine()
    }
    val ellipse = Ellipse(base.width / 2, base.height / 2, Point.Zero)

    if (r == 1.0) {
      return ellipse.walk(step)
    }

    debugLog(r)

    val exponent = (2.0..100.0).interpolate(1 - r, Linear.f)

    val twoPi = 2 * PI
    val radStep = (step * twoPi) / base.perimeter
    val center = base.center
    val a = ellipse.xRadius
    val b = ellipse.yRadius

    return (0.0..twoPi step radStep).map { t ->
      val polarRadius = Point(cos(t) / a, sin(t) / b)
        .abs()
        .pow(exponent)
        .sum()
        .pow(-1 / exponent)

      return@map center + polarToPoint(t, polarRadius)
    }
  }
}
