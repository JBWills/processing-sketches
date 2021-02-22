package util.algorithms

import ca.pjer.ekmeans.EKmeans
import coordinate.Point
import util.letWith

fun Iterable<Point>.to2DDoubleArray(): Array<DoubleArray> = map { doubleArrayOf(it.x, it.y) }.toTypedArray()


fun Iterable<Point>.kMeans(centroids: List<Point>, equalCardinality: Boolean = false): List<Set<Point>> {
  val list: List<Point> = if (this is List<Point>) this else this.toList()
  val assignments: IntArray = EKmeans(centroids.to2DDoubleArray(), to2DDoubleArray()).letWith {
    iteration = 32
    isEqual = equalCardinality
    run()
    return@letWith assignments
  }

  val clusters: Array<MutableSet<Point>> = Array(centroids.size) { mutableSetOf() }

  assignments.mapIndexed { pointIndex, clusterInt ->
    if (pointIndex == -1 || clusterInt == -1) {

      println("One of the indexes are negative: pointIndex: $pointIndex, clusterint: $clusterInt")
    } else if (clusters.size <= clusterInt) {
      println("trying to assign index: $clusterInt to cluster array of size: ${clusters.size}")
    } else if (list.size <= pointIndex) {
      println("trying to assign index: $pointIndex to list array of size: ${list.size}")
    } else {
      clusters[clusterInt].add(list[pointIndex])
    }
  }

  return clusters.toList()
}
