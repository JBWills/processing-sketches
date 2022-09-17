package sketches

import appletExtensions.withStroke
import controls.controlsealedclasses.Slider.Companion.slider
import controls.controlsealedclasses.Toggle.Companion.toggle
import controls.panels.ControlStyle
import controls.panels.TabsBuilder.Companion.tabs
import controls.panels.panelext.noisePanel
import controls.props.PropData
import coordinate.BoundRect
import coordinate.Circ
import coordinate.Deg
import coordinate.Point
import fastnoise.Noise
import kotlinx.serialization.Serializable
import org.locationtech.jts.geom.Envelope
import sketches.base.SimpleCanvasSketch
import util.algorithms.packMemo
import util.algorithms.streamLines
import util.layers.LayerSVGConfig
import util.numbers.sqrt
import util.polylines.PolyLine
import util.print.Pen
import util.quadTree.GQuadtree
import util.random.randItem
import util.random.randomLightColor
import java.awt.Color
import kotlin.random.Random

/**
 * Flow lines
 */
class FlowLinesSketch : SimpleCanvasSketch<FlowLinesData>("FlowLines", FlowLinesData()) {

  private fun Point.toEnvelope(dist: Double) = Envelope(
    x - dist,
    x + dist,
    y - dist,
    y + dist,
  )

  private fun Random.generateCircles(generatorData: FlowGeneratorData): List<Circ> {
    val bound = boundRect.expandPercent(0.5)
    return packMemo(
      this,
      generatorData.placementRandomSeed,
      bound,
      (bound.area / generatorData.numPoints).sqrt() / 2,
      generatorData.numPoints,
      generatorData.numAttempts,
    )
  }

  private fun Point.generateFlowLine(
    bound: BoundRect,
    generatorData: FlowGeneratorData,
    linesData: FlowLineData,
    pointTree: GQuadtree<Point>
  ): PolyLine? {
    val steps = max(1, (linesData.length / linesData.step).toInt())
    val resultLine = mutableListOf<Point>()

    var lastPoint = this

    var length = 0.0

    for (stepIndex in 0..steps) {
      resultLine.add(lastPoint)

      val direction = Deg(generatorData.fieldNoise.get(lastPoint) * 360)
      val newPoint = lastPoint + direction.unitVector * linesData.step
      if (linesData.limitDistanceToOtherLines &&
        pointTree.query(newPoint.toEnvelope(linesData.maxDistanceFromOtherLines / 2))
          .any { it.dist(newPoint) < linesData.maxDistanceFromOtherLines }
      ) {
        break
      }

      if (!bound.contains(newPoint)) {
        break
      }

      length += linesData.step

      lastPoint = newPoint
    }

    return if (length > linesData.minLength) resultLine else null
  }

  private fun List<PolyLine>.groupByColor(
    random: Random,
    colorData: FlowColorData
  ): List<Pair<Color, PolyLine>> {
    val colors = Array(colorData.numColors) { random.randomLightColor() }

    return map { line -> random.randItem(colors) to line }
  }

  override fun drawLayers(drawInfo: DrawInfo, onNextLayer: (LayerSVGConfig) -> Unit) {
    val (generatorData, linesData, colorData) = drawInfo.dataValues
    val maxDistDiv2 = linesData.maxDistanceFromOtherLines / 2

    onNextLayer(LayerSVGConfig(nextLayerName = "Circles", style = Pen.GellyColorDarkPeach.style))

    val r = Random(generatorData.placementRandomSeed)
    val circles = r.generateCircles(generatorData)

    onNextLayer(LayerSVGConfig(nextLayerName = "Lines", style = Pen.GellyColorBlue.style))
    val pointTree = GQuadtree<Point> {
      Envelope(
        x - maxDistDiv2,
        x + maxDistDiv2,
        y - maxDistDiv2,
        y + maxDistDiv2,
      )
    }

    val lines = if (!linesData.useOldVersion) {
      streamLines(
        generatorData.placementRandomSeed,
        boundRect.expand(0.5),
        distance = linesData.maxDistanceFromOtherLines,
        lengthRange = linesData.minLength..(if (linesData.enforceMaxLength) linesData.length else Double.MAX_VALUE),
        step = linesData.step,
        dTest = linesData.maxDistanceFromOtherLines * linesData.dTest,
      ) {
        Deg(generatorData.fieldNoise.get(it) * 360 * generatorData.angleScale)
      }
    } else {
      circles
        .shuffled()
        .asSequence()
        .map(Circ::origin)
        .map { point ->
          val line =
            point.generateFlowLine(
              boundRect.expandPercent(0.5),
              generatorData,
              linesData,
              pointTree,
            )
          if (linesData.limitDistanceToOtherLines) {
            line?.map { pointTree.insert(it) }
          }
          line
        }
        .filterNotNull()
        .toList()
    }

    lines.groupByColor(r, colorData).map { (color, lines) ->
      withStroke(color) {
        lines.draw(boundRect)
      }
    }
  }
}

@Serializable
data class FlowColorData(
  var numColors: Int = 2,
)

@Serializable
data class FlowLineData(
  var step: Double = 3.0,
  var length: Double = 10.0,
  var enforceMaxLength: Boolean = false,
  var minLength: Double = 0.0,
  var maxDistanceFromOtherLines: Double = 0.0,
  var dTest: Double = 0.5,
  var limitDistanceToOtherLines: Boolean = false,
  var useOldVersion: Boolean = false,
)

@Serializable
data class FlowGeneratorData(
  var fieldNoise: Noise = Noise.DEFAULT,
  var angleScale: Double = 1.0,
  var placementRandomSeed: Int = 0,
  var numPoints: Int = 1_000,
  var numAttempts: Int = 5,
)

@Serializable
data class FlowLinesData(
  var generatorData: FlowGeneratorData = FlowGeneratorData(),
  var linesData: FlowLineData = FlowLineData(),
  var colorData: FlowColorData = FlowColorData(),
) : PropData<FlowLinesData> {
  override fun bind() = tabs {
    tab("Global") {
      noisePanel(generatorData::fieldNoise, showStrengthSliders = false, style = ControlStyle.Gray)
      slider(generatorData::angleScale, 0.01..2.0)

      row {
        slider(generatorData::placementRandomSeed, 0..1000)
        slider(generatorData::numAttempts, 1..50)
      }
      slider(generatorData::numPoints, 1..100_000)
    }

    tab("Lines") {
      slider(linesData::step, 0.1..10.0)
      row {
        slider(linesData::length, 0.0..800.0)
        col {
          widthRatio = 0.3
          toggle(linesData::enforceMaxLength)
        }
      }
      slider(linesData::minLength, 0.0..100.0)
      row {
        slider(linesData::maxDistanceFromOtherLines, 0..100)
        slider(linesData::dTest, 0.0..1.0)
      }
      row {
        toggle(linesData::limitDistanceToOtherLines)
        toggle(linesData::useOldVersion)
      }
    }

    tab("Colors") {
      slider(colorData::numColors, 1..5)
    }
  }

  override fun clone() = copy()

  override fun toSerializer() = serializer()
}

fun main() = FlowLinesSketch().run()
