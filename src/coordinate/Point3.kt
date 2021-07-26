package coordinate

import interfaces.shape.Scalar
import kotlinx.serialization.Serializable

@Serializable
data class Point3(val x: Double, val y: Double, val z: Double) : Scalar<Point3> {
  override val values: List<Double> = listOf(x, y, z)

  init {
    if (x.isNaN() || y.isNaN() || z.isNaN()) throw Exception("Can't create a point with nan values. x=$x, y=$y, z=$z")
  }

  val xf get() = x.toFloat()
  val yf get() = y.toFloat()
  val zf get() = z.toFloat()

  val xl get() = x.toLong()
  val yl get() = y.toLong()
  val zl get() = z.toLong()

  val xi get() = x.toInt()
  val yi get() = y.toInt()
  val zi get() = z.toInt()

  constructor(x: Number, y: Number, z: Number) : this(x.toDouble(), y.toDouble(), z.toDouble())

  constructor(p: Number) : this(p, p, p)
  constructor(p: Number, p2: Number) : this(p, p2, 0.0)
  constructor(p: Point) : this(p.x, p.y, 0.0)

  fun toPoint(): Point = Point(this)
  override fun fromValues(values: List<Double>): Point3 = Point3(values[0], values[1], values[2])

  @Suppress("NAME_SHADOWING")
  override fun scaled(scale: Point, anchor: Point): Point3 {
    val anchor = anchor.toPoint3D()
    val scale = scale.toPoint3D()
    val diffVector = minus(anchor)
    val scaledDiffVector = diffVector * scale
    return anchor + scaledDiffVector
  }

  override fun scaled3(scale: Point3, anchor: Point3): Point3 {
    val diffVector = minus(anchor)
    val scaledDiffVector = diffVector * scale
    return anchor + scaledDiffVector
  }

  override fun translated(translate: Point) = plus(translate.toPoint3D())
  override fun translated3(translate: Point3) = plus(translate)

  override fun toString(): String = toString("Point3", decimalPrecision = 5)

  companion object {

  }
}
