package sketches

import controls.panels.ControlStyle
import controls.panels.TabsBuilder.Companion.layerTab
import controls.panels.TabsBuilder.Companion.tabs
import controls.panels.panelext.button
import controls.panels.panelext.fileSelect
import controls.panels.panelext.slider
import controls.panels.panelext.slider2D
import controls.props.PropData
import controls.props.types.ContourProp
import coordinate.BoundRect
import coordinate.Point
import coordinate.coordSystems.getCoordinateMap
import interfaces.shape.transform
import kotlinx.serialization.Serializable
import sketches.base.LayeredCanvasSketch
import util.image.bounds
import util.image.opencvContouring.contourMemo
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
    val (geoTiffFile, mapCenter, mapScale, contourProp, manualThresholds) = layerInfo.globalValues
    geoTiffFile ?: return

    val (mat, contours) = contourMemo(geoTiffFile, contourProp.getThresholds())

    val scaleAndMove = getTiffToScreenTransform(mat.bounds, mapScale, mapCenter)

    val bound = boundRect
      .boundsIntersection(mat.bounds.transform(scaleAndMove)) ?: return

    contours.forEach { (_, lines) ->
      lines.transform(scaleAndMove).draw(bound)
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
