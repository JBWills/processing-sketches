package sketches

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
import coordinate.Mesh
import coordinate.Point
import coordinate.coordSystems.getCoordinateMap
import geomerativefork.src.util.bound
import interfaces.shape.transform
import kotlinx.serialization.Serializable
import org.opencv.core.Mat
import sketches.base.LayeredCanvasSketch
import util.image.ImageFormat
import util.image.opencvMat.bounds
import util.image.opencvMat.fillPoly
import util.image.opencvMat.findContours
import util.image.opencvMat.get
import util.image.opencvMat.maskedByImage
import util.image.opencvMat.threshold
import util.io.geoJson.loadGeoMatMemo
import util.percentAlong
import util.polylines.bound
import util.polylines.expandEndpointsToMakeMask
import util.polylines.transform
import util.polylines.translated

/**
 * Draws a map with topology that can be offset to create a 3d effect.
 */
class MapSketchLines : LayeredCanvasSketch<MapLinesData, MapLinesLayerData>(
  "MapSketchLines",
  defaultGlobal = MapLinesData(),
  layerToDefaultTab = { MapLinesLayerData() },
) {

  val MaxMoveAmount = 300

  var currMat: Mat? = null

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

  override fun drawOnce(layerInfo: LayerInfo) {
    val (geoTiffFile, mapCenter, mapScale, minElevation, maxElevation, elevationMoveVector, samplePointsXY, showHorizontalLines, showVerticalLines, drawMinElevationOutline, drawUnionMat) = layerInfo.globalValues
    geoTiffFile ?: return

    val elevationRange = minElevation..maxElevation
    val elevationMoveAmount = elevationMoveVector.scaledVector(MaxMoveAmount)

    currMat = loadGeoMatMemo(geoTiffFile)
    val mat = currMat ?: return

    val scaleAndMove = getTiffToScreenTransform(mat.bounds, mapScale, mapCenter)
    val scaleAndMoveInverted = scaleAndMove.inverted()

    fun elevationPercent(p: Point) =
      elevationRange.percentAlong(
        mat.get(p.transform(scaleAndMoveInverted))?.bound(elevationRange) ?: minElevation,
      )

    fun isPointVisible(p: Point): Boolean =
      elevationRange.contains(
        mat.get(p.transform(scaleAndMoveInverted)) ?: (minElevation - 1),
      )

    val (horizontalLines, verticalLines) = Mesh(
      boundRect.expand(elevationMoveAmount.abs() * 2),
      samplePointsXY.xi,
      samplePointsXY.yi,
      pointTransformFunc = { pointLocation, _, _ ->
        pointLocation + (elevationMoveAmount * elevationPercent(pointLocation))
      },
      pointVisibleFunc = { pointLocation, _, _, _ ->
        isPointVisible(pointLocation)
      },
    ).toLinesByIndex()

    val unionMat = Mat.zeros(boundRect.size.toSize(), ImageFormat.Gray.openCVFormat)
    horizontalLines.reversed().forEachIndexed { _, lineAtIndex ->
      val lineOnMat = lineAtIndex.bound(boundRect).translated(-boundRect.topLeft)

      lineOnMat
        .maskedByImage(unionMat, inverted = true)
        .translated(boundRect.topLeft).draw()
      unionMat.fillPoly(lineOnMat.map { it.expandEndpointsToMakeMask(unionMat.rows().toDouble()) })
    }

    if (drawUnionMat) {
      unionMat.draw(boundRect.topLeft)
    }

    if (drawMinElevationOutline) {
      mat.threshold(minElevation)
        .findContours()
        .transform(scaleAndMove)
        .draw(boundRect)
    }

    unionMat.release()

//    if (showHorizontalLines)
//      horizontalLines.map { it.draw(boundRect) }

    if (showVerticalLines) verticalLines.map { it.draw(boundRect) }
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
  var mapCenter: Point = Point.Zero,
  var mapScale: Double = 1.0,
  var minElevation: Double = 0.0,
  var maxElevation: Double = 5000.0,
  var elevationMoveVector: VectorProp = VectorProp(Point.Zero, 0.0),
  var samplePointsXY: Point = Point(2, 2),
  var showHorizontalLines: Boolean = true,
  var showVerticalLines: Boolean = true,
  var drawMinElevationOutline: Boolean = true,
  var drawUnionMat: Boolean = true,
) : PropData<MapLinesData> {
  override fun bind() = tabs {
    tab("Map") {
      row {
        fileSelect(::geoTiffFile)
      }

      row {
        heightRatio = 5
        slider2D(::mapCenter, -1..1).withHeight(3)
      }
      slider(::mapScale, 0.1..10.0)
    }

    tab("Lines") {
      toggle(::drawUnionMat)
      toggle(::drawMinElevationOutline)
      row {
        slider(::minElevation, 0..5000)
        slider(::maxElevation, 0..5000)
      }
      panel(::elevationMoveVector)
      sliderPair(::samplePointsXY, 1.0..3000.0)
      row {
        toggle(::showHorizontalLines)
        toggle(::showVerticalLines)
      }
    }
  }

  override fun clone() = copy()

  override fun toSerializer() = serializer()
}

fun main() = MapSketchLines().run()
