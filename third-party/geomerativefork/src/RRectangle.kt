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
package geomerativefork.src

class RRectangle(var topLeft: RPoint, var bottomRight: RPoint) {

  /**
   * @invisible
   */
  //return (topLeft.x > bottomRight.x) ? topLeft.x : bottomRight.x;
  //return (topLeft.x < bottomRight.x) ? topLeft.x : bottomRight.x;
  /**
   * The x coordinate of the point.
   *
   * @eexample RPoint_x
   * @usage Geometry
   * @related y
   */
  val maxX: Float
    get() {
      //return (topLeft.x > bottomRight.x) ? topLeft.x : bottomRight.x;
      return bottomRight.x
    }
  val minX: Float
    get() {
      //return (topLeft.x < bottomRight.x) ? topLeft.x : bottomRight.x;
      return topLeft.x
    }
  /**
   * @invisible
   */
  //return (topLeft.y > bottomRight.y) ? topLeft.y : bottomRight.y;
  //return (topLeft.y < bottomRight.y) ? topLeft.y : bottomRight.y;
  /**
   * The y coordinate of the point.
   *
   * @eexample RPoint_y
   * @usage Geometry
   * @related x
   */
  val maxY: Float
    get() {
      //return (topLeft.y > bottomRight.y) ? topLeft.y : bottomRight.y;
      return bottomRight.y
    }

  val minY: Float
    get() {
      //return (topLeft.y < bottomRight.y) ? topLeft.y : bottomRight.y;
      return topLeft.y
    }

  constructor(
    x: Float, y: Float, w: Float, h: Float,
  ) : this(topLeft = RPoint(x, y), bottomRight = RPoint(x + w, y + h))

  val points: Array<RPoint>
    get() = arrayOf(
      RPoint(topLeft),
      RPoint(bottomRight.x, topLeft.y),
      RPoint(bottomRight),
      RPoint(topLeft.x, bottomRight.y),
    )

  override fun toString(): String {
    return ""
  }
}