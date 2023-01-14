package coordinate

import interfaces.shape.Polarable
import interfaces.shape.Walkable
import util.base.step
import util.numbers.squared
import util.tuple.map
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class Ellipse(val xRadius: Double, val yRadius: Double, val center: Point) : Polarable,
  Walkable {
  val c = sqrt(abs(xRadius.squared() - yRadius.squared()))
  private val isHorizontal = xRadius > yRadius

  private val perimeter = 2 * PI * sqrt(
    (xRadius.squared() + yRadius.squared()) / 2,
  )

  val foci: Pair<Point, Point> =
    Pair(-c, c).map { if (isHorizontal) Point(it, 0) else Point(0, it) }

  override fun polarRadius(rad: Double): Double {
    return (xRadius * yRadius) / sqrt(
      (yRadius * cos(rad)).squared() + (xRadius * sin(rad)).squared(),
    )
  }

  override fun walk(step: Double): List<Point> = walk(step) { it }

  override fun <T> walk(step: Double, block: (Point) -> T): List<T> {
    val stepPercent = (step / perimeter)

    return (0.0..1.0 step stepPercent).map {
      val theta = it * 2 * PI
      val radius = polarRadius(theta)
      block(center + radius * Point(cos(theta), sin(theta)))
    }
  }
}
