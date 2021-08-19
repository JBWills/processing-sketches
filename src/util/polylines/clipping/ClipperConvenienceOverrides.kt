package util.polylines.clipping

import coordinate.Segment
import util.polylines.PolyLine
import util.polylines.toSegment

@JvmName("clipperIntersectionPolyLine")
fun List<PolyLine>.intersection(other: PolyLine, forceClosed: Boolean? = null) =
  intersection(listOf(other), forceClosed)

@JvmName("clipperDiffPolyLine")
fun List<PolyLine>.diff(other: PolyLine, forceClosed: Boolean? = null) =
  diff(listOf(other), forceClosed)

@JvmName("clipperUnionPolyLine")
fun List<PolyLine>.union(other: PolyLine, forceClosed: Boolean? = null) =
  union(listOf(other), forceClosed)

@JvmName("clipperXorPolyLine")
fun List<PolyLine>.xor(other: PolyLine, forceClosed: Boolean? = null) =
  xor(listOf(other), forceClosed)

@JvmName("singleClipperIntersection")
fun PolyLine.intersection(other: PolyLine, forceClosed: Boolean? = null) =
  listOf(this).intersection(other, forceClosed)

@JvmName("singleClipperDiff")
fun PolyLine.diff(other: PolyLine, forceClosed: Boolean? = null) =
  listOf(this).diff(other, forceClosed)

@JvmName("singleClipperUnion")
fun PolyLine.union(other: PolyLine, forceClosed: Boolean? = null) =
  listOf(this).union(other, forceClosed)

@JvmName("singleClipperXor")
fun PolyLine.xor(other: PolyLine, forceClosed: Boolean? = null) =
  listOf(this).xor(other, forceClosed)

@JvmName("singleClipperIntersectionMulti")
fun PolyLine.intersection(other: List<PolyLine>, forceClosed: Boolean? = null) =
  listOf(this).intersection(other, forceClosed)

@JvmName("singleClipperDiffMulti")
fun PolyLine.diff(other: List<PolyLine>, forceClosed: Boolean? = null) =
  listOf(this).diff(other, forceClosed)

@JvmName("singleClipperUnionMulti")
fun PolyLine.union(other: List<PolyLine>, forceClosed: Boolean? = null) =
  listOf(this).union(other, forceClosed)

@JvmName("singleClipperXorMulti")
fun PolyLine.xor(other: List<PolyLine>, forceClosed: Boolean? = null) =
  listOf(this).xor(other, forceClosed)

fun List<Segment>.intersection(other: List<PolyLine>): List<Segment> = map { it.toPolyLine() }
  .intersection(other, forceClosed = false)
  .map { it.toSegment() }
