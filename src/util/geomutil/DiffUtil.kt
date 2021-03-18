package util.geomutil

import coordinate.BoundRect
import coordinate.Circ
import coordinate.Point
import geomerativefork.src.RPath

fun List<Point>.diff(path: RPath): List<List<Point>> =
  toRPath(closed = !isEmpty() && first() == last())
    .diff(path)
    .map { it.points.map { (x, y) -> Point(x, y) } }

fun List<Point>.intersection(path: RPath): List<List<Point>> =
  toRPath(closed = !isEmpty() && first() == last())
    .intersection(path)
    .map { it.points.map { (x, y) -> Point(x, y) } }

fun List<Point>.intersection(r: BoundRect): List<List<Point>> = intersection(r.toRPath())

fun List<Point>.intersection(c: Circ): List<List<Point>> = intersection(c.toRPath())

fun List<Point>.diff(r: BoundRect): List<List<Point>> = diff(r.toRPath())

fun List<Point>.diff(c: Circ): List<List<Point>> = diff(c.toRPath())
