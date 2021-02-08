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

import processing.core.PConstants
import processing.core.PGraphics

/**
 * RStrip is a reduced interface for creating, holding and drawing triangle strips. Triangle strips are ordered lists of points (RPoint) which define the vertices of a mesh.
 *
 * @eexample RStrip
 * @usage Geometry
 * @related RPoint
 * @related RMesh
 * @extended
 */
class RStrip {
  /**
   * @invisible
   */
  var type = RGeomElem.TRISTRIP

  /**
   * Array of RPoint objects holding the vertices of the strip.
   *
   * @eexample vertices
   * @related RPoint
   * @related addVertex ( )
   */
  var points: Array<RPoint> = arrayOf()
  // ----------------------
  // --- Public Methods ---
  // ----------------------
  /**
   * Use this method to create a new strip.
   *
   * @param s the object of which to make a copy
   * @eexample RStrip ( )
   * @related addVertex ( )
   */
  constructor(s: RStrip) {
    points = s.points
  }

  constructor()

  /**
   * Use this method to draw the strip.
   *
   * @param g PGraphics, the graphics object on which to draw the strip
   * @eexample drawStrip
   */
  fun draw(g: PGraphics) {
    g.beginShape(PConstants.TRIANGLE_STRIP)
    points.forEach { (x, y) -> g.vertex(x, y) }
    g.endShape()
  }

  /**
   * Use this method to add new vertices to the strip.
   *
   * @eexample addVertex ( )
   */
  fun addVertex(p: RPoint) = append(p)

  fun addVertex(x: Float, y: Float) = append(RPoint(x, y))

  /**
   * Use this method to get the bounding box of the strip.
   *
   * @return RContour, the bounding box of the strip in the form of a four-point contour
   * @eexample getBounds
   * @related draw ( )
   */
  val bounds: RContour
    get() {
      var xmin = Float.MAX_VALUE
      var ymin = Float.MAX_VALUE
      var xmax = Float.MIN_VALUE
      var ymax = Float.MIN_VALUE

      points.forEach { (x, y) ->
        if (x < xmin) xmin = x
        if (x > xmax) xmax = x
        if (y < ymin) ymin = y
        if (y > ymax) ymax = y
      }

      val c = RContour()
      c.addPoint(xmin, ymin)
      c.addPoint(xmin, ymax)
      c.addPoint(xmax, ymax)
      c.addPoint(xmax, ymin)
      return c
    }

  /**
   * Use this method to transform the strip.
   *
   * @param m RMatrix, the matrix of the affine transformation to apply to the strip
   * @eexample transformStrip
   */
  fun transform(m: RMatrix) = points.forEach { it.transform(m) }

  fun add(p: RPoint) = append(p)

  fun add(x: Float, y: Float) = append(RPoint(x, y))

  /**
   * Remove all of the points.  Creates an empty polygon.
   */
  fun clear() {
    points = arrayOf()
  }

  fun append(nextvertex: RPoint) {
    points += nextvertex
  }
}