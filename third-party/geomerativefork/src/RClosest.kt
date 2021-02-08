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

/**
 * This class allows contains information about an evaluation of the closest points or intersections between shapes/paths/commands.
 */
class RClosest {
  var intersects: Array<RPoint> = arrayOf()
  var closest: Array<RPoint> = arrayOf()

  // TODO: get here the max value of an integer
  var distance = 10000f
  var advancements: FloatArray = floatArrayOf()
  fun update(other: RClosest) {
    if (other.intersects.isEmpty()) {
      if (other.distance > distance) return
      distance = other.distance
      closest = other.closest
      advancements = other.advancements
    } else {
      closest = arrayOf()
      advancements = floatArrayOf()
      distance = 0f
      intersects += other.intersects.clone()
    }
  }
}