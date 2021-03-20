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

import geomerativefork.src.util.flatMapArray
import geomerativefork.src.util.mapArray
import geomerativefork.src.util.toArrayString
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PGraphics
import kotlin.math.*

/**
 * RShape is a reduced interface for creating, holding and drawing complex shapes. Shapes are groups of one or more paths (RPath).  Shapes can be selfintersecting and can contain holes.  This interface also allows you to transform shapes into polygons by segmenting the curves forming the shape.
 *
 * @eexample RShape
 * @usage Geometry
 * @related RPath
 */
open class RShape() : RGeomElem() {
  /**
   * @invisible
   */
  override val type = SHAPE

  /**
   * Array of RPath objects holding the paths of the polygon.
   *
   * @eexample paths
   * @related RPath
   * @related countPaths ( )
   * @related addPath ( )
   */
  @JvmField
  var paths: Array<RPath> = arrayOf()
  protected var currentPath = 0

  @JvmField
  var children: Array<RShape> = arrayOf()
  protected var currentChild = 0
  // ----------------------
  // --- Public Methods ---
  // ----------------------

  constructor(newpath: RPath) : this(paths = arrayOf(newpath), children = null, style = null)

  constructor(newpaths: Array<RPath>) : this(paths = newpaths, children = null, style = null)

  constructor(paths: Array<RPath>?, children: Array<RShape>?, style: RGeomElem? = null) : this() {
    this.paths = paths ?: arrayOf()
    this.children = children ?: arrayOf()
    style?.let { setStyle(it) }
  }

  constructor(
    points: Array<Array<RPoint>>,
  ) : this(paths = points.mapArray { RPath(it) }, children = null, style = null)

  constructor(points: Array<RPoint>) : this(RPath(points))

  constructor(s: RShape) : this() {
    paths = Array(s.paths.size) { i -> s.paths[i].clone() }
    children = Array(s.children.size) { i -> RShape(s.children[i]) }
    setStyle(s)
  }

  /**
   * Extracts a shape by its name. The shape is returned as an RShape object, or null is returned if no shape with the name has been found.
   *
   * @return RShape or null, the target shape or null if not found
   */
  fun getChild(target: String): RShape? {
    if (elemName == target) {
      return this
    }
    children.forEachIndexed { i, child ->
      val shp = children[i].getChild(target)
      if (shp != null) return shp
    }
    return null
  }

  /**
   * Use this method to get the centroid of the element.
   *
   * @return RPoint, the centroid point of the element
   * @eexample RGroup_getCentroid
   * @related getBounds ( )
   * @related getCenter ( )
   */
  override val centroid: RPoint?
    get() {
      var bestCentroid: RPoint? = null
      var bestArea = Float.NEGATIVE_INFINITY

      paths.forEach { path ->
        val area = abs(path.area)
        if (area > bestArea) {
          bestArea = area
          bestCentroid = path.centroid
        }
      }

      return bestCentroid
    }

  /**
   * Use this method to add a new shape.  The paths of the shape we are adding will simply be added to the current shape.
   *
   * @param s RShape, the shape to be added.
   * @eexample addShape
   * @related setPath ( )
   * @related addMoveTo ( )
   * @invisible
   */
  fun addShape(s: RShape) = repeat(s.paths.size) { append(s.paths[it]) }

  /**
   * Use this method to create a new path.  The first point of the new path will be set to (0,0).  Use addMoveTo ( ) in order to add a new path with a different first point.
   *
   * @param s the path to be added.
   * @eexample addPath
   * @related setPath ( )
   * @related addMoveTo ( )
   */
  fun addPath(s: RPath) = append(s)

  fun addPaths(s: Array<RPath>) = s.forEach { addPath(it) }

  fun addPath() = append(RPath())

  fun addChild() = appendChild(RShape())

  fun addChild(s: RShape) = appendChild(s)

  fun addChildren(s: Array<RShape>) = s.forEach { addChild(it) }

  /**
   * Use this method to set the current path.
   *
   * @eexample setPath
   * @related addMoveTo ( )
   * @related addLineTo ( )
   * @related addQuadTo ( )
   * @related addBezierTo ( )
   * @related addPath ( )
   */
  fun setPath(indPath: Int) {
    currentPath = indPath
  }

  /**
   * Use this method to add a new moveTo command to the shape.  The command moveTo acts different to normal commands, in order to make a better analogy to its borthers classes Polygon and Mesh.  MoveTo creates a new path in the shape.  It's similar to adding a new contour to a polygon.
   *
   * @param endx the x coordinate of the first point for the new path.
   * @param endy the y coordinate of the first point for the new path.
   * @eexample addMoveTo
   * @related addLineTo ( )
   * @related addQuadTo ( )
   * @related addBezierTo ( )
   * @related addPath ( )
   * @related setPath ( )
   */
  fun addMoveTo(endx: Float, endy: Float) {
    if (paths.isEmpty()) {
      append(RPath(RPoint(endx, endy)))
      currentPath = 0
      return
    }

    if (paths[currentPath].commands.size == 0) {
      paths[currentPath].lastPoint = RPoint(endx, endy)
    } else {
      append(RPath(endx, endy))
    }
  }

  fun addMoveTo(p: RPoint) {
    addMoveTo(p.x, p.y)
  }

  /**
   * Use this method to add a new lineTo command to the current path.  This will add a line from the last point added to the point passed as argument.
   *
   * @param endx the x coordinate of the ending point of the line.
   * @param endy the y coordinate of the ending point of the line.
   * @eexample addLineTo
   * @related addMoveTo ( )
   * @related addQuadTo ( )
   * @related addBezierTo ( )
   * @related addPath ( )
   * @related setPath ( )
   */
  fun addLineTo(endx: Float, endy: Float) {
    if (paths.isEmpty()) {
      append(RPath())
      currentPath = 0
    }

    paths[currentPath].addLineTo(endx, endy)
  }

  fun addLineTo(p: RPoint) = addLineTo(p.x, p.y)

  /**
   * Use this method to add a new quadTo command to the current path.  This will add a quadratic bezier from the last point added with the control and ending points passed as arguments.
   *
   * @param cp1x the x coordinate of the control point of the bezier.
   * @param cp1y the y coordinate of the control point of the bezier.
   * @param endx the x coordinate of the ending point of the bezier.
   * @param endy the y coordinate of the ending point of the bezier.
   * @eexample addQuadTo
   * @related addMoveTo ( )
   * @related addLineTo ( )
   * @related addBezierTo ( )
   * @related addPath ( )
   * @related setPath ( )
   */
  fun addQuadTo(cp1x: Float, cp1y: Float, endx: Float, endy: Float) {
    if (paths.isEmpty()) {
      append(RPath())
      currentPath = 0
    }

    paths[currentPath].addQuadTo(cp1x, cp1y, endx, endy)
  }

  fun addQuadTo(p1: RPoint, p2: RPoint) = addQuadTo(p1.x, p1.y, p2.x, p2.y)

  /**
   * Use this method to add a new bezierTo command to the current path.  This will add a cubic bezier from the last point added with the control and ending points passed as arguments.
   *
   * @param cp1x the x coordinate of the first control point of the bezier.
   * @param cp1y the y coordinate of the first control point of the bezier.
   * @param cp2x the x coordinate of the second control point of the bezier.
   * @param cp2y the y coordinate of the second control point of the bezier.
   * @param endx the x coordinate of the ending point of the bezier.
   * @param endy the y coordinate of the ending point of the bezier.
   * @eexample addArcTo
   * @related addMoveTo ( )
   * @related addLineTo ( )
   * @related addQuadTo ( )
   * @related addPath ( )
   * @related setPath ( )
   */
  fun addBezierTo(cp1x: Float, cp1y: Float, cp2x: Float, cp2y: Float, endx: Float, endy: Float) {
    if (paths.isEmpty()) {
      append(RPath())
      currentPath = 0
    }

    paths[currentPath].addBezierTo(cp1x, cp1y, cp2x, cp2y, endx, endy)
  }

  fun addBezierTo(p1: RPoint, p2: RPoint, p3: RPoint) =
    addBezierTo(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y)

  fun addClose() {
    if (paths.isEmpty()) {
      append(RPath())
      currentPath = 0
    }

    paths[currentPath].addClose()
  }

  /**
   * Use this method to create a new mesh from a given polygon.
   *
   * @return RMesh, the mesh made of tristrips resulting of a tesselation of the polygonization followed by tesselation of the shape.
   * @eexample toMesh
   * @related draw ( )
   */
  override fun toMesh(): RMesh {
    return toPolygon().toMesh()
  }

  /**
   * Use this method to create a new polygon from a given shape.
   *
   * @return RPolygon, the polygon resulting of the segmentation of the commands in each path.
   * @eexample toPolygon
   * @related draw ( )
   */
  override fun toPolygon(): RPolygon =
    RPolygon(children.flatMapArray { it.toPolygon().contours } +
               paths.flatMapArray { path ->
                 if (path.points.isEmpty()) return@flatMapArray arrayOf()
                 arrayOf(RContour(path.points).apply {
                   closed = path.closed
                   setStyle(path)
                 })
               })

  fun polygonize() {
    paths.forEach(RPath::polygonize)
    children.forEach(RShape::polygonize)
  }

  /**
   * @invisible
   */
  override fun toShape(): RShape {
    return this
  }

  /**
   * Use this method to get the intersection of this polygon with the polygon passed in as a parameter.
   */
  fun intersection(p: RShape): RShape = applyTransform(p, RClip::intersection, RShape::intersection)

  /**
   * Use this method to get the union of this polygon with the polygon passed in as a parameter.
   */
  fun union(p: RShape): RShape = applyTransform(p, RClip::union, RShape::union)

  /**
   * Use this method to get the xor of this polygon with the polygon passed in as a parameter.
   */
  fun xor(p: RShape): RShape = applyTransform(p, RClip::xor, RShape::xor)

  /**
   * Use this method to get the difference between this polygon and the polygon passed in as a parameter.
   */
  fun diff(p: RShape): RShape = applyTransform(p, RClip::diff, RShape::diff)

  private fun applyTransform(
    p: RShape,
    polyTransform: (RPolygon, RPolygon) -> RPolygon,
    shapeTransform: RShape.(RShape) -> RShape,
  ): RShape =
    RShape(paths = polyTransform(toPolygon(), p.toPolygon()).toShape().paths,
           children = children.mapArray { shapeTransform(it, p) }, style = this
    )

  /**
   * Use this to return the start, control and end points of the shape.  It returns the points as an array of RPoint.
   *
   * @return RPoint[], the start, control and end points returned in an array.
   * @eexample RShape_getHandles
   */
  override val handles: Array<RPoint>
    get() = paths.flatMapArray { it.handles } + children.flatMapArray { it.handles }

  /**
   * Use this to return a point on the curve given a certain advancement.  It returns the point as an RPoint.
   *
   * @return RPoint[], the point on the curve.
   * @eexample RShape_getPoints
   */
  override fun getPoint(t: Float): RPoint? {
    val indAndAdv = indAndAdvAt(t)
    val indOfElement = indAndAdv[0].toInt()
    val advOfElement = indAndAdv[1]
    return if (indOfElement < paths.size) {
      paths[indOfElement].getPoint(advOfElement)
    } else {
      children[indOfElement - paths.size].getPoint(advOfElement)
    }
  }

  /**
   * Use this to return the points on the curve of the shape.  It returns the points as an array of RPoint.
   *
   * @return RPoint[], the points returned in an array.
   * @eexample RShape_getPoints
   */
  override val points: Array<RPoint>
    get() = paths.flatMapArray { it.points } + children.flatMapArray { it.points }

  /**
   * Use this to return a point on the curve given a certain advancement.  It returns the point as an RPoint.
   *
   * @return RPoint[], the point on the curve.
   * @eexample RShape_getTangents
   */
  override fun getTangent(t: Float): RPoint? {
    val indAndAdv = indAndAdvAt(t)
    val indOfElement = indAndAdv[0].toInt()
    val advOfElement = indAndAdv[1]
    return if (indOfElement < paths.size) {
      paths[indOfElement].getTangent(advOfElement)
    } else {
      children[indOfElement - paths.size].getTangent(advOfElement)
    }
  }

  /**
   * Use this to return a specific tangent on the curve.  It returns true if the point passed as a parameter is inside the shape.  Implementation taken from: http://www.ecse.rpi.edu/Homepages/wrf/Research/Short_Notes/pnpoly.html
   *
   * @param x the X coordinate of the point for which to test containment.
   * @param y the Y coordinate of the point for which to test containment.
   * @return boolean, true if the point is in the path.
   */
  open fun contains(x: Float, y: Float): Boolean = contains(RPoint(x, y))

  /**
   * Use this to return a specific tangent on the curve.  It returns true if the point passed as a parameter is inside the shape.  Implementation taken from: http://www.ecse.rpi.edu/Homepages/wrf/Research/Short_Notes/pnpoly.html
   *
   * @param p the point for which to test containment.
   * @return boolean, true if the point is in the path.
   */
  override fun contains(p: RPoint): Boolean {
    val testx = p.x
    val testy = p.y

    // Test for containment in bounding box
    val bbox = bounds
    val xmin = bbox.minX
    val xmax = bbox.maxX
    val ymin = bbox.minY
    val ymax = bbox.maxY
    if (testx < xmin || testx > xmax || testy < ymin || testy > ymax) {
      return false
    }

    // Test for containment in shape
    val verts = pointsInPaths.flatMapArray { pointPath -> pointPath + RPoint(0f, 0f) }
    val nvert = verts.size
    var j = 0
    var c = false
    var i = 0
    j = nvert - 1
    while (i < nvert) {
      if (verts[i].y > testy != verts[j].y > testy && testx < (verts[j].x - verts[i].x) * (testy - verts[i].y) / (verts[j].y - verts[i].y) + verts[i].x) {
        c = !c
      }
      j = i++
    }
    return c
  }

  /**
   * Use this to return the points on the curve of the shape.  It returns the point as an RPoint.
   *
   * @return RPoint[], the points returned in an array.
   * @eexample RShape_getTangents
   */
  override val tangents: Array<RPoint>
    get() = paths.flatMapArray { it.tangents } + children.flatMapArray { it.tangents }

  /**
   * Use this to return the points of each path of the group.  It returns the points as an array of arrays of RPoint.
   *
   * @return RPoint[], the points returned in an array.
   * @eexample RGroup_getPoints
   */
  override val pointsInPaths: Array<Array<RPoint>>
    get() = arrayOf(*paths.flatMapArray { it.pointsInPaths },
                    *children.flatMapArray { it.pointsInPaths })

  override val handlesInPaths: Array<Array<RPoint>>
    get() = arrayOf(*paths.flatMapArray { it.handlesInPaths },
                    *children.flatMapArray { it.handlesInPaths })

  override val tangentsInPaths: Array<Array<RPoint>>
    get() = arrayOf(*paths.flatMapArray { it.tangentsInPaths },
                    *children.flatMapArray { it.tangentsInPaths })

  fun splitPaths(t: Float): Array<RShape> {
    val result = mutableListOf<RShape>()
    result[0] = RShape()
    result[1] = RShape()
    paths.forEach { path ->
      val splittedPaths = path.split(t)
      if (splittedPaths.isEmpty()) {
        result[0].addPath(splittedPaths[0])
        result[1].addPath(splittedPaths[1])
      }
    }
    children.forEachIndexed { i, child ->
      val splittedPaths = child.splitPaths(t)
      result[0].addChild(splittedPaths[0])
      result[1].addChild(splittedPaths[1])
    }
    result[0].setStyle(this)
    result[1].setStyle(this)

    return result.toTypedArray()
  }

  /**
   * Use this to insert a split point into the shape.
   *
   * @param t the parameter of advancement on the curve. t must have values between 0 and 1.
   * @eexample insertHandle
   */
  fun insertHandle(t: Float) {
    if (t == 0f || t == 1f) {
      return
    }
    val indAndAdv = indAndAdvAt(t)
    val indOfElement = indAndAdv[0].toInt()
    val advOfElement = indAndAdv[1]
    if (indOfElement < paths.size) {
      paths[indOfElement].insertHandle(advOfElement)
    } else {
      children[indOfElement - paths.size].insertHandle(advOfElement)
    }

    // Clear the cache
    lenCurves = floatArrayOf()
    lenCurve = -1f
    return
  }

  /**
   * Use this to insert a split point into each command of the shape.
   *
   * @param t the parameter of advancement on the curve. t must have values between 0 and 1.
   * @eexample insertHandleInPaths
   */
  fun insertHandleInPaths(t: Float) {
    if (t == 0f || t == 1f) {
      return
    }
    val numPaths = paths.size
    if (numPaths == 0) {
      return
    }
    for (i in 0 until numPaths) {
      paths[i].insertHandleInPaths(t)
    }

    // Clear the cache
    lenCurves = floatArrayOf()
    lenCurve = -1f
    return
  }

  fun split(t: Float): Array<RShape> {
    val result = mutableListOf<RShape>()
    result[0] = RShape()
    result[1] = RShape()
    if (t == 0.0f) {
      result[0] = RShape()
      result[0].setStyle(this)
      result[1] = RShape(this)
      result[1].setStyle(this)
      return result.toTypedArray()
    }
    if (t == 1.0f) {
      result[0] = RShape(this)
      result[0].setStyle(this)
      result[1] = RShape()
      result[1].setStyle(this)
      return result.toTypedArray()
    }
    val indAndAdv = indAndAdvAt(t)
    var indOfElement = indAndAdv[0].toInt()
    val advOfElement = indAndAdv[1]
    return if (indOfElement < paths.size) {
      val splittedShapes = paths[indOfElement].split(advOfElement)
      result[0] = RShape()
      for (i in 0 until indOfElement) {
        result[0].addPath(RPath(paths[i]))
      }
      result[0].addPath(RPath(splittedShapes[0]))
      result[0].setStyle(this)
      result[1] = RShape()
      result[1].addPath(RPath(splittedShapes[1]))
      for (i in indOfElement + 1 until paths.size) {
        result[1].addPath(RPath(paths[i]))
      }
      children.forEachIndexed { i, child ->
        result[1].appendChild(RShape(children[i]))
      }
      result[1].setStyle(this)
      result.toTypedArray()
    } else {
      indOfElement -= paths.size

      // Add the elements before the cut point
      for (i in 0 until indOfElement) {
        result[0].addChild(RShape(children[i]))
      }

      // Add the cut point element cutted
      val splittedChild = children[indOfElement].split(advOfElement)
      result[0].addChild(RShape(splittedChild[0]))
      result[1].addChild(RShape(splittedChild[1]))

      // Add the elements after the cut point
      for (i in indOfElement + 1 until children.size) {
        result[1].addChild(RShape(children[i]))
      }
      result[0].setStyle(this)
      result[1].setStyle(this)
      result.toTypedArray()
    }
  }

  /**
   * Use this method to get the points of intersection between this shape and another shape passed in as a parameter.
   *
   * @param other the path with which to check for intersections
   */
  fun getIntersections(other: RShape): Array<RPoint> {
    // TODO: when we will be able to intersect between all
    //       geometric elements the polygonization will not be necessary
    val shp = RShape(this)
    shp.polygonize()
    val otherPol = RShape(other)
    otherPol.polygonize()
    return shp.polygonIntersectionPoints(otherPol)
  }

  fun getIntersections(other: RCommand): Array<RPoint> {
    // TODO: when we will be able to intersect between all
    //       geometric elements the polygonization will not be necessary
    val shp = RShape(this)
    shp.polygonize()
    return shp.polygonIntersectionPoints(other)
  }

  fun polygonIntersectionPoints(other: RCommand): Array<RPoint> {
    val result: MutableList<RPoint> = mutableListOf()
    paths.forEach { path -> result.addAll(path.intersectionPoints(other)) }
    children.forEach { child -> result.addAll(child.polygonIntersectionPoints(other)) }

    return result.toTypedArray()
  }

  fun polygonIntersectionPoints(other: RPath): Array<RPoint> {
    val result: MutableList<RPoint> = mutableListOf()
    paths.forEach { path -> result.addAll(path.intersectionPoints(other)) }
    children.forEach { child -> result.addAll(child.polygonIntersectionPoints(other)) }

    return result.toTypedArray()
  }

  fun polygonIntersectionPoints(other: RShape): Array<RPoint> {
    val result: MutableList<RPoint> = mutableListOf()
    paths.forEach { path -> result.addAll(other.polygonIntersectionPoints(path)) }
    children.forEach { child -> result.addAll(child.polygonIntersectionPoints(other)) }

    return result.toTypedArray()
  }

  /**
   * Use this method to get the closest or intersection points of the shape with another shape passed as argument.
   *
   * @param other the path with which to check for intersections
   */
  fun getClosest(other: RShape): RClosest {
    // TODO: when we will be able to intersect between all
    //       geometric elements the polygonization will not be necessary
    val shp = RShape(this)
    shp.polygonize()
    val otherPol = RShape(other)
    otherPol.polygonize()
    return shp.polygonClosestPoints(otherPol)
  }

  fun getClosest(other: RCommand): RClosest {
    // TODO: when we will be able to intersect between all
    //       geometric elements the polygonization will not be necessary
    val shp = RShape(this)
    shp.polygonize()
    return shp.polygonClosestPoints(other)
  }

  fun polygonClosestPoints(other: RCommand): RClosest {
    val result = RClosest()
    paths.forEach { path ->
      path.closestPoints(other)?.let { result.update(it) }
    }
    children.forEach { child -> result.update(child.polygonClosestPoints(other)) }
    return result
  }

  fun polygonClosestPoints(other: RPath): RClosest {
    val result = RClosest()
    paths.forEach { path ->
      path.closestPoints(other)?.let { result.update(it) }
    }
    children.forEach { child -> result.update(child.polygonClosestPoints(other)) }
    return result
  }

  fun polygonClosestPoints(other: RShape): RClosest {
    val numChildren = children.size
    val numPaths = paths.size
    val result = RClosest()


    paths.forEach { path ->
      result.update(other.polygonClosestPoints(path))
    }
    children.forEach { child ->
      result.update(other.polygonClosestPoints(child))
    }
    return result
  }

  /**
   * Use this method to adapt a group of of figures to a shape.
   *
   * @param shp the path to which to adapt
   * @eexample RGroup_adapt
   */
  @JvmOverloads
  @Throws(RuntimeException::class)
  fun adapt(
    shp: RShape,
    wght: Float = RG.adaptorScale,
    lngthOffset: Float = RG.adaptorLengthOffset,
  ) {
    val c = this.bounds
    val xmin = c.minX
    val xmax = c.maxX
    val numChildren = children.size
    when (RG.adaptorType) {
      RG.BYPOINT -> {
        val ps = this.handles
        var k = 0
        while (k < ps.size) {
          val px = ps[k].x
          val py = ps[k].y
          val t = ((px - xmin) / (xmax - xmin) + lngthOffset) % 1.001f
          val tg = shp.getTangent(t)
          val p = shp.getPoint(t)

          if (tg == null || p == null) continue

          val angle = atan2(tg.y, tg.x) - Math.PI.toFloat() / 2f
          ps[k].x = p.x + wght * py * cos(angle)
          ps[k].y = p.y + wght * py * sin(angle)
          k++
        }
      }
      RG.BYELEMENTINDEX -> {
        var i = 0
        while (i < numChildren) {
          val elem = children[i]
          val elemc = elem.bounds
          val px = (elemc.bottomRight.x + elemc.topLeft.x) / 2f
          val py = (elemc.bottomRight.y - elemc.topLeft.y) / 2f
          val t = (i.toFloat() / numChildren.toFloat() + lngthOffset) % 1f
          val tg = shp.getTangent(t)
          val p = shp.getPoint(t)
          if (tg == null || p == null) continue
          val angle = atan2(tg.y, tg.x)
          val pletter = RPoint(px, py)
          p.sub(pletter)
          elem.transform(RMatrix().apply {
            translate(p)
            rotate(angle, pletter)
            scale(wght, pletter)
          })
          i++
        }
      }
      RG.BYELEMENTPOSITION -> {
        var i = 0
        while (i < numChildren) {
          val elem = children[i]
          val elemc = elem.bounds
          val px = (elemc.bottomRight.x + elemc.topLeft.x) / 2f
          val py = (elemc.bottomRight.y - elemc.topLeft.y) / 2f
          val t = ((px - xmin) / (xmax - xmin) + lngthOffset) % 1f
          val tg = shp.getTangent(t)
          val p = shp.getPoint(t)
          if (tg == null || p == null) continue
          val angle = atan2(tg.y, tg.x)
          val pletter = RPoint(px, py)
          p.sub(pletter)
          val mtx = RMatrix()
          mtx.translate(p)
          mtx.rotate(angle, pletter)
          mtx.scale(wght, pletter)
          elem.transform(mtx)
          i++
        }
      }
      else -> throw RuntimeException(
        "Unknown adaptor type : " + RG.adaptorType + ". The method RG.setAdaptor() only accepts RG.BYPOINT or RG.BYELEMENT as parameter values."
      )
    }
  }

  override fun print() {
    println("paths [count " + paths.size + "]: ")
    paths.forEachIndexed { i, path ->
      println("--- path $i ---")
      path.print()
      println("---------------")
    }
    println("children [count " + children.size + "]: ")
    children.forEachIndexed { i, child ->
      println("--- child $i ---")
      child.print()
      println("---------------")
    }
  }

  override fun draw(g: PGraphics) {
    if (!RG.ignoreStyles) {
      saveContext(g)
      setContext(g)
    }
    this.drawPaths(g)
    paths.forEach { it.draw(g) }
    if (!RG.ignoreStyles) {
      restoreContext(g)
    }
  }

  override fun draw(g: PApplet) {
    if (!RG.ignoreStyles) {
      saveContext(g)
      setContext(g)
    }
    this.drawPaths(g)
    paths.forEach { it.draw(g) }
    if (!RG.ignoreStyles) {
      restoreContext(g)
    }
  }

  /**
   * Use this method to draw the shape.
   *
   * @param g PGraphics, the graphics object on which to draw the shape
   * @eexample drawShape
   */
  private fun drawPaths(g: PGraphics) = drawUsingBreakShape(g)

  private fun drawPaths(g: PApplet) = drawUsingBreakShape(g)

  // ----------------------
  // --- Private Methods ---
  // ----------------------
  override fun calculateCurveLengths() {
    lenCurves = FloatArray(paths.size + children.size)
    lenCurve = 0f
    paths.forEachIndexed { i, path ->
      lenCurves[i] = path.curveLength
      lenCurve += lenCurves[i]
    }
    children.forEachIndexed { i, child ->
      lenCurves[i + paths.size] = child.curveLength
      lenCurve += lenCurves[i + paths.size]
    }
  }

  private fun indAndAdvAt(t: Float): FloatArray {
    var indOfElement = 0
    val lengthsCurves = curveLengths
    val lengthCurve = curveLength

    /* Calculate the amount of advancement t mapped to each command */
    /* We use a simple algorithm where we give to each command the same amount of advancement */
    /* A more useful way would be to give to each command an advancement proportional to the length of the command */
    var accumulatedAdvancement = lengthsCurves[indOfElement] / lengthCurve
    var prevAccumulatedAdvancement = 0f

    /* Find in what command the advancement point is  */
    while (t > accumulatedAdvancement) {
      indOfElement++
      prevAccumulatedAdvancement = accumulatedAdvancement
      accumulatedAdvancement += lengthsCurves[indOfElement] / lengthCurve
    }
    val advOfElement =
      (t - prevAccumulatedAdvancement) / (lengthsCurves[indOfElement] / lengthCurve)

    return floatArrayOf(indOfElement.toFloat(), PApplet.constrain(advOfElement, 0.0f, 1.0f))
  }

  private fun appendChild(nextShape: RShape) {
    children += nextShape
  }

  private fun append(nextPath: RPath) {
    paths += nextPath
  }

  private fun drawUsingInternalTesselator(g: PGraphics) {
    val numPaths = paths.size
    if (numPaths != 0) {
      if (isIn(g)) {

        // Save the information about the current context
        val strokeBefore = g.stroke
        val strokeColorBefore = g.strokeColor
        val strokeWeightBefore = g.strokeWeight
        val smoothBefore = g.smooth
        val fillBefore = g.fill
        val fillColorBefore = g.fillColor

        // By default always drawy with an ADAPTATIVE segmentator
        val lastSegmentator = RCommand.segmentType
        RCommand.setSegmentator(RCommand.ADAPTATIVE)

        // Check whether to draw the fill or not
        if (g.fill) {
          // Since we are drawing the different tristrips we must turn off the stroke or make it the same color as the fill
          // NOTE: there's currently no way of drawing the outline of a mesh, since no information is kept about what vertices are at the edge

          // This is here because when rendering meshes we get unwanted lines between the triangles
          g.noStroke()
          try {
            g.noSmooth()
          } catch (e: Exception) {
          }
          val tempMesh = toMesh()
          tempMesh.draw(g)

          // Restore the old context
          g.stroke(strokeColorBefore)
          if (!strokeBefore) {
            g.noStroke()
          }
          try {
            if (smoothBefore > 0) {
              g.smooth()
            }
          } catch (e: Exception) {
          }
        }

        // Check whether to draw the stroke
        g.noFill()
        if (!strokeBefore) {
          // If there is no stroke to draw
          // we will still draw one the color of the fill in order to have antialiasing
          g.stroke(g.fillColor)
          g.strokeWeight(1f)
        }
        for (i in 0 until numPaths) {
          paths[i].draw(g)
        }

        // Restore the fill state and stroke state and color
        if (fillBefore) {
          g.fill(fillColorBefore)
        } else {
          g.noFill()
        }
        g.strokeWeight(strokeWeightBefore)
        g.stroke(strokeColorBefore)
        if (!strokeBefore) {
          g.noStroke()
        }

        // Restore the user set segmentator
        RCommand.setSegmentator(lastSegmentator)
      }
    }
  }

  private fun drawUsingInternalTesselator(p: PApplet) {
    val numPaths = paths.size

    if (numPaths == 0 || !isIn(p)) return
    // Save the information about the current context
    val strokeBefore = p.g.stroke
    val strokeColorBefore = p.g.strokeColor
    val strokeWeightBefore = p.g.strokeWeight
    val smoothBefore = p.g.smooth
    val fillBefore = p.g.fill
    val fillColorBefore = p.g.fillColor

    // By default always drawy with an ADAPTATIVE segmentator
    val lastSegmentator = RCommand.segmentType
    RCommand.setSegmentator(RCommand.ADAPTATIVE)

    // Check whether to draw the fill or not
    if (p.g.fill) {
      // Since we are drawing the different tristrips we must turn off the stroke
      // or make it the same color as the fill
      // NOTE: there's currently no way of drawing the outline of a mesh,
      // since no information is kept about what vertices are at the edge

      // This is here because when rendering meshes we get unwanted lines between the triangles
      p.noStroke()
      try {
        p.noSmooth()
      } catch (e: Exception) {
      }
      val tempMesh = toMesh()
      tempMesh.draw(p)

      // Restore the old context
      p.stroke(strokeColorBefore)
      p.strokeWeight(strokeWeightBefore)
      if (!strokeBefore) {
        p.noStroke()
      }
      try {
        if (smoothBefore > 0) {
          p.smooth()
        }
      } catch (e: Exception) {
      }
    }

    // Check whether to draw the stroke
    p.noFill()
    if (smoothBefore > 0 && fillBefore || strokeBefore) {
      if (!strokeBefore) {
        // If there is no stroke to draw
        // we will still draw one the color
        // of the fill in order to have antialiasing
        p.stroke(fillColorBefore)
        p.strokeWeight(1f)
      }
      for (i in 0 until numPaths) {
        paths[i].draw(p)
      }

      // Restore the old context
      if (fillBefore) {
        p.fill(fillColorBefore)
      }
      p.strokeWeight(strokeWeightBefore)
      p.stroke(strokeColorBefore)
      if (!strokeBefore) {
        p.noStroke()
      }
    }

    // Restore the user set segmentator
    RCommand.setSegmentator(lastSegmentator)
  }

  private fun drawUsingBreakShape(g: PGraphics) {
    if (paths.isEmpty() || !isIn(g)) return
    var closed = false
    val useContours = paths.size > 1
    g.beginShape()
    paths.forEachIndexed { pathIndex, path ->
      if (useContours && pathIndex > 0) g.beginContour()

      closed = closed or path.closed

      path.commands.forEachIndexed { index, command ->
        val pnts = command.handles

        if (index == 0) g.vertex(pnts[0].x, pnts[0].y)

        when (command.type) {
          RCommand.LINETO -> g.vertex(pnts[1].x, pnts[1].y)
          RCommand.QUADBEZIERTO -> g.bezierVertex(
            pnts[1].x,
            pnts[1].y,
            pnts[2].x,
            pnts[2].y,
            pnts[2].x,
            pnts[2].y
          )
          RCommand.CUBICBEZIERTO -> g.bezierVertex(
            pnts[1].x,
            pnts[1].y,
            pnts[2].x,
            pnts[2].y,
            pnts[3].x,
            pnts[3].y
          )
        }
      }
      if (useContours && pathIndex > 0) g.endContour()
    }
    g.endShape(if (closed) PConstants.CLOSE else PConstants.OPEN)
  }

  private fun drawUsingBreakShape(g: PApplet) {
    if (paths.isEmpty() || !isIn(g)) return
    var closed = false
    val useContours = paths.size > 1
    g.beginShape()
    paths.forEachIndexed { i, path ->
      if (useContours && i > 0) g.beginContour()
      closed = closed or path.closed
      path.commands.forEachIndexed { index, command ->
        val pnts = command.handles
        if (index == 0) g.vertex(pnts[0].x, pnts[0].y)

        when (command.type) {
          RCommand.LINETO -> g.vertex(pnts[1].x, pnts[1].y)
          RCommand.QUADBEZIERTO -> g.bezierVertex(
            pnts[1].x, pnts[1].y, pnts[2].x, pnts[2].y,
            pnts[2].x, pnts[2].y
          )
          RCommand.CUBICBEZIERTO -> g.bezierVertex(
            pnts[1].x, pnts[1].y, pnts[2].x, pnts[2].y,
            pnts[3].x, pnts[3].y
          )
        }
      }
      if (useContours && i > 0) g.endContour()
    }
    g.endShape(if (closed) PConstants.CLOSE else PConstants.OPEN)
  }

  override fun toString(): String {
    return "RShape(\n" + "  paths (${paths.size}): ${paths.toArrayString()},\n" + "  children (${children.size}): ${children.toArrayString()},\n" + ")"
  }

  companion object {
    /**
     * Use this method to create a new line.
     *
     * @param x1 x coordinate of the first point of the line
     * @param y1 y coordinate of the first point of the line
     * @param x2 x coordinate of the last point of the line
     * @param y2 y coordinate of the last point of the line
     * @return RShape, the ring polygon newly created
     * @eexample createRing
     */
    @JvmStatic
    fun createLine(x1: Float, y1: Float, x2: Float, y2: Float): RShape {
      val line = RShape()
      val path = RPath()
      val lineCommand = RCommand(x1, y1, x2, y2)
      path.addCommand(lineCommand)
      line.addPath(path)
      return line
    }

    /**
     * Use this method to create a new ring polygon.
     *
     * @param x          x coordinate of the center of the shape
     * @param y          y coordinate of the center of the shape
     * @param widthBig   the outer width of the ring polygon
     * @param widthSmall the inner width of the ring polygon
     * @return RShape, the ring polygon newly created
     * @eexample createRing
     */
    @JvmStatic
    fun createRing(x: Float, y: Float, widthBig: Float, widthSmall: Float): RShape {
      val ring = RShape()
      val outer = createCircle(x, y, widthBig)
      val inner = createCircle(x, y, -widthSmall)
      ring.addPath(outer.paths[0])
      ring.addPath(inner.paths[0])
      return ring
    }

    /**
     * Use this method to create a new starform polygon.
     *
     * @param widthBig   the outer width of the star polygon
     * @param widthSmall the inner width of the star polygon
     * @param spikes     the amount of spikes on the star polygon
     * @return RShape, the starform polygon newly created
     * @eexample createStar
     */
    @JvmStatic
    fun createStar(x: Float, y: Float, widthBig: Float, widthSmall: Float, spikes: Int): RShape {
      val radiusBig = widthBig / 2f
      val radiusSmall = widthSmall / 2f
      val star = RShape()
      star.addMoveTo(x - radiusBig, y)
      star.addLineTo(
        x - (radiusSmall * cos(Math.PI / spikes)).toFloat(),
        y - (radiusSmall * sin(Math.PI / spikes)).toFloat()
      )
      var i = 2
      while (i < 2 * spikes) {
        star.addLineTo(
          x - (radiusBig * cos(Math.PI * i / spikes)).toFloat(),
          y - (radiusBig * sin(Math.PI * i / spikes)).toFloat()
        )
        star.addLineTo(
          x - (radiusSmall * cos(Math.PI * (i + 1) / spikes)).toFloat(),
          y - (radiusSmall * sin(Math.PI * (i + 1) / spikes)).toFloat()
        )
        i += 2
      }
      star.addClose()
      return star
    }

    /**
     * Use this method to create a new circle shape.
     *
     * @param x the x position of the rectangle
     * @param y the y position of the rectangle
     * @param w the width of the rectangle
     * @param h the height of the rectangle
     * @return RShape, the rectangular shape just created
     * @eexample createRectangle
     */
    @JvmStatic
    fun createRectangle(x: Float, y: Float, w: Float, h: Float): RShape = RShape(RPath(
      arrayOf(
        RPoint(x, y),
        RPoint(x + w, y),
        RPoint(x + w, y + h),
        RPoint(x, y + h),
      )
    ).also { it.addClose() })

    fun createRectangle(center: RPoint, w: Number, h: Number) =
      createRectangle(center.x, center.y, w.toFloat(), h.toFloat())

    /**
     * Use this method to create a new elliptical shape.
     *
     * @param x the x position of the ellipse
     * @param y the y position of the ellipse
     * @param w the width of the ellipse
     * @param h the height of the ellipse
     * @return RShape, the elliptical shape just created
     * @eexample createEllipse
     */
    @JvmStatic
    fun createEllipse(x: Float, y: Float, w: Float, h: Float): RShape {
      val rx = w / 2f
      val ry = h / 2f
      val center = RPoint(x, y)

      val kx = (8f / sqrt(2.0).toFloat() - 4f) / 3f * rx
      val ky = (8f / sqrt(2.0).toFloat() - 4f) / 3f * ry

      return RShape().apply {
        addMoveTo(center.plusY(-ry))
        addBezierTo(
          center + RPoint(kx, -ry),
          center + RPoint(rx, -ky),
          center + RPoint(rx, 0f),
        )
        addBezierTo(
          center + RPoint(rx, ky),
          center + RPoint(kx, ry),
          center + RPoint(0f, ry),
        )
        addBezierTo(
          center + RPoint(-kx, ry),
          center + RPoint(-rx, ky),
          center + RPoint(-rx, 0f),
        )
        addBezierTo(
          center + RPoint(-rx, -ky),
          center + RPoint(-kx, -ry),
          center + RPoint(0f, -ry),
        )
        addClose()
      }
    }

    fun createEllipse(center: RPoint, size: RPoint): RShape =
      createEllipse(center.x, center.y, size.x, size.y)

    fun createCircle(x: Number, y: Number, d: Number): RShape =
      createEllipse(x.toFloat(), y.toFloat(), d.toFloat(), d.toFloat())

    fun createCircle(center: RPoint, d: Number): RShape = createCircle(center.x, center.y, d)
  }
}
