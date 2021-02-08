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
@file:Suppress("SpellCheckingInspection")

package geomerativefork.src

import processing.core.PApplet

/**
 * RMatrix is a very simple interface for creating, holding 3x3 matrices with the most common 2D affine transformations such as translation, rotation, scaling and shearing.  We only have access to the first to rows of the matrix the last row is considered a constant 0, 0, 1 in order to have better performance.
 * @eexample RMatrix
 * @usage Geometry
 * @extended
 */
class RMatrix {
  var m00 = 1f
  var m01 = 0f
  var m02 = 0f
  var m10 = 0f
  var m11 = 1f
  var m12 = 0f
  var m20 = 0f
  var m21 = 0f
  var m22 = 1f

  /**
   * Create a new matrix given the coefficients.
   * @eexample RMatrix
   * @param m00  coefficient 00 of the matrix
   * @param m01  coefficient 01 of the matrix
   * @param m02  coefficient 02 of the matrix
   * @param m10  coefficient 10 of the matrix
   * @param m11  coefficient 11 of the matrix
   * @param m12  coefficient 12 of the matrix
   * @usage Geometry
   * @related apply ( )
   * @related translate ( )
   * @related rotate ( )
   * @related scale ( )
   * @related shear ( )
   */
  constructor(
    m00: Float,
    m01: Float,
    m02: Float,
    m10: Float,
    m11: Float,
    m12: Float,
  ) {
    set(m00, m01, m02, m10, m11, m12)
  }

  /**
   * Create a new identity matrix.
   * @eexample RMatrix
   * @usage Geometry
   * @related apply ( )
   * @related translate ( )
   * @related rotate ( )
   * @related scale ( )
   * @related shear ( )
   */
  constructor() {
    m00 = 1f
    m01 = 0f
    m02 = 0f
    m10 = 0f
    m11 = 1f
    m12 = 0f
  }

  /**
   * Copy a matrix.
   * @eexample RMatrix
   * @param src  source matrix from where to copy the matrix
   * @usage Geometry
   * @related apply ( )
   * @related translate ( )
   * @related rotate ( )
   * @related scale ( )
   * @related shear ( )
   */
  constructor(src: RMatrix) {
    set(src.m00, src.m01, src.m02, src.m10, src.m11, src.m12)
  }

  constructor(transformationString: String?) {
    val transfTokens = PApplet.splitTokens(transformationString, ")")

    // Loop through all transformations
    for (i in transfTokens.indices) {
      // Check the transformation and the parameters
      val transf = PApplet.splitTokens(transfTokens[i], "(")
      val params = PApplet.splitTokens(transf[1], ", ")
      val fparams = FloatArray(params.size)
      for (j in params.indices) {
        fparams[j] = PApplet.parseFloat(params[j])
      }
      transf[0] = PApplet.trim(transf[0])
      if (transf[0] == "translate") {
        if (params.size == 1) {
          this.translate(fparams[0])
        } else if (params.size == 2) {
          this.translate(fparams[0], fparams[1])
        }
      } else if (transf[0] == "rotate") {
        if (params.size == 1) {
          this.rotate(PApplet.radians(fparams[0]))
        } else if (params.size == 3) {
          this.rotate(PApplet.radians(fparams[0]), fparams[1], fparams[2])
        }
      } else if (transf[0] == "scale") {
        if (params.size == 1) {
          this.scale(fparams[0])
        } else if (params.size == 2) {
          this.scale(fparams[0], fparams[1])
        }
      } else if (transf[0] == "skewX") {
        skewX(PApplet.radians(fparams[0]))
      } else if (transf[0] == "skewY") {
        skewY(PApplet.radians(fparams[0]))
      } else if (transf[0] == "matrix") {
        this.apply(fparams[0], fparams[2], fparams[4], fparams[1], fparams[3], fparams[5])
      } else {
        throw RuntimeException("Transformation unknown. '" + transf[0] + "'")
      }
    }
  }

  private operator fun set(
    m00: Float, m01: Float, m02: Float,
    m10: Float, m11: Float, m12: Float,
  ) {
    this.m00 = m00
    this.m01 = m01
    this.m02 = m02
    this.m10 = m10
    this.m11 = m11
    this.m12 = m12
  }

  /**
   * Multiply the matrix with another matrix.  This is mostly use to chain transformations.
   * @eexample RMatrix_apply
   * @param n00  coefficient 00 of the matrix to be applied
   * @param n01  coefficient 01 of the matrix to be applied
   * @param n02  coefficient 02 of the matrix to be applied
   * @param n10  coefficient 10 of the matrix to be applied
   * @param n11  coefficient 11 of the matrix to be applied
   * @param n12  coefficient 12 of the matrix to be applied
   * @usage Geometry
   * @related translate ( )
   * @related rotate ( )
   * @related scale ( )
   * @related shear ( )
   */
  fun apply(
    n00: Float, n01: Float, n02: Float,
    n10: Float, n11: Float, n12: Float,
  ) {
    val r00 = m00 * n00 + m01 * n10
    val r01 = m00 * n01 + m01 * n11
    val r02 = m00 * n02 + m01 * n12 + m02
    val r10 = m10 * n00 + m11 * n10
    val r11 = m10 * n01 + m11 * n11
    val r12 = m10 * n02 + m11 * n12 + m12
    m00 = r00
    m01 = r01
    m02 = r02
    m10 = r10
    m11 = r11
    m12 = r12
  }

  /**
   * Multiply the matrix with another matrix.  This is mostly use to chain transformations.
   * @eexample RMatrix_apply
   * @param rhs  right hand side matrix
   * @usage Geometry
   * @related translate ( )
   * @related rotate ( )
   * @related scale ( )
   * @related shear ( )
   */
  fun apply(rhs: RMatrix) {
    apply(rhs.m00, rhs.m01, rhs.m02, rhs.m10, rhs.m11, rhs.m12)
  }

  /**
   * Apply a translation to the matrix, given the coordinates.
   * @eexample RMatrix_translate
   * @param tx  x coordinate translation
   * @param ty  y coordinate translation
   * @usage Geometry
   * @related rotate ( )
   * @related scale ( )
   * @related shear ( )
   */
  @JvmOverloads
  fun translate(tx: Float, ty: Float = 0f) {
    apply(1f, 0f, tx, 0f, 1f, ty)
  }

  /**
   * Apply a translation to the matrix, given a point.
   * @eexample RMatrix_translate
   * @param t  vector translation
   * @usage Geometry
   * @related rotate ( )
   * @related scale ( )
   * @related shear ( )
   */
  fun translate(t: RPoint) {
    translate(t.x, t.y)
  }

  /**
   * Apply a rotation to the matrix, given an angle and optionally a rotation center.
   * @eexample RPoint_rotate
   * @usage Geometry
   * @param angle  the angle of rotation to be applied
   * @param vx  the x coordinate of the center of rotation
   * @param vy  the y coordinate of the center of rotation
   * @related transform ( )
   * @related translate ( )
   * @related scale ( )
   */
  fun rotate(angle: Float, vx: Float, vy: Float) {
    translate(vx, vy)
    rotate(angle)
    translate(-vx, -vy)
  }

  fun rotate(angle: Float) {
    val c = Math.cos(angle.toDouble()).toFloat()
    val s = Math.sin(angle.toDouble()).toFloat()
    apply(c, -s, 0f, s, c, 0f)
  }

  /**
   * Apply a rotation to the matrix, given an angle and optionally a rotation center.
   * @eexample RPoint_rotate
   * @usage Geometry
   * @param angle  the angle of rotation to be applied
   * @param v  the position vector of the center of rotation
   * @related transform ( )
   * @related translate ( )
   * @related scale ( )
   */
  fun rotate(angle: Float, v: RPoint) {
    rotate(angle, v.x, v.y)
  }

  /**
   * Apply a scale to the matrix, given scaling factors and optionally a scaling center.
   * @eexample RPoint_scale
   * @usage Geometry
   * @param sx  the scaling coefficient over the x axis
   * @param sy  the scaling coefficient over the y axis
   * @param x  x coordinate of the position vector of the center of the scaling
   * @param y  y coordinate of the position vector of the center of the scaling
   * @related transform ( )
   * @related translate ( )
   * @related rotate ( )
   */
  fun scale(sx: Float, sy: Float, x: Float, y: Float) {
    translate(x, y)
    scale(sx, sy)
    translate(-x, -y)
  }

  fun scale(sx: Float, sy: Float) {
    apply(sx, 0f, 0f, 0f, sy, 0f)
  }

  /**
   * Apply a scale to the matrix, given scaling factors and optionally a scaling center.
   * @eexample RPoint_scale
   * @usage Geometry
   * @param s  the scaling coefficient for a uniform scaling
   * @param x  x coordinate of the position vector of the center of the scaling
   * @param y  y coordinate of the position vector of the center of the scaling
   * @related transform ( )
   * @related translate ( )
   * @related rotate ( )
   */
  fun scale(s: Float, x: Float, y: Float) {
    scale(s, s, x, y)
  }

  /**
   * Apply a scale to the matrix, given scaling factors and optionally a scaling center.
   * @eexample RPoint_scale
   * @usage Geometry
   * @param sx  the scaling coefficient over the x axis
   * @param sy  the scaling coefficient over the y axis
   * @param p  the position vector of the center of the scaling
   * @related transform ( )
   * @related translate ( )
   * @related rotate ( )
   */
  fun scale(sx: Float, sy: Float, p: RPoint) {
    scale(sx, sy, p.x, p.y)
  }

  /**
   * Apply a scale to the matrix, given scaling factors and optionally a scaling center.
   * @eexample RPoint_scale
   * @usage Geometry
   * @param s  the scaling coefficient for a uniform scaling
   * @param p  the position vector of the center of the scaling
   * @related transform ( )
   * @related translate ( )
   * @related rotate ( )
   */
  fun scale(s: Float, p: RPoint) {
    scale(s, s, p.x, p.y)
  }

  fun scale(s: Float) {
    scale(s, s)
  }

  /**
   * Use this to apply a skewing to the matrix.
   * @eexample RMatrix_skewing
   * @param angle  skewing angle
   * @usage Geometry
   * @related rotate ( )
   * @related scale ( )
   * @related translate ( )
   */
  fun skewX(angle: Float) {
    apply(1f, Math.tan(angle.toDouble()).toFloat(), 0f, 0f, 1f, 0f)
  }

  fun skewY(angle: Float) {
    apply(1f, 0f, 0f, Math.tan(angle.toDouble()).toFloat(), 1f, 0f)
  }

  /**
   * Use this to apply a shearing to the matrix.
   * @eexample RMatrix_translate
   * @param shx  x coordinate shearing
   * @param shy  y coordinate shearing
   * @usage Geometry
   * @related rotate ( )
   * @related scale ( )
   * @related translate ( )
   */
  fun shear(shx: Float, shy: Float) {
    apply(1f, -shx, 0f, shy, 1f, 0f)
  }
}