package geomerativefork.src.util

import geomerativefork.src.RPoint
import geomerativefork.src.RPoint.Companion.maxXY
import geomerativefork.src.RPoint.Companion.minXY
import processing.core.PApplet
import processing.core.PGraphics

fun contains(p: RPoint, width: Number, height: Number) =
  p.x in 0f..width.toFloat() && p.y in 0f..height.toFloat()

fun PGraphics.contains(p: RPoint) = contains(p, width, height)
fun PApplet.contains(p: RPoint) = contains(p, width, height)

/**
 * Use this method to know if the points are inside a graphics object.
 * This might be useful if we want to delete objects that go offscreen.
 */
fun PGraphics.containsWorldPointsInScreen(vararg points: RPoint): Boolean {
  val screenPoints = points.mapArray { RPoint(screenX(it.x, it.y), screenY(it.x, it.y)) }
  val (minPoint, maxPoint) = minXY(*screenPoints) to maxXY(*screenPoints)
  return contains(minPoint) && contains(maxPoint)
}

/**
 * Use this method to know if the points are inside a graphics object.
 * This might be useful if we want to delete objects that go offscreen.
 */
fun PApplet.containsWorldPointsInScreen(vararg points: RPoint): Boolean {
  val screenPoints = points.mapArray { RPoint(screenX(it.x, it.y), screenY(it.x, it.y)) }
  val (minPoint, maxPoint) = minXY(*screenPoints) to maxXY(*screenPoints)
  return contains(minPoint) && contains(maxPoint)
}
