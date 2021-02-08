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

import geomerativefork.src.RCommand.Companion.createBezier3
import geomerativefork.src.RCommand.Companion.createLine
import geomerativefork.src.util.flatMapArray
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PGraphics

/**
 * RPath is a reduced interface for creating, holding and drawing contours. Paths are ordered lists of commands (RCommand) which define the outlines of shapes.  Paths can be self-intersecting.
 *
 * @eexample RPath
 * @usage Geometry
 * @related RCommand
 * @related RPolygon
 * @extended
 */
class RPath() : RGeomElem() {
  /**
   * @invisible
   */
  override val type = SUBSHAPE

  /**
   * Array of RCommand objects holding the commands of the path.
   *
   * @eexample commands
   * @related RCommand
   * @related commands.size ( )
   */
  @JvmField var commands: Array<RCommand> = arrayOf()

  /**
   * Last point from where to add the next command.  Initialized to null.
   *
   * @eexample lastPoint
   * @related RPoint
   * @invisible
   */
  var lastPoint: RPoint? = null
  @JvmField var closed = false

  /**
   * Create a new path, given an array of points.
   *
   * @param points the points of the new path
   * @eexample RPath
   */
  constructor(points: Array<RPoint>) : this() {
    lastPoint = points.firstOrNull()
    points.forEach { point -> addLineTo(point) }
  }

  /**
   * Create a new path, given the coordinates of the first point.
   *
   * @param x x coordinate of the first point of the new path
   * @param y y coordinate of the first point of the new path
   * @eexample RPath
   */
  constructor(x: Float, y: Float) : this() {
    lastPoint = RPoint(x, y)
  }

  /**
   * Create a new path, given the first point.
   *
   * @param p first point of the new path
   * @eexample RPath
   */
  constructor(p: RPoint) : this() {
    lastPoint = p
  }

  /**
   * Copy a path.
   *
   * @param s path to be copied
   * @eexample RPath
   */
  constructor(s: RPath) : this() {
    val numCommands = s.commands.size
    if (numCommands != 0) {
      var prevPoint = RPoint(s.commands.first().startPoint)
      for (i in 0 until numCommands) {
        append(RCommand(s.commands[i], prevPoint))
        prevPoint = commands[i].endPoint
      }
      lastPoint = prevPoint
    }
    closed = s.closed
    setStyle(s)
  }

  constructor(c: RCommand) : this() {
    addCommand(c)
  }

  override val handles: Array<RPoint>
    get() {
      val numCommands = commands.size
      if (numCommands == 0) return arrayOf()

      // Add the curve points of each command

      // First set the accumulated offset to the value of the inital offset
      RCommand.segmentAccOffset = RCommand.segmentOffset
      val result: MutableList<RPoint> = mutableListOf()

      for (i in 0 until numCommands) {
        val newPoints = commands[i].handles
        if (result.isEmpty()) {
          result.addAll(newPoints)
        } else if (newPoints.isNotEmpty()) {
          // Check for overlapping
          // Overlapping happens when the last point of the last command
          // is the same as the first point of the current command
          val lastp = result[result.size - 1]
          val firstp = newPoints[0]
          var overlap = 0
          if (lastp.x == firstp.x && lastp.y == firstp.y) {
            overlap = 1
          }

          result.addAll(newPoints.slice(0..(newPoints.size - overlap)))
        }
      }
      return result.toTypedArray()
    }

  /**
   * Use this to return the points on the curve.  It returns the points in the way of an array of RPoint.
   *
   * @return RPoint[], the vertices returned in an array.
   * @eexample getPoints
   */
  override val points: Array<RPoint>
    get() {
      // First set the accumulated offset to the value of the inital offset
      RCommand.segmentAccOffset = RCommand.segmentOffset
      var last: RPoint? = null
      return commands.flatMapArray { command ->
        val commandPoints = command.getPoints(false)
        return@flatMapArray when {
          commandPoints.isEmpty() -> arrayOf()
          last == null || last != command.startPoint -> commandPoints
          else -> commandPoints.sliceArray(1 until commandPoints.size)
        }.also { last = it.lastOrNull() ?: last }
      }
    }

  /**
   * Use this to return the points of each path of the path.  It returns the points in the way of an array of array of RPoint.
   *
   * @return RPoint[], the points returned in an array.
   * @eexample RGroup_getPoints
   */
  override val pointsInPaths: Array<Array<RPoint>>
    get() {
      return arrayOf(points)
    }

  /**
   * Use this to return the handles of each path of the path.  It returns the handles in the way of an array of array of RPoint.
   *
   * @return RPoint[], the handles returned in an array.
   * @eexample RGroup_getHandles
   */
  override val handlesInPaths: Array<Array<RPoint>>
    get() {
      return arrayOf(handles)
    }

  /**
   * Use this to return the tangents of each path of the path.  It returns the tangents in the way of an array of array of RPoint.
   *
   * @return RPoint[], the tangents returned in an array.
   * @eexample RGroup_getTangents
   */
  override val tangentsInPaths: Array<Array<RPoint>>
    get() {
      return arrayOf(tangents)
    }

  override fun calculateCurveLengths() {
    lenCurves = FloatArray(commands.size)
    lenCurve = 0f
    for (i in 0 until commands.size) {
      lenCurves[i] = commands[i].curveLength
      lenCurve += lenCurves[i]
    }
  }

  /**
   * Use this to return the tangents on the curve.  It returns the vectors in the way of an array of RPoint.
   *
   * @return RPoint[], the tangent vectors returned in an array.
   * @eexample getTangents
   */
  override val tangents: Array<RPoint>
    get() {
      val numCommands = commands.size
      if (numCommands == 0) {
        return arrayOf()
      }
      val result: MutableList<RPoint> = mutableListOf()
      for (i in 0 until numCommands) {
        val newTangents = commands[i].tangents
        if (newTangents.isNotEmpty()) {
          if (newTangents.size != 1) {
            val overlap = 1
            if (result.isEmpty()) {
              result.addAll(newTangents)
            } else {
              result.addAll(newTangents.slice(0 until newTangents.size))
            }
          }
        }
      }
      return result.toTypedArray()
    }

  /**
   * Use this to return the intersection points between this path and a command. Returns null if no intersection exists.
   *
   * @return RPoint[], the intersection points returned in an array.
   */
  fun intersectionPoints(other: RCommand): Array<RPoint> =
    commands.map { it.intersectionPoints(other).toList() }.flatten().toTypedArray()

  /**
   * Use this to return the intersection points between two paths. Returns null if no intersection exists.
   *
   * @return RPoint[], the intersection points returned in an array.
   */
  fun intersectionPoints(other: RPath): Array<RPoint> {
    if (commands.isEmpty()) return arrayOf()
    val result: MutableList<RPoint> = mutableListOf()

    other.commands.forEach { otherCommand ->
      commands.forEach { command ->
        result.addAll(command.intersectionPoints(otherCommand))
      }
    }

    return result.toTypedArray()
  }

  /**
   * Use this to find the closest or intersection points between this path and a command.
   *
   * @return RPoint[], the intersection points returned in an array.
   */
  fun closestPoints(other: RCommand): RClosest? {
    val numCommands = commands.size
    if (numCommands == 0) {
      return null
    }

    // TODO: get here the max value of an integer
    val minDist = 100000f
    val result = RClosest()
    for (i in 0 until numCommands) {
      val currResult = commands[i].closestPoints(other)
      result.update(currResult)
    }
    return result
  }

  /**
   * Use this to return the intersection points between two paths. Returns null if no intersection exists.
   *
   * @return RPoint[], the intersection points returned in an array.
   */
  fun closestPoints(other: RPath): RClosest? {
    if (commands.isEmpty()) return null

    // TODO: get here the max value of an integer
    val minDist = 100000f
    val result = RClosest()

    other.commands.forEach { otherCommand ->
      commands.forEach { command ->
        result.update(command.closestPoints(otherCommand))
      }
    }

    return result
  }

  /**
   * Return a specific point on the curve.  It returns the RPoint for a given advancement parameter t on the curve.
   *
   * @param t the parameter of advancement on the curve. t must have values between 0 and 1.
   * @return RPoint, the vertice returned.
   * @eexample getPoint
   */
  override fun getPoint(t: Float): RPoint? {
    val numCommands = commands.size
    if (commands.isEmpty()) return null
    if (t == 0.0f) return commands.first().getPoint(0f)
    if (t == 1.0f) return commands[numCommands - 1].getPoint(1f)
    val indAndAdv = indAndAdvAt(t)
    val indOfElement = indAndAdv[0].toInt()
    val advOfElement = indAndAdv[1]
    return commands[indOfElement].getPoint(advOfElement)
  }

  /**
   * Use this to return a specific tangent on the curve.  It returns the RPoint tangent for a given advancement parameter t on the curve.
   *
   * @param t float, the parameter of advancement on the curve. t must have values between 0 and 1.
   * @return RPoint, the vertice returned.
   * @eexample getPoint
   */
  override fun getTangent(t: Float): RPoint? {
    val numCommands = commands.size
    if (commands.isEmpty()) return null
    if (t == 0.0f) return commands.first().getTangent(0f)
    if (t == 1.0f) return commands[numCommands - 1].getTangent(1f)
    val indAndAdv = indAndAdvAt(t)
    val indOfElement = indAndAdv[0].toInt()
    val advOfElement = indAndAdv[1]

    return commands[indOfElement].getTangent(advOfElement)
  }

  /**
   * Use this to return a specific tangent on the curve.  It returns true if the point passed as a parameter is inside the path.  Implementation taken from: http://www.ecse.rpi.edu/Homepages/wrf/Research/Short_Notes/pnpoly.html
   *
   * @param p the point for which to test containement..
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

    // Test for containment in path
    val verts = points
    val nvert = verts.size
    var j = 0
    var c = false
    var i = 0
    j = nvert - 1
    while (i < nvert) {
      if (verts[i].y > testy != verts[j].y > testy && testx < (verts[j].x - verts[i].x) * (testy - verts[i].y) / (verts[j].y - verts[i].y) + verts[i].x) c =
        !c
      j = i++
    }
    return c
  }

  /**
   * Use this to insert a split point into the path.
   *
   * @param t float, the parameter of advancement on the curve. t must have values between 0 and 1.
   * @eexample insertHandle
   */
  fun insertHandle(t: Float) {
    if (t == 0f || t == 1f) {
      return
    }
    val indAndAdv = indAndAdvAt(t)
    val indOfElement = indAndAdv[0].toInt()
    val advOfElement = indAndAdv[1]

    // Split the affected command and reconstruct each of the shapes
    val splittedCommands = commands[indOfElement].split(advOfElement)
    if (splittedCommands.size < 2) {
      return
    }

    // Extract the splitted command
    extract(indOfElement)

    // Insert the splittedCommands
    insert(splittedCommands[1], indOfElement)
    insert(splittedCommands[0], indOfElement)

    // Clear the cache
    lenCurves = floatArrayOf()
    lenCurve = -1f
    return
  }

  /**
   * Use this to insert a split point into each command of the path.
   *
   * @param t float, the parameter of advancement on the curve. t must have values between 0 and 1.
   * @eexample insertHandleInPaths
   */
  fun insertHandleInPaths(t: Float) {
    if (t == 0f || t == 1f) {
      return
    }
    val numCommands = commands.size
    var i = 0
    while (i < numCommands * 2) {

      // Split the affected command and reconstruct each of the shapes
      val splittedCommands = commands[i].split(t)
      if (splittedCommands.size < 2) {
        return
      }

      // Extract the splitted command
      extract(i)

      // Insert the splittedCommands
      insert(splittedCommands[1], i)
      insert(splittedCommands[0], i)
      i += 2
    }

    // Clear the cache
    lenCurves = floatArrayOf()
    lenCurve = -1f
    return
  }

  /**
   * Use this to split a path into two separate new paths.
   *
   * @param t float, the parameter of advancement on the curve. t must have values between 0 and 1.
   * @return RPath[], an array of two RPath.
   * @eexample split
   */
  fun split(t: Float): Array<RPath> {
    val result = mutableListOf<RPath>()
    val numCommands = commands.size
    if (numCommands == 0) {
      return arrayOf()
    }
    if (t == 0.0f) {
      result[0] = RPath()
      result[1] = RPath(this)
      result[0].setStyle(this)
      result[1].setStyle(this)
      return result.toTypedArray()
    }
    if (t == 1.0f) {
      result[0] = RPath(this)
      result[1] = RPath()
      result[0].setStyle(this)
      result[1].setStyle(this)
      return result.toTypedArray()
    }
    val indAndAdv = indAndAdvAt(t)
    val indOfElement = indAndAdv[0].toInt()
    val advOfElement = indAndAdv[1]

    // Split the affected command and reconstruct each of the shapes
    val splittedCommands = commands[indOfElement].split(advOfElement)
    result[0] = RPath()
    for (i in 0 until indOfElement) {
      result[0].addCommand(RCommand(commands[i]))
    }
    result[0].addCommand(RCommand(splittedCommands[0]))
    result[0].setStyle(this)
    result[1] = RPath()
    for (i in indOfElement + 1 until commands.size) {
      result[1].addCommand(RCommand(commands[i]))
    }
    result[1].addCommand(RCommand(splittedCommands[1]))
    result[1].setStyle(this)
    return result.toTypedArray()
  }

  fun polygonize() {
    commands = RPath(points).commands
  }

  /**
   * Use this method to draw the path.
   *
   * @param g PGraphics, the graphics object on which to draw the path
   * @eexample drawPath
   */
  override fun draw(g: PGraphics) {
    commands.size

    // By default always draw with an adaptative segmentator
    val lastSegmentator = RCommand.segmentType
    RCommand.setSegmentator(RCommand.ADAPTATIVE)
    val points = points
    g.beginShape()
    for (i in points.indices) {
      g.vertex(points[i].x, points[i].y)
    }
    g.endShape(if (closed) PConstants.CLOSE else PConstants.OPEN)

    // Restore the user set segmentator
    RCommand.setSegmentator(lastSegmentator)
  }

  override fun draw(g: PApplet) {
    commands.size

    // By default always draw with an adaptative segmzentator
    val lastSegmentator = RCommand.segmentType
    RCommand.setSegmentator(RCommand.ADAPTATIVE)
    val points: Array<RPoint>? = points
    RCommand.setSegmentator(lastSegmentator)
    if (points == null) {
      return
    }
    g.beginShape()
    for (i in points.indices) {
      g.vertex(points[i].x, points[i].y)
    }
    g.endShape(if (closed) PConstants.CLOSE else PConstants.OPEN)

    // Restore the user set segmentator
    RCommand.setSegmentator(lastSegmentator)
  }

  /**
   * Use this method to add new commands to the contour.
   *
   * @eexample addCommand
   * @invisible
   */
  fun addCommand(p: RCommand) {
    append(p)
    lastPoint = commands.last().endPoint
  }

  /**
   * Add a new cubic bezier to the path. The first point of the bezier will be the last point added to the path.
   *
   * @param cp1 first control point
   * @param cp2 second control point
   * @param end end point
   * @eexample addBezierTo
   */
  fun addBezierTo(cp1: RPoint, cp2: RPoint, end: RPoint) {
    addCommand(RCommand.createBezier4(lastPoint!!, cp1, cp2, end))
  }

  /**
   * Add a new cubic bezier to the path. The first point of the bezier will be the last point added to the path.
   *
   * @param cp1x the x coordinate of the first control point
   * @param cp1y the y coordinate of the first control point
   * @param cp2x the x coordinate of the second control point
   * @param cp2y the y coordinate of the second control point
   * @param endx the x coordinate of the end point
   * @param endy the y coordinate of the end point
   * @eexample addBezierTo
   */
  fun addBezierTo(cp1x: Float, cp1y: Float, cp2x: Float, cp2y: Float, endx: Float, endy: Float) {
    val cp1 = RPoint(cp1x, cp1y)
    val cp2 = RPoint(cp2x, cp2y)
    val end = RPoint(endx, endy)
    addBezierTo(cp1, cp2, end)
  }

  /**
   * Add a new quadratic bezier to the path. The first point of the bezier will be the last point added to the path.
   *
   * @param cp1 first control point
   * @param end end point
   * @eexample addQuadTo
   */
  fun addQuadTo(cp1: RPoint, end: RPoint) {
    addCommand(createBezier3(lastPoint!!, cp1, end))
  }

  /**
   * Add a new quadratic bezier to the path. The first point of the bezier will be the last point added to the path.
   *
   * @param cp1x the x coordinate of the first control point
   * @param cp1y the y coordinate of the first control point
   * @param endx the x coordinate of the end point
   * @param endy the y coordinate of the end point
   * @eexample addQuadTo
   */
  fun addQuadTo(cp1x: Float, cp1y: Float, endx: Float, endy: Float) {
    val cp1 = RPoint(cp1x, cp1y)
    val end = RPoint(endx, endy)
    addQuadTo(cp1, end)
  }

  /**
   * Add a new line to the path. The first point of the line will be the last point added to the path.
   *
   * @param end end point
   * @eexample addLineTo
   */
  fun addLineTo(end: RPoint) {

    if (lastPoint == null) lastPoint = end

    lastPoint?.let { addCommand(createLine(it, end)) }
  }

  /**
   * Add a new line to the path. The first point of the line will be the last point added to the path.
   *
   * @param endx the x coordinate of the end point
   * @param endy the y coordinate of the end point
   * @eexample addLineTo
   */
  fun addLineTo(endx: Float, endy: Float) {
    val end = RPoint(endx, endy)
    addLineTo(end)
  }

  fun addClose() {
    if (commands.size < 2) return

    val lastCommandPoint = commands.last().endPoint
    val firstCommandPoint = commands.first().startPoint
    if (lastCommandPoint == firstCommandPoint) {
      commands[commands.size - 1] = RCommand(commands.last(), ep = RPoint(firstCommandPoint))
      lastPoint = commands.last().endPoint
    } else {
      addLineTo(RPoint(firstCommandPoint))
    }
    closed = true
  }

  /**
   * @invisible
   */
  override fun toPolygon(): RPolygon {
    return toShape().toPolygon()
  }

  /**
   * @invisible
   */
  override fun toShape(): RShape {
    return RShape(this)
  }

  /**
   * @invisible
   */
  override fun toMesh(): RMesh {
    return toPolygon().toMesh()
  }

  override fun print() {
    for (i in 0 until commands.size) {
      var type = ""
      when (commands[i].type) {
        RCommand.LINETO -> type = "LINETO"
        RCommand.CUBICBEZIERTO -> type = "BEZIERTO"
        RCommand.QUADBEZIERTO -> type = "QUADBEZIERTO"
      }
      println("cmd type: $type")
      print("start point: ")
      commands[i].startPoint.print()
      print("\n")
      print("end point: ")
      commands[i].endPoint.print()
      print("\n")
      if (commands[i].controlPoints != null) {
        println("control points: ")
        for (j in commands[i].controlPoints.indices) {
          commands[i].controlPoints[j].print()
          print(" ")
          print("\n")
        }
      }
      print("\n")
    }
  }

  /**
   * Use this method to transform the shape.
   *
   * @param m RMatrix, the matrix defining the affine transformation
   * @eexample RPath_transform
   * @related draw ( )
   */
  // OPT: not transform the EndPoint since it's equal to the next StartPoint
  /*
    public void transform(RMatrix m){
    RPoint[] ps = getHandles();
    if(ps!=null){
    for(int i=0;i<ps.length;i++){
    ps[i].transform(m);
    }
    }

    int numCommands = commands.size;
    if(numCommands!=0){
    commands.first().startPoint.transform(m);
    for(int i=0;i<numCommands-1;i++){
    for(int j=0;j<commands[i].controlPoints.size;j++){
    commands[i].controlPoints[j].transform(m);
    }
    commands[i].endPoint.transform(m);
    }
    }

    }
  */
  private fun indAndAdvAt(t: Float): FloatArray {
    var indOfElement = 0
    val lengthsCurves = curveLengths
    val lengthCurve = curveLength

    /* Calculate the amount of advancement t mapped to each command */
    /* We use a simple algorithm where we give to each command the same amount of advancement */
    /* A more useful way would be to give to each command an advancement proportional to the length of the command */
    /* Old method with uniform advancement per command
       float advPerCommand;
       advPerCommand = 1F / numPaths;
       indOfElement = (int)(Math.floor(t / advPerCommand)) % numPaths;
       advOfElement = (t*numPaths - indOfElement);
    */
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

  private fun append(nextcommand: RCommand) {
    commands += nextcommand
  }

  @Throws(RuntimeException::class)
  private fun insert(newcommand: RCommand, i: Int) {
    if (i < 0) {
      throw RuntimeException("Negative values for indexes are not valid.")
    }

    if (i > commands.size) {
      throw RuntimeException(
        "Index out of the bounds.  You are trying to insert an element with an index higher than the number of commands in the group.")
    }
    commands += newcommand
  }

  @Throws(RuntimeException::class)
  private fun extract(i: Int) {
    if (commands.isEmpty()) {
      throw RuntimeException("The group is empty. No commands to remove.")
    }
    if (i < 0) {
      throw RuntimeException("Negative values for indexes are not valid.")
    }
    if (i > commands.size - 1) {
      throw RuntimeException(
        "Index out of the bounds of the group.  You are trying to erase an element with an index higher than the number of commands in the group.")
    }
    commands = when {
      commands.size == 1 -> arrayOf()
      i == 0 -> commands.sliceArray(1 until commands.size)
      i == commands.size - 1 -> commands.sliceArray(0 until commands.size)
      else -> commands.sliceArray(0..i) + commands.sliceArray(i + 1 until commands.size)
    }
  }

  override fun toString(): String {
    return "RPath(length = ${points.size}, start = ${points.firstOrNull()}, end = ${points.lastOrNull()})"
  }

  fun clone() = RPath(this)
}
