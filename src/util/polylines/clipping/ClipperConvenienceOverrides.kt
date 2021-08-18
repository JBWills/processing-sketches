package util.polylines.clipping

import util.polylines.PolyLine

@JvmName("clipperIntersectionPolyLine")
fun List<PolyLine>.clipperIntersection(other: PolyLine, forceClosedValue: Boolean? = null) =
  clipperIntersection(listOf(other), forceClosedValue)

@JvmName("clipperDiffPolyLine")
fun List<PolyLine>.clipperDiff(other: PolyLine, forceClosedValue: Boolean? = null) =
  clipperDiff(listOf(other), forceClosedValue)

@JvmName("clipperUnionPolyLine")
fun List<PolyLine>.clipperUnion(other: PolyLine, forceClosedValue: Boolean? = null) =
  clipperUnion(listOf(other), forceClosedValue)

@JvmName("clipperXorPolyLine")
fun List<PolyLine>.clipperXor(other: PolyLine, forceClosedValue: Boolean? = null) =
  clipperXor(listOf(other), forceClosedValue)

@JvmName("singleClipperIntersection")
fun PolyLine.clipperIntersection(other: PolyLine, forceClosedValue: Boolean? = null) =
  listOf(this).clipperIntersection(other, forceClosedValue)

@JvmName("singleClipperDiff")
fun PolyLine.clipperDiff(other: PolyLine, forceClosedValue: Boolean? = null) =
  listOf(this).clipperDiff(other, forceClosedValue)

@JvmName("singleClipperUnion")
fun PolyLine.clipperUnion(other: PolyLine, forceClosedValue: Boolean? = null) =
  listOf(this).clipperUnion(other, forceClosedValue)

@JvmName("singleClipperXor")
fun PolyLine.clipperXor(other: PolyLine, forceClosedValue: Boolean? = null) =
  listOf(this).clipperXor(other, forceClosedValue)

@JvmName("singleClipperIntersectionMulti")
fun PolyLine.clipperIntersection(other: List<PolyLine>, forceClosedValue: Boolean? = null) =
  listOf(this).clipperIntersection(other, forceClosedValue)

@JvmName("singleClipperDiffMulti")
fun PolyLine.clipperDiff(other: List<PolyLine>, forceClosedValue: Boolean? = null) =
  listOf(this).clipperDiff(other, forceClosedValue)

@JvmName("singleClipperUnionMulti")
fun PolyLine.clipperUnion(other: List<PolyLine>, forceClosedValue: Boolean? = null) =
  listOf(this).clipperUnion(other, forceClosedValue)

@JvmName("singleClipperXorMulti")
fun PolyLine.clipperXor(other: List<PolyLine>, forceClosedValue: Boolean? = null) =
  listOf(this).clipperXor(other, forceClosedValue)
