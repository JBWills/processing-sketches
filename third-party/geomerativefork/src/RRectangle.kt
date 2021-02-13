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

  val maxX: Float
    get() = bottomRight.x

  val minX: Float
    get() = topLeft.x

  val maxY: Float
    get() = bottomRight.y

  val minY: Float
    get() = topLeft.y

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

  fun contains(p: RPoint): Boolean = p.x in minX..maxX && p.y in minY..maxY

  override fun toString(): String {
    return "RRectangle(Topleft: $topLeft, bottomRight: $bottomRight)"
  }
}
