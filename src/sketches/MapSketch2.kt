package sketches

import appletExtensions.withStroke
import controls.panels.TabsBuilder.Companion.layerTab
import controls.panels.TabsBuilder.Companion.singleTab
import controls.panels.panelext.fileSelect
import controls.panels.panelext.slider
import controls.panels.panelext.slider2D
import controls.props.PropData
import coordinate.BoundRect
import coordinate.Point
import kotlinx.serialization.Serializable
import sketches.base.LayeredCanvasSketch
import util.geo.GeoContourLine
import util.geo.GeoContourLine.Companion.toContourLine
import util.io.geoJson.readGeoJsonCollection
import util.lerp
import util.percentAlong
import java.awt.Color
import java.io.File

/**
 * Starter sketch that uses all of the latest bells and whistles.
 *
 * Copy and paste this to create a new sketch.
 */
class MapSketch2 : LayeredCanvasSketch<MapData2, MapLayerData2>(
  "MapSketch2",
  defaultGlobal = MapData2(),
  layerToDefaultTab = { MapLayerData2() },
) {
  var coordFile: String? = null
  var coordsByElevation: Map<Double, List<GeoContourLine>> = mapOf()
  var overallBounds: BoundRect? = null

  var filteredCoords: Map<Double, List<GeoContourLine>> = mapOf()

  fun populateCoords(filename: String): Map<Double, List<GeoContourLine>> {
    val lines: MutableMap<Double, MutableList<GeoContourLine>> = mutableMapOf()
    File(filename).readGeoJsonCollection { feature ->
      val contourLine = feature.toContourLine()
      lines.putIfAbsent(contourLine.elevation, mutableListOf())
      lines[contourLine.elevation]?.add(contourLine)
      overallBounds = overallBounds?.unionBound(contourLine.bounds) ?: contourLine.bounds
    }

    return lines
  }

  override fun drawSetup(layerInfo: DrawInfo) {
    val (geoJsonFile, elevationMin, elevationRangeSize, elevationStep) = layerInfo.globalValues
    geoJsonFile ?: return

    if (coordFile != geoJsonFile) {
      coordsByElevation = populateCoords(geoJsonFile)
      coordFile = geoJsonFile
    }

    filteredCoords =
      coordsByElevation.filter { (key, _) -> key in elevationMin..(elevationMin + elevationRangeSize) && (key % (5 * elevationStep)) == 0.0 }
  }

  override fun drawOnce(layerInfo: LayerInfo) {
    val (geoJsonFile, elevationMin, elevationRangeSize, elevationStep, mapCenter, mapScale) = layerInfo.globalValues
    val (exampleTabField) = layerInfo.tabValues

    val overallBounds = overallBounds ?: return

    val scale =
      Point(
        boundRect.width / overallBounds.width,
        -boundRect.height / overallBounds.height,
      ) * mapScale

    val preScaleMoveAmount: Point = -overallBounds.center
    val postScaleMoveAmount: Point = boundRect.center + mapCenter - overallBounds.center

    fun moveAndScalePoint(p: Point) = ((p + preScaleMoveAmount) * scale) + postScaleMoveAmount

    filteredCoords.forEach { (k, v) ->
      val elevationPercent = (-30..600).percentAlong(k)

      withStroke(c = listOf(Color.RED, Color.GREEN).lerp(elevationPercent)) {
        v.map { lines -> lines.map(::moveAndScalePoint) }
          .draw(boundRect)
      }
    }
  }
}

@Serializable
data class MapLayerData2(
  var exampleTabField: Int = 1,
) : PropData<MapLayerData2> {
  override fun bind() = layerTab {
    slider(::exampleTabField, 0..10)
  }

  override fun clone() = copy()
  override fun toSerializer() = serializer()
}

@Serializable
data class MapData2(
  var geoJsonFile: String? = null,
  var elevationMin: Double = 0.0,
  var elevationRangeSize: Double = 5.0,
  var elevationStep: Int = 1,
  var mapCenter: Point = Point.Zero,
  var mapScale: Double = 1.0,
) : PropData<MapData2> {
  override fun bind() = singleTab("Global") {
    fileSelect(::geoJsonFile)
    row {
      slider(::elevationMin, -100..600)
      slider(::elevationRangeSize, 0..1000)
    }

    slider(::elevationStep, range = 0..10)

    slider2D(::mapCenter, Point(-1000, -1000)..Point(1000, 1000))
    slider(::mapScale, 0.5..100.0)
  }

  override fun clone() = copy()

  override fun toSerializer() = serializer()
}

fun main() = MapSketch2().run()
