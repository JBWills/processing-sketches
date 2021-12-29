package util.comparators

import coordinate.Point

class PointComparator : Comparator<Point> {
  override fun compare(o1: Point?, o2: Point?): Int {
    if (o1 == null && o2 == null) return 0

    if (o1 == null) return -1
    if (o2 == null) return 1

    if (o1 == o2) return 0

    return if (o1.x == o2.x) {
      if (o1.y == o2.y) {
        0
      } else if (o1.y < o2.y) {
        -1
      } else {
        1
      }
    } else if (o1.x < o2.x) {
      -1
    } else {
      1
    }
  }
}
