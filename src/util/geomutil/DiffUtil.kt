package util.geomutil

import arrow.core.memoize
import coordinate.BoundRect
import coordinate.Circ
import coordinate.Point
import geomerativefork.src.RPath


fun List<Point>.diff(path: RPath) = diffMemoized(path)

fun List<Point>.intersection(path: RPath): List<List<Point>> = intersectionMemoized(path)

private val List<Point>.diffMemoized: (path: RPath) -> List<List<Point>>
  get() = { path: RPath ->
    toRPath(closed = !isEmpty() && first() == last())
      .diff(path)
      .toPoints()
  }.memoize()

private val List<Point>.intersectionMemoized: (path: RPath) -> List<List<Point>>
  get() = { path: RPath ->
    toRPath(closed = !isEmpty() && first() == last())
      .intersection(path)
      .toPoints()
  }.memoize()

fun List<Point>.intersection(r: BoundRect): List<List<Point>> = intersection(r.toRPath())

fun List<Point>.intersection(c: Circ): List<List<Point>> =
  intersection(c.toRPath().also { it.polygonize() })

fun List<Point>.diff(r: BoundRect): List<List<Point>> = diff(r.toRPath())

fun List<Point>.diff(c: Circ): List<List<Point>> = diff(c.toRPath())
