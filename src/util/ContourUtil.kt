package util

import conrec.Conrec
import coordinate.BoundRect
import coordinate.Point
import fastnoise.Noise
import util.iterators.mapArray
import util.iterators.replaceKey
import java.util.LinkedList

fun Noise.noiseContour(bounds: BoundRect, threshold: Double): List<List<Point>> =
  noiseContours(bounds, listOf(threshold))
    .getOrDefault(threshold, listOf())

fun Noise.noiseContours(
  bounds: BoundRect,
  thresholds: List<Double>
): Map<Double, List<List<Point>>> {
  val contours = thresholds.map { it to mutableListOf<LinkedList<Point>>() }.toMap()
  val startPointsToIndices = thresholds.map { it to mutableMapOf<Point, Int>() }.toMap()
  val endPointsToIndices = thresholds.map { it to mutableMapOf<Point, Int>() }.toMap()
  contour(toValueMatrix(bounds), thresholds) { startPoint, endPoint, contourLevel ->
    val contourLines = contours.getValue(contourLevel)
    val startPointsMap = startPointsToIndices.getValue(contourLevel)
    val endPointsMap = endPointsToIndices.getValue(contourLevel)
    if (startPointsMap.size < 25)
      p(startPoint, endPoint, startPointsMap, endPointsMap)

    if (startPoint == endPoint) {
      println("here0")
      return@contour
    } else if (startPoint in startPointsMap && endPoint in endPointsMap) {
      println("here1")
      val polyIndexStart = startPointsMap.getValue(startPoint)
      val polyIndexEnd = endPointsMap.getValue(endPoint)
      startPointsMap.remove(startPoint)
      endPointsMap.remove(endPoint)
      contourLines[polyIndexEnd] =
        LinkedList(contourLines[polyIndexEnd] + contourLines[polyIndexStart])
    } else if (startPoint in endPointsMap && endPoint in startPointsMap) {
      println("here2")
      val polyIndexStart = startPointsMap.getValue(endPoint)
      val polyIndexEnd = endPointsMap.getValue(startPoint)
      startPointsMap.remove(startPoint)
      endPointsMap.remove(endPoint)
      contourLines[polyIndexEnd] =
        LinkedList(contourLines[polyIndexEnd] + contourLines[polyIndexStart])
    } else if (startPoint in startPointsMap && endPoint in startPointsMap) {
      println("here3")
      val polyIndexStart = startPointsMap.getValue(endPoint)
      val polyIndexEnd = startPointsMap.getValue(startPoint)
      startPointsMap.remove(startPoint)
      startPointsMap.remove(endPoint)
      contourLines[polyIndexEnd] =
        LinkedList(contourLines[polyIndexEnd].reversed() + contourLines[polyIndexStart])
    } else if (startPoint in endPointsMap && endPoint in endPointsMap) {
      println("here4")
      val polyIndexStart = endPointsMap.getValue(endPoint)
      val polyIndexEnd = endPointsMap.getValue(startPoint)
      endPointsMap.remove(startPoint)
      endPointsMap.remove(endPoint)
      contourLines[polyIndexEnd] =
        LinkedList(contourLines[polyIndexEnd] + contourLines[polyIndexStart].reversed())
    } else if (startPoint in startPointsMap || endPoint in startPointsMap) {
      println("here5")
      val (existingPoint, newPoint) =
        if (startPoint in startPointsMap) startPoint to endPoint
        else endPoint to startPoint
      val polyIndex = startPointsMap.getValue(existingPoint)
      startPointsMap.replaceKey(existingPoint, newPoint)
      contourLines[polyIndex].addFirst(newPoint)
    } else if (startPoint in endPointsMap || endPoint in endPointsMap) {
      println("here6")
      val (existingPoint, newPoint) =
        if (startPoint in endPointsMap) startPoint to endPoint
        else endPoint to startPoint
      val polyIndex = endPointsMap.getValue(existingPoint)
      endPointsMap.replaceKey(existingPoint, newPoint)
      contourLines[polyIndex].addLast(newPoint)
    } else {
      println("here7")
      contourLines.add(LinkedList<Point>(listOf(startPoint, endPoint)))
      startPointsMap[startPoint] = contourLines.size - 1
      endPointsMap[endPoint] = contourLines.size - 1
    }
//    val res2 = thresholds.map { it to mutableListOf(mutableListOf<Point>()) }.toMap()
//    val segment = Segment(startPoint, endPoint) + bounds.topLeft
//
//    val contours = res2.getValue(contourLevel)
//    val lastContourLine = contours.last()
//    if (lastContourLine.isEmpty()) {
//      lastContourLine.addAll(segment.points)
//    } else if (lastContourLine.last() == segment.p1) {
//      lastContourLine.add(segment.p2)
//    } else if (lastContourLine.last() == segment.p2) {
//      lastContourLine.add(segment.p1)
//    } else if (lastContourLine.size == 1 && lastContourLine.first() == segment.p1) {
//      contours.removeLast()
//      contours.add(mutableListOf(lastContourLine.last(), lastContourLine.first(), segment.p2))
//    } else if (lastContourLine.size == 1 && lastContourLine.first() == segment.p2) {
//      contours.removeLast()
//      contours.add(mutableListOf(lastContourLine.last(), lastContourLine.first(), segment.p1))
//    } else {
//      contours.add(mutableListOf(*segment.points))
//    }
  }

  p(contours.mapValues { u -> "threshold: ${u.key}" to "Size: ${u.value.size}" })

  return contours.mapValues { (_, value) -> value.toList() }
}

fun contour(
  doubles: List<List<Double>>,
  thresholds: List<Double>,
  block: (startPoint: Point, endPoint: Point, contourLevel: Double) -> Unit
) =
  Conrec { startX, startY, endX, endY, contourLevel ->
    block(
      Point(startX, startY),
      Point(endX, endY),
      contourLevel
    )
  }.contour(
    doubles.mapArray { it.toDoubleArray() },
    0,
    doubles.size - 1,
    0,
    (doubles.getOrNull(0)?.size ?: 0) - 1,
    doubles.indices.map { it.toDouble() }.toDoubleArray(),
    (doubles.getOrNull(0) ?: listOf()).indices.map { it.toDouble() }.toDoubleArray(),
    thresholds.size,
    thresholds.toDoubleArray(),
  )
