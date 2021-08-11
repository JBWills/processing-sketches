package sketches

import appletExtensions.parallelLinesInBound
import controls.panels.TabsBuilder.Companion.layerTab
import controls.panels.TabsBuilder.Companion.tabs
import controls.panels.panelext.fileSelect
import controls.panels.panelext.slider
import controls.panels.panelext.slider2D
import controls.panels.panelext.sliderPair
import controls.panels.panelext.toggle
import controls.props.PropData
import controls.props.types.VectorProp
import coordinate.BoundRect
import coordinate.Deg
import coordinate.Point
import coordinate.coordSystems.getCoordinateMap
import geomerativefork.src.util.deepDeepMap
import interfaces.shape.transform
import kotlinx.serialization.Serializable
import org.opencv.core.Mat
import sketches.base.LayeredCanvasSketch
import util.boundPercentAlong
import util.image.ImageFormat
import util.image.opencvMat.bounds
import util.image.opencvMat.fillPoly
import util.image.opencvMat.findContours
import util.image.opencvMat.gaussianBlur
import util.image.opencvMat.getSubPix
import util.image.opencvMat.maskedByImage
import util.image.opencvMat.rotate
import util.image.opencvMat.scale
import util.image.opencvMat.subtract
import util.image.opencvMat.threshold
import util.io.geoJson.loadGeoMatAndBlurMemo
import util.polylines.PolyLine
import util.polylines.bound
import util.polylines.expandEndpointsToMakeMask
import util.polylines.simplify
import util.polylines.toSegment
import util.polylines.transform

/**
 * Draws a map with topology that can be offset to create a 3d effect.
 */
class MapSketchLines : LayeredCanvasSketch<MapLinesData, MapLinesLayerData>(
  "MapSketchLines",
  defaultGlobal = MapLinesData(),
  layerToDefaultTab = { MapLinesLayerData() },
) {

  private val MaxMoveAmount = 300

  private var currMat: Mat? = null

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

  override suspend fun SequenceScope<Unit>.drawLayers(layerInfo: DrawInfo) {
    val (geoTiffFile, drawMat, mapCenter, mapScale, minElevation, maxElevation, elevationMoveVector, samplePointsXY, drawMinElevationOutline, drawUnionMat, occludeLines, drawOceanLines, blurAmount, lineSimplifyEpsilon, imageRotation, shrinkLinesHorizontally) = layerInfo.globalValues
    geoTiffFile ?: return

    val elevationRange = minElevation..maxElevation
    val elevationMoveAmount = elevationMoveVector.scaledVector(MaxMoveAmount)

    currMat = loadGeoMatAndBlurMemo(geoTiffFile, blurAmount)
    val mat = currMat
      ?.scale(Point(mapScale))
      ?.rotate(imageRotation, inPlace = true) ?: return

    val matThreshold = mat.threshold(minElevation).subtract(mat.threshold(maxElevation))

    val matToScreen = getTiffToScreenTransform(mat.bounds, mapScale, mapCenter)
    val screenToMat = matToScreen.inverted()

    fun getValue(p: Point): Double? = mat.getSubPix(p.transform(screenToMat))

    fun elevationPercent(p: Point) =
      (getValue(p) ?: minElevation).boundPercentAlong(elevationRange)

    // Horizontal lines from the bottom of the image to the top
    val linesBottomToTop: List<PolyLine> = boundRect
      .expand(elevationMoveAmount.abs() * 2)
      .parallelLinesInBound(Deg.HORIZONTAL, boundRect.height / samplePointsXY.yi)
      .sortedByDescending { it.p1.y }
      .map { it.asPolyLine }

    // straight lines masked by the minElevation image
    val maskedLinesBottomToTop: List<List<PolyLine>> = linesBottomToTop
      .transform(screenToMat)
      .map { line ->
        line.toSegment()
          .maskedByImage(matThreshold, inverted = false)
          .map {
            val originalSegmentLength = it.length
            val segmentLengthRatio = originalSegmentLength / boundRect.width
            it.expand(-shrinkLinesHorizontally * 2)
              .split(samplePointsXY.x * segmentLengthRatio)
          }
      }
      .transform(matToScreen)

    // Masked and morphed lines based on elevation values.
    val elevationLinesBottomToTop: List<List<PolyLine>> = maskedLinesBottomToTop
      .deepDeepMap { point: Point -> point + (elevationMoveAmount * elevationPercent(point)) }
      .map {
        it.simplify(lineSimplifyEpsilon)
      }

    val unionMat = Mat.zeros(windowBounds.size.toSize(), ImageFormat.Gray.openCVFormat)

    if (occludeLines || drawUnionMat) {
      elevationLinesBottomToTop.forEachIndexed { index, elevationLine ->
        val minYPosition = linesBottomToTop[index].first().y
        val lineOnMat = elevationLine
          .bound(windowBounds)

        if (occludeLines) {
          lineOnMat
            .maskedByImage(unionMat, inverted = true)
            .draw(boundRect)
        }

        unionMat.fillPoly(lineOnMat.map { it.expandEndpointsToMakeMask(newBottom = minYPosition) })
      }

      if (drawUnionMat) unionMat.draw()
    } else {
      elevationLinesBottomToTop.draw(boundRect)
    }

    if (drawMinElevationOutline || drawMat) {
      if (drawMat) matThreshold.draw(matToScreen, boundRect)
      if (drawMinElevationOutline) {
        var matContours = matThreshold.findContours()
          .transform(matToScreen)

        if (occludeLines) {
          matContours =
            matContours.maskedByImage(
              unionMat
                .gaussianBlur(7, inPlace = true),
              inverted = true,
              thresholdValueRange = 240.0..256.0,
            )
              .flatten()
        }

        matContours.draw(boundRect)
      }
    }

    unionMat.release()

    if (drawOceanLines) {
      yield(Unit)
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
  var drawUnionMat: Boolean = true,
  var occludeLines: Boolean = true,
  var drawOceanLines: Boolean = true,
  var blurAmount: Double = 0.0,
  var lineSimplifyEpsilon: Double = 0.0,
  var imageRotation: Deg = Deg(0),
  var shrinkLinesHorizontally: Double = 0.0,
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
      toggle(::drawUnionMat)
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
      slider(::shrinkLinesHorizontally, 0..10)
    }

    tab("ocean") {
      toggle(::drawOceanLines)
    }
  }

  override fun clone() = copy()

  override fun toSerializer() = serializer()
}

fun main() = MapSketchLines().run()
