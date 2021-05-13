package util.geomutil

import arrow.core.memoize
import geomerativefork.src.RPath
import interfaces.shape.Maskable
import util.pointsAndLines.polyLine.PolyLine
import util.pointsAndLines.polyLine.isClosed


fun PolyLine.diff(path: RPath, memoized: Boolean = false) =
  if (memoized) diffMemoizedBase(this, path) else diffBase(path)

fun PolyLine.intersection(path: RPath, memoized: Boolean = false) =
  if (memoized) intersectionMemoizedBase(this, path) else intersectionBase(path)

private fun PolyLine.intersectionBase(path: RPath): List<PolyLine> =
  toRPath(closed = isClosed())
    .intersection(path)
    .toPoints()

private fun PolyLine.diffBase(path: RPath): List<PolyLine> =
  toRPath(closed = isClosed())
    .diff(path)
    .toPoints()

private val diffMemoizedBase = { polyLine: PolyLine, path: RPath ->
  polyLine.diffBase(path)
}.memoize()

private val intersectionMemoizedBase = { polyLine: PolyLine, path: RPath ->
  polyLine.intersectionBase(path)
}.memoize()

private val maskableIntersectionMemoized = { polyLine: PolyLine, maskable: Maskable ->
  maskable.intersection(polyLine)
}.memoize()

private val maskableDiffMemoized = { polyLine: PolyLine, maskable: Maskable ->
  maskable.diff(polyLine)
}.memoize()

fun PolyLine.intersection(r: Maskable): List<PolyLine> = maskableIntersectionMemoized(this, r)

fun PolyLine.diff(r: Maskable): List<PolyLine> = maskableDiffMemoized(this, r)
