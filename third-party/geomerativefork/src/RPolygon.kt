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
@file:Suppress("unused", "SpellCheckingInspection")

package geomerativefork.src

import geomerativefork.src.RClip.diff
import geomerativefork.src.RClip.intersection
import geomerativefork.src.RClip.polygonToMesh
import geomerativefork.src.RClip.union
import geomerativefork.src.RClip.update
import geomerativefork.src.RClip.xor
import geomerativefork.src.util.flatMapArray
import geomerativefork.src.util.mapArray
import geomerativefork.src.util.toArrayString
import processing.core.PApplet
import processing.core.PGraphics
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * RPolygon is a reduced interface for creating, holding and drawing complex polygons. Polygons are groups of one or more contours (RContour).  This interface allows us to perform binary operations (difference, xor, union and intersection) on polygons.
 *
 * @eexample RPolygon
 * @usage Geometry
 * @related RContour
 * @related createCircle ( )
 * @related createRing ( )
 * @related createStar ( )
 * @related diff ( )
 * @related xor ( )
 * @related union ( )
 * @related intersection ( )
 * @extended
 */
class RPolygon : RGeomElem {
  /**
   * Use this method to get the type of element this is.
   *
   * @return int, will allways return RGeomElem.POLYGON
   * @eexample RPolygon_getType
   */
  /**
   * @invisible
   */
  override val type = POLYGON

  /**
   * Array of RContour objects holding the contours of the polygon.
   *
   * @eexample contours
   * @related RContour
   * @related countContours ( )
   * @related addContour ( )
   */
  @JvmField var contours: Array<RContour> = arrayOf()
  var currentContour = 0
  // ----------------------
  // --- Public Methods ---
  // ----------------------
  /**
   * Make a copy of the given polygon.
   *
   * @param p the object of which to make a copy
   * @eexample createPolygon
   */
  constructor(p: RPolygon) {
    append(p.contours)
    setStyle(p)
  }

  /**
   * Create a new polygon given an array of points.
   *
   * @param points the points for the new polygon.
   * @eexample createPolygon
   */
  constructor(points: Array<RPoint>) : this(RContour(points))

  /**
   * Create a new polygon given a contour.
   *
   * @param newcontour the contour for the new polygon.
   */
  constructor(newcontour: RContour) : this(arrayOf(newcontour))

  /**
   * Create an empty polygon.
   */
  constructor() : this(arrayOf<RContour>())

  /**
   * Create an empty polygon.
   */
  constructor(contours: Array<RContour>) {
    this.contours = contours
  }

  /**
   * Use this method to get the centroid of the element.
   *
   * @return RPo the centroid point of the element
   * @eexample RGroup_getCentroid
   * @related getBounds ( )
   * @related getCenter ( )
   */
  override val centroid: RPoint?
    get() {
      if (numPoints == 0) return null

      var bestCentroid: RPoint = points[0]
      var bestArea = Float.NEGATIVE_INFINITY
      contours.forEach { contour ->
        val area = abs(contour.area)
        if (area > bestArea) {
          bestArea = area
          bestCentroid = contour.centroid!!
        }
      }
      return bestCentroid
    }

  /**
   * Add a new contour to the polygon.
   *
   * @param c the contour to be added
   * @eexample addContour
   * @related addPoint ( )
   */
  fun addContour(c: RContour) = append(c)

  /**
   * Add an empty contour to the polygon.
   *
   * @eexample addContour
   * @related addPoint ( )
   */
  fun addContour() = append(RContour())

  /**
   * Add a new contour to the polygon given an array of points.
   *
   * @param points the points of the new contour to be added
   * @eexample addContour
   * @related addPoint ( )
   */
  fun addContour(points: Array<RPoint>) = append(RContour(points))

  /**
   * Use this method to set the current contour to which append points.
   *
   * @eexample addContour
   * @related addPoint ( )
   */
  fun setContour(indContour: Int) {
    currentContour = indContour
  }

  /**
   * Add a new point to the current contour.
   *
   * @param p the point to be added
   * @eexample addPoint
   * @related addContour ( )
   * @related setCurrent ( )
   */
  fun addPoint(p: RPoint) = addPoint(currentContour, p)

  /**
   * Add a new point to the current contour.
   *
   * @param x the x coordinate of the point to be added
   * @param y the y coordinate of the point to be added
   * @eexample addPoint
   * @related addContour ( )
   * @related setCurrent ( )
   */
  fun addPoint(x: Float, y: Float) = addPoint(currentContour, RPoint(x, y))

  /**
   * Add a new point to the selected contour.
   *
   * @param indContour the index of the contour to which the point will be added
   * @param p          the point to be added
   * @eexample addPoint
   * @related addContour ( )
   * @related setCurrent ( )
   */
  fun addPoint(indContour: Int, p: RPoint) = contours[indContour].append(p)

  /**
   * Add a new point to the selected contour.
   *
   * @param indContour the index of the contour to which the point will be added
   * @param x          the x coordinate of the point to be added
   * @param y          the y coordinate of the point to be added
   * @eexample addPoint
   * @related addContour ( )
   * @related setCurrent ( )
   */
  fun addPoint(indContour: Int, x: Float, y: Float) = addPoint(indContour, RPoint(x, y))

  fun addClose() = contours[contours.size - 1].addClose()

  /**
   * Use this method to create a new mesh from a given polygon.
   *
   * @return RMesh, the mesh made of tristrips resulting of a tesselation of the polygon
   * @eexample toMesh
   * @related draw ( )
   */
  override fun toMesh(): RMesh = polygonToMesh(this).apply { setStyle(this) }

  override fun print() {
    println("polygon: ")
    for (i in 0 until contours.size) {
      println("---  contour $i ---")
      contours[i].print()
      println("---------------")
    }
  }

  /**
   * Removes contours with less than 3 points.  These are contours that are open.
   * Since close polygons have points[0] == points[-1] and two more points to form a triangle at least.
   * This is useful to avoid the clipping algorithm from breaking.
   *
   * @invisible
   */
  fun removeOpenContours(): RPolygon = RPolygon().apply {
    contours.forEach { contour -> if (contour.points.size > 3) addContour(contour) }
    setStyle(this)
  }

  /**
   * @invisible
   */
  override fun toPolygon(): RPolygon = RPolygon(this)

  /**
   * @invisible
   */
  override fun toShape(): RShape = RShape(contours.mapArray { contour ->
    RPath(contour.points).apply {
      setStyle(contour)
      if (contour.closed) addClose()
    }
  }).apply { setStyle(this) }

  /**
   * Use this to return the points of the polygon.  It returns the points in the way of an array of RPoint.
   *
   * @return RPoint[], the points returned in an array.
   * @eexample RPolygon_getHandles
   */
  override val handles: Array<RPoint>
    get() = contours.flatMapArray { it.handles }

  /**
   * Use this to return the points of the polygon.  It returns the points in the way of an array of RPoint.
   *
   * @return RPoint[], the points returned in an array.
   * @eexample RPolygon_getPoints
   */
  override val points: Array<RPoint>
    get() = contours.flatMapArray { it.points }

  /**
   * Use this method to get the area covered by the polygon.
   *
   * @return float, the area covered by the polygon
   * @eexample getArea
   * @related draw ( )
   */
  override val area: Float
    get() {
      if (numPoints < 3) {
        return 0.0f
      }
      val ax = getX(0)
      val ay = getY(0)
      var area = 0.0f
      for (i in 1 until numPoints - 1) {
        val bx = getX(i)
        val by = getY(i)
        val cx = getX(i + 1)
        val cy = getY(i + 1)
        val tarea = (cx - bx) * (ay - by) - (ax - bx) * (cy - by)
        area += tarea
      }
      area = 0.5f * abs(area)
      return area
    }

  /**
   * Use this method to draw the polygon.
   *
   * @param g PGraphics, the graphics object on which to draw the polygon
   * @eexample drawPolygon
   * @related draw ( )
   */
  override fun draw(g: PGraphics) {
    val numContours = contours.size
    if (numContours != 0) {
      if (isIn(g)) {
        if (!RG.ignoreStyles) {
          saveContext(g)
          setContext(g)
        }

        // Check whether to draw the fill or not
        if (g.fill) {
          // Since we are drawing the different tristrips we must turn off the stroke or make it the same color as the fill
          // NOTE: there's currently no way of drawing the outline of a mesh, since no information is kept about what vertices are at the edge

          // Save the information about the current stroke color and turn off
          val stroking = g.stroke
          g.noStroke()

          // Save smoothing state and turn off
          val smoothing = g.smooth
          try {
            if (smoothing > 0) {
              g.noSmooth()
            }
          } catch (ignored: Exception) {
          }
          val tempMesh = toMesh()
          tempMesh.draw(g)

          // Restore the old stroke color
          if (stroking) g.stroke(g.strokeColor)

          // Restore the old smoothing state
          try {
            if (smoothing > 0) {
              g.smooth()
            }
          } catch (ignored: Exception) {
          }
        }

        // Check whether to draw the stroke or not
        if (g.stroke) {
          for (i in 0 until numContours) {
            contours[i].draw(g)
          }
        }
        if (!RG.ignoreStyles) {
          restoreContext(g)
        }
      }
    }
  }

  override fun draw(g: PApplet) {
    val numContours = contours.size
    if (numContours != 0) {
      if (isIn(g)) {
        if (!RG.ignoreStyles) {
          saveContext(g)
          setContext(g)
        }

        // Check whether to draw the fill or not
        if (g.g.fill) {
          // Since we are drawing the different tristrips we must turn off the stroke or make it the same color as the fill
          // NOTE: there's currently no way of drawing the outline of a mesh, since no information is kept about what vertices are at the edge

          // Save the information about the current stroke color and turn off
          val stroking = g.g.stroke
          g.noStroke()

          // Save smoothing state and turn off
          val smoothing = g.g.smooth
          try {
            if (smoothing > 0) {
              g.noSmooth()
            }
          } catch (ignored: Exception) {
          }
          val tempMesh = toMesh()
          if (tempMesh.points.isNotEmpty()) tempMesh.draw(g)

          // Restore the old stroke color
          if (stroking) g.stroke(g.g.strokeColor)

          // Restore the old smoothing state
          try {
            if (smoothing > 0) {
              g.smooth()
            }
          } catch (ignored: Exception) {
          }
        }

        // Check whether to draws the stroke or not
        if (g.g.stroke) {
          for (i in 0 until numContours) {
            contours[i].draw(g)
          }
        }
        if (!RG.ignoreStyles) {
          restoreContext(g)
        }
      }
    }
  }

  /**
   * Use this method to get the intersection of this polygon with the polygon passed in as a parameter.
   *
   * @param p RPolygon, the polygon with which to perform the intersection
   * @return RPolygon, the intersection of the two polygons
   * @eexample intersection
   * @related union ( )
   * @related xor ( )
   * @related diff ( )
   */
  fun intersection(p: RPolygon): RPolygon =
    intersection(p, this).apply { style = this@RPolygon.style }

  /**
   * Use this method to get the union of this polygon with the polygon passed in as a parameter.
   *
   * @param p RPolygon, the polygon with which to perform the union
   * @return RPolygon, the union of the two polygons
   * @eexample union
   * @related intersection ( )
   * @related xor ( )
   * @related diff ( )
   */
  fun union(p: RPolygon): RPolygon = union(p, this).apply { style = this@RPolygon.style }

  /**
   * Use this method to get the xor of this polygon with the polygon passed in as a parameter.
   *
   * @param p RPolygon, the polygon with which to perform the xor
   * @return RPolygon, the xor of the two polygons
   * @eexample xor
   * @related union ( )
   * @related intersection ( )
   * @related diff ( )
   */
  fun xor(p: RPolygon): RPolygon = xor(p, this).apply { style = this@RPolygon.style }

  /**
   * Use this method to get the difference between this polygon and the polygon passed in as a parameter.
   *
   * @param p RPolygon, the polygon with which to perform the difference
   * @return RPolygon, the difference of the two polygons
   * @eexample diff
   * @related union ( )
   * @related xor ( )
   * @related intersection ( )
   */
  fun diff(p: RPolygon): RPolygon = diff(p, this).apply { style = this@RPolygon.style }

  /**
   * Use this method to get a rebuilt version of a given polygon by removing extra points and solving intersecting contours or holes.
   *
   * @return RPolygon, the updated polygon
   * @eexample RPolygon_update
   * @related diff ( )
   * @related union ( )
   * @related xor ( )
   * @related intersection ( )
   */
  fun update(): RPolygon = update(this)

  // ----------------------
  // --- Private Methods ---
  // ----------------------
  /**
   * Remove all of the points.  Creates an empty polygon.
   */
  fun clear() {
    contours = arrayOf()
  }

  /**
   * Add a point to the first inner polygon.
   */
  fun add(x: Float, y: Float) = add(RPoint(x, y))

  /**
   * Add a point to the first inner polygon.
   */
  fun add(p: RPoint) = contours[0].append(p)

  /**
   * Add an inner polygon to this polygon - assumes that adding polygon does not
   * have any inner polygons.
   */
  fun add(p: RPolygon) {
    if (p.numPoints == 0) return

    val c = RContour()
    for (i in 0 until p.numPoints) {
      c.addPoint(p.getX(i), p.getY(i))
    }
    append(c)
  }

  /**
   * Add an inner polygon to this polygon - assumes that adding polygon does not
   * have any inner polygons.
   */
  fun add(c: RContour) = append(c)

  /**
   * Return true if the polygon is empty
   */
  val isEmpty: Boolean
    get() = contours.isEmpty()

  /**
   * Return false if the polygon is empty
   */
  val isNotEmpty: Boolean
    get() = contours.isNotEmpty()

  /**
   * Returns the bounding box of the polygon.
   */
  val bBox: RRectangle
    get() = when {
      contours.isEmpty() -> RRectangle(RPoint(0, 0), RPoint(0, 0))
      contours.size == 1 -> {
        var xmin = Float.MAX_VALUE
        var ymin = Float.MAX_VALUE
        var xmax = Float.MIN_VALUE
        var ymax = Float.MIN_VALUE

        contours[0].points.forEach { (px, py) ->
          if (px < xmin) xmin = px
          if (px > xmax) xmax = px
          if (py < ymin) ymin = py
          if (py > ymax) ymax = py
        }

        RRectangle(xmin, ymin, xmax - xmin, ymax - ymin)
      }
      else -> {
        throw UnsupportedOperationException("getBounds not supported on complex poly.")
      }
    }

  /**
   * Returns the polygon at this index.
   */
  fun getInnerPoly(polyIndex: Int): RPolygon = RPolygon(contours[polyIndex])

  /**
   * Returns the number of inner polygons - inner polygons are assumed to return one here.
   */
  val numInnerPoly: Int
    get() = contours.size

  /**
   * Return the number points of the first inner polygon
   */
  val numPoints: Int
    get() = contours.sumBy { it.points.size }

  /**
   * Return the X value of the point at the index in the first inner polygon
   */
  fun getX(index: Int): Float = contours.first().points[index].x

  /**
   * Return the Y value of the point at the index in the first inner polygon
   */
  fun getY(index: Int): Float = contours.first().points[index].y

  /**
   * Return true if this polygon is a hole.  Holes are assumed to be inner polygons of
   * a more complex polygon.
   *
   * @throws IllegalStateException if called on a complex polygon.
   */
  /**
   * Set whether or not this polygon is a hole.  Cannot be called on a complex polygon.
   *
   * @throws IllegalStateException if called on a complex polygon.
   */
  var isHole: Boolean
    get() {
      check(
        !(contours.isEmpty() || contours.size > 1)) { "Cannot call on a poly made up of more than one poly." }
      return contours[0].isHole
    }
    set(isHole) {
      check(
        !(contours.isEmpty() || contours.size > 1)) { "Cannot call on a poly made up of more than one poly." }
      contours[0].isHole = isHole
    }

  /**
   * Return true if the given inner polygon is contributing to the set operation.
   * This method should NOT be used outside the Clip algorithm.
   */
  fun isContributing(polyIndex: Int): Boolean = contours[polyIndex].isContributing

  /**
   * Set whether or not this inner polygon is constributing to the set operation.
   * This method should NOT be used outside the Clip algorithm.
   */
  fun setContributing(polyIndex: Int, contributes: Boolean) {
    if (contours.size != 1) {
      throw IllegalStateException("Only applies to polys of size 1")
    }
    contours[polyIndex].isContributing = contributes
  }

  private fun append(newContours: Array<RContour>) {
    contours += newContours
  }

  private fun append(newContour: RContour) {
    contours += newContour
  }

  override fun toString(): String {
    return "RPolygon(contours(${contours.size} = ${contours.toArrayString()}]"
  }

  companion object {
    var defaultDetail = 50

    /**
     * Use this method to create a new circle polygon.
     *
     * @param radius the radius of the circle
     * @param detail the number of vertices of the polygon
     * @return RPolygon, the circular polygon newly created
     * @eexample createCircle
     */
    @JvmOverloads
    fun createCircle(
      x: Float,
      y: Float,
      radius: Float,
      detail: Int = defaultDetail,
    ): RPolygon {
      val radiansPerStep = 2 * Math.PI / detail

      return RPolygon((0 until detail).map { i ->
        RPoint(radius * cos(i * radiansPerStep) + x, radius * sin(i * radiansPerStep) + y)
      }.toTypedArray())
    }

    fun createCircle(radius: Float, detail: Int): RPolygon = createCircle(0f, 0f, radius, detail)

    fun createCircle(radius: Float): RPolygon = createCircle(0f, 0f, radius, defaultDetail)

    /**
     * Use this method to create a new rectangle polygon.
     *
     * @param x the upper-left corner x coordinate
     * @param y the upper-left corner y coordinate
     * @param w the width
     * @param h the height
     * @return RPolygon, the circular polygon newly created
     * @eexample createRectangle
     */
    fun createRectangle(x: Float, y: Float, w: Float, h: Float): RPolygon = RPolygon().apply {
      addPoint(x, y)
      addPoint(x + w, y)
      addPoint(x + w, y + h)
      addPoint(x, y + h)
      addPoint(x, y)
    }

    fun createRectangle(w: Float, h: Float): RPolygon = createRectangle(0f, 0f, w, h)

    /**
     * Use this method to create a new starform polygon.
     *
     * @param radiusBig   the outter radius of the star polygon
     * @param radiusSmall the inner radius of the star polygon
     * @param spikes      the amount of spikes on the star polygon
     * @return RPolygon, the starform polygon newly created
     * @eexample createStar
     */
    fun createStar(
      x: Float, y: Float, radiusBig: Float, radiusSmall: Float, spikes: Int,
    ): RPolygon {
      val numPoints = spikes * 2
      val radiansPerStep = Math.PI / spikes

      val points = (0 until numPoints step 2).flatMap { i ->
        listOf(
          RPoint(radiusBig * cos(i * radiansPerStep) + x, radiusBig * sin(i * radiansPerStep) + y),
          RPoint(radiusSmall * cos(i * radiansPerStep) + x,
            radiusSmall * sin(i * radiansPerStep) + y))
      }
      return RPolygon(points.toTypedArray())
    }

    fun createStar(radiusBig: Float, radiusSmall: Float, spikes: Int): RPolygon {
      return createStar(0f, 0f, radiusBig, radiusSmall, spikes)
    }

    /**
     * Use this method to create a new ring polygon.
     *
     * @param radiusBig   the outter radius of the ring polygon
     * @param radiusSmall the inner radius of the ring polygon
     * @param detail      the number of vertices on each contour of the ring
     * @return RPolygon, the ring polygon newly created
     * @eexample createRing
     */
    @JvmOverloads
    fun createRing(
      x: Float,
      y: Float,
      radiusBig: Float,
      radiusSmall: Float,
      detail: Int = defaultDetail,
    ): RPolygon {
      val radiansPerStep = 2 * Math.PI / detail

      val (inner, outer) = (0 until detail).map { i ->
        RPoint(radiusSmall * cos(i * radiansPerStep) + x,
          radiusSmall * sin(i * radiansPerStep) + y) to RPoint(
          radiusBig * cos(i * radiansPerStep) + x, radiusBig * sin(i * radiansPerStep) + y)
      }.unzip()

      return RPolygon().apply {
        addContour(outer.toTypedArray())
        addContour(inner.toTypedArray())
      }
    }

    fun createRing(radiusBig: Float, radiusSmall: Float, detail: Int): RPolygon =
      createRing(0f, 0f, radiusBig, radiusSmall, detail)

    fun createRing(radiusBig: Float, radiusSmall: Float): RPolygon =
      createRing(0f, 0f, radiusBig, radiusSmall, defaultDetail)
  }
}