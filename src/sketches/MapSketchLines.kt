package sketches

import appletExtensions.parallelLinesInBound
import arrow.core.memoize
import controls.panels.TabsBuilder.Companion.layerTab
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
import coordinate.coordSystems.getCoordinateMap
import de.lighti.clipper.Clipper.EndType
import de.lighti.clipper.Clipper.JoinType
import interfaces.shape.transform
import kotlinx.serialization.Serializable
import org.opencv.core.Mat
import sketches.base.LayeredCanvasSketch
import util.boundPercentAlong
import util.doIf
import util.image.opencvMat.ContourRetrievalMode.External
import util.image.opencvMat.bounds
import util.image.opencvMat.findContours
import util.image.opencvMat.gaussianBlur
import util.image.opencvMat.getSubPix
import util.image.opencvMat.rotate
import util.image.opencvMat.scale
import util.image.opencvMat.threshold
import util.io.geoJson.loadGeoMatAndBlurMemo
import util.iterators.deepMap
import util.iterators.groupValuesBy
import util.iterators.skipFirst
import util.mapIf
import util.polylines.PolyLine
import util.polylines.bound
import util.polylines.clipping.diff
import util.polylines.clipping.intersection
import util.polylines.clipping.offsetByMemo
import util.polylines.clipping.union
import util.polylines.length
import util.polylines.moveEndpoints
import util.polylines.simplify
import util.polylines.toSegment
import util.polylines.transform
import util.tuple.map
import java.awt.Color

/**
 * Draws a map with topology that can be offset to create a 3d effect.
 */
class MapSketchLines : LayeredCanvasSketch<MapLinesData, MapLinesLayerData>(
  "MapSketchLines",
  defaultGlobal = MapLinesData(),
  layerToDefaultTab = { MapLinesLayerData() },
) {

  private val MaxMoveAmount = 300

  init {
    numLayers = 1
  }

  override fun drawSetup(layerInfo: DrawInfo) {
  }

  private fun getTiffToScreenTransform(
    contourBounds: BoundRect,
    mapScale: Double,
    mapCenter: Point
  ) = getCoordinateMap(
    contourBounds,
    boundRect
      .scaled(mapScale).let { it.translated(mapCenter * it.size - (it.size / 2)) },
  )

  override fun drawOnce(layerInfo: LayerInfo) {}

  private fun loadScaleAndRotateMat(
    fileName: String,
    blurAmount: Double,
    scaleAndRotation: Pair<Point, Deg>,
  ): Mat = loadGeoMatAndBlurMemo(fileName, blurAmount)
    .scale(Point(scaleAndRotation.first))
    .rotate(scaleAndRotation.second, inPlace = true)

  val loadScaleAndRotateMatMemo = ::loadScaleAndRotateMat.memoize()

  override suspend fun SequenceScope<Unit>.drawLayers(layerInfo: DrawInfo) {
    val (geoTiffFile, drawMat, mapCenter, mapScale, minElevation, maxElevation, elevationMoveVector, samplePointsXY, drawMinElevationOutline, occludeLines, drawOceanLines, blurAmount, lineSimplifyEpsilon, imageRotation, oceanContours, maxOceanDistanceFromLand) = layerInfo.globalValues
    geoTiffFile ?: return

    val elevationRange = minElevation..maxElevation
    val elevationMoveAmount = elevationMoveVector.scaledVector(MaxMoveAmount)

    val mat = loadScaleAndRotateMatMemo(geoTiffFile, blurAmount, Point(mapScale) to imageRotation)

    val matThreshold = mat.threshold(minElevation)

    val matToScreen = getTiffToScreenTransform(mat.bounds, mapScale, mapCenter)
    val screenToMat = matToScreen.inverted()

    val minElevationContours = matThreshold.findContours().transform(matToScreen)

    fun getValue(p: Point): Double? = mat.getSubPix(p.transform(screenToMat))

    fun elevationPercent(p: Point) =
      (getValue(p) ?: minElevation).boundPercentAlong(elevationRange)

    // Horizontal lines from the bottom of the image to the top
    val linesBottomToTop: List<PolyLine> = boundRect
      .expand(elevationMoveAmount.abs() * 2)
      .parallelLinesInBound(Deg.HORIZONTAL, boundRect.height / samplePointsXY.yi)
      .sortedByDescending { it.p1.y }
      .map { it.asPolyLine }

    val matThresholdContours = matThreshold.findContours().transform(matToScreen).bound(boundRect)

    val maskedLines: Map<Double, List<PolyLine>> = linesBottomToTop
      .intersection(matThresholdContours)
      .filterNot { it.isEmpty() }
      .map { line ->
        val originalSegmentLength = line.length
        val segmentLengthRatio = originalSegmentLength / boundRect.width
        line
          .toSegment()
          .split(samplePointsXY.x * segmentLengthRatio)
      }.groupValuesBy { first().y }

    var unionShape: List<PolyLine> = listOf()
    maskedLines
      .map { (yValue, lines) ->
        lines
          .deepMap { point: Point -> point + (elevationMoveAmount * elevationPercent(point)) }
          .moveEndpoints { endpoints -> endpoints.map { it.withY(yValue) } }
          .simplify(lineSimplifyEpsilon)
      }
      .sortedByDescending { it.firstOrNull()?.firstOrNull()?.y ?: -1.0 }
      .mapIf(occludeLines) { lines ->
        lines.diff(unionShape, forceClosed = false).also {
          unionShape = unionShape.union(lines)
        }
      }
      .draw(boundRect)

    if (drawMat) matThreshold.draw(matToScreen, boundRect)
    if (drawMinElevationOutline) {
      minElevationContours
        .doIf(occludeLines) { it.diff(unionShape, forceClosed = false) }
        .draw(boundRect)
    }

    if (drawOceanLines) {
      yield(Unit)
      stroke(Color.BLUE)

      val simplifyAmount = oceanContours.simplifier.simplifyAmount

      val matContours = matThreshold
        .gaussianBlur(5)
        .findContours(retrievalMode = External)
        .transform(matToScreen)
        .simplify(simplifyAmount)

      val offsets = oceanContours.getThresholds(maxOceanDistanceFromLand).skipFirst()

      matContours
        .offsetByMemo(offsets, simplifyAmount, JoinType.ROUND, EndType.CLOSED_POLYGON)
        .values
        .mapIf(occludeLines) { lines -> lines.diff(unionShape, forceClosed = false) }
        .draw(boundRect)
    }
  }
}

@Serializable
data class MapLinesLayerData(
  var exampleTabField: Int = 1,
) : PropData<MapLinesLayerData> {
  override fun bind() = layerTab {
    slider(::exampleTabField, 0..10)
  }

  override fun clone() = copy()
  override fun toSerializer() = serializer()
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
      panel(::oceanContours)
      slider(::maxOceanDistanceFromLand, 0..1000)
    }
  }

  override fun clone() = copy()

  override fun toSerializer() = serializer()
}

fun main() = MapSketchLines().run()
