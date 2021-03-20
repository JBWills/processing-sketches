/**
 * Copyright 2004-2008 Ricard Marxer  <email></email>@ricardmarxer.com>
 *
 *
 * This file is part of Geomerative.
 *
 *
 * Geomerative is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *
 * Geomerative is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *
 * You should have received a copy of the GNU General Public License
 * along with Geomerative.  If not, see <http:></http:>//www.gnu.org/licenses/>.
 */
@file:Suppress("SpellCheckingInspection", "unused")

package geomerativefork.src

import geomerativefork.src.util.mapArray
import geomerativefork.src.util.maxAll
import geomerativefork.src.util.minAll
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * RPoint is a very simple interface for creating, holding and drawing 2D points.
 *
 * @eexample RPoint
 * @usage Geometry
 * @related x
 * @related y
 */
data class RPoint(var x: Float, var y: Float) {

  init {
    if (x.isNaN() || y.isNaN()) throw Exception("cannot create a point with NaN values")
  }

  constructor(x: Number, y: Number) : this(x.toFloat(), y.toFloat())

  constructor(n: Number) : this(n.toFloat(), n.toFloat())

  /**
   * Copy a point.
   *
   * @param p the point we wish to make a copy of
   * @eexample RPoint_constructor
   * @usage Geometry
   * @related x
   * @related y
   */
  constructor(p: RPoint) : this(p.x, p.y)

  /**
   * @invisible
   */
  fun setLocation(nx: Float, ny: Float) {
    x = nx
    y = ny
  }

  /**
   * Use this to apply a transformation to the point.
   *
   * @param m the transformation matrix to be applied
   * @eexample RPoint_transform
   * @usage Geometry
   * @related translate ( )
   * @related rotate ( )
   * @related scale ( )
   */
  fun transform(m: RMatrix) {
    val tempx = m.m00 * x + m.m01 * y + m.m02
    val tempy = m.m10 * x + m.m11 * y + m.m12
    x = tempx
    y = tempy
  }

  /**
   * Apply a translation to the point.
   *
   * @param tx the coefficient of x translation
   * @param ty the coefficient of y translation
   * @eexample RPoint_translate
   * @usage Geometry
   * @related transform ( )
   * @related rotate ( )
   * @related scale ( )
   */
  fun translate(tx: Float, ty: Float) {
    x += tx
    y += ty
  }

  /**
   * Apply a translation to the point.
   *
   * @param t the translation vector to be applied
   * @eexample RPoint_translate
   * @usage Geometry
   * @related transform ( )
   * @related rotate ( )
   * @related scale ( )
   */
  fun translate(t: RPoint) {
    x += t.x
    y += t.y
  }

  /**
   * Apply a rotation to the point, given the angle and optionally the coordinates of the center of rotation.
   *
   * @param angle the angle of rotation to be applied
   * @param vx    the x coordinate of the center of rotation
   * @param vy    the y coordinate of the center of rotation
   * @eexample RPoint_rotate
   * @usage Geometry
   * @related transform ( )
   * @related translate ( )
   * @related scale ( )
   */
  fun rotate(angle: Float, vx: Float, vy: Float) {
    val c = cos(angle.toDouble()).toFloat()
    val s = sin(angle.toDouble()).toFloat()
    x -= vx
    y -= vy
    val tempx = x
    val tempy = y
    x = tempx * c - tempy * s
    y = tempx * s + tempy * c
    x += vx
    y += vy
  }

  fun rotate(angle: Float) {
    val c = cos(angle.toDouble()).toFloat()
    val s = sin(angle.toDouble()).toFloat()
    val tempx = x
    val tempy = y
    x = tempx * c - tempy * s
    y = tempx * s + tempy * c
  }

  /**
   * Apply a rotation to the point, given the angle and optionally the point of the center of rotation.
   *
   * @param angle the angle of rotation to be applied
   * @param v     the position vector of the center of rotation
   * @eexample RPoint_rotate
   * @usage Geometry
   * @related transform ( )
   * @related translate ( )
   * @related scale ( )
   */
  fun rotate(angle: Float, v: RPoint) {
    val c = cos(angle.toDouble()).toFloat()
    val s = sin(angle.toDouble()).toFloat()
    x -= v.x
    y -= v.y
    val tempx = x
    val tempy = y
    x = tempx * c - tempy * s
    y = tempx * s + tempy * c
    x += v.x
    y += v.y
  }

  /**
   * Apply a scaling to the point, given the scaling factors.
   *
   * @param sx the scaling coefficient over the x axis
   * @param sy the scaling coefficient over the y axis
   * @eexample RPoint_scale
   * @usage Geometry
   * @related transform ( )
   * @related translate ( )
   * @related rotate ( )
   */
  fun scale(sx: Float, sy: Float) {
    x *= sx
    y *= sy
  }

  /**
   * Apply a scaling to the point, given a scaling factor.
   *
   * @param s the scaling coefficient for a uniform scaling
   * @eexample RPoint_scale
   * @usage Geometry
   * @related transform ( )
   * @related translate ( )
   * @related rotate ( )
   */
  fun scale(s: Float) {
    x *= s
    y *= s
  }

  /**
   * Apply a scaling to the point, given a scaling vector.
   *
   * @param s the scaling vector
   * @eexample RPoint_scale
   * @usage Geometry
   * @related transform ( )
   * @related translate ( )
   * @related rotate ( )
   */
  fun scale(s: RPoint) {
    x *= s.x
    y *= s.y
  }

  /**
   * Use this to normalize the point. This means that after applying, it's norm will be equal to 1.
   *
   * @eexample RPoint_normalize
   * @usage Geometry
   * @related transform ( )
   * @related translate ( )
   * @related rotate ( )
   * @related scale ( )
   */
  fun normalize() {
    val norma = norm()
    if (norma != 0f) scale(1 / norma)
  }

  /**
   * Use this to subtract a vector from this point.
   *
   * @param p the vector to substract
   * @eexample RPoint_sub
   * @usage Geometry
   * @related add ( )
   * @related mult ( )
   * @related cross ( )
   */
  fun sub(p: RPoint) {
    x -= p.x
    y -= p.y
  }

  /**
   * Use this to add a vector to this point.
   *
   * @param p the vector to add
   * @eexample RPoint_add
   * @usage Geometry
   * @related sub ( )
   * @related mult ( )
   * @related cross ( )
   */
  fun add(p: RPoint) {
    x += p.x
    y += p.y
  }

  fun plusX(xAddition: Float) = RPoint(x + xAddition, y)
  fun plusY(yAddition: Float) = RPoint(x, y + yAddition)

  operator fun plus(p: RPoint): RPoint = RPoint(x + p.x, y + p.y)
  operator fun plus(p: Array<RPoint>): Array<RPoint> = arrayOf(this, *p)
  operator fun plus(p: Number): RPoint = RPoint(x + p.toFloat(), y + p.toFloat())
  operator fun minus(p: Number): RPoint = RPoint(x - p.toFloat(), y - p.toFloat())
  operator fun minus(p: RPoint): RPoint = RPoint(x - p.x, y - p.y)
  operator fun unaryMinus(): RPoint = RPoint(-x, -y)
  operator fun times(p: RPoint): RPoint = RPoint(x * p.x, y * p.y)
  operator fun times(p: Number): RPoint = this * RPoint(p, p)
  operator fun div(p: RPoint): RPoint = RPoint(x / p.x, y / p.y)
  operator fun div(p: Number): RPoint = this * RPoint(p, p)

  /**
   * Use this to multiply a vector to this point. This returns a float corresponding to the scalar product of both vectors.
   *
   * @param p the vector to multiply
   * @return float, the result of the scalar product
   * @eexample RPoint_mult
   * @usage Geometry
   * @related add ( )
   * @related sub ( )
   * @related cross ( )
   */
  fun mult(p: RPoint): Float {
    return x * p.x + y * p.y
  }

  /**
   * Use this to perform a cross product of the point with another point.  This returns a RPoint corresponding to the cross product of both vectors.
   *
   * @param p the vector to perform the cross product with
   * @return RPoint, the resulting vector of the cross product
   * @eexample RPoint_cross
   * @usage Geometry
   * @related add ( )
   * @related sub ( )
   * @related mult ( )
   */
  fun cross(p: RPoint): RPoint {
    return RPoint(x * p.y - p.x * y, y * p.x - p.y * x)
  }

  /**
   * Use this to obtain the norm of the point.
   *
   * @return float, the norm of the point
   * @eexample RPoint_norm
   * @usage Geometry
   * @related angle ( )
   */
  fun norm(): Float {
    return sqrt(mult(this).toDouble()).toFloat()
  }

  /**
   * Use this to obtain the square norm of the point.
   *
   * @return float, the norm of the point
   * @eexample RPoint_norm
   * @usage Geometry
   * @related angle ( )
   */
  fun sqrnorm(): Float {
    return mult(this)
  }

  /**
   * Use this to obtain the angle between the vector and another vector
   *
   * @param p the vector relative to which we want to evaluate the angle
   * @return float, the angle between the two vectors
   * @eexample RPoint_angle
   * @usage Geometry
   * @related norm ( )
   */
  fun angle(p: RPoint): Float {
    val normp = p.norm()
    val normthis = norm()
    return acos((mult(p) / (normp * normthis)).toDouble()).toFloat()
  }

  /**
   * Use this to obtain the distance between the vector and another vector
   *
   * @param p the vector relative to which we want to evaluate the distance
   * @return float, the distance between the two vectors
   * @eexample RPoint_dist
   * @usage Geometry
   * @related norm ( )
   */
  fun dist(p: RPoint): Float {
    val dx = p.x - x
    val dy = p.y - y
    return sqrt((dx * dx + dy * dy).toDouble()).toFloat()
  }

  fun print() {
    print("($x,$y)\n")
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as RPoint

    if (x != other.x) return false
    if (y != other.y) return false

    return true
  }

  override fun hashCode(): Int {
    var result = x.hashCode()
    result = 31 * result + y.hashCode()
    return result
  }

  companion object {
    fun maxXY(vararg points: RPoint) = RPoint(
      maxAll(*points.mapArray { it.x }.toFloatArray()),
      maxAll(*points.mapArray { it.y }.toFloatArray())
    )

    fun minXY(vararg points: RPoint) = RPoint(
      minAll(*points.mapArray { it.x }.toFloatArray()),
      minAll(*points.mapArray { it.y }.toFloatArray())
    )

    operator fun Number.times(p: RPoint) = p * this
    operator fun Number.plus(p: RPoint) = p + this
  }

  fun clone() = RPoint(this)

  override fun toString(): String {
    return "RPoint(x = ${"%2f".format(x)}, y = ${"%2f".format(y)})"
  }
}
