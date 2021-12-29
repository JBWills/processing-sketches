package util.comparators

import coordinate.Point

private const val ApproxDist = 5

class ApproxPointComparator : OrderedComparator<Point>(
  getFieldList = listOf({ it.x }, { it.y }),
  customComparator = ApproxComparator(ApproxDist),
)
