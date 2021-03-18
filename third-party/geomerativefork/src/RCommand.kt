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
@file:Suppress("SpellCheckingInspection", "unused", "UNUSED_PARAMETER")

package geomerativefork.src

import geomerativefork.src.RPoint.Companion.times
import geomerativefork.src.util.bound
import processing.core.PApplet
import processing.core.PGraphics
import util.with
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.min
import kotlin.math.sqrt

/**
 *
 */
class RCommand(

  /**
   * Use this to return the start point of the curve.
   * @eexample getStartPoint
   * @return RPoint, the start point of the curve.
   * @invisible
   */
  val startPoint: RPoint,
  /**
   * Use this to return the end point of the curve.
   * @eexample getEndPoint
   * @return RPoint, the end point of the curve.
   * @invisible
   */
  val endPoint: RPoint,
  /**
   * Use this to return the control points of the curve.  It returns the points in the way of an array of RPoint.
   * @eexample getControlPoints
   * @return RPoint[], the control points returned in an array.
   * @invisible
   */
  var controlPoints: Array<RPoint> = arrayOf(),

  /**
   * Use this to return the command type.
   * @eexample getCommandType
   * @return int, an integer which can take the following values: RCommand.LINETO, RCommand.QUADBEZIERTO, RCommand.CUBICBEZIERTO.
   */
  override var type: Int = COMMAND,
) : RGeomElem() {

  var curvePoints: Array<RPoint> = arrayOf()
  var oldSegmentType = UNIFORMLENGTH

  /* Parameters for ADAPTATIVE (dependent of the PGraphics on which drawing) */
  var oldSegmentCollinearityEpsilon = 1.192092896e-07f
  var oldSegmentAngleTolEpsilon = 0.01f
  var oldSegmentGfxStrokeWeight = 1.0f
  var oldSegmentGfxScale = 1.0f
  var oldSegmentApproxScale = 1.0f
  var oldSegmentDistTolSqr = 0.25f
  var oldSegmentDistTolMnhttn = 4.0f
  var oldSegmentAngleTol = 0.0f
  var oldSegmentCuspLimit = 0.0f

  /* Parameters for UNIFORMLENGTH (dependent of the PGraphics on which drawing) */
  var oldSegmentLength = 4.0f
  var oldSegmentOffset = 0.0f
  var oldSegmentAccOffset = 0.0f

  /* Parameters for UNIFORMSTEP */
  var oldSegmentSteps = 0
  var oldSegmentLines = false

  /**
   * Make a copy of another RCommand object.  This can be useful when wanting to transform one but at the same time keep the original.
   * @param c  the object of which to make the copy
   * @invisible
   */
  constructor(c: RCommand) : this(c.startPoint, c.endPoint, c.controlPoints.clone(), c.type)

  /**
   * Make a copy of another RCommand object with a specific start point.
   * @param c  the object of which to make the copy
   * @param sp  the start point of the command to be created
   */
  constructor(c: RCommand, sp: RPoint? = null, ep: RPoint? = null) : this(
    sp ?: c.startPoint, ep
      ?: c.endPoint, c.controlPoints.clone(), c.type
  )

  /**
   * Create a LINETO command object with specific start and end points.
   * @param sp  the start point of the command to be created
   * @param ep  the end point of the command to be created
   */
  constructor(sp: RPoint, ep: RPoint) : this(sp, ep, arrayOf(), LINETO)

  /**
   * Create a LINETO command object with specific start and end point coordinates.
   * @param spx  the x coordinate of the start point of the command to be created
   * @param spy  the y coordinate of the start point of the command to be created
   * @param epx  the x coordinate of the end point of the command to be created
   * @param epy  the y coordinate of the end point of the command to be created
   */
  constructor(
    spx: Float, spy: Float, epx: Float, epy: Float,
  ) : this(RPoint(spx, spy), RPoint(epx, epy))

  /**
   * Create a QUADBEZIERTO command object with specific start, control and end point coordinates.
   * @param sp  the start point of the command to be created
   * @param cp1  the first control point of the command to be created
   * @param ep  the end point of the command to be created
   */
  constructor(sp: RPoint, cp1: RPoint, ep: RPoint) : this(sp, ep, arrayOf(cp1), QUADBEZIERTO)

  /**
   * Create a QUADBEZIERTO command object with specific start, control and end point coordinates.
   * @param spx  the x coordinate of the start point of the command to be created
   * @param spy  the y coordinate of the start point of the command to be created
   * @param cp1x  the x coordinate of the first control point of the command to be created
   * @param cp1y  the y coordinate of the first control point of the command to be created
   * @param epx  the x coordinate of the end point of the command to be created
   * @param epy  the y coordinate of the end point of the command to be created
   */
  constructor(
    spx: Float, spy: Float, cp1x: Float, cp1y: Float, epx: Float, epy: Float,
  ) : this(RPoint(spx, spy), RPoint(cp1x, cp1y), RPoint(epx, epy))

  /**
   * Create a CUBICBEZIERTO command object with specific start, control and end point coordinates.
   * @param sp  the start point of the command to be created
   * @param cp1  the first control point of the command to be created
   * @param cp2  the second control point of the command to be created
   * @param ep  the end point of the command to be created
   */
  constructor(
    sp: RPoint, cp1: RPoint, cp2: RPoint, ep: RPoint,
  ) : this(sp, ep, arrayOf(cp1, cp2), QUADBEZIERTO)

  /**
   * Create a CUBICBEZIERTO command object with specific start, control and end point coordinates.
   * @param spx  the x coordinate of the start point of the command to be created
   * @param spy  the y coordinate of the start point of the command to be created
   * @param cp1x  the x coordinate of the first control point of the command to be created
   * @param cp1y  the y coordinate of the first control point of the command to be created
   * @param cp2x  the x coordinate of the second control point of the command to be created
   * @param cp2y  the y coordinate of the second control point of the command to be created
   * @param epx  the x coordinate of the end point of the command to be created
   * @param epy  the y coordinate of the end point of the command to be created
   */
  constructor(
    spx: Float, spy: Float, cp1x: Float, cp1y: Float, cp2x: Float, cp2y: Float, epx: Float,
    epy: Float,
  ) : this(RPoint(spx, spy), RPoint(cp1x, cp1y), RPoint(cp2x, cp2y), RPoint(epx, epy))

  /**
   * @invisible
   */
  override fun toShape(): RShape {
    return RShape(RPath(this))
  }

  protected fun saveSegmentatorContext() {
    oldSegmentType = segmentType

    /* Parameters for ADAPTATIVE (dependent of the PGraphics on which drawing) */
    oldSegmentGfxStrokeWeight = segmentGfxStrokeWeight
    oldSegmentGfxScale = segmentGfxScale
    oldSegmentApproxScale = segmentApproxScale
    oldSegmentDistTolSqr = segmentDistTolSqr
    oldSegmentDistTolMnhttn = segmentDistTolMnhttn
    oldSegmentAngleTol = segmentAngleTol
    oldSegmentCuspLimit = segmentCuspLimit

    /* Parameters for UNIFORMLENGTH (dependent of the PGraphics on which drawing) */
    oldSegmentLength = commandSegmentLength
    oldSegmentOffset = segmentOffset
    oldSegmentAccOffset = segmentAccOffset

    /* Parameters for UNIFORMSTEP */
    oldSegmentSteps = segmentSteps
    oldSegmentLines = segmentLines
  }

  protected fun restoreSegmentatorContext() {
    segmentType = oldSegmentType

    /* Parameters for ADAPTATIVE (dependent of the PGraphics on which drawing) */
    segmentGfxStrokeWeight = oldSegmentGfxStrokeWeight
    segmentGfxScale = oldSegmentGfxScale
    segmentApproxScale = oldSegmentApproxScale
    segmentDistTolSqr = oldSegmentDistTolSqr
    segmentDistTolMnhttn = oldSegmentDistTolMnhttn
    segmentAngleTol = oldSegmentAngleTol
    segmentCuspLimit = oldSegmentCuspLimit

    /* Parameters for UNIFORMLENGTH (dependent of the PGraphics on which drawing) */
    commandSegmentLength = oldSegmentLength
    segmentOffset = oldSegmentOffset
    segmentAccOffset = oldSegmentAccOffset

    /* Parameters for UNIFORMSTEP */
    segmentSteps = oldSegmentSteps
    segmentLines = oldSegmentLines
  }

  /**
   * Use this to return the points on the curve.  It returns the points in the way of an array of RPoint.
   * @eexample getPoints
   * @return RPoint[], the vertices returned in an array.
   */
  override val points: Array<RPoint>
    get() = getPoints(true)

  fun getPoints(resetSegmentator: Boolean): Array<RPoint> {
    if (resetSegmentator) {
      saveSegmentatorContext()
      segmentOffset = 0f
      segmentAccOffset = 0f
    }
    var result: Array<RPoint> = arrayOf()
    when (segmentType) {
      ADAPTATIVE -> when (type) {
        LINETO -> {
          result = arrayOf(startPoint, endPoint)
        }
        QUADBEZIERTO -> {
          quadBezierAdaptative()
          result = curvePoints
          curvePoints = arrayOf()
        }
        CUBICBEZIERTO -> {
          cubicBezierAdaptative()
          result = curvePoints
          curvePoints = arrayOf()
        }
      }
      UNIFORMLENGTH -> when (type) {
        LINETO -> {
          lineUniformLength()
          result = curvePoints
          curvePoints = arrayOf()
        }
        QUADBEZIERTO -> {
          quadBezierUniformLength()
          result = curvePoints
          curvePoints = arrayOf()
        }
        CUBICBEZIERTO -> {
          cubicBezierUniformLength()
          result = curvePoints
          curvePoints = arrayOf()
        }
      }
      UNIFORMSTEP -> when (type) {
        LINETO -> if (segmentLines) {
          lineUniformStep()
          result = curvePoints
          curvePoints = arrayOf()
        } else {
          result = arrayOf(startPoint, endPoint)
        }
        QUADBEZIERTO -> {
          quadBezierUniformStep()
          result = curvePoints
          curvePoints = arrayOf()
        }
        CUBICBEZIERTO -> {
          cubicBezierUniformStep()
          result = curvePoints
          curvePoints = arrayOf()
        }
      }
    }
    if (resetSegmentator) {
      restoreSegmentatorContext()
    }
    return result
  }

  /**
   * Use this to return a specific point on the curve.  It returns the RPoint for a given advancement parameter t on the curve.
   * @eexample getPoint
   * @param t float, the parameter of advancement on the curve. t must have values between 0 and 1.
   * @return RPoint, the vertice returned.
   */
  override fun getPoint(t: Float): RPoint {
    /* limit the value of t between 0 and 1 */
    val boundT = when {
      t > 1f -> 1f
      t < 0f -> 0f
      else -> t
    }

    return when (type) {
      LINETO -> return startPoint + ((endPoint - startPoint) * boundT)
      QUADBEZIERTO -> {
        /* calculate the polynomial coefficients */
        val coefficientB = controlPoints[0] - startPoint
        val coefficientA = endPoint - controlPoints[0] - coefficientB

        /* calculate the curve point at parameter value t */
        val tSquared = boundT * boundT
        val tDoubled = 2f * boundT
        startPoint + (coefficientA * tSquared) + (coefficientB * tDoubled)
      }
      CUBICBEZIERTO -> {
        /* calculate the polynomial coefficients */
        val c = (3f * controlPoints[0]) - startPoint
        val b = (3f * (controlPoints[1] - controlPoints[0])) - c
        val a = endPoint - startPoint - c - b

        /* calculate the curve point at parameter value t */
        val tSquared = boundT * boundT
        val tCubed = tSquared * boundT

        a * tCubed + b * tSquared + c * boundT + startPoint
      }
      else -> throw Exception("Can call getTangent with type: $type")
    }
  }

  /**
   * Use this to return the tangents on the curve.  It returns the vectors in the form of an array of RPoint.
   * @eexample getTangents
   * @param segments int, the number of segments in which to divide the curve.
   * @return RPoint[], the tangent vectors returned in an array.
   */
  fun getTangents(segments: Int): Array<RPoint> {
    val result: Array<RPoint?>
    val dt: Float
    var t: Float
    when (type) {
      LINETO -> return arrayOf(startPoint, endPoint)
      QUADBEZIERTO, CUBICBEZIERTO -> {
        result = arrayOfNulls(segments)
        dt = 1f / segments
        t = 0f
        var i = 0
        while (i < segments) {
          result[i] = getTangent(t)
          t += dt
          i++
        }
        return result.filterNotNull().toTypedArray()
      }
    }
    return arrayOf()
  }

  override val tangents: Array<RPoint>
    get() = getTangents(100)

  /**
   * Use this to return a specific tangent on the curve.  It returns the RPoint representing the tangent vector for a given value of the advancement parameter t on the curve.
   * @eexample getTangent
   * @param t float, the parameter of advancement on the curve. t must have values between 0 and 1.
   * @return RPoint, the vertice returned.
   */
  override fun getTangent(t: Float): RPoint {
    /* limit the value of t between 0 and 1 */
    var boundT = t
    boundT = if (boundT > 1f) 1f else boundT
    boundT = if (boundT < 0f) 0f else boundT

    return when (type) {
      LINETO -> endPoint - startPoint
      QUADBEZIERTO -> {
        /* calculate the curve point at parameter value t */
        2f * ((startPoint - 2 * controlPoints[0] + endPoint) * boundT + (controlPoints[0] - startPoint))
      }
      CUBICBEZIERTO -> {
        /* calculate the curve point at parameter value t */
        val tsquared = boundT * boundT
        val inverseT = 1 - boundT
        val inverseTSquared = inverseT * inverseT

        3f * (
          (-inverseTSquared * startPoint) +
            ((3f * tsquared - 4f * boundT + 1f) * controlPoints[0]) +
            (boundT * (2f - (3f * boundT)) * controlPoints[1]) +
            (tsquared * endPoint)
          )
      }
      else -> throw Exception("Can call getTangent with type: $type")
    }
  }

  /**
   * Use this to return arc length of a curve.  It returns the float representing the length given the value of the advancement parameter t on the curve. The current implementation of this function is very slow, not recommended for using during frame draw.
   * @eexample RCommand_getCurveLength
   * @param t float, the parameter of advancement on the curve. t must have values between 0 and 1.
   * @return float, the length returned.
   * @invisible
   */
  fun getCurveLength(t: Float): Float = when (type) {
    LINETO -> {
      val boundT = t.bound(0f, 1f)
      val d = endPoint - startPoint
      val t2 = boundT * boundT
      sqrt((d.x + d.y) * t2)
    }
    /* calculate the curve point at parameter value t */
    QUADBEZIERTO -> quadBezierLength()
    /* calculate the curve point at parameter value t */
    CUBICBEZIERTO -> cubicBezierLength()
    else -> -1f
  }

  /**
   * Use this to return arc length of a curve.  It returns the float representing the length given the value of the advancement parameter t on the curve. The current implementation of this function is very slow, not recommended for using during frame draw.
   * @eexample RCommand_getCurveLength
   * @return float, the length returned.
   * @invisible
   */
  override val curveLength: Float
    get() = getCurveLength(1f)

  /**
   * Use this method to draw the command.
   * @eexample drawCommand
   * @param g PGraphics, the graphics object on which to draw the command
   */
  override fun draw(g: PGraphics) = g.with {
    beginShape()
    points.forEach { vertex(it.x, it.y) }
    endShape()
  }

  /**
   * Use this method to draw the command.
   * @eexample drawCommand
   * @param g  the applet object on which to draw the command
   */
  override fun draw(g: PApplet) = g.with {
    beginShape()
    points.forEach { vertex(it.x, it.y) }
    endShape()
  }

  /**
   * Use this to return the start, control and end points of the curve.  It returns the points in the way of an array of RPoint.
   * @eexample getHandles
   * @return RPoint[], the vertices returned in an array.
   */
  override val handles: Array<RPoint>
    get() {
      return arrayOf(startPoint) + controlPoints + endPoint
    }

  /**
   * Returns two commands resulting of splitting the command.
   * @eexample split
   * @param t  the advancement on the curve where command should be split.
   * @return RPoint[], the tangent vectors returned in an array.
   */
  fun split(t: Float): Array<RCommand> = when (type) {
    LINETO -> splitLine(t)
    QUADBEZIERTO -> splitQuadBezier(t)
    CUBICBEZIERTO -> splitCubicBezier(t)
    else -> arrayOf()
  }

  /**
   * Taken from:
   * http://steve.hollasch.net/cgindex/curves/cbezarclen.html
   *
   * who took it from:
   * Schneider's Bezier curve-fitter
   *
   */
  private fun splitCubicBezier(t: Float): Array<RCommand> {
    val triangleMatrix = Array(4) { Array(4) { RPoint(0, 0) } }
    val ctrlPoints = handles

    // Copy control points to triangle matrix
    for (i in 0..3) {
      triangleMatrix[0][i].x = ctrlPoints[i].x
      triangleMatrix[0][i].y = ctrlPoints[i].y
    }

    // Triangle computation
    for (i in 1..3) {
      for (j in 0..3 - i) {
        val last = triangleMatrix[i - 1][j]
        val next = triangleMatrix[i - 1][j + 1]

        triangleMatrix[i][j] = (1 - t) * last + t * next
      }
    }

    return arrayOf(
      createBezier4(startPoint, triangleMatrix[1][0], triangleMatrix[2][0], triangleMatrix[3][0]),
      createBezier4(triangleMatrix[3][0], triangleMatrix[2][1], triangleMatrix[1][2], endPoint)
    )
  }

  private fun splitQuadBezier(t: Float): Array<RCommand> {
    val triangleMatrix = Array(3) { Array(3) { RPoint(0, 0) } }
    val ctrlPoints = handles

    // Copy control points to triangle matrix
    for (i in 0..2) {
      triangleMatrix[0][i] = ctrlPoints[i]
    }

    // Triangle computation
    for (i in 1..2) {
      for (j in 0..2 - i) {
        val last = triangleMatrix[i - 1][j]
        val next = triangleMatrix[i - 1][j + 1]

        triangleMatrix[i][j] = (1 - t) * last + t * next
      }
    }

    return arrayOf(
      createBezier3(startPoint, triangleMatrix[1][0], triangleMatrix[2][0]),
      createBezier3(triangleMatrix[2][0], triangleMatrix[1][1], endPoint)
    )
  }

  private fun splitLine(t: Float): Array<RCommand> {
    val triangleMatrix = Array(2) { Array(2) { RPoint(0, 0) } }
    val ctrlPoints = handles

    // Copy control points to triangle matrix
    for (i in 0..1) {
      triangleMatrix[0][i] = ctrlPoints[i]
    }

    // Triangle computation
    for (i in 1..1) {
      for (j in 0..1 - i) {
        val last = triangleMatrix[i - 1][j]
        val next = triangleMatrix[i - 1][j + 1]

        triangleMatrix[i][j] = (1 - t) * last + t * next
      }
    }
    return arrayOf(
      createLine(startPoint, triangleMatrix[1][0]),
      createLine(triangleMatrix[1][0], endPoint)
    )
  }

  private fun quadBezierAdaptative() {
    addCurvePoint(RPoint(startPoint))
    quadBezierAdaptativeRecursive(startPoint, controlPoints[0], endPoint, 0)
    addCurvePoint(RPoint(endPoint))
  }

  private fun quadBezierAdaptativeRecursive(
    a: RPoint,
    b: RPoint,
    c: RPoint,
    level: Int,
  ) = quadBezierAdaptativeRecursive(a.x, a.y, b.x, b.y, c.x, c.y, level)

  private fun quadBezierAdaptativeRecursive(
    x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float, level: Int,
  ) {
    if (level > segmentRecursionLimit) {
      return
    }

    // Calculate all the mid-points of the line segments
    //----------------------
    val x12 = (x1 + x2) / 2
    val y12 = (y1 + y2) / 2
    val x23 = (x2 + x3) / 2
    val y23 = (y2 + y3) / 2
    val x123 = (x12 + x23) / 2
    val y123 = (y12 + y23) / 2
    val dx = x3 - x1
    val dy = y3 - y1
    val d = abs((x2 - x3) * dy - (y2 - y3) * dx)
    if (d > segmentCollinearityEpsilon) {
      // Regular care
      //-----------------
      if (d * d <= segmentDistTolSqr * (dx * dx + dy * dy)) {
        // If the curvature doesn't exceed the distance_tolerance value
        // we tend to finish subdivisions.
        //----------------------
        if (segmentAngleTol < segmentAngleTolEpsilon) {
          addCurvePoint(RPoint(x123, y123))
          return
        }

        // Angle & Cusp Condition
        //----------------------
        var da = abs(atan2((y3 - y2), (x3 - x2)) - atan2((y2 - y1), (x2 - x1)))
        if (da >= Math.PI) da = 2 * Math.PI.toFloat() - da
        if (da < segmentAngleTol) {
          // Finally we can stop the recursion
          //----------------------
          addCurvePoint(RPoint(x123, y123))
          return
        }
      }
    } else {
      if (abs(x1 + x3 - x2 - x2) + abs(y1 + y3 - y2 - y2) <= segmentDistTolMnhttn) {
        addCurvePoint(RPoint(x123, y123))
        return
      }
    }

    // Continue subdivision
    //----------------------
    quadBezierAdaptativeRecursive(x1, y1, x12, y12, x123, y123, level + 1)
    quadBezierAdaptativeRecursive(x123, y123, x23, y23, x3, y3, level + 1)
  }

  private fun cubicBezierAdaptative() {
    addCurvePoint(RPoint(startPoint))
    cubicBezierAdaptativeRecursive(startPoint, controlPoints[0], controlPoints[1], endPoint, 0)
    addCurvePoint(RPoint(endPoint))
  }

  private fun cubicBezierAdaptativeRecursive(
    a: RPoint,
    b: RPoint,
    c: RPoint,
    d: RPoint,
    level: Int,
  ) = cubicBezierAdaptativeRecursive(a.x, a.y, b.x, b.y, c.x, c.y, d.x, d.y, level)

  private fun cubicBezierAdaptativeRecursive(
    x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float, x4: Float, y4: Float,
    level: Int,
  ) {
    if (level > segmentRecursionLimit) {
      return
    }

    // Calculate all the mid-points of the line segments
    //----------------------
    val x12 = (x1 + x2) / 2
    val y12 = (y1 + y2) / 2
    val x23 = (x2 + x3) / 2
    val y23 = (y2 + y3) / 2
    val x34 = (x3 + x4) / 2
    val y34 = (y3 + y4) / 2
    val x123 = (x12 + x23) / 2
    val y123 = (y12 + y23) / 2
    val x234 = (x23 + x34) / 2
    val y234 = (y23 + y34) / 2
    val x1234 = (x123 + x234) / 2
    val y1234 = (y123 + y234) / 2

    // Try to approximate the full cubic curve by a single straight line
    //------------------
    val dx = x4 - x1
    val dy = y4 - y1
    val d2 = abs((x2 - x4) * dy - (y2 - y4) * dx)
    val d3 = abs((x3 - x4) * dy - (y3 - y4) * dx)
    var da1: Float
    var da2: Float
    val d2b = if (d2 > segmentCollinearityEpsilon) 1 else 0
    val d3b = if (d3 > segmentCollinearityEpsilon) 1 else 0
    when ((d2b shl 1) + d3b) {
      0 ->         // All collinear OR p1==p4
        //----------------------
        if (abs(x1 + x3 - x2 - x2) + abs(y1 + y3 - y2 - y2) + abs(x2 + x4 - x3 - x3) + abs(
            y2 + y4 - y3 - y3
          ) <= segmentDistTolMnhttn
        ) {
          addCurvePoint(RPoint(x1234, y1234))
          return
        }
      1 ->         // p1,p2,p4 are collinear, p3 is considerable
        //----------------------
        if (d3 * d3 <= segmentDistTolSqr * (dx * dx + dy * dy)) {
          if (segmentAngleTol < segmentAngleTolEpsilon) {
            addCurvePoint(RPoint(x23, y23))
            return
          }

          // Angle Condition
          //----------------------
          da1 = abs(atan2((y4 - y3), (x4 - x3)) - atan2((y3 - y2), (x3 - x2)))
          if (da1 >= Math.PI.toFloat()) da1 = 2 * Math.PI.toFloat() - da1
          if (da1 < segmentAngleTol) {
            addCurvePoint(RPoint(x2, y2))
            addCurvePoint(RPoint(x3, y3))
            return
          }
          if (segmentCuspLimit != 0f) {
            if (da1 > segmentCuspLimit) {
              addCurvePoint(RPoint(x3, y3))
              return
            }
          }
        }
      2 ->         // p1,p3,p4 are collinear, p2 is considerable
        //----------------------
        if (d2 * d2 <= segmentDistTolSqr * (dx * dx + dy * dy)) {
          if (segmentAngleTol < segmentAngleTolEpsilon) {
            addCurvePoint(RPoint(x23, y23))
            return
          }

          // Angle Condition
          //----------------------
          da1 = abs(atan2((y3 - y2), (x3 - x2)) - atan2((y2 - y1), (x2 - x1)))
          if (da1 >= Math.PI.toFloat()) da1 = 2 * Math.PI.toFloat() - da1
          if (da1 < segmentAngleTol) {
            addCurvePoint(RPoint(x2, y2))
            addCurvePoint(RPoint(x3, y3))
            return
          }
          if (segmentCuspLimit != 0f) {
            if (da1 > segmentCuspLimit) {
              addCurvePoint(RPoint(x2, y2))
              return
            }
          }
        }
      3 ->         // Regular care
        //-----------------
        if ((d2 + d3) * (d2 + d3) <= segmentDistTolSqr * (dx * dx + dy * dy)) {
          // If the curvature doesn't exceed the distance_tolerance value
          // we tend to finish subdivisions.
          //----------------------
          if (segmentAngleTol < segmentAngleTolEpsilon) {
            addCurvePoint(RPoint(x23, y23))
            return
          }

          // Angle & Cusp Condition
          //----------------------
          val a23 = atan2((y3 - y2), (x3 - x2))
          da1 = abs(a23 - atan2((y2 - y1), (x2 - x1)))
          da2 = abs(atan2((y4 - y3), (x4 - x3)) - a23)
          if (da1 >= Math.PI.toFloat()) da1 = 2 * Math.PI.toFloat() - da1
          if (da2 >= Math.PI.toFloat()) da2 = 2 * Math.PI.toFloat() - da2
          if (da1 + da2 < segmentAngleTol) {
            // Finally we can stop the recursion
            //----------------------
            addCurvePoint(RPoint(x23, y23))
            return
          }
          if (segmentCuspLimit != 0f) {
            if (da1 > segmentCuspLimit) {
              addCurvePoint(RPoint(x2, y2))
              return
            }
            if (da2 > segmentCuspLimit) {
              addCurvePoint(RPoint(x3, y3))
              return
            }
          }
        }
    }

    // Continue subdivision
    //----------------------
    cubicBezierAdaptativeRecursive(x1, y1, x12, y12, x123, y123, x1234, y1234, level + 1)
    cubicBezierAdaptativeRecursive(x1234, y1234, x234, y234, x34, y34, x4, y4, level + 1)
  }

  private fun lineUniformStep() {
    // If the number of steps is equal to 0 then choose a number of steps adapted to the curve
    var steps = segmentSteps
    if (segmentSteps.toFloat() == 0.0f) {
      val dx = endPoint.x - startPoint.x
      val dy = endPoint.y - startPoint.y
      val len = sqrt((dx * dx + dy * dy))
      steps = (len * 0.25).toInt()
      if (steps < 4) steps = 4
    }
    val dt = 1f / steps
    var fx: Float = startPoint.x
    val fdx: Float = (endPoint.x - startPoint.x) * dt
    var fy: Float = startPoint.y
    val fdy: Float = (endPoint.y - startPoint.y) * dt
    for (loop in 0 until steps) {
      addCurvePoint(RPoint(fx, fy))
      fx += fdx
      fy += fdy
    }
    addCurvePoint(RPoint(endPoint))
  }

  private fun cubicBezierUniformStep() {

    // If the number of steps is equal to 0 then choose a number of steps adapted to the curve
    var steps = segmentSteps
    if (segmentSteps.toFloat() == 0.0f) {
      val dx1 = controlPoints[0].x - startPoint.x
      val dy1 = controlPoints[0].y - startPoint.y
      val dx2 = controlPoints[1].x - controlPoints[0].x
      val dy2 = controlPoints[1].y - controlPoints[0].y
      val dx3 = endPoint.x - controlPoints[1].x
      val dy3 = endPoint.y - controlPoints[1].y
      val len =
        sqrt((dx1 * dx1 + dy1 * dy1)) + sqrt((dx2 * dx2 + dy2 * dy2)) + sqrt(
          (dx3 * dx3 + dy3 * dy3)
        )
      steps = (len * 0.25).toInt()
      if (steps < 4) {
        steps = 4
      }
    }
    val dt = 1f / steps
    var fddx: Float
    var fddy: Float
    val fdddx: Float
    val fdddy: Float
    var fddPer2x: Float
    var fddPer2y: Float
    val fdddPer2x: Float
    val fdddPer2y: Float
    val temp = dt * dt
    var fx: Float = startPoint.x
    var fdx: Float = 3f * (controlPoints[0].x - startPoint.x) * dt
    fddPer2x = 3f * (startPoint.x - 2f * controlPoints[0].x + controlPoints[1].x) * temp
    fdddPer2x =
      3f * (3f * (controlPoints[0].x - controlPoints[1].x) + endPoint.x - startPoint.x) * temp * dt
    fdddx = fdddPer2x + fdddPer2x
    fddx = fddPer2x + fddPer2x
    val fdddPer6x: Float = fdddPer2x * (1.0f / 3f)
    var fy: Float = startPoint.y
    var fdy: Float = 3f * (controlPoints[0].y - startPoint.y) * dt
    fddPer2y = 3f * (startPoint.y - 2f * controlPoints[0].y + controlPoints[1].y) * temp
    fdddPer2y =
      3f * (3f * (controlPoints[0].y - controlPoints[1].y) + endPoint.y - startPoint.y) * temp * dt
    fdddy = fdddPer2y + fdddPer2y
    fddy = fddPer2y + fddPer2y
    val fdddPer6y: Float = fdddPer2y * (1.0f / 3f)
    for (loop in 0 until steps) {
      addCurvePoint(RPoint(fx, fy))
      fx += fdx + fddPer2x + fdddPer6x
      fdx += fddx + fdddPer2x
      fddx += fdddx
      fddPer2x += fdddPer2x
      fy += fdy + fddPer2y + fdddPer6y
      fdy += fddy + fdddPer2y
      fddy += fdddy
      fddPer2y += fdddPer2y
    }
    addCurvePoint(RPoint(endPoint))
  }

  private fun quadBezierUniformStep() {
    // If the number of steps is equal to 0 then choose a number of steps adapted to the curve
    var steps = segmentSteps
    if (segmentSteps.toFloat() == 0.0f) {
      val dx1 = controlPoints[0].x - startPoint.x
      val dy1 = controlPoints[0].y - startPoint.y
      val dx2 = endPoint.x - controlPoints[0].x
      val dy2 = endPoint.y - controlPoints[0].y
      val len = sqrt((dx1 * dx1 + dy1 * dy1)) + sqrt((dx2 * dx2 + dy2 * dy2))
      steps = (len * 0.25).toInt()
      if (steps < 4) steps = 4
    }
    val dt = 1f / steps
    val fddx: Float
    val fddy: Float
    val fddPer2x: Float
    val fddPer2y: Float
    val temp = dt * dt
    var fx: Float = startPoint.x
    var fdx: Float = 2f * (controlPoints[0].x - startPoint.x) * dt
    fddPer2x = (startPoint.x - 2f * controlPoints[0].x + endPoint.x) * temp
    fddx = fddPer2x + fddPer2x
    var fy: Float = startPoint.y
    var fdy: Float = 2f * (controlPoints[0].y - startPoint.y) * dt
    fddPer2y = (startPoint.y - 2f * controlPoints[0].y + endPoint.y) * temp
    fddy = fddPer2y + fddPer2y
    for (loop in 0 until steps) {
      addCurvePoint(RPoint(fx, fy))
      fx += fdx + fddPer2x
      fdx += fddx
      fy += fdy + fddPer2y
      fdy += fddy
    }
    addCurvePoint(RPoint(endPoint))
  }

  // Use Horner's method to advance
  //----------------------
  private fun lineUniformLength() {
    val endPoint = endPoint
    val startPoint = startPoint

    // If the number of steps is equal to 0 then choose a number of steps adapted to the curve
    val dx1 = endPoint.x - startPoint.x
    val dy1 = endPoint.y - startPoint.y
    val len = sqrt((dx1 * dx1 + dy1 * dy1))
    var steps: Float = (len * 2)
    if (steps < 4) steps = 4f

    // This holds the amount of steps used to calculate segment lengths
    val dt = 1f / steps

    // This holds how much length has to bee advanced until adding a point
    var untilPoint = segmentAccOffset
    var fx: Float = startPoint.x
    val fdx: Float = (endPoint.x - startPoint.x) * dt
    var fy: Float = startPoint.y
    val fdy: Float = (endPoint.y - startPoint.y) * dt
    var loop = 0
    while (loop <= steps) {

      /* Add point to curve if segment length is reached */
      if (untilPoint <= 0) {
        addCurvePoint(RPoint(fx, fy))
        untilPoint += commandSegmentLength
      }

      /* Add segment differential to segment length */
      untilPoint -= sqrt((fdx * fdx + fdy * fdy)) // Eventually try other distance measures
      fx += fdx
      fy += fdy
      loop++
    }

    //addCurvePoint(new RPoint(endPoint));
    segmentAccOffset = untilPoint
  }

  // Use Horner's method to advance
  //----------------------
  private fun quadBezierUniformLength() {
    val dx1 = controlPoints[0].x - startPoint.x
    val dy1 = controlPoints[0].y - startPoint.y
    val dx2 = endPoint.x - controlPoints[0].x
    val dy2 = endPoint.y - controlPoints[0].y
    val len = sqrt((dx1 * dx1 + dy1 * dy1)) + sqrt((dx2 * dx2 + dy2 * dy2))
    var steps: Float = (len * 2)
    if (steps < 4) steps = 4f
    val dt = 1f / steps
    var untilPoint = segmentAccOffset
    val fddx: Float
    val fddy: Float
    val fddPer2x: Float
    val fddPer2y: Float
    var fix: Float
    var fiy: Float
    val temp = dt * dt
    var fx: Float = startPoint.x
    var fdx: Float = 2f * (controlPoints[0].x - startPoint.x) * dt
    fddPer2x = (startPoint.x - 2f * controlPoints[0].x + endPoint.x) * temp
    fddx = fddPer2x + fddPer2x
    var fy: Float = startPoint.y
    var fdy: Float = 2f * (controlPoints[0].y - startPoint.y) * dt
    fddPer2y = (startPoint.y - 2f * controlPoints[0].y + endPoint.y) * temp
    fddy = fddPer2y + fddPer2y
    var loop = 0
    while (loop <= steps) {

      /* Add point to curve if segment length is reached */
      if (untilPoint <= 0) {
        addCurvePoint(RPoint(fx, fy))
        untilPoint += commandSegmentLength
      }

      /* Add segment differential to segment length */
      fix = fdx + fddPer2x
      fiy = fdy + fddPer2y
      untilPoint -= sqrt((fix * fix + fiy * fiy)) // Eventually try other distance measures
      fx += fix
      fdx += fddx
      fy += fiy
      fdy += fddy
      loop++
    }

    //addCurvePoint(new RPoint(endPoint));
    segmentAccOffset = untilPoint
  }

  // Use Horner's method to advance
  //----------------------
  private fun cubicBezierUniformLength() {
    val dx1 = controlPoints[0].x - startPoint.x
    val dy1 = controlPoints[0].y - startPoint.y
    val dx2 = controlPoints[1].x - controlPoints[0].x
    val dy2 = controlPoints[1].y - controlPoints[0].y
    val dx3 = endPoint.x - controlPoints[1].x
    val dy3 = endPoint.y - controlPoints[1].y
    val len =
      sqrt((dx1 * dx1 + dy1 * dy1)) + sqrt((dx2 * dx2 + dy2 * dy2)) + sqrt((dx3 * dx3 + dy3 * dy3))
    var steps: Float = (len * 2)
    if (steps < 4) steps = 4f
    val dt = 1f / steps
    var untilPoint = segmentAccOffset
    var fddx: Float
    var fddy: Float
    val fdddx: Float
    val fdddy: Float
    var fddPer2x: Float
    var fddPer2y: Float
    val fdddPer2x: Float
    val fdddPer2y: Float
    var fix: Float
    var fiy: Float
    val temp = dt * dt
    var fx: Float = startPoint.x
    var fdx: Float = 3f * (controlPoints[0].x - startPoint.x) * dt
    fddPer2x = 3f * (startPoint.x - 2f * controlPoints[0].x + controlPoints[1].x) * temp
    fdddPer2x =
      3f * (3f * (controlPoints[0].x - controlPoints[1].x) + endPoint.x - startPoint.x) * temp * dt
    fdddx = fdddPer2x + fdddPer2x
    fddx = fddPer2x + fddPer2x
    val fdddPer6x: Float = fdddPer2x * (1.0f / 3f)
    var fy: Float = startPoint.y
    var fdy: Float = 3f * (controlPoints[0].y - startPoint.y) * dt
    fddPer2y = 3f * (startPoint.y - 2f * controlPoints[0].y + controlPoints[1].y) * temp
    fdddPer2y =
      3f * (3f * (controlPoints[0].y - controlPoints[1].y) + endPoint.y - startPoint.y) * temp * dt
    fdddy = fdddPer2y + fdddPer2y
    fddy = fddPer2y + fddPer2y
    val fdddPer6y: Float = fdddPer2y * (1.0f / 3f)
    var loop = 0
    while (loop < steps) {

      /* Add point to curve if segment length is reached */
      if (untilPoint <= 0) {
        addCurvePoint(RPoint(fx, fy))
        untilPoint += commandSegmentLength
      }

      /* Add segment differential to segment length */
      fix = fdx + fddPer2x + fdddPer6x
      fiy = fdy + fddPer2y + fdddPer6y
      untilPoint -= sqrt((fix * fix + fiy * fiy)) // Eventually try other distance measures
      fx += fix
      fdx += fddx + fdddPer2x
      fddx += fdddx
      fddPer2x += fdddPer2x
      fy += fiy
      fdy += fddy + fdddPer2y
      fddy += fdddy
      fddPer2y += fdddPer2y
      loop++
    }

    //addCurvePoint(new RPoint(endPoint));
    segmentAccOffset = untilPoint
  }

  private fun quadBezierLength(): Float {
    val dx1 = controlPoints[0].x - startPoint.x
    val dy1 = controlPoints[0].y - startPoint.y
    val dx2 = endPoint.x - controlPoints[0].x
    val dy2 = endPoint.y - controlPoints[0].y
    val len = sqrt((dx1 * dx1 + dy1 * dy1)) + sqrt((dx2 * dx2 + dy2 * dy2))
    var steps: Float = (len * 2)
    if (steps < 4) steps = 4f
    val dt = 1f / steps
    val fddx: Float
    val fddy: Float
    val fddPer2x: Float
    val fddPer2y: Float
    var fix: Float
    var fiy: Float
    val temp = dt * dt
    var totallen = 0f
    var fx: Float = startPoint.x
    var fdx: Float = 2f * (controlPoints[0].x - startPoint.x) * dt
    fddPer2x = (startPoint.x - 2f * controlPoints[0].x + endPoint.x) * temp
    fddx = fddPer2x + fddPer2x
    var fy: Float = startPoint.y
    var fdy: Float = 2f * (controlPoints[0].y - startPoint.y) * dt
    fddPer2y = (startPoint.y - 2f * controlPoints[0].y + endPoint.y) * temp
    fddy = fddPer2y + fddPer2y
    var loop = 0
    while (loop <= steps) {

      /* Add segment differential to segment length */
      fix = fdx + fddPer2x
      fiy = fdy + fddPer2y
      totallen += sqrt((fix * fix + fiy * fiy)) // Eventually try other distance measures
      fx += fix
      fdx += fddx
      fy += fiy
      fdy += fddy
      loop++
    }
    return totallen
  }

  private fun cubicBezierLength(): Float {
    val dx1 = controlPoints[0].x - startPoint.x
    val dy1 = controlPoints[0].y - startPoint.y
    val dx2 = controlPoints[1].x - controlPoints[0].x
    val dy2 = controlPoints[1].y - controlPoints[0].y
    val dx3 = endPoint.x - controlPoints[1].x
    val dy3 = endPoint.y - controlPoints[1].y
    val len =
      sqrt((dx1 * dx1 + dy1 * dy1)) + sqrt((dx2 * dx2 + dy2 * dy2)) + sqrt((dx3 * dx3 + dy3 * dy3))
    var steps: Float = (len * 2)
    if (steps < 4) steps = 4f
    val dt = 1f / steps
    var fddx: Float
    var fddy: Float
    val fdddx: Float
    val fdddy: Float
    var fddPer2x: Float
    var fddPer2y: Float
    val fdddPer2x: Float
    val fdddPer2y: Float
    var fix: Float
    var fiy: Float
    val temp = dt * dt
    var totallen = 0f
    var fx: Float = startPoint.x
    var fdx: Float = 3f * (controlPoints[0].x - startPoint.x) * dt
    fddPer2x = 3f * (startPoint.x - 2f * controlPoints[0].x + controlPoints[1].x) * temp
    fdddPer2x =
      3f * (3f * (controlPoints[0].x - controlPoints[1].x) + endPoint.x - startPoint.x) * temp * dt
    fdddx = fdddPer2x + fdddPer2x
    fddx = fddPer2x + fddPer2x
    val fdddPer6x: Float = fdddPer2x * (1.0f / 3f)
    var fy: Float = startPoint.y
    var fdy: Float = 3f * (controlPoints[0].y - startPoint.y) * dt
    fddPer2y = 3f * (startPoint.y - 2f * controlPoints[0].y + controlPoints[1].y) * temp
    fdddPer2y =
      3f * (3f * (controlPoints[0].y - controlPoints[1].y) + endPoint.y - startPoint.y) * temp * dt
    fdddy = fdddPer2y + fdddPer2y
    fddy = fddPer2y + fddPer2y
    val fdddPer6y: Float = fdddPer2y * (1.0f / 3f)
    var loop = 0
    while (loop < steps) {
      /* Add segment differential to segment length */
      fix = fdx + fddPer2x + fdddPer6x
      fiy = fdy + fddPer2y + fdddPer6y
      totallen += sqrt((fix * fix + fiy * fiy)) // Eventually try other distance measures
      fx += fix
      fdx += fddx + fdddPer2x
      fddx += fdddx
      fddPer2x += fdddPer2x
      fy += fiy
      fdy += fddy + fdddPer2y
      fddy += fdddy
      fddPer2y += fdddPer2y
      loop++
    }
    return totallen
  }

  private fun append(vararg nextcontrolpoints: RPoint) {
    controlPoints += nextcontrolpoints
  }

  private fun addCurvePoint(vararg nextcurvepoints: RPoint) {
    curvePoints += nextcurvepoints
  }

  fun intersectionPoints(other: RCommand): Array<RPoint> = when (type) {
    LINETO -> when (other.type) {
      LINETO -> lineLineIntersection(this, other)
      QUADBEZIERTO -> lineQuadIntersection(this, other)
      CUBICBEZIERTO -> lineCubicIntersection(this, other)
      else -> throw Exception("Invalid other command type: ${other.type}")
    }
    QUADBEZIERTO -> when (other.type) {
      LINETO -> lineQuadIntersection(other, this)
      QUADBEZIERTO -> quadQuadIntersection(this, other)
      CUBICBEZIERTO -> quadCubicIntersection(this, other)
      else -> throw Exception("Invalid other command type: ${other.type}")
    }
    CUBICBEZIERTO -> when (other.type) {
      LINETO -> lineCubicIntersection(other, this)
      QUADBEZIERTO -> quadCubicIntersection(other, this)
      CUBICBEZIERTO -> cubicCubicIntersection(this, other)
      else -> throw Exception("Invalid other command type: ${other.type}")
    }
    else -> throw Exception("Invalid command type: $type")
  }

  fun closestPoints(other: RCommand): RClosest {
    var result = RClosest()
    result.distance = 0f
    val temp: RPoint
    when (type) {
      LINETO -> when (other.type) {
        LINETO -> {
          result.intersects = lineLineIntersection(this, other)
          if (result.intersects.isEmpty()) {
            result = lineLineClosest(this, other)
          }
        }
        QUADBEZIERTO -> {
          result.intersects = lineQuadIntersection(this, other)
          if (result.intersects.isEmpty()) {
            result = lineQuadClosest(this, other)
          }
        }
        CUBICBEZIERTO -> {
          result.intersects = lineCubicIntersection(this, other)
          if (result.intersects.isEmpty()) {
            result = lineCubicClosest(this, other)
          }
        }
      }
      QUADBEZIERTO -> when (other.type) {
        LINETO -> {
          result.intersects = lineQuadIntersection(other, this)
          if (result.intersects.isEmpty()) {
            result = lineQuadClosest(other, this)
            temp = (result).closest[0]
            result.closest = arrayOf(result.closest[1], temp)
          }
        }
        QUADBEZIERTO -> {
          result.intersects = quadQuadIntersection(this, other)
          if (result.intersects.isEmpty()) {
            result = quadQuadClosest(this, other)
          }
        }
        CUBICBEZIERTO -> {
          result.intersects = quadCubicIntersection(this, other)
          if (result.intersects.isEmpty()) {
            result = quadCubicClosest(this, other)
          }
        }
      }
      CUBICBEZIERTO -> when (other.type) {
        LINETO -> {
          result.intersects = lineCubicIntersection(other, this)
          if (result.intersects.isEmpty()) {
            result = lineCubicClosest(other, this)
            temp = (result).closest[0]
            result.closest = arrayOf(result.closest[1], temp)
          }
        }
        QUADBEZIERTO -> {
          result.intersects = quadCubicIntersection(other, this)
          if (result.intersects.isEmpty()) {
            result = quadCubicClosest(other, this)
            temp = result.closest[0]
            result.closest = arrayOf(result.closest[1], temp)
          }
        }
        CUBICBEZIERTO -> {
          result.intersects = cubicCubicIntersection(this, other)
          if (result.intersects.isEmpty()) {
            result = cubicCubicClosest(this, other)
          }
        }
      }
    }
    return result
  }

  fun typeString(typeInt: Int) = when (typeInt) {
    LINETO -> "LINETO"
    QUADBEZIERTO -> "QUADBEZIERTO"
    CUBICBEZIERTO -> "CUBICBEZIERTO"
    else -> "UNKNOWN"
  }

  override fun toString(): String {
    return "RCommand(startPoint=$startPoint, endPoint=$endPoint, controlPoints=${controlPoints.contentToString()}, type=$type, curvePoints=${curvePoints.contentToString()})"
  }

  companion object {
    // TODO: Convert these to enums
    const val LINETO = 0
    const val QUADBEZIERTO = 1
    const val CUBICBEZIERTO = 2

    const val ADAPTATIVE = 0
    const val UNIFORMLENGTH = 1
    const val UNIFORMSTEP = 2
    var segmentType = UNIFORMLENGTH

    /* Parameters for ADAPTATIVE (dependent of the PGraphics on which drawing) */
    const val segmentRecursionLimit = 32
    const val segmentDistanceEpsilon = 1.192092896e-07f
    const val segmentCollinearityEpsilon = 1.192092896e-07f
    const val segmentAngleTolEpsilon = 0.01f
    var segmentGfxStrokeWeight = 1.0f
    var segmentGfxScale = 1.0f
    var segmentApproxScale = 1.0f
    var segmentDistTolSqr = 0.25f
    var segmentDistTolMnhttn = 4.0f
    var segmentAngleTol = 0.0f
    var segmentCuspLimit = 0.0f

    /* Parameters for UNIFORMLENGTH (dependent of the PGraphics on which drawing) */
    var commandSegmentLength = 4.0f

    @JvmField
    var segmentOffset = 0.0f

    @JvmField
    var segmentAccOffset = 0.0f

    /* Parameters for UNIFORMSTEP */
    var segmentSteps = 0
    var segmentLines = false
    fun createLine(start: RPoint, end: RPoint): RCommand = RCommand(start, end)

    fun createLine(startx: Float, starty: Float, endx: Float, endy: Float): RCommand =
      createLine(RPoint(startx, starty), RPoint(endx, endy))

    fun createBezier3(start: RPoint, cp1: RPoint, end: RPoint): RCommand =
      RCommand(startPoint = start, endPoint = end, arrayOf(cp1), QUADBEZIERTO)

    fun createBezier3(
      startx: Float, starty: Float, cp1x: Float, cp1y: Float, endx: Float, endy: Float,
    ): RCommand = createBezier3(RPoint(startx, starty), RPoint(cp1x, cp1y), RPoint(endx, endy))

    fun createBezier4(start: RPoint, cp1: RPoint, cp2: RPoint, end: RPoint): RCommand =
      RCommand(startPoint = start, endPoint = end, arrayOf(cp1, cp2), type = CUBICBEZIERTO)

    fun createBezier4(
      startx: Float, starty: Float, cp1x: Float, cp1y: Float, cp2x: Float, cp2y: Float, endx: Float,
      endy: Float,
    ): RCommand =
      createBezier4(
        RPoint(startx, starty), RPoint(cp1x, cp1y), RPoint(cp2x, cp2y),
        RPoint(endx, endy)
      )

    /**
     * Use this to set the segmentator type.  ADAPTATIVE segmentator minimizes the number of segments avoiding perceptual artifacts like angles or cusps.  Use this in order to have Polygons and Meshes with the fewest possible vertices.  This can be useful when using or drawing a lot the same Polygon or Mesh deriving from this Shape.  UNIFORMLENGTH segmentator is the slowest segmentator and it segments the curve on segments of equal length.  This can be useful for very specific applications when for example drawing incrementaly a shape with a uniform speed.  UNIFORMSTEP segmentator is the fastest segmentator and it segments the curve based on a constant value of the step of the curve parameter, or on the number of segments wanted.  This can be useful when segmpointsentating very often a Shape or when we know the amount of segments necessary for our specific application.
     * @eexample setSegment
     */
    @JvmStatic
    fun setSegmentator(segmentatorType: Int) {
      segmentType = segmentatorType
    }

    /**
     * Use this to set the segmentator graphic context.
     * @eexample setSegmentGraphic
     * @param g  graphics object too which to adapt the segmentation of the command.
     */
    fun setSegmentGraphic(g: PGraphics) {
      // Set the segmentApproxScale from the graphic context g
      segmentApproxScale = 1.0f

      // Set all the gfx-context dependent parameters for all segmentators
      segmentDistTolSqr = 0.5f / segmentApproxScale
      segmentDistTolSqr *= segmentDistTolSqr
      segmentDistTolMnhttn = 4.0f / segmentApproxScale
      segmentAngleTol = 0.0f
      if (g.stroke && g.strokeWeight * segmentApproxScale > 1.0f) {
        segmentAngleTol = 0.1f
      }
    }

    /**
     * Use this to set the segmentator angle tolerance for the ADAPTATIVE segmentator and set the segmentator to ADAPTATIVE.
     * @eexample setSegmentAngle
     * @param segmentAngleTolerance  an angle from 0 to PI/2 it defines the maximum angle between segments.
     */
    @JvmStatic
    fun setSegmentAngle(segmentAngleTolerance: Float) {
      //segmentType = ADAPTATIVE;
      segmentAngleTol = segmentAngleTolerance
    }

    /**
     * Use this to set the segmentator length for the UNIFORMLENGTH segmentator and set the segmentator to UNIFORMLENGTH.
     * @eexample setSegmentLength
     * @param segmentLngth  the length of each resulting segment.
     */
    @JvmStatic
    fun setSegmentLength(segmentLngth: Float) {
      commandSegmentLength = if (segmentLngth >= 1) segmentLngth else 4f
    }

    /**
     * Use this to set the segmentator offset for the UNIFORMLENGTH segmentator and set the segmentator to UNIFORMLENGTH.
     * @eexample setSegmentOffset
     * @param segmentOffset  the offset of the first point on the path.
     */
    fun setSegmentOffset(segmentOffset: Float) {
      this.segmentOffset = if (segmentOffset >= 0) segmentOffset else 0f
    }

    /**
     * Use this to set the segmentator step for the UNIFORMSTEP segmentator and set the segmentator to UNIFORMSTEP.
     * @eexample setSegmentStep
     * @param segmentStps  if a float from +0.0 to 1.0 is passed it's considered as the step, else it's considered as the number of steps.  When a value of 0.0 is used the steps will be calculated automatically depending on an estimation of the length of the curve.  The special value -1 is the same as 0.0 but also turning of the segmentation of lines (faster segmentation).
     */
    @JvmStatic
    fun setSegmentStep(segmentStps: Float) {
      //segmentType = UNIFORMSTEP;
      var boundSegmentSteps = segmentStps
      if (boundSegmentSteps == -1f) {
        segmentLines = false
        boundSegmentSteps = 0f
      } else {
        segmentLines = true
      }
      // Set the parameters
      boundSegmentSteps = abs(boundSegmentSteps)
      segmentSteps = if (boundSegmentSteps > 0.0f && boundSegmentSteps < 1.0f) {
        (1f / boundSegmentSteps).toInt()
      } else {
        boundSegmentSteps.toInt()
      }
    }

    fun lineLineIntersection(c1: RCommand, c2: RCommand): Array<RPoint> {
      val a = c1.startPoint.clone()
      val b = c1.endPoint.clone()
      val (x, y) = c2.startPoint.clone()
      val (x1, y1) = c2.endPoint.clone()
      val epsilon = 1e-9f

      //test for parallel case
      val denom = (y1 - y) * (b.x - a.x) - (x1 - x) * (b.y - a.y)
      if (abs(denom) < epsilon) return arrayOf()

      //calculate segment parameter and ensure its within bounds
      val t1 = ((x1 - x) * (a.y - y) - (y1 - y) * (a.x - x)) / denom
      val t2 = ((b.x - a.x) * (a.y - y) - (b.y - a.y) * (a.x - x)) / denom
      if (t1 < 0.0f || t1 > 1.0f || t2 < 0.0f || t2 > 1.0f) return arrayOf()

      //store actual intersection
      return arrayOf(a + ((b - a) * t1))
    }

    fun lineQuadIntersection(c1: RCommand?, c2: RCommand?): Array<RPoint> {
      return arrayOf()
    }

    fun lineCubicIntersection(c1: RCommand?, c2: RCommand?): Array<RPoint> {
      return arrayOf()
    }

    fun quadQuadIntersection(c1: RCommand?, c2: RCommand?): Array<RPoint> {
      return arrayOf()
    }

    fun quadCubicIntersection(c1: RCommand?, c2: RCommand?): Array<RPoint> {
      return arrayOf()
    }

    fun cubicCubicIntersection(c1: RCommand?, c2: RCommand?): Array<RPoint> {
      return arrayOf()
    }

    fun closestAdvFrom(c: RCommand, p: RPoint): Float {
      val a = RPoint(c.startPoint)
      val b = RPoint(c.endPoint)
      val ap = RPoint(p)
      ap.sub(a)
      val ab = RPoint(b)
      ab.sub(a)
      val denom = ab.sqrnorm()
      val epsilon = 1e-19f
      if (denom < epsilon) return 0.5f
      var t = (ab.x * ap.x + ab.y * ap.y) / denom
      t = if (t > 0.0f) t else 0.0f
      t = if (t < 1.0f) t else 1.0f
      return t
    }

    fun lineLineClosest(c1: RCommand, c2: RCommand): RClosest {
      val c1b = RPoint(c1.startPoint)
      val c1e = RPoint(c1.endPoint)
      val c2t1 = closestAdvFrom(c2, c1b)
      val c2t2 = closestAdvFrom(c2, c1e)
      val c2p1 = c2.getPoint(c2t1)
      val c2p2 = c2.getPoint(c2t2)
      val dist1c2 = c2p1.dist(c1b)
      val dist2c2 = c2p2.dist(c1e)
      val c2b = RPoint(c2.startPoint)
      val c2e = RPoint(c2.endPoint)
      val c1t1 = closestAdvFrom(c1, c2b)
      val c1t2 = closestAdvFrom(c1, c2e)
      val c1p1 = c1.getPoint(c1t1)
      val c1p2 = c1.getPoint(c1t2)
      val dist1c1 = c1p1.dist(c2b)
      val dist2c1 = c1p2.dist(c2e)
      val result = RClosest()
      result.distance = min(min(dist1c2, dist2c2), min(dist1c1, dist2c1))
      result.closest = arrayOf()
      result.advancements = FloatArray(2)
      when (result.distance) {
        dist1c2 -> {
          result.closest = arrayOf(c1b, c2p1)
          result.advancements = floatArrayOf(0f, c2t1)
        }
        dist2c2 -> {
          result.closest = arrayOf(c1e, c2p2)
          result.advancements = floatArrayOf(1f, c2t2)
        }
        dist1c1 -> {
          result.closest = arrayOf(c2b, c1p1)
          result.advancements = floatArrayOf(0f, c1t1)
        }
        else -> /*if (result.distance == dist2c1)*/ {
          result.closest = arrayOf(c2e, c1p2)
          result.advancements = floatArrayOf(1f, c1t2)
        }
      }

      return result
    }

    fun lineQuadClosest(c1: RCommand?, c2: RCommand?): RClosest {
      return RClosest()
    }

    fun lineCubicClosest(c1: RCommand?, c2: RCommand?): RClosest {
      return RClosest()
    }

    fun quadQuadClosest(c1: RCommand?, c2: RCommand?): RClosest {
      return RClosest()
    }

    fun quadCubicClosest(c1: RCommand?, c2: RCommand?): RClosest {
      return RClosest()
    }

    fun cubicCubicClosest(c1: RCommand?, c2: RCommand?): RClosest {
      return RClosest()
    }
  }


}
