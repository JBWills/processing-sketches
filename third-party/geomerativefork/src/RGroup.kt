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
import processing.core.PGraphics

/**
 * RGroup is a holder for a group of geometric elements that can be drawn and transformed, such as shapes, polygons or meshes.
 * @usage geometry
 * @extended
 */
class RGroup : RGeomElem {
  /**
   * Use this method to get the type of element this is.
   * @eexample RPolygon_getType
   * @return int, will allways return RGeomElem.POLYGON
   */
  /**
   * @invisible
   */
  override var type = GROUP

  /**
   * Array of RGeomElem objects holding the elements of the group. When accessing theses elements we must cast them to their class in order to get all the functionalities of each representation. e.g. RShape s = group.elements[i].toShape()  If the element cannot be converted to the target class it will throw a RuntimeException, to ignore these, use try-catch syntax.
   * @eexample RGroup_elements
   * @related RShape
   * @related RPolygon
   * @related RMesh
   * @related countElements ( )
   * @related addElement ( )
   * @related removeElement ( )
   */
  @JvmField var elements: Array<RGeomElem> = arrayOf()

  /**
   * Use this method to create a new empty group.
   * @eexample RGroup
   */
  constructor() {
    elements = arrayOf()
  }

  /**
   * Use this method to create a copy of a group.
   * @eexample RGroup
   */
  constructor(grp: RGroup) {
    grp.elements.forEach { element ->
      addElement(when (element.type) {
        MESH -> RMesh((element as RMesh))
        GROUP -> RGroup((element as RGroup))
        POLYGON -> RPolygon((element as RPolygon))
        SHAPE -> RShape((element as RShape))
        else -> throw IllegalArgumentException("invalid element type: $element.type")
      })
    }
    setStyle(grp)
  }

  /**
   * Use this method to get the centroid of the element.
   * @eexample RGroup_getCentroid
   * @return RPoint, the centroid point of the element
   * @related getBounds ( )
   * @related getCenter ( )
   */
  override val centroid: RPoint?
    get() {
      var bestCentroid: RPoint? = null
      var bestArea = Float.NEGATIVE_INFINITY
      if (elements.isNotEmpty()) {
        elements.forEach { element ->
          val area = element.area
          if (area > bestArea) {
            bestArea = area
            bestCentroid = element.centroid
          }
        }
        return bestCentroid
      }
      return null
    }

  override fun print() {
    println("group: ")
    for (i in 0 until elements.size) {
      println("---  $i ---")
      elements[i].print()
      println("---------------")
    }
  }

  /**
   * Use this method to draw the group.  This will draw each element at a time, without worrying about intersections or holes.  This is the main difference between having a shape with multiple paths and having a group with multiple shapes.
   * @eexample RGroup_draw
   * @param g PGraphics, the graphics object on which to draw the group
   */
  override fun draw(g: PGraphics) {
    if (!RG.ignoreStyles) {
      saveContext(g)
      setContext(g)
    }
    for (i in 0 until elements.size) {
      elements[i].draw(g)
    }
    if (!RG.ignoreStyles) {
      restoreContext(g)
    }
  }

  override fun draw(a: PApplet) {
    if (!RG.ignoreStyles) {
      saveContext(a)
      setContext(a)
    }
    for (i in 0 until elements.size) {
      elements[i].draw(a)
    }
    if (!RG.ignoreStyles) {
      restoreContext(a)
    }
  }

  /**
   * Use this method to add a new element.
   * @eexample RGroup_addElement
   * @param elem RGeomElem, any kind of RGeomElem to add.  It accepts the classes RShape, RPolygon and RMesh.
   * @related removeElement ( )
   */
  fun addElement(elem: RGeomElem) {
    this.append(elem)
  }

  /**
   * Use this method to add a new element.
   * @eexample RGroup_addGroup
   * @param grupo RGroup, A group of elements to add to this group.
   * @related removeElement ( )
   */
  fun addGroup(grupo: RGroup) {
    for (i in 0 until grupo.elements.size) {
      addElement(grupo.elements[i])
    }
  }

  /**
   * Use this method to remove an element.
   * @eexample RGroup_removeElement
   * @param i int, the index of the element to remove from the group.
   * @related addElement ( )
   */
  @Throws(RuntimeException::class)
  fun removeElement(i: Int) {
    extract(i)
  }

  /**
   * Use this method to get a new group whose elements are the corresponding meshes of the elements in the current group.  This can be used for increasing performance in exchange of losing abstraction.
   * @eexample RGroup_toMeshGroup
   * @return RGroup, the new group made of RMeshes
   * @related toPolygonGroup ( )
   * @related toShapeGroup ( )
   */
  @Throws(RuntimeException::class)
  fun toMeshGroup(): RGroup {
    val result = RGroup()
    for (i in 0 until elements.size) {
      result.addElement(elements[i].toMesh())
    }
    return result
  }

  /**
   * Use this method to get a new group whose elements are the corresponding polygons of the elements in the current group.  At this moment there is no implementation for transforming a mesh to a polygon so applying this method to groups holding mesh elements will generate an exception.
   * @eexample RGroup_toPolygonGroup
   * @return RGroup, the new group made of RPolygons
   * @related toMeshGroup ( )
   * @related toShapeGroup ( )
   */
  @Throws(RuntimeException::class)
  fun toPolygonGroup(): RGroup {
    val result = RGroup()
    for (i in 0 until elements.size) {
      val element = elements[i]
      if (element.type == GROUP) {
        val newElement: RGeomElem = (element as RGroup?)!!.toPolygonGroup()
        result.addElement(newElement)
      } else {
        result.addElement(element.toPolygon())
      }
    }
    result.setStyle(this)
    return result
  }

  /**
   * Use this method to get a new group whose elements are all the corresponding shapes of the elements in the current group.  At this moment there is no implementation for transforming a mesh or a polygon to a shape so applying this method to groups holding mesh or polygon elements will generate an exception.
   * @eexample RGroup_toShapeGroup
   * @return RGroup, the new group made of RShapes
   * @related toMeshGroup ( )
   * @related toPolygonGroup ( )
   */
  @Throws(RuntimeException::class)
  fun toShapeGroup(): RGroup {
    val result = RGroup()
    for (i in 0 until elements.size) {
      val element = elements[i]
      if (element.type == GROUP) {
        val newElement: RGeomElem = (element as RGroup?)!!.toShapeGroup()
        result.addElement(newElement)
      } else {
        result.addElement(element.toShape())
      }
    }
    result.setStyle(this)
    return result
  }

  /**
   * @invisible
   */
  @Throws(RuntimeException::class)
  override fun toMesh(): RMesh {
    //throw new RuntimeException("Transforming a Group to a Mesh is not yet implemented.");
    val meshGroup = toMeshGroup()
    val result = RMesh()
    for (i in 0 until elements.size) {
      val currentMesh = meshGroup.elements[i] as RMesh?
      for (j in 0 until currentMesh!!.strips.size) {
        result.addStrip(currentMesh.strips[j])
      }
    }
    result.setStyle(this)
    return result
  }

  /**
   * @invisible
   */
  @Throws(RuntimeException::class)
  override fun toPolygon(): RPolygon {
    //throw new RuntimeException("Transforming a Group to a Polygon is not yet implemented.");
    //RGroup polygonGroup = toPolygonGroup();
    val result = RPolygon()
    for (i in 0 until elements.size) {
      val currentPolygon = elements[i].toPolygon()
      for (j in 0 until currentPolygon.contours.size) {
        result.addContour(currentPolygon.contours[j])
      }
    }
    result.setStyle(this)
    return result
  }

  /**
   * @invisible
   */
  @Throws(RuntimeException::class)
  override fun toShape(): RShape {
    //throw new RuntimeException("Transforming a Group to a Shape is not yet implemented.");
    val result = RShape()
    for (i in 0 until elements.size) {
      val currentShape = elements[i].toShape()
      for (j in 0 until currentShape.paths.size) {
        result.addPath(currentShape.paths[j])
      }
    }
    result.setStyle(this)
    return result
  }

  /**
   * Use this to return the points of the group.  It returns the points as an array of RPoint.
   * @eexample RGroup_getHandles
   * @return RPoint[], the points returned in an array.
   */
  override val handles: Array<RPoint>
    get() = elements.flatMap { it.handles.toList() }.toTypedArray()

  override fun getPoint(t: Float): RPoint? {
    val indAndAdv = indAndAdvAt(t)
    val indOfElement = indAndAdv[0].toInt()
    val advOfElement = indAndAdv[1]
    return elements[indOfElement].getPoint(advOfElement)
  }

  /**
   * Use this to return the points of the group.  It returns the points as an array of RPoint.
   * @eexample RGroup_getPoints
   * @return RPoint[], the points returned in an array.
   */
  override val points: Array<RPoint>
    get() {
      RCommand.segmentAccOffset = RCommand.segmentOffset
      return elements.flatMap { it.points.toList() }.toTypedArray()
    }

  override fun getTangent(t: Float): RPoint? {
    val indAndAdv = indAndAdvAt(t)
    val indOfElement = indAndAdv[0].toInt()
    val advOfElement = indAndAdv[1]
    return elements[indOfElement].getTangent(advOfElement)
  }

  /**
   * Use this to return the points of the group.  It returns the points as an array of RPoint.
   * @eexample RGroup_getPoints
   * @return RPoint[], the points returned in an array.
   */
  override val tangents: Array<RPoint>
    get() = elements.flatMap { it.tangents.toList() }.toTypedArray()

  /**
   * Use this to return the points of each path of the group.  It returns the points as an array of arrays of RPoint.
   * @eexample RGroup_getPoints
   * @return RPoint[], the points returned in an array.
   */
  override val pointsInPaths: Array<Array<RPoint>>
    get() = elements.flatMap { it.pointsInPaths.toList() }.toTypedArray()

  override val handlesInPaths: Array<Array<RPoint>>
    get() = elements.flatMap { it.handlesInPaths.toList() }.toTypedArray()

  override val tangentsInPaths: Array<Array<RPoint>>
    get() = elements.flatMap { it.tangentsInPaths.toList() }.toTypedArray()

  /**
   * Use this to return a specific tangent on the curve.  It returns true if the point passed as a parameter is inside the group.
   * @param p  the point for which to test containment..
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

    // Test for containment in elements
    var result = false
    for (i in 0 until elements.size) {
      result = result or elements[i].contains(p)
    }
    return result
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
    val indAndAdv = FloatArray(2)
    indAndAdv[0] = indOfElement.toFloat()
    indAndAdv[1] = advOfElement
    return indAndAdv
  }

  fun split(t: Float): Array<RGroup> {
    val result = when (t) {
      0.0f -> {
        val x = arrayOf(RGroup(), RGroup(this))
        x[0].setStyle(this)
        x[1].setStyle(this)
        return x
      }
      1.0f -> {
        val x = arrayOf(RGroup(this), RGroup())
        x[0].setStyle(this)
        x[1].setStyle(this)
        return x
      }
      else -> arrayOf(RGroup(), RGroup())
    }

    val indAndAdv = indAndAdvAt(t)
    val indOfElement = indAndAdv[0].toInt()
    val advOfElement = indAndAdv[1]

    // Add the elements before the cut point
    for (i in 0 until indOfElement) {
      when (elements[i].type) {
        MESH -> result[0].addElement(RMesh((elements[i] as RMesh?)!!))
        GROUP -> result[0].addElement(RGroup(elements[i] as RGroup))
        POLYGON -> result[0].addElement(RPolygon(elements[i] as RPolygon))
        SHAPE -> result[0].addElement(RShape((elements[i] as RShape)))
      }
    }

    // Add the cut point element cutted
    val element = elements[indOfElement]
    when (element.type) {
      GROUP -> {
        val splittedGroups = (element as RGroup).split(advOfElement)
        result[0].addElement(RGroup(splittedGroups[0]))
        result[1].addElement(RGroup(splittedGroups[1]))
      }
      SHAPE -> {
        val splittedShapes = (element as RShape?)!!.split(advOfElement)
        result[0].addElement(RShape(splittedShapes[0]))
        result[1].addElement(RShape(splittedShapes[1]))
      }
    }

    // Add the elements after the cut point    
    for (i in indOfElement + 1 until elements.size) {
      when (elements[i].type) {
        MESH -> result[1].addElement(RMesh((elements[i] as RMesh?)!!))
        GROUP -> result[1].addElement(RGroup(elements[i] as RGroup))
        POLYGON -> result[1].addElement(RPolygon(elements[i] as RPolygon))
        SHAPE -> result[1].addElement(RShape(elements[i] as RShape))
      }
    }
    result[0].setStyle(this)
    result[1].setStyle(this)
    return result
  }

  fun splitPaths(t: Float): Array<RGroup> {
    val result = Array(2) { RGroup() }
    for (i in 0 until elements.size) {
      val element = elements[i]
      when (element.type) {
        GROUP -> {
          val splittedGroups = (element as RGroup).splitPaths(t)
          result[0].addElement(splittedGroups[0])
          result[1].addElement(splittedGroups[1])

        }
        SHAPE -> {
          val splittedShapes = (element as RShape).splitPaths(t)
          result[0].addElement(splittedShapes[0])
          result[1].addElement(splittedShapes[1])
        }
      }
    }
    result[0].setStyle(this)
    result[1].setStyle(this)
    return result
  }

  /**
   * Use this to insert a split point into each command of the group.
   * @eexample insertHandleInPaths
   * @param t float, the parameter of advancement on the curve. t must have values between 0 and 1.
   */
  fun insertHandleInPaths(t: Float) {
    if (t == 0f || t == 1f) {
      return
    }
    for (i in 0 until elements.size) {
      val element = elements[i]
      when (element.type) {
        GROUP -> (element as RGroup).insertHandleInPaths(t)
        SHAPE -> (element as RShape).insertHandleInPaths(t)
      }
    }
    return
  }

  override fun calculateCurveLengths() {
    lenCurves = FloatArray(elements.size)
    lenCurve = 0f
    for (i in 0 until elements.size) {
      lenCurves[i] = elements[i].curveLength
      lenCurve += lenCurves[i]
    }
  }

  /**
   * Use this method to adapt a group of of figures to a group.
   * @eexample RGroup_adapt
   * @param grp  the path to which to adapt
   */
  @JvmOverloads
  @Throws(RuntimeException::class)
  fun adapt(
    grp: RGroup,
    wght: Float = RG.adaptorScale,
    lngthOffset: Float = RG.adaptorLengthOffset,
  ) {
    val c = bounds
    val xmin = c.minX
    val xmax = c.maxX
    val ymax = c.maxY
    val numElements = elements.size
    when (RG.adaptorType) {
      RG.BYPOINT -> {
        var i = 0
        while (i < numElements) {
          val elem = elements[i]
          val ps = elem.handles
          if (ps != null) {
            var k = 0
            while (k < ps.size) {
              val px = ps[k].x
              val py = ps[k].y
              val t = ((px - xmin) / (xmax - xmin) + lngthOffset) % 1.001f
              val amp = ymax - py
              val tg = grp.getTangent(t)
              val p = grp.getPoint(t)
              val angle =
                Math.atan2(tg!!.y.toDouble(), tg.x.toDouble()).toFloat() - Math.PI.toFloat() / 2f
              ps[k].x = p!!.x + wght * amp * Math.cos(angle.toDouble()).toFloat()
              ps[k].y = p.y + wght * amp * Math.sin(angle.toDouble()).toFloat()
              k++
            }
          }
          i++
        }
      }
      RG.BYELEMENTPOSITION -> {
        var i = 0
        while (i < numElements) {
          val elem = elements[i]
          val elemc = elem.bounds
          val px = (elemc.bottomRight.x + elemc.topLeft.x) / 2f
          val py = (elemc.bottomRight.y - elemc.topLeft.y) / 2f
          val t = ((px - xmin) / (xmax - xmin) + lngthOffset) % 1f
          val tg = grp.getTangent(t)
          val p = grp.getPoint(t)
          val angle = Math.atan2(tg!!.y.toDouble(), tg.x.toDouble()).toFloat()
          val pletter = RPoint(px, py)
          p!!.sub(pletter)
          val mtx = RMatrix()
          mtx.translate(p)
          mtx.rotate(angle, pletter)
          mtx.scale(wght, pletter)
          elem.transform(mtx)
          i++
        }
      }
      RG.BYELEMENTINDEX -> {
        var i = 0
        while (i < numElements) {
          val elem = elements[i]
          val elemc = elem.bounds
          val px = (elemc.bottomRight.x + elemc.topLeft.x) / 2f
          val py = (elemc.bottomRight.y - elemc.topLeft.y) / 2f
          val t = (i.toFloat() / numElements.toFloat() + lngthOffset) % 1f
          val tg = grp.getTangent(t)
          val p = grp.getPoint(t)
          val angle = Math.atan2(tg!!.y.toDouble(), tg.x.toDouble()).toFloat()
          val pletter = RPoint(px, py)
          p!!.sub(pletter)
          val mtx = RMatrix()
          mtx.translate(p)
          mtx.rotate(angle, pletter)
          mtx.scale(wght, pletter)
          elem.transform(mtx)
          i++
        }
      }
      else -> throw RuntimeException(
        "Unknown adaptor type : " + RG.adaptorType + ". The method RG.setAdaptor() only accepts RG.BYPOINT or RG.BYELEMENT as parameter values.")
    }
  }

  fun adapt(shp: RShape) {
    val grp = RGroup()
    grp.addElement(shp)
    adapt(grp)
  }

  fun adapt(shp: RShape, wght: Float, lngthOffset: Float) {
    val grp = RGroup()
    grp.addElement(shp)
    adapt(grp, wght, lngthOffset)
  }

  fun polygonize() {
    val grp = toPolygonGroup().toShapeGroup()
    elements = grp.elements
  }

  private fun append(elem: RGeomElem) {
    elements += elem
  }

  @Throws(RuntimeException::class)
  private fun extract(i: Int) {
    val newelements: Array<RGeomElem>

    if (i < 0) {
      throw RuntimeException("Negative values for indexes are not valid.")
    }
    if (i > elements.size - 1) {
      throw RuntimeException(
        "Index out of the bounds of the group.  You are trying to erase an element with an index higher than the number of elements in the group.")
    }
    if (elements.size == 1) {
      elements = arrayOf()
    } else if (i == 0) {
      elements = elements.sliceArray(0..i) + elements.sliceArray((i + 1)..elements.size)
    }
  }
}