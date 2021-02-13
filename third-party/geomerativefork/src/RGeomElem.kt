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

import geomerativefork.src.RPoint.Companion.maxXY
import geomerativefork.src.RPoint.Companion.minXY
import geomerativefork.src.util.reduceTo
import processing.core.PApplet
import processing.core.PGraphics
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * RGeomElem is an interface to any geometric element that can be drawn and transformed, such as shapes, polygons or meshes.
 *
 * @extended
 */
abstract class RGeomElem {
  /**
   * Shape document width.
   */
  @JvmField var elemWidth = 0f

  /**
   * Shape document height.
   */
  @JvmField var elemHeight = 0f
  @JvmField var elemOrigWidth = 0f
  @JvmField var elemOrigHeight = 0f

  // Functions dependent of the type of element
  // They must be overrided
  abstract fun draw(g: PGraphics)
  abstract fun draw(g: PApplet)
  fun draw() = draw(RG.parent())

  open fun getPoint(t: Float): RPoint? = null
  open fun getTangent(t: Float): RPoint? = null
  open val handles: Array<RPoint> = arrayOf()
  open val points: Array<RPoint> = arrayOf()
  open val tangents: Array<RPoint> = arrayOf()
  open val handlesInPaths: Array<Array<RPoint>> = arrayOf()
  open val pointsInPaths: Array<Array<RPoint>> = arrayOf()
  open val tangentsInPaths: Array<Array<RPoint>> = arrayOf()
  open operator fun contains(p: RPoint): Boolean = false

  /**
   * Use this method to test if the shape contains all the points of another shape.
   *
   * @return boolean, true if the shape contains all the points of the other shape
   * @eexample contains
   * @related containsBounds ( )
   * @related containsHandles ( )
   */
  operator fun contains(shp: RGeomElem): Boolean {
    return contains(shp.points)
  }

  /**
   * Use this method to test if the shape contains the bounding box of another shape.
   *
   * @return boolean, true if the shape contains the bounding box of the other shape
   * @eexample contains
   * @related contains ( )
   * @related containsHandles ( )
   */
  fun containsBounds(shp: RGeomElem): Boolean {
    val tl = shp.topLeft
    val tr = shp.topRight
    val bl = shp.bottomRight
    val br = shp.bottomLeft
    return (this.contains(tl) && this.contains(tr) && this.contains(bl) && this.contains(br))
  }

  /**
   * Use this method to test if the shape contains the handles of another shape. This method is faster than contains(), but the results might not be perfect.
   *
   * @return boolean, true if the shape contains all the handles of the other shape
   * @eexample contains
   * @related containsBounds ( )
   * @related contains ( )
   */
  fun containsHandles(shp: RGeomElem): Boolean {
    return contains(shp.handles)
  }

  /**
   * Use this method to test if the shape contains an array of points.
   *
   * @return boolean, true if the shape contains all the points
   * @eexample contains
   * @related contains ( )
   * @related containsBounds ( )
   * @related containsHandles ( )
   */
  operator fun contains(pts: Array<RPoint>): Boolean {
    if (pts.isEmpty()) return false

    var inside = true
    for (pt in pts) {
      if (!contains(pt)) {
        inside = false
        break
      }
    }
    return inside
  }

  /**
   * Use this method to test if the shape intersects another shape.
   *
   * @return boolean, true if the shape intersects all the points of the other shape
   * @eexample intersects
   * @related intersectsBounds ( )
   * @related intersectsHandles ( )
   */
  fun intersects(shp: RGeomElem): Boolean {
    return intersects(shp.points)
  }

  /**
   * Use this method to test if the shape intersects the bounding box of another shape.
   *
   * @return boolean, true if the shape intersects the bounding box of the other shape
   * @eexample intersects
   * @related intersects ( )
   * @related intersectsHandles ( )
   */
  fun intersectsBounds(shp: RGeomElem): Boolean {
    val tl = shp.topLeft
    val tr = shp.topRight
    val bl = shp.bottomRight
    val br = shp.bottomLeft
    return (this.contains(tl) || this.contains(tr) || this.contains(bl) || this.contains(br))
  }

  /**
   * Use this method to test if the shape intersects the handles of another shape. This method is faster than intersects(), but the results might not be perfect.
   *
   * @return boolean, true if the shape intersects all the handles of the other shape
   * @eexample intersects
   * @related intersectsBounds ( )
   * @related intersects ( )
   */
  fun intersectsHandles(shp: RGeomElem): Boolean {
    return intersects(shp.handles)
  }

  /**
   * Use this method to test if the shape intersects an array of points.
   *
   * @return boolean, true if the shape intersects all the points
   * @eexample intersects
   * @related intersects ( )
   * @related intersectsBounds ( )
   * @related intersectsHandles ( )
   */
  fun intersects(ps: Array<RPoint>?): Boolean {
    var intersects = false
    if (ps != null) {
      for (i in ps.indices) {
        intersects = intersects or this.contains(ps[i])
      }
    }
    return intersects
  }

  abstract val type: Int

  //public abstract RMesh toMesh();
  //public abstract RPolygon toPolygon();
  abstract fun toShape(): RShape
  open fun print() {}
  @JvmField protected var lenCurves: FloatArray = floatArrayOf()
  @JvmField protected var lenCurve = -1f
  @JvmField var elemName = ""
  @JvmField var style = RStyle()
  fun setFill(_fill: Boolean) {
    style.setFill(_fill)
  }

  fun setFill(_fillColor: Int) {
    style.setFill(_fillColor)
  }

  fun setFill(str: String?) {
    style.setFill(str)
  }

  fun setStroke(_stroke: Boolean) {
    style.setStroke(_stroke)
  }

  fun setStroke(_strokeColor: Int) {
    style.setStroke(_strokeColor)
  }

  fun setStroke(str: String?) {
    style.setStroke(str)
  }

  fun setStrokeWeight(value: Float) {
    style.setStrokeWeight(value)
  }

  fun setStrokeWeight(str: String?) {
    style.setStrokeWeight(str)
  }

  fun setStrokeCap(str: String?) {
    style.setStrokeCap(str)
  }

  fun setStrokeJoin(str: String?) {
    style.setStrokeJoin(str)
  }

  fun setStrokeAlpha(opacity: Int) {
    style.setStrokeAlpha(opacity)
  }

  fun setStrokeAlpha(str: String?) {
    style.setStrokeAlpha(str)
  }

  fun setFillAlpha(opacity: Int) {
    style.setFillAlpha(opacity)
  }

  fun setFillAlpha(str: String?) {
    style.setFillAlpha(str)
  }

  fun setAlpha(opacity: Float) {
    style.setAlpha(opacity)
  }

  fun setAlpha(opacity: Int) {
    style.setAlpha(opacity)
  }

  fun setAlpha(str: String?) {
    style.setAlpha(str)
  }

  protected fun saveContext(g: PGraphics?) {
    style.saveContext(g)
  }

  protected fun saveContext(p: PApplet?) {
    style.saveContext(p)
  }

  protected fun saveContext() {
    style.saveContext()
  }

  protected fun restoreContext(g: PGraphics?) {
    style.restoreContext(g)
  }

  protected fun restoreContext(p: PApplet?) {
    style.restoreContext(p)
  }

  protected fun restoreContext() {
    style.restoreContext()
  }

  protected fun setContext(g: PGraphics?) {
    style.setContext(g)
  }

  protected fun setContext(p: PApplet?) {
    style.setContext(p)
  }

  protected fun setContext() {
    style.setContext()
  }

  fun setStyle(p: RGeomElem) {
    elemName = p.elemName
    elemWidth = p.elemWidth
    elemHeight = p.elemHeight
    elemOrigWidth = p.elemOrigWidth
    elemOrigHeight = p.elemOrigHeight
    style = RStyle(p.style)
  }

  fun setStyle(styleString: String?) {
    style.setStyle(styleString)
  }

  fun setName(str: String) {
    elemName = str
  }

  protected open fun calculateCurveLengths() {
    PApplet.println("Feature not yet implemented for this class.")
  }/* If the cache with the commands lengths is empty, we fill it up */

  /**
   * Use this to return arclengths of each command on the curve.
   *
   * @return float[], the arclengths of each command on the curve.
   * @eexample getCurveLength
   */
  val curveLengths: FloatArray
    get() {
      /* If the cache with the commands lengths is empty, we fill it up */
      if (lenCurves.isEmpty()) calculateCurveLengths()
      return lenCurves
    }

  /**
   * Use this to return the length of the curve.
   *
   * @return float, the arclength of the path.
   * @eexample getCurveLength
   */
  open val curveLength: Float
    get() {
      /* If the cache with the commands lengths is empty, we fill it up */
      if (lenCurve == -1f) {
        calculateCurveLengths()
      }
      return lenCurve
    }

  open fun toPolygon(): RPolygon = toShape().toPolygon()

  open fun toMesh(): RMesh = toShape().toPolygon().toMesh()

  // Functions independent of the type of element
  // No need of being overrided
  open fun transform(m: RMatrix) = handles.forEach { it.transform(m) }

  /**
   * Transform the geometric object to fit in a rectangle defined by the parameters passed.
   *
   * @eexample getBounds
   * @related getCenter ( )
   */
  fun transform(x: Float, y: Float, w: Float, h: Float, keepAspectRatio: Boolean) {
    val mtx = RMatrix()
    val orig = bounds
    val origW = orig.maxX - orig.minX
    val origH = orig.maxY - orig.minY
    mtx.translate(-orig.minX, -orig.minY)
    if (keepAspectRatio) {
      mtx.scale(min(w / origW, h / origH))
    } else {
      mtx.scale(w / origW, h / origH)
    }
    mtx.translate(x, y)
    this.transform(mtx)
    return
  }

  fun transform(x: Float, y: Float, w: Float, h: Float) = transform(x, y, w, h, true)

  /**
   * Use this method to get the bounding box of the element.
   *
   * @return RRectangle, the bounding box of the element in the form of a four-point contour
   * @eexample getBounds
   * @related getCenter ( )
   */
  val bounds: RRectangle
    get() {
      val initialMinPoint = RPoint(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
      val initialMaxPoint = RPoint(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY)

      val (minPoint, maxPoint) = handles.reduceTo(
        initialMinPoint to initialMaxPoint) { (minP, maxP), p ->
        minXY(minP, p) to maxXY(maxP, p)
      }

      return RRectangle(minPoint, maxPoint)
    }

  /**
   * Use this method to get the points of the bounding box of the element.
   *
   * @return RRectangle, the bounding box of the element in the form of a four-point contour
   * @eexample getBounds
   * @related getCenter ( )
   */
  val boundsPoints: Array<RPoint>
    get() = bounds.points

  /**
   * Use this method to get the top left position of the element.
   *
   * @eexample getX
   * @related getTopRight ( )
   * @related getBottomLeft ( )
   * @related getBottomRight ( )
   * @related getWidth ( )
   * @related getHeight ( )
   * @related getCenter ( )
   */
  val topLeft: RPoint
    get() {
      val orig = bounds
      return RPoint(orig.minX, orig.minY)
    }

  /**
   * Use this method to get the top right position of the element.
   *
   * @eexample getX
   * @related getTopRight ( )
   * @related getBottomLeft ( )
   * @related getBottomRight ( )
   * @related getWidth ( )
   * @related getHeight ( )
   * @related getCenter ( )
   */
  val topRight: RPoint
    get() {
      val orig = bounds
      return RPoint(orig.maxX, orig.minY)
    }

  /**
   * Use this method to get the bottom left position of the element.
   *
   * @eexample getX
   * @related getTopRight ( )
   * @related getBottomLeft ( )
   * @related getBottomRight ( )
   * @related getWidth ( )
   * @related getHeight ( )
   * @related getCenter ( )
   */
  val bottomLeft: RPoint
    get() {
      val orig = bounds
      return RPoint(orig.minX, orig.maxY)
    }

  /**
   * Use this method to get the bottom right position of the element.
   *
   * @eexample getX
   * @related getTopRight ( )
   * @related getBottomLeft ( )
   * @related getBottomRight ( )
   * @related getWidth ( )
   * @related getHeight ( )
   * @related getCenter ( )
   */
  val bottomRight: RPoint
    get() {
      val orig = bounds
      return RPoint(orig.maxX, orig.maxY)
    }

  /**
   * Use this method to get the x (left side) position of the element.
   *
   * @return float, the x coordinate of the element
   * @eexample getX
   * @related getY ( )
   * @related getWidth ( )
   * @related getHeight ( )
   * @related getCenter ( )
   */
  val x: Float
    get() {
      val orig = bounds
      return orig.minX
    }

  /**
   * Use this method to get the y position of the element.
   *
   * @return float, the y coordinate of the element
   * @eexample getY
   * @related getY ( )
   * @related getWidth ( )
   * @related getHeight ( )
   * @related getCenter ( )
   */
  val y: Float
    get() {
      val orig = bounds
      return orig.minY
    }

  /**
   * Use this method to get the original height of the element.
   *
   * @return float, the original height of the element before applying any transformations
   * @eexample getOrigHeight
   * @related getCenter ( )
   */
  fun getOrigHeight(): Float {
    return if (elemOrigHeight.toDouble() != 0.0) elemOrigHeight else getHeight()
  }

  /**
   * Use this method to get the original width of the element.
   *
   * @return float, the original width of the element before applying any transformations
   * @eexample getOrigWidth
   * @related getCenter ( )
   */
  fun getOrigWidth(): Float {
    return if (elemOrigWidth.toDouble() != 0.0) elemOrigWidth else getWidth()
  }

  fun updateOrigParams() {
    elemOrigWidth = getWidth()
    elemOrigHeight = getHeight()
  }

  /**
   * Use this method to get the width of the element.
   *
   * @return float, the width of the element
   * @eexample getWidth
   * @related getCenter ( )
   */
  fun getWidth(): Float {
    val orig = bounds
    return orig.maxX - orig.minX
  }

  /**
   * Use this method to get the height of the element.
   *
   * @return float, the height of the element
   * @eexample getHeight
   * @related getCenter ( )
   */
  fun getHeight(): Float {
    val orig = bounds
    return orig.maxY - orig.minY
  }

  /**
   * Use this method to get the center point of the element.
   *
   * @return RPoint, the center point of the element
   * @eexample RGroup_getCenter
   * @related getBounds ( )
   */
  val center: RPoint
    get() {
      val c = bounds
      return RPoint((c.maxX + c.minX) / 2, (c.maxY + c.minY) / 2)
    }

  /**
   * Use this method to get the centroid of the element.
   *
   * @return RPoint, the centroid point of the element
   * @eexample RGroup_getCentroid
   * @related getBounds ( )
   * @related getCenter ( )
   */
  open val centroid: RPoint?
    get() {
      val ps = points
      var areaAcc = 0.0f
      var xAcc = 0.0f
      var yAcc = 0.0f
      for (i in 0 until ps.size - 1) {
        areaAcc += ps[i].x * ps[i + 1].y - ps[i + 1].x * ps[i].y
        xAcc += (ps[i].x + ps[i + 1].x) * (ps[i].x * ps[i + 1].y - ps[i + 1].x * ps[i].y)
        yAcc += (ps[i].y + ps[i + 1].y) * (ps[i].x * ps[i + 1].y - ps[i + 1].x * ps[i].y)
      }
      areaAcc /= 2.0f
      return RPoint(xAcc / (6.0f * areaAcc), yAcc / (6.0f * areaAcc))
    }

  /**
   * Use this method to get the area of an element.
   *
   * @return float, the area point of the element
   * @eexample RGroup_getArea
   * @related getBounds ( )
   * @related getCenter ( )
   * @related getCentroid ( )
   */
  open val area: Float
    get() {
      val ps = points
      var areaAcc = 0.0f
      for (i in 0 until ps.size - 1) {
        areaAcc += ps[i].x * ps[i + 1].y - ps[i + 1].x * ps[i].y
      }
      areaAcc /= 2.0f
      return abs(areaAcc)
    }

  /**
   * Use this method to know if the shape is inside a graphics object. This might be useful if we want to delete objects that go offscreen.
   *
   * @param g the graphics object
   * @return boolean, whether the shape is in or not the graphics object
   * @eexample RShape_isIn
   * @usage Geometry
   */
  fun isIn(g: PGraphics): Boolean {
    val c = bounds
    val x0 = g.screenX(c.topLeft.x, c.topLeft.y)
    val y0 = g.screenY(c.topLeft.x, c.topLeft.y)
    val x1 = g.screenX(c.bottomRight.x, c.topLeft.y)
    val y1 = g.screenY(c.bottomRight.x, c.topLeft.y)
    val x2 = g.screenX(c.bottomRight.x, c.bottomRight.y)
    val y2 = g.screenY(c.bottomRight.x, c.bottomRight.y)
    val x3 = g.screenX(c.topLeft.x, c.bottomRight.y)
    val y3 = g.screenY(c.topLeft.x, c.bottomRight.y)
    val xmax = max(max(x0, x1), max(x2, x3))
    val ymax = max(max(y0, y1), max(y2, y3))
    val xmin = min(min(x0, x1), min(x2, x3))
    val ymin = min(min(y0, y1), min(y2, y3))
    return !((xmax < 0 || xmin > g.width) && (ymax < 0 || ymin > g.height))
  }

  fun isIn(g: PApplet): Boolean {
    val c = bounds
    val x0 = g.screenX(c.topLeft.x, c.topLeft.y)
    val y0 = g.screenY(c.topLeft.x, c.topLeft.y)

    val x1 = g.screenX(c.bottomRight.x, c.topLeft.y)
    val y1 = g.screenY(c.bottomRight.x, c.topLeft.y)

    val x2 = g.screenX(c.bottomRight.x, c.bottomRight.y)
    val y2 = g.screenY(c.bottomRight.x, c.bottomRight.y)

    val x3 = g.screenX(c.topLeft.x, c.bottomRight.y)
    val y3 = g.screenY(c.topLeft.x, c.bottomRight.y)

    val xmax = max(max(x0, x1), max(x2, x3))
    val ymax = max(max(y0, y1), max(y2, y3))
    val xmin = min(min(x0, x1), min(x2, x3))
    val ymin = min(min(y0, y1), min(y2, y3))
    return !((xmax < 0 || xmin > g.width) && (ymax < 0 || ymin > g.height))
  }

  /**
   * Use this method to get the transformation matrix in order to fit and center the element on the canvas. Scaling and translation damping parameters are available, in order to create animations.
   *
   * @param g           the canvas to which to fit and center the path
   * @param margin      the margin to take into account when fitting
   * @param sclDamping  a value from 0 to 1. The damping coefficient for the scale, if the value is 0, then no scaling is applied.
   * @param trnsDamping a value from 0 to 1. The damping coefficient for the translation, if the value is 0, then no translation is applied.
   * @return RMatrix, the transformation matrix
   * @eexample RGeomElem_getCenteringTransf
   * @related getBounds ( )
   */
  @Throws(RuntimeException::class)
  fun getCenteringTransf(
    g: PGraphics, margin: Float, sclDamping: Float, trnsDamping: Float,
  ): RMatrix {
    val mrgn = margin * 2
    val c = bounds
    val scl = min((g.width - mrgn) / abs(c.minX - c.maxX), (g.height - mrgn) / abs(c.minY - c.maxY))
    val trns = center
    val transf = RMatrix()
    if (sclDamping != 0f) {
      transf.scale(1 + (scl - 1) * sclDamping)
    }
    if (trnsDamping != 0f) {
      transf.translate(-trns.x * trnsDamping, -trns.y * trnsDamping)
    }
    return transf
  }

  @Throws(RuntimeException::class)
  fun getCenteringTransf(g: PGraphics): RMatrix {
    return getCenteringTransf(g, 0f, 1f, 1f)
  }

  @Throws(RuntimeException::class)
  fun getCenteringTransf(g: PGraphics, margin: Float): RMatrix {
    return getCenteringTransf(g, margin, 1f, 1f)
  }

  fun centerIn(g: PGraphics) {
    transform(getCenteringTransf(g))
  }

  fun centerIn(g: PGraphics, margin: Float) {
    transform(getCenteringTransf(g, margin, 1f, 1f))
  }

  @Throws(RuntimeException::class)
  fun centerIn(g: PGraphics, margin: Float, sclDamping: Float, trnsDamping: Float) {
    transform(getCenteringTransf(g, margin, sclDamping, trnsDamping))
  }

  /**
   * Apply a translation to the element, given translation coordinates.
   *
   * @param tx the coefficient of x translation
   * @param ty the coefficient of y translation
   * @eexample RGeomElem_translate
   * @usage Geometry
   * @related transform ( )
   * @related rotate ( )
   * @related scale ( )
   */
  fun translate(tx: Float, ty: Float) {
    val transf = RMatrix()
    transf.translate(tx, ty)
    transform(transf)
  }

  /**
   * Apply a translation to the element, given a point.
   *
   * @param t the translation vector to be applied
   * @eexample RGeomElem_translate
   * @usage Geometry
   * @related transform ( )
   * @related rotate ( )
   * @related scale ( )
   */
  fun translate(t: RPoint) {
    val transf = RMatrix()
    transf.translate(t)
    transform(transf)
  }

  /**
   * Apply a rotation to the element, given an angle and optionally a rotation center.
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
    val transf = RMatrix()
    transf.rotate(angle, vx, vy)
    transform(transf)
  }

  fun rotate(angle: Float) {
    val transf = RMatrix()
    transf.rotate(angle)
    transform(transf)
  }

  /**
   * Apply a rotation to the element, given an angle and optionally a rotation center.
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
    val transf = RMatrix()
    transf.rotate(angle, v)
    transform(transf)
  }

  /**
   * Apply a scale to the element, given scaling factors and optionally a scaling center.
   *
   * @param sx the scaling coefficient over the x axis
   * @param sy the scaling coefficient over the y axis
   * @param p  the position vector of the center of the scaling
   * @eexample RPoint_scale
   * @usage Geometry
   * @related transform ( )
   * @related translate ( )
   * @related rotate ( )
   */
  fun scale(sx: Float, sy: Float, p: RPoint) {
    val transf = RMatrix()
    transf.scale(sx, sy, p)
    transform(transf)
  }

  fun scale(sx: Float, sy: Float) {
    val transf = RMatrix()
    transf.scale(sx, sy)
    transform(transf)
  }

  /**
   * Apply a scale to the element, given scaling factors and optionally a scaling center.
   *
   * @param sx the scaling coefficient over the x axis
   * @param sy the scaling coefficient over the y axis
   * @param x  x coordinate of the position vector of the center of the scaling
   * @param y  y coordinate of the position vector of the center of the scaling
   * @eexample RPoint_scale
   * @usage Geometry
   * @related transform ( )
   * @related translate ( )
   * @related rotate ( )
   */
  fun scale(sx: Float, sy: Float, x: Float, y: Float) {
    val transf = RMatrix()
    transf.scale(sx, sy, x, y)
    transform(transf)
  }

  /**
   * Apply a scale to the element, given scaling factors and optionally a scaling center.
   *
   * @param s the scaling coefficient for a uniform scaling
   * @param p the position vector of the center of the scaling
   * @eexample RPoint_scale
   * @usage Geometry
   * @related transform ( )
   * @related translate ( )
   * @related rotate ( )
   */
  fun scale(s: Float, p: RPoint) {
    val transf = RMatrix()
    transf.scale(s, p)
    transform(transf)
  }

  fun scale(s: Float) {
    val transf = RMatrix()
    transf.scale(s)
    transform(transf)
  }

  /**
   * Apply a scale to the element, given scaling factors and optionally a scaling center.
   *
   * @param s the scaling coefficient for a uniform scaling
   * @param x x coordinate of the position vector of the center of the scaling
   * @param y y coordinate of the position vector of the center of the scaling
   * @eexample RPoint_scale
   * @usage Geometry
   * @related transform ( )
   * @related translate ( )
   * @related rotate ( )
   */
  fun scale(s: Float, x: Float, y: Float) {
    val transf = RMatrix()
    transf.scale(s, x, y)
    transform(transf)
  }

  /**
   * Apply a horizontal skew to the element, given skewing angle
   *
   * @param angle skewing angle
   * @eexample RMatrix_skewing
   * @usage Geometry
   * @related rotate ( )
   * @related scale ( )
   * @related translate ( )
   */
  fun skewX(angle: Float) {
    val transf = RMatrix()
    transf.skewY(angle)
    transform(transf)
  }

  /**
   * Apply a vertical skew to the element, given skewing angle
   *
   * @param angle skewing angle
   * @eexample RMatrix_skewing
   * @usage Geometry
   * @related rotate ( )
   * @related scale ( )
   * @related translate ( )
   */
  fun skewY(angle: Float) {
    val transf = RMatrix()
    transf.skewY(angle)
    transform(transf)
  }

  /**
   * Apply a shear to the element, given shearing factors
   *
   * @param shx x coordinate shearing
   * @param shy y coordinate shearing
   * @eexample RMatrix_translate
   * @usage Geometry
   * @related rotate ( )
   * @related scale ( )
   * @related translate ( )
   */
  fun shear(shx: Float, shy: Float) {
    val transf = RMatrix()
    transf.shear(shx, shy)
    transform(transf)
  }

  companion object {
    /**
     * @invisible
     */
    const val SHAPE = 0

    /**
     * @invisible
     */
    const val SUBSHAPE = 1

    /**
     * @invisible
     */
    const val COMMAND = 2

    /**
     * @invisible
     */
    const val POLYGON = 3

    /**
     * @invisible
     */
    const val CONTOUR = 4

    /**
     * @invisible
     */
    const val MESH = 5

    /**
     * @invisible
     */
    const val TRISTRIP = 6

    /**
     * @invisible
     */
    const val GROUP = 7

    /**
     * @invisible
     */
    const val UNKNOWN = 8
  }
}
