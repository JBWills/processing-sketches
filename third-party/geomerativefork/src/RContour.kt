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
import processing.core.PConstants
import processing.core.PGraphics
import util.with

/**
 * RContour is a reduced interface for creating, holding and drawing contours. Contours are ordered lists of points (RPoint) which define the outlines of polygons.  Contours can be self-intersecting.
 * @eexample RContour
 * @usage Geometry
 * @related RPoint
 * @related RPolygon
 * @extended
 */
class RContour : RGeomElem {
  /**
   * @invisible
   */
  override val type = CONTOUR

  /**
   * Array of RPoint objects holding the points of the contour.
   * @eexample points
   * @related RPoint
   * @related countPoints ( )
   * @related addPoint ( )
   */
  override var points: Array<RPoint> = arrayOf()
  override val handles = points
  @JvmField var isContributing = true

  /**
   * Use this method to know if the contour is a hole. Remember to use the method update() on the polygon before using this method.
   * @eexample RPolygon_isHole
   * @return boolean, true if it is a hole
   * @related update ( )
   */
  var isHole = false
  @JvmField var closed = true

  /**
   * Create a countour given an array of points.
   * @param  contourpoints  the points of the new contour
   * @invisible
   */
  constructor(contourpoints: Array<RPoint>) {
    points = contourpoints
  }

  constructor()
  constructor(c: RContour) : this(c.points.clone()) {
    isHole = c.isHole
    isContributing = c.isContributing
    setStyle(c)
  }

  /**
   * Use this method to draw the contour.
   * @eexample drawContour
   * @param g PGraphics, the graphics object on which to draw the contour
   */
  override fun draw(g: PGraphics) = g.with {
    val beforeFill = fill
    noFill()
    beginShape()
    points.forEach { (xP, yP) -> vertex(xP, yP) }
    endShape(if (closed) PConstants.CLOSE else PConstants.OPEN)
    if (beforeFill) fill(fillColor)
  }

  override fun draw(p: PApplet) = p.with {
    val beforeFill = g.fill
    noFill()
    beginShape()
    points.forEach { (xP, yP) -> vertex(xP, yP) }
    endShape(if (closed) PConstants.CLOSE else PConstants.OPEN)
    if (beforeFill) fill(g.fillColor)
  }

  /**
   * Use this method to add new points to the contour.
   * @eexample addPoint ( )
   */
  fun addPoint(p: RPoint) = append(p)

  fun addPoint(x: Float, y: Float) = append(RPoint(x, y))

  /**
   * Efficiently add an array of points to the contour.
   */
  fun addPoints(morePoints: Array<RPoint>) {
    points += morePoints
  }

  /**
   * Efficiently add a list of points to the contour.
   */
  fun addPoints(morePoints: List<RPoint>) {
    points += morePoints
  }

  override fun getPoint(t: Float): RPoint {
    PApplet.println("Feature not yet implemented for this class.")

    throw Exception("Can't call getPoint, it's not implemented")
  }

  override fun getTangent(t: Float): RPoint {
    PApplet.println("Feature not yet implemented for this class.")
    throw Exception("Can't call getPoint, it's not implemented")
  }

  override fun contains(p: RPoint): Boolean {
    PApplet.println("Feature not yet implemented for this class.")
    throw Exception("Can't call getPoint, it's not implemented")
  }

  override fun print() {
    println("contour: ")
    for (i in 0 until points.size) {
      println("---  point $i ---")
      points[i].print()
      println("---------------")
    }
  }

  fun addClose() {
    if (points.isEmpty() || points[0] == points[points.size - 1]) return
    addPoint(RPoint(points[0].x, points[0].y))
    closed = true
  }

  /**
   * @invisible
   */
  override fun toPolygon(): RPolygon = RPolygon(this)

  /**
   * @invisible
   */
  @Throws(RuntimeException::class)
  override fun toShape(): RShape {
    throw RuntimeException("Transforming a Contour to a Shape is not yet implemented.")
  }

  /**
   * @invisible
   */
  override fun toMesh(): RMesh = toPolygon().toMesh()

  fun append(nextpoint: RPoint) {
    points += nextpoint
  }

  override fun toString(): String {
    return "RContour(type=$type, startPoint=${points.firstOrNull()}, endPoint=${points.lastOrNull()} points=${points.size}, closed=$closed)"
  }
}