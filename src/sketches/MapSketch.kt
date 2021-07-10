package sketches

import controls.panels.ControlStyle
import controls.panels.TabsBuilder.Companion.layerTab
import controls.panels.TabsBuilder.Companion.tabs
import controls.panels.panelext.button
import controls.panels.panelext.fileSelect
import controls.panels.panelext.slider
import controls.panels.panelext.slider2D
import controls.panels.panelext.toggle
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
import processing.event.MouseEvent
import sketches.base.LayeredCanvasSketch
import util.algorithms.chaikin
import util.algorithms.contouring.mergeSegments
import util.algorithms.douglassPeucker
import util.image.bounds
import util.image.contains
import util.image.gaussianBlur
import util.image.get
import util.image.size
import util.io.geoJson.loadGeoMatMemo
import util.io.input.point
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
  private var lastBlur: Double? = null

  private var tiffMat: Mat? = null

  init {
    numLayers = 1
  }

  override fun drawSetup(layerInfo: DrawInfo) {
    val (geoTiffFile, _, _, mapBlurRadius, _) = layerInfo.globalValues
    geoTiffFile ?: return

    val needsToLoadTiff = geoTiffFile != tiffPath

    if (needsToLoadTiff) {
      lastBlur = null
      tiffPath = geoTiffFile
      preBlurTiff = loadGeoMatMemo(geoTiffFile)
    }

    if (needsToLoadTiff || lastBlur != mapBlurRadius.toDouble()) {
      lastBlur = mapBlurRadius.toDouble()
      tiffMat = preBlurTiff?.gaussianBlur(mapBlurRadius, 3.0)
    }
  }

  override fun mouseClicked(event: MouseEvent?) {
    event ?: return
    val tiffMat = tiffMat ?: return
    modifyPropsDirectly { allProps ->
      val props = allProps.globalValues

      val tiffPoint =
        event.point.transform(
          getTiffToScreenTransform(tiffMat, props.mapScale, props.mapCenter)
            .inverted(),
        )

      if (!tiffMat.contains(tiffPoint)) return@modifyPropsDirectly false

      val elevationValue = tiffMat.get(tiffPoint)

      if (props.manualThresholds.contains(elevationValue)) return@modifyPropsDirectly false

      props.manualThresholds.add(elevationValue)

      true
    }
  }

  private fun getTiffToScreenTransform(tiffMat: Mat, mapScale: Double, mapCenter: Point) =
    ShapeTransformGroup(
      ScaleTransform(Point((boundRect.size.x / tiffMat.size.x) * mapScale), tiffMat.bounds.center),
      TranslateTransform(boundRect.center + (mapCenter * tiffMat.size) - (tiffMat.size / 2)),
    )

  override fun drawOnce(layerInfo: LayerInfo) {
    val (_, mapCenter, mapScale, mapBlurRadius, dontBlurWaterLevel, contourProp, manualThresholds) = layerInfo.globalValues

    val tiffMat = tiffMat ?: return
    val preBlurTiff = preBlurTiff ?: return

    fun PolyLine.smoothLine() =
      chaikin(contourProp.chaikinTimes)
        .douglassPeucker(contourProp.smoothEpsilon)


    val scaleAndMove = getTiffToScreenTransform(tiffMat, mapScale, mapCenter)

    val tiffBoundsDrawCoordinates = tiffMat.bounds
      .transform(scaleAndMove)
      .boundsIntersection(boundRect)

    tiffBoundsDrawCoordinates?.draw()

    val tiffBoundsTiffCoordinates =
      tiffBoundsDrawCoordinates?.revertTransform(scaleAndMove) ?: return

    val newGridStep =
      contourProp.gridStep.let { if (it * mapScale < 1.0) (it / mapScale) else it }

    fun contour(thresholds: List<Double>, mat: Mat) =
      contourProp.contour(tiffBoundsTiffCoordinates, newGridStep, thresholds) { p ->
        mat.get(p.bound(Point.Zero, mat.size - 1))
      }

    val allThresholds = (contourProp.getThresholds() + manualThresholds).distinct()
    val unblurredThresholds =
      if (dontBlurWaterLevel) listOf(0.0) else listOf()

    val blurredThresholds =
      if (dontBlurWaterLevel) allThresholds.filter { it != 0.0 } else allThresholds

    val blurredContourLines = contour(blurredThresholds, tiffMat)
    val unblurredContourLines = contour(unblurredThresholds, preBlurTiff)

    (blurredContourLines + unblurredContourLines).forEach { (threshold, thresholdShapes) ->
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
  var dontBlurWaterLevel: Boolean = true,
  var contourProp: ContourProp = ContourProp(),
  var manualThresholds: MutableList<Double> = mutableListOf(),
) : PropData<MapData> {
  override fun bind() = tabs {
    tab("Map") {
      fileSelect(::geoTiffFile)

      row {
        heightRatio = 5
        slider2D(::mapCenter, Point(-1)..Point(1)).withHeight(3)
      }
      slider(::mapScale, 0.1..10.0)
      row {
        slider(::mapBlurRadius, 0..50)
        toggle(::dontBlurWaterLevel)
      }
    }


    tab("Contour") {
      row {
        style = ControlStyle.Yellow
        button("Remove last Threshold") {
          if (manualThresholds.isNotEmpty()) {
            manualThresholds.removeAt(manualThresholds.size - 1)
          }
        }
        button("Clear thresholds") {
          manualThresholds.clear()
          markDirty()
        }
      }
      panel(::contourProp)
    }
  }

  override fun clone() = copy()

  override fun toSerializer() = serializer()
}

fun main() = MapSketch().run()
