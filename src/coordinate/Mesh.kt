package coordinate

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import util.iterators.mapWithSibling
import util.iterators.mapWithSiblingIndexed
import util.numbers.map
import util.percentAlong
import util.polylines.MutablePolyLine
import util.polylines.PolyLine
import util.polylines.appendSegmentOrStartNewLine
import util.tuple.map

typealias PointTransformFunc = (pointLocation: Point, xIndex: Int, yIndex: Int) -> Point
typealias PointVisibilityFunc = (pointLocation: Point, transformedPointLocation: Point, xIndex: Int, yIndex: Int) -> Boolean
typealias IndexedMeshLines = Pair<List<List<PolyLine>>, List<List<PolyLine>>>
typealias MeshLines = Pair<List<PolyLine>, List<PolyLine>>

/**
 * @property bounds the screen bounds the mesh extends to fit
 * @property xPoints the number of columns in the mesh
 * @property yPoints the number of rows in the mesh
 * @property pointTransformFunc function to move a point.
 * @property pointVisibleFunc function to show or hide a point
 */
@Serializable
data class Mesh(
  val bounds: BoundRect,
  val xPoints: Int,
  val yPoints: Int,
  var pointTransformFunc: PointTransformFunc,
  var pointVisibleFunc: PointVisibilityFunc,
) {

  @Transient
  lateinit var transformedPoints: List<List<Point>>

  @Transient
  lateinit var visiblePoints: List<List<Boolean>>

  init {
    updateTransformedPoints()
    updateVisiblePoints()
  }

  private fun isPointVisible(x: Int, y: Int) = visiblePoints.getOrNull(x)?.getOrNull(y) ?: false

  private fun getScreenPoint(x: Int, y: Int) =
    bounds.pointAt((0 until xPoints).percentAlong(x), (0 until yPoints).percentAlong(y))

  private fun <K> mapPoints(block: (screenPoint: Point, x: Int, y: Int) -> K): List<List<K>> =
    xPoints.map { x ->
      yPoints.map { y ->
        block(getScreenPoint(x, y), x, y)
      }
    }

  private fun PolyLine.mapIndicesToScreenPoints(): PolyLine =
    map { (x, y) -> transformedPoints[x.toInt()][y.toInt()] }

  private fun updateTransformedPoints() {
    transformedPoints = mapPoints(pointTransformFunc)
  }

  private fun updateVisiblePoints() {
    visiblePoints = mapPoints { p, x, y -> pointVisibleFunc(p, transformedPoints[x][y], x, y) }
  }

  fun updateMeshFuncs(
    pointTransform: PointTransformFunc? = null,
    isPointVisible: PointVisibilityFunc? = null
  ) {
    this.pointTransformFunc = pointTransform ?: this.pointTransformFunc
    this.pointVisibleFunc = isPointVisible ?: this.pointVisibleFunc
    if (pointTransform != null) {
      updateTransformedPoints()
      updateVisiblePoints()
    } else if (isPointVisible != null) {
      updateVisiblePoints()
    }
  }

  /**
   * Represent the mesh as polylines
   *
   * @return Pair from horizontal lines to vertical lines
   */
  fun toLinesByIndex(): IndexedMeshLines {
    val inBoxes: MutableSet<Segment> = mutableSetOf()
    val xIndices = (0 until xPoints).toList()
    val yIndices = (0 until yPoints).toList()

    // First we loop over all the "boxes" and determine if all sides of the box are visible
    xIndices.mapWithSibling { x, nextX ->
      yIndices.mapWithSibling { y, nextY ->
        val isBox = isPointVisible(x, y) &&
          isPointVisible(nextX, y) &&
          isPointVisible(x, nextY) &&
          isPointVisible(nextX, nextY)
        if (isBox) {
          inBoxes.addAll(BoundRect(Point(x, y), Point(nextX, nextY)).segments)
        }
      }
    }

    val verticalLines: List<MutableList<MutablePolyLine>> = List(xPoints) {
      mutableListOf(mutableListOf())
    }
    val horizontalLines: List<MutableList<MutablePolyLine>> = List(yPoints) {
      mutableListOf(mutableListOf())
    }

    fun MutableList<MutablePolyLine>.maybeAppendSegment(s: Segment) {
      if (inBoxes.contains(s)) last().appendSegmentOrStartNewLine(s)?.let { add(it) }
    }

    // Then we loop over all the segments and merge them into horizontal and vertical lines.
    xIndices.mapWithSiblingIndexed { xIndex, x, nextX ->
      val verticalLineList = verticalLines[x]
      val isLastCol = xIndex == xIndices.last()
      yIndices.mapWithSiblingIndexed { yIndex, y, nextY ->
        val isLastRow = yIndex == yIndices.last()
        val horizontalLineList = horizontalLines[y]
        val verticalSegment = Segment(Point(x, y), Point(x, nextY))
        val horizontalSegment = Segment(Point(x, y), Point(nextX, y))
        verticalLineList.maybeAppendSegment(verticalSegment)
        horizontalLineList.maybeAppendSegment(horizontalSegment)

        if (isLastCol) {
          val verticalSegmentEnd = Segment(Point(nextX, y), Point(nextX, nextY))
          verticalLineList.maybeAppendSegment(verticalSegmentEnd)
        }

        if (isLastRow) {
          val horizontalSegmentEnd = Segment(Point(x, nextY), Point(nextX, nextY))
          horizontalLineList.maybeAppendSegment(horizontalSegmentEnd)
        }
      }
    }

    return (horizontalLines to verticalLines).map { it.map { lines -> lines.map { line -> line.mapIndicesToScreenPoints() } } }
  }

  fun toLines(): MeshLines = toLinesByIndex().map { it.flatten() }
}
