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
import sketches.base.LayeredCanvasSketch
import util.image.bounds
import util.image.get
import util.io.geoJson.loadGeoMatMemo
import util.percentAlong


/**
 * Draws a map with topology that can be offset to create a 3d effect.
 */
class MapSketchLines : LayeredCanvasSketch<MapLinesData, MapLinesLayerData>(
  "MapSketchLines",
  defaultGlobal = MapLinesData(),
  layerToDefaultTab = { MapLinesLayerData() },
) {

  val MaxDisBetweenLines = 300
  val MaxMoveAmount = 300

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

  private fun getThresholdToOffset(
    allThresholds: List<Double>,
    layerMove: Point
  ): Map<Double, Point> =
    allThresholds.associateWith { threshold ->
      val thresholdPercent = (allThresholds.first()..allThresholds.last()).percentAlong(threshold)
      if (thresholdPercent.isNaN()) Point(0)
      else layerMove * thresholdPercent
    }

  override fun drawOnce(layerInfo: LayerInfo) {
    val (geoTiffFile, drawMap, mapCenter, mapScale, minElevation, maxElevation, elevationMoveVector, samplePointsXY, showHorizontalLines, showVerticalLines) = layerInfo.globalValues
    geoTiffFile ?: return

    val elevationRange = minElevation..maxElevation
    val elevationMoveAmount = elevationMoveVector.scaledVector(MaxMoveAmount)

    val mat = loadGeoMatMemo(geoTiffFile)
    
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
      pointVisibleFunc = { pointLocation, transformedPointLocation, x, y ->
        isPointVisible(pointLocation)
      },
    ).toLines()

    if (showHorizontalLines) horizontalLines.draw(boundRect)
    if (showVerticalLines) verticalLines.draw(boundRect)
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
  var drawMap: Boolean = false,
  var mapCenter: Point = Point.Zero,
  var mapScale: Double = 1.0,
  var minElevation: Double = 0.0,
  var maxElevation: Double = 5000.0,
  var elevationMoveVector: VectorProp = VectorProp(Point.Zero, 0.0),
  var samplePointsXY: Point = Point(2, 2),
  var showHorizontalLines: Boolean = true,
  var showVerticalLines: Boolean = true,
) : PropData<MapLinesData> {
  override fun bind() = tabs {
    tab("Map") {
      row {
        fileSelect(::geoTiffFile)
        toggle(::drawMap).withWidth(0.5)
      }

      row {
        heightRatio = 5
        slider2D(::mapCenter, -1..1).withHeight(3)
      }
      slider(::mapScale, 0.1..10.0)
    }

    tab("Lines") {
      row {
        slider(::minElevation, 0..5000)
        slider(::maxElevation, 0..5000)
      }
      panel(::elevationMoveVector)
      sliderPair(::samplePointsXY, 1.0..500.0)
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
