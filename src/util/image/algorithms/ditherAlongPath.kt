package util.image.algorithms

import coordinate.Point
import org.opencv.core.Mat
import util.image.opencvMat.get
import util.iterators.PolyLineIterator
import util.polylines.PolyLine
import util.polylines.chunkedByDistance
import util.polylines.iterators.mergeConsecutiveIfConnected
import util.polylines.length
import util.polylines.walk

private const val DefaultDitherStep = 5.0
private const val DefaultDitherChunkSize = 15.0
private const val DitherSegmentsPerOneHundredPixels = 6

// TODO: Implement this
private const val MinBlankSpotDistance = 10.0

private data class PointAndValue(val point: Point, val value: Double?)

private fun Mat.getPointAndValue(p: Point) = PointAndValue(p, get(p))

fun PolyLine.ditherConsistently(
  percentInked: Double, // between 0 and 1
  numDitherSegments: Int = DitherSegmentsPerOneHundredPixels
): List<PolyLine> {

  if (percentInked >= 0.95) {
    return listOf(this)
  }

  if (percentInked <= 0) {
    return listOf()
  }

  val pathLength = length
  val numSegments = (pathLength / 100.0) * numDitherSegments

  val inkSegmentDist = (pathLength * percentInked) / numSegments
  val blankSegmentDist = (pathLength * (1 - percentInked)) / numSegments

  val pathIterator = PolyLineIterator(this)

  val inkPaths = mutableListOf<PolyLine>()
  while (!pathIterator.atEnd()) {
    val pathChunk = pathIterator.move(inkSegmentDist)
    inkPaths.add(pathChunk.points)
    pathIterator.move(blankSegmentDist)
  }

  return inkPaths
}

fun Mat.ditherAlongPath(
  path: PolyLine,
  step: Double = DefaultDitherStep,
  ditherChunkSize: Double = DefaultDitherChunkSize
): List<PolyLine> {
  val steppedPath = path.walk(step)
  val chunks = steppedPath.chunkedByDistance(ditherChunkSize)

  return chunks.map { chunk ->
    val inkPercent = 1 - (averageAlongPath(chunk, 1.0) / 255.0)
    chunk.ditherConsistently(inkPercent)
  }.flatten()
    .mergeConsecutiveIfConnected()
}

