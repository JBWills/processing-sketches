package sketches

import appletExtensions.parallelLinesInBound
import arrow.core.memoize
import controls.panels.TabsBuilder.Companion.tabs
import controls.panels.panelext.fileSelect
import controls.panels.panelext.slider
import controls.panels.panelext.slider2D
import controls.panels.panelext.sliderPair
import controls.panels.panelext.toggle
import controls.props.PropData
import controls.props.types.ContourProp
import controls.props.types.VectorProp
import coordinate.BoundRect
import coordinate.Deg
import coordinate.Point
import coordinate.Segment
import coordinate.coordSystems.getCoordinateMap
import de.lighti.clipper.Clipper.ClipType
import de.lighti.clipper.Clipper.ClipType.INTERSECTION
import de.lighti.clipper.Clipper.ClipType.UNION
import de.lighti.clipper.Clipper.EndType
import de.lighti.clipper.Clipper.JoinType
import interfaces.shape.transform
import kotlinx.serialization.Serializable
import org.opencv.core.Mat
import sketches.base.SimpleCanvasSketch
import util.base.doIf
import util.boundPercentAlong
import util.image.opencvMat.bounds
import util.image.opencvMat.findContours
import util.image.opencvMat.gaussianBlur
import util.image.opencvMat.getSubPix
import util.image.opencvMat.rotate
import util.image.opencvMat.scale
import util.image.opencvMat.threshold
import util.io.geoJson.loadGeoMatAndBlurMemo
import util.iterators.deepMap
import util.iterators.groupToSortedList
import util.polylines.PolyLine
import util.polylines.bound
import util.polylines.clipping.ClipperPaths.Companion.asPaths
import util.polylines.clipping.ForceClosedOption.Close
import util.polylines.clipping.ForceClosedOption.NoClose
import util.polylines.clipping.clip
import util.polylines.clipping.offsetByMemo
import util.polylines.moveEndpoints
import util.polylines.simplify
import util.polylines.transform
import util.tuple.map
import java.awt.Color

/**
 * Draws a map with topology that can be offset to create a 3d effect.
 */
class MapSketchLines : SimpleCanvasSketch<MapLinesData>("MapSketchLines", MapLinesData()) {

  val MaxMoveAmount = 300

  private fun getTiffToScreenTransform(
    contourBounds: BoundRect,
    mapScale: Double,
    mapCenter: Point
  ) = getCoordinateMap(
    contourBounds,
    boundRect
      .scaled(mapScale).let { it.translated(mapCenter * it.size - (it.size / 2)) },
  )

  /**
   * Given a list of split lines, occlude them, assuming the outer list is sorted front-to-back.
   *
   * @return a pair where:
   *  The first element is the unionShape of all the occlusions
   *  The second element is a nested list, where each sub-list is the occluded elevation lines at a
   *  certain height on the image.
   */
  private fun List<List<PolyLine>>.occludeElevationLines(
    shrinkUnionShapeBy: Double = 0.0,
  ): Pair<List<PolyLine>, List<List<PolyLine>>> {
    var unionShape: List<PolyLine> = listOf()
    val occludedLines = map { linesAtElevation ->
      linesAtElevation.clip(unionShape, ClipType.DIFFERENCE, NoClose).also {
        unionShape = unionShape.clip(linesAtElevation, UNION)
      }
    }

    val shrunkUnionShape = unionShape.doIf(shrinkUnionShapeBy != 0.0) {
      unionShape.offsetByMemo(
        listOf(shrinkUnionShapeBy),
        JoinType.ROUND,
        EndType.CLOSED_POLYGON,
      ).values.flatten()
    }

    return Pair(shrunkUnionShape, occludedLines)
  }

  private fun Segment.convertToElevationLine(step: Double, getDelta: (Point) -> Point): PolyLine {
    val elevationLine = walk(step) { point -> point + getDelta(point) }

    return elevationLine.moveEndpoints { endpoints -> endpoints.map { it.withY(p1.y) } }
  }

  private fun loadScaleAndRotateMat(
    fileName: String,
    blurAmount: Double,
    scaleAndRotation: Pair<Point, Deg>,
  ): Mat = loadGeoMatAndBlurMemo(fileName, blurAmount)
    .scale(Point(scaleAndRotation.first))
    .rotate(scaleAndRotation.second, inPlace = true)

  val loadScaleAndRotateMatMemo = ::loadScaleAndRotateMat.memoize()

  override suspend fun SequenceScope<Unit>.drawLayers(drawInfo: DrawInfo) {
    val (geoTiffFile, drawMat, mapCenter, mapScale, minElevation, maxElevation, elevationMoveVector, samplePointsXY, drawMinElevationOutline, occludeLines, drawOceanLines, blurAmount, lineSimplifyEpsilon, imageRotation, oceanContours, initialOceanLinesOffset, maxOceanDistanceFromLand) = drawInfo.dataValues
    geoTiffFile ?: return

    val elevationRange = minElevation..maxElevation
    val elevationMoveAmount = elevationMoveVector.scaledVector(MaxMoveAmount)

    val mat = loadScaleAndRotateMatMemo(geoTiffFile, blurAmount, Point(mapScale) to imageRotation)

    val matThreshold = mat.threshold(minElevation)

    val matToScreen = getTiffToScreenTransform(mat.bounds, mapScale, mapCenter)
    val screenToMat = matToScreen.inverted()

    val xyStep = boundRect.size / samplePointsXY

    fun getValue(p: Point): Double? = mat.getSubPix(p.transform(screenToMat))

    fun elevationDelta(p: Point) =
      elevationMoveAmount * (getValue(p) ?: minElevation).boundPercentAlong(elevationRange)

    val expandedBoundRect = boundRect.expand(elevationMoveAmount.abs() * 2)

    // Horizontal lines from the bottom of the image to the top
    val linesBottomToTop: List<Segment> =
      expandedBoundRect
        .parallelLinesInBound(Deg.HORIZONTAL, boundRect.height / samplePointsXY.yi)
        .sortedByDescending { it.p1.y }

    val matThresholdContours = matThreshold
      .findContours(lineSimplifyEpsilon)
      .transform(matToScreen)
      .bound(expandedBoundRect, Close)

    val maskedLines: List<List<Segment>> = linesBottomToTop
      .clip(matThresholdContours.asPaths(), INTERSECTION, NoClose)
      .groupToSortedList(sortDescending = true) { p1.y }

    val (unionShape: List<PolyLine>, elevationLines: List<List<PolyLine>>) =
      maskedLines
        .deepMap { segment -> segment.convertToElevationLine(xyStep.x, ::elevationDelta) }
        .simplify(lineSimplifyEpsilon)
//        .deepMap { it.interpolate(5.0) }
        .doIf(
          occludeLines,
          ifTrue = { it.occludeElevationLines(-1.0) },
          ifFalse = { Pair(listOf(), it) },
        )

    elevationLines
      .draw(boundRect)

    if (drawMat) matThreshold.draw(matToScreen, boundRect)
    if (drawMinElevationOutline) {
      matThresholdContours
        .doIf(occludeLines) { it.clip(unionShape, ClipType.DIFFERENCE, NoClose) }
        .draw(boundRect)
    }

    if (drawOceanLines) {
      yield(Unit)
      stroke(Color.BLUE)

      val simplifyAmount = oceanContours.simplifier.simplifyAmount

      val matContours = matThreshold
        .gaussianBlur(3)
        .findContours(simplifyAmount)
        .transform(matToScreen)

      val offsets =
        oceanContours.getThresholds(maxOceanDistanceFromLand).map { it + initialOceanLinesOffset }

      matContours
        .offsetByMemo(offsets, JoinType.ROUND, EndType.CLOSED_POLYGON)
        .values
        .flatten()
        .simplify(simplifyAmount)
        .clip(boundRect.toPolyLine(), INTERSECTION, NoClose)
        .doIf(occludeLines) { it.clip(unionShape, ClipType.DIFFERENCE, NoClose) }
        .draw(boundRect)
    }
  }
}

@Serializable
data class MapLinesData(
  var geoTiffFile: String? = null,
  var drawMat: Boolean = false,
  var mapCenter: Point = Point.Zero,
  var mapScale: Double = 1.0,
  var minElevation: Double = 0.0,
  var maxElevation: Double = 5000.0,
  var elevationMoveVector: VectorProp = VectorProp(Point.Zero, 0.0),
  var samplePointsXY: Point = Point(2, 2),
  var drawMinElevationOutline: Boolean = true,
  var occludeLines: Boolean = true,
  var drawOceanLines: Boolean = true,
  var blurAmount: Double = 0.0,
  var lineSimplifyEpsilon: Double = 0.0,
  var imageRotation: Deg = Deg(0),
  var oceanContours: ContourProp = ContourProp(),
  var initialOceanLinesOffset: Int = 10,
  var maxOceanDistanceFromLand: Double = 500.0,
) : PropData<MapLinesData> {
  override fun bind() = tabs {
    tab("Map") {
      row {
        fileSelect(::geoTiffFile)
        toggle(::drawMat)
      }

      slider(::blurAmount, 0.0..1000.0)

      row {
        heightRatio = 5
        slider2D(::mapCenter, -1..1).withHeight(3)
      }
      slider(::mapScale, 0.1..10.0)
      slider(::imageRotation)
    }

    tab("Lines") {
      row {
        toggle(::occludeLines)
        toggle(::drawMinElevationOutline)
      }
      slider(::lineSimplifyEpsilon, 0..2)
      row {
        slider(::minElevation, 0.1..5000.0)
        slider(::maxElevation, 0..5000)
      }
      panel(::elevationMoveVector)
      sliderPair(::samplePointsXY, 1.0..3000.0)
    }

    tab("ocean") {
      toggle(::drawOceanLines)
      slider(::initialOceanLinesOffset, 0..50)
      panel(::oceanContours)
      slider(::maxOceanDistanceFromLand, 0..2000)
    }
  }

  override fun clone() = copy()

  override fun toSerializer() = serializer()
}

fun main() = MapSketchLines().run()
