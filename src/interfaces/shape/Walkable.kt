package interfaces.shape

import coordinate.Point

interface Walkable {

  /**
   * The shape can be walked point by point with a given step distance value.
   */
  fun walk(step: Double): List<Point>

  /**
   * The shape can be walked point by point with a given step distance value.
   *
   * This function also includes a block to map the points to anything else.
   */
  fun <T> walk(step: Double, block: (Point) -> T): List<T>
}
