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

/**
 * RMesh is a reduced interface for creating, holding and drawing meshes. A mesh is a group of triangular strips (RStrip).
 * @eexample RMesh
 * @usage Geometry
 * @related RStrip
 * @extended
 */
@Suppress("unused") class RMesh : RGeomElem {
  /**
   * Use this method to get the type of element this is.
   * @eexample RMesh_getType
   * @return int, will allways return RGeomElem.MESH
   */
  /**
   * @invisible
   */
  override var type = MESH

  /**
   * Array of RStrip objects holding the contours of the polygon.
   * @eexample strips
   * @related RStrip
   * @related countStrips ( )
   * @related addStrip ( )
   */
  @JvmField var strips: Array<RStrip> = arrayOf()
  var currentStrip = 0
  // ----------------------
  // --- Public Methods ---
  // ----------------------
  /**
   * Create a new empty mesh.
   * @eexample createaMesh
   */
  constructor() {
    strips = arrayOf()
    type = MESH
  }

  /**
   * Copy a mesh.
   * @eexample createaMesh
   * @param m  the object of which to make a copy
   */
  constructor(m: RMesh) {
    for (i in 0 until m.strips.size) {
      append(RStrip(m.strips[i]))
    }
    type = MESH
    setStyle(m)
  }

  /**
   * Add a new strip.
   * @eexample addStrip
   * @param s  the strip to be added
   * @related addPoint ( )
   */
  fun addStrip(s: RStrip) {
    append(s)
  }

  fun addStrip() {
    append(RStrip())
  }

  /**
   * Use this method to set the current strip to which append points.
   * @eexample addStrip
   * @related addPoint ( )
   * @invisible
   */
  fun setCurrent(indStrip: Int) {
    currentStrip = indStrip
  }

  /**
   * Add a new point to the current strip.
   * @eexample addPoint
   * @param p  the point to be added
   * @related addStrip ( )
   * @related setCurrent ( )
   * @invisible
   */
  fun addPoint(p: RPoint) = addPoint(currentStrip, p.x, p.y)

  /**
   * Add a new point to the current strip.
   * @eexample addPoint
   * @param x  the x coordinate of the point to be added
   * @param y  the y coordinate of the point to be added
   * @related addStrip ( )
   * @related setCurrent ( )
   * @invisible
   */
  fun addPoint(x: Float, y: Float) = addPoint(currentStrip, x, y)

  /**
   * Add a new point to the given strip.
   * @eexample addPoint
   * @param indStrip  the index of the strip to which the point will be added
   * @param p  the point to be added
   * @related addStrip ( )
   * @related setCurrent ( )
   * @invisible
   */
  fun addPoint(indStrip: Int, p: RPoint) = addPoint(indStrip, p.x, p.y)

  /**
   * Add a new point to the given strip.
   * @eexample addPoint
   * @param indStrip  the index of the strip to which the point will be added
   * @param x  the x coordinate of the point to be added
   * @param y  the y coordinate of the point to be added
   * @related addStrip ( )
   * @related setCurrent ( )
   * @invisible
   */
  fun addPoint(indStrip: Int, x: Float, y: Float) = strips[indStrip].append(RPoint(x, y))

  /**
   * Use this method to draw the mesh.
   * @eexample drawMesh
   * @param g PGraphics, the graphics object on which to draw the mesh
   */
  override fun draw(g: PGraphics) {
    strips.forEach { strip ->
      g.beginShape(PConstants.TRIANGLE_STRIP)
      style.texture?.let { g.texture(it) }

      strip.points.forEach { vertex ->
        g.vertex(vertex.x, vertex.y)
      }
      g.endShape(PConstants.CLOSE)
    }
  }

  override fun draw(g: PApplet) {
    strips.forEach { strip ->
      g.beginShape(PConstants.TRIANGLE_STRIP)
      style.texture?.let { g.texture(it) }

      strip.points.forEach { vertex ->
        g.vertex(vertex.x, vertex.y)
      }
      g.endShape(PConstants.CLOSE)
    }
  }

  /**
   * Use this to get the vertices of the mesh.  It returns the points as an array of RPoint.
   * @eexample RMesh_getHandles
   * @return RPoint[], the vertices returned in an array.
   */
  override val handles: Array<RPoint>
    get() = strips.flatMap { strip ->
      strip.points.toList()
    }.toTypedArray()

  /**
   * Use this to get the vertices of the mesh.  It returns the points as an array of RPoint.
   * @eexample RMesh_getPoints
   * @return RPoint[], the vertices returned in an array.
   */
  override val points: Array<RPoint>
    get() = strips.flatMap { strip ->
      strip.points.toList()
    }.toTypedArray()

  /**
   * Use this method to transform the mesh.
   * @eexample transformMesh
   * @param m RMatrix, the matrix of the affine transformation to apply to the mesh
   */
  override fun transform(m: RMatrix) = strips.forEach { it.transform((m)) }

  /**
   * @invisible
   */
  override fun toMesh(): RMesh = this

  /**
   * @invisible
   */
  @Throws(RuntimeException::class)
  override fun toPolygon(): RPolygon {
    throw RuntimeException("Transforming a Mesh to a Polygon is not yet implemented.")
  }

  /**
   * @invisible
   */
  @Throws(RuntimeException::class)
  override fun toShape(): RShape {
    throw RuntimeException("Transforming a Mesh to a Shape is not yet implemented.")
  }

  /**
   * Remove all of the points.  Creates an empty polygon.
   */
  fun clear() {
    strips = arrayOf()
  }

  fun append(nextstrip: RStrip) {
    strips += nextstrip
  }
}