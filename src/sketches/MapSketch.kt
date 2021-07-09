package sketches

import controls.panels.TabsBuilder.Companion.layerTab
import controls.panels.TabsBuilder.Companion.tabs
import controls.panels.panelext.fileSelect
import controls.panels.panelext.slider
import controls.panels.panelext.slider2D
import controls.props.PropData
import controls.props.types.ContourProp
import coordinate.Point
import coordinate.ScaleTransform
import coordinate.ShapeTransformGroup
import coordinate.TranslateTransform
import geomerativefork.src.util.mapIf
import interfaces.shape.revertTransform
import interfaces.shape.transform
import kotlinx.serialization.Serializable
import org.opencv.core.Mat
import sketches.base.LayeredCanvasSketch
import util.algorithms.chaikin
import util.algorithms.contouring.mergeSegments
import util.algorithms.douglassPeucker
import util.image.bounds
import util.image.gaussianBlur
import util.image.get
import util.image.size
import util.io.geoJson.loadGeoMatMemo
import util.pointsAndLines.polyLine.PolyLine
import util.pointsAndLines.polyLine.transform


/**
 * Starter sketch that uses all of the latest bells and whistles.
 *
 * Copy and paste this to create a new sketch.
 */
class MapSketch : LayeredCanvasSketch<MapData, MapLayerData>(
  "MapSketch",
  defaultGlobal = MapData(),
  layerToDefaultTab = { MapLayerData() },
) {
  private var tiffPath: String? = null
  private var preBlurTiff: Mat? = null
  private var lastBlur: Pair<Double, Double>? = null

  private var tiffMat: Mat? = null

  init {
    numLayers = 1
  }

  override fun drawSetup(layerInfo: DrawInfo) {
    val (geoTiffFile, _, _, mapBlurRadius, mapBlurSigma, _) = layerInfo.globalValues
    geoTiffFile ?: return

    if (geoTiffFile != tiffPath) {
      lastBlur = null
      tiffPath = geoTiffFile
      preBlurTiff = loadGeoMatMemo(geoTiffFile)
    }

    if (lastBlur != mapBlurRadius.toDouble() to mapBlurSigma) {
      lastBlur = mapBlurRadius.toDouble() to mapBlurSigma
      tiffMat = preBlurTiff?.gaussianBlur(mapBlurRadius, mapBlurSigma)
    }
  }

  override fun drawOnce(layerInfo: LayerInfo) {
    val (_, mapCenter, mapScale, _, _, contourProp) = layerInfo.globalValues

    val tiffMat = tiffMat ?: return

    fun PolyLine.smoothLine() =
      chaikin(contourProp.chaikinTimes)
        .douglassPeucker(contourProp.smoothEpsilon)

    val scaleAndMove = ShapeTransformGroup(
      ScaleTransform(Point((boundRect.size.x / tiffMat.size.x) * mapScale), tiffMat.bounds.center),
      TranslateTransform(boundRect.center + (mapCenter * tiffMat.size) - (tiffMat.size / 2)),
    )

    val tiffBoundsDrawCoordinates = tiffMat.bounds
      .transform(scaleAndMove)
      .boundsIntersection(boundRect)

    tiffBoundsDrawCoordinates?.draw()

    val tiffBoundsTiffCoordinates =
      tiffBoundsDrawCoordinates?.revertTransform(scaleAndMove) ?: return

    val newGridStep =
      contourProp.gridStep.let { if (it * mapScale < 1.0) (it / mapScale) else it }

    contourProp
      .contour(tiffBoundsTiffCoordinates, newGridStep) { p ->
        tiffMat.get(p.bound(Point.Zero, tiffMat.size - 1))
      }
      .forEach { (threshold, thresholdShapes) ->
        thresholdShapes
          .mergeSegments()
          .mapIf(contourProp.shouldSmooth, PolyLine::smoothLine)
          .transform(scaleAndMove)
          .draw()
      }

  }
}

@Serializable
data class MapLayerData(
  var exampleTabField: Int = 1,
) : PropData<MapLayerData> {
  override fun bind() = layerTab {
    slider(::exampleTabField, 0..10)
  }

  override fun clone() = copy()
  override fun toSerializer() = serializer()
}

@Serializable
data class MapData(
  var geoTiffFile: String? = null,
  var mapCenter: Point = Point.Zero,
  var mapScale: Double = 1.0,
  var mapBlurRadius: Int = 0,
  var mapBlurSigma: Double = 5.0,
  var contourProp: ContourProp = ContourProp()
) : PropData<MapData> {
  override fun bind() = tabs {
    tab("Map") {
      fileSelect(::geoTiffFile)

      row {
        heightRatio = 3
        slider2D(::mapCenter, Point(-1)..Point(1)).withHeight(3)
      }
      slider(::mapScale, 0.1..400.0)
      row {
        slider(::mapBlurRadius, 0..50)
        slider(::mapBlurSigma, 0..50)
      }
    }


    tab("Contour") {
      panel(::contourProp)
    }
  }

  override fun clone() = copy()

  override fun toSerializer() = serializer()
}

fun main() = MapSketch().run()
