package util.polylines.clipping

import arrow.core.memoize
import de.lighti.clipper.Clipper.EndType
import de.lighti.clipper.Clipper.EndType.CLOSED_POLYGON
import de.lighti.clipper.Clipper.JoinType
import de.lighti.clipper.Clipper.JoinType.ROUND
import de.lighti.clipper.ClipperOffset
import de.lighti.clipper.Paths
import util.polylines.PolyLine
import util.polylines.simplify

/**
 * See http://www.angusj.com/delphi/clipper/documentation/Docs/Units/ClipperLib/Classes/ClipperOffset/Properties/MiterLimit.htm
 * for more information on miter limits
 */
const val DefaultMiterLimit: Double = 2.0

/**
 * See: http://www.angusj.com/delphi/clipper/documentation/Docs/Units/ClipperLib/Classes/ClipperOffset/Properties/ArcTolerance.htm
 * for more information on arc tolerance.
 */
const val DefaultArcTolerance = 0.25

fun List<PolyLine>.getOffsetClipper(
  joinType: JoinType = ROUND,
  endType: EndType = CLOSED_POLYGON
): ClipperOffset = ClipperOffset(DefaultMiterLimit, DefaultArcTolerance)
  .apply { addPaths(toClipperPaths(), joinType, endType) }

val getClipperMemo: List<PolyLine>.(JoinType, EndType) -> ClipperOffset =
  List<PolyLine>::getOffsetClipper.memoize()

fun ClipperOffset.offset(amount: Double): List<PolyLine> = Paths().also { resultPaths ->
  execute(resultPaths, amount)
}.toPolyLines(closed = true)

fun List<PolyLine>.offsetBy(
  amounts: Iterable<Double>,
  simplifySigma: Double = 0.0,
  joinType: JoinType,
  endType: EndType,
): Map<Double, List<PolyLine>> {
  val clipper = getClipperMemo(joinType, endType)
  return amounts.associateBy({ it }) { offsetAmount ->
    clipper.offset(offsetAmount).simplify(simplifySigma)
  }
}

val offsetByMemo: List<PolyLine>.(Iterable<Double>, Double, JoinType, EndType) -> Map<Double, List<PolyLine>> =
  List<PolyLine>::offsetBy.memoize()

fun List<PolyLine>.offset(
  amount: Double,
  joinType: JoinType = ROUND,
  endType: EndType = CLOSED_POLYGON
) = getOffsetClipper(joinType, endType).offset(amount)

