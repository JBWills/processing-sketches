package sketches

import appletExtensions.withStroke
import controls.controlsealedclasses.Slider.Companion.slider
import controls.controlsealedclasses.Toggle.Companion.toggle
import controls.panels.TabsBuilder.Companion.tabs
import controls.panels.panelext.SliderPairArgs
import controls.panels.panelext.sliderPair
import controls.props.PropData
import coordinate.BoundRect.Companion.centeredRect
import coordinate.Point
import kotlinx.serialization.Serializable
import sketches.base.SimpleCanvasSketch
import util.layers.LayerSVGConfig
import util.numbers.map
import util.polylines.centroid
import util.polylines.scale
import util.print.Pen
import util.random.randPoint
import util.voronoi.triangulate
import util.voronoi.voronoi2
import java.awt.Color
import kotlin.random.Random

/**
 * Make a little voronoi diagram from random points
 */
class VoronoiSketch : SimpleCanvasSketch<VoronoiData>("Voronoi", VoronoiData()) {

//  val points: MutableList<Point> = mutableListOf()
//
//  override fun mouseClicked(event: MouseEvent?, drawInfo: DrawInfo?) {
//    super.mouseClicked(event, drawInfo)
//    event?.let { points.add(it.point) }
//    markDirty()
//  }

  override fun drawLayers(drawInfo: DrawInfo, onNextLayer: (LayerSVGConfig) -> Unit) {
    val (numPoints, randomSeed, pointBoxSize, drawTriangles, drawVoronoi, drawPoints, scale) = drawInfo.dataValues

    onNextLayer(LayerSVGConfig(style = Pen.GellyColorBlue.style))

    val pointBound = centeredRect(boundRect.center, pointBoxSize)

    val r = Random(randomSeed)
    val points = numPoints.map { r.randPoint(pointBound) }


    val t = triangulate(points)

    onNextLayer(LayerSVGConfig(style = Pen.GellyColorNeonOrange.style))
    if (drawTriangles) t.map { it.toPolyLine().draw(boundRect) }

    onNextLayer(LayerSVGConfig(style = Pen.GellyColorDarkBlue.style))
    if (drawVoronoi) voronoi2(points).map { poly ->
      withStroke(listOf(Color.RED, Color.GREEN, Color.CYAN, Color.BLUE).random(r)) {
        poly.scale(scale, poly.centroid()).draw(boundRect)
      }
    }
    onNextLayer(LayerSVGConfig(style = Pen.GellyColorDarkPink.style))
    if (drawPoints) points.drawPoints(3)
  }
}

@Serializable
data class VoronoiData(
  var numPoints: Int = 10,
  var randomSeed: Int = 0,
  var pointBoxSize: Point = Point(100, 100),
  var drawTriangles: Boolean = true,
  var drawVoronoi: Boolean = true,
  var drawPoints: Boolean = true,
  var scale: Double = 1.0,
) : PropData<VoronoiData> {
  override fun bind() = tabs {
    tab("Global") {
      slider(::numPoints, 0..10_000)
      slider(::randomSeed, 0..1_000)
      sliderPair(::pointBoxSize, SliderPairArgs(0.0..1_000.0, 0.0..1_000.0, withLockToggle = true))
      row {
        toggle(::drawTriangles)
        col {
          toggle(::drawVoronoi)
          slider(::scale, 0.0..1.0)
        }
        toggle(::drawPoints)
      }
    }
  }

  override fun clone() = copy()

  override fun toSerializer() = serializer()
}

fun main() = VoronoiSketch().run()
