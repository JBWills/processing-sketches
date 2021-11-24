package sketches

import controls.controlsealedclasses.Button.Companion.button
import controls.controlsealedclasses.Slider.Companion.slider
import controls.controlsealedclasses.Slider2D.Companion.slider2D
import controls.controlsealedclasses.Toggle.Companion.toggle
import controls.panels.ControlStyle
import controls.panels.TabsBuilder.Companion.layerTab
import controls.panels.TabsBuilder.Companion.tabs
import controls.panels.panelext.fileSelect
import controls.props.PropData
import controls.props.types.ContourProp
import coordinate.BoundRect
import coordinate.Point
import coordinate.coordSystems.getCoordinateMap
import kotlinx.serialization.Serializable
import sketches.base.LayeredCanvasSketch
import util.image.opencvMat.contourMemo
import util.image.opencvMat.geoTiffToGray
import util.percentAlong
import util.polylines.transform

/**
 * Draws a map with topology that can be offset to create a 3d effect.
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
    mapCenter: Point,
  ) = getCoordinateMap(
    contourBounds,
    boundRect
      .scaled(mapScale)
      .let { it.translated(mapCenter * it.size - (it.size / 2)) },
  )

  private fun getThresholdToOffset(
    allThresholds: List<Double>,
    layerMove: Point,
  ): Map<Double, Point> =
    allThresholds.associateWith { threshold ->
      val thresholdPercent =
        (allThresholds.first()..allThresholds.last()).percentAlong(threshold)
      if (thresholdPercent.isNaN()) Point(0)
      else layerMove * thresholdPercent
    }

  override fun drawOnce(layerInfo: LayerInfo) {
    val (geoTiffFile, drawMap, mapCenter, mapScale, contourProp, layerMove) = layerInfo.globalValues
    geoTiffFile ?: return

    val allThresholds = contourProp.getThresholds()

    val contourResponse =
      contourMemo(geoTiffFile, getThresholdToOffset(allThresholds, layerMove))

    val scaleAndMove =
      getTiffToScreenTransform(
        contourResponse.baseMatBoundsInUnionRect,
        mapScale,
        mapCenter,
      )

    if (drawMap) {
      contourResponse.contours.first().binaryImage.draw(boundRect.topLeft)
      contourResponse.baseMat.geoTiffToGray().draw(boundRect.topLeft)
    }

    contourResponse.contours.forEachIndexed { _, contourData ->
      val lines = contourData.contours

      lines.transform(scaleAndMove).draw(boundRect)
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
  var drawMap: Boolean = false,
  var mapCenter: Point = Point.Zero,
  var mapScale: Double = 1.0,
  var contourProp: ContourProp = ContourProp(),
  var layerMove: Point = Point.Zero,
  var manualThresholds: MutableList<Double> = mutableListOf(),
) : PropData<MapData> {
  override fun bind() = tabs {
    tab("Map") {
      row {
        fileSelect(::geoTiffFile)
        toggle(::drawMap).withWidth(0.5)
      }

      row {
        heightRatio = 5
        slider2D(::mapCenter, Point(-1)..Point(1)).withHeight(3)
      }
      row {
        button("Reset") { layerMove = Point.Zero; markDirty() }
        slider2D(::layerMove, Point(-1000)..Point(1000))
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
