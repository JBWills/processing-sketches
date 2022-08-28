package sketches

import appletExtensions.parallelLinesInBound
import arrow.core.memoize
import controls.controlsealedclasses.Slider.Companion.slider
import controls.controlsealedclasses.Toggle.Companion.toggle
import controls.panels.ControlStyle
import controls.panels.TabsBuilder.Companion.tabs
import controls.panels.panelext.noisePanel
import controls.props.PropData
import coordinate.BoundRect
import coordinate.Deg
import coordinate.Point
import de.lighti.clipper.Clipper.ClipType
import de.lighti.clipper.Clipper.ClipType.INTERSECTION
import fastnoise.Noise
import kotlinx.serialization.Serializable
import sketches.base.SimpleCanvasSketch
import util.base.numSteps
import util.debugLog
import util.layers.LayerSVGConfig
import util.numbers.asPercentString
import util.numbers.minMax
import util.polylines.PolyLine
import util.polylines.clipping.clip
import util.polylines.doConvexPolysIntersect
import util.polylines.rotate
import util.polylines.toEnvelope
import util.print.Pen
import util.quadTree.GQuadtree
import util.random.randomDouble
import util.random.randomPoint
import kotlin.math.max
import kotlin.random.Random

/**
 * Pack squares but align them based on a noise grid
 */
class SquarePacking : SimpleCanvasSketch<SquarePackingData>("SquarePacking", SquarePackingData()) {
  var r: Random? = null

  private fun intersectsRects(tree: GQuadtree<PolyLine>, poly: PolyLine): Boolean =
    tree.query(poly.toEnvelope())
      .any {
        doConvexPolysIntersect(poly, it)
      }

  private fun BoundRect.moveAndRotate(angle: Deg, center: Point) =
    recentered(center).toPolyLine().rotate(angle, center)

  private fun getRects(
    getRectsData: GetRectsData
  ): List<PolyLine> {
    val (_, sizes, bounds, noise, padding, minRatio, minSize, maxTries, noiseAffectSizing) = getRectsData
    val rectTree = GQuadtree(PolyLine::toEnvelope)
    val r = this.r ?: return listOf()
    sizes.map { size ->

      var numTries = 0
      var validRect: PolyLine? = null

      while (validRect == null && numTries < maxTries) {
        val p = r.randomPoint(bounds)
        val noiseVal = noise.get(p.x, p.y)
        val maxSize = (noiseVal * noiseAffectSizing + 1 * (1 - noiseAffectSizing)) * size
        val sizeRange = max(max(minSize, padding), minRatio * maxSize)..maxSize
        val (smallEdge, largeEdge) = minMax(r.randomDouble(sizeRange), r.randomDouble(sizeRange))

        val baseRect = BoundRect(Point(0, 0), smallEdge, largeEdge)
        val angle = Deg(noise.get(p.x, p.y) * 90)
        val newRect = baseRect.moveAndRotate(angle, p)
        val newRectWithPadding = baseRect.expand(padding).moveAndRotate(angle, p)

        if (!intersectsRects(rectTree, newRectWithPadding)) {
          validRect = newRect
        }

        numTries++
      }

      validRect?.let { rectTree.insert(validRect) }
    }


    return rectTree.queryAll()
  }

  val getRectsMemo: (
    data: GetRectsData
  ) -> List<PolyLine> = ::getRects.memoize()

  override fun drawLayers(drawInfo: DrawInfo, onNextLayer: (LayerSVGConfig) -> Unit) {
    val (noise, linesData, rectsData, clipToBounds, noiseAffectSizing) = drawInfo.dataValues

    r = Random(rectsData.positionSeed)

    val bounds = if (clipToBounds) boundRect else windowBounds
    val allRects = getRectsMemo(
      GetRectsData(
        seed = rectsData.positionSeed,
        sizes = (rectsData.smallest..rectsData.largest).numSteps(rectsData.targetNumber).reversed(),
        bounds = bounds,
        noise = noise,
        padding = rectsData.padding,
        minRatio = rectsData.minRatio,
        minSize = rectsData.minSize,
        maxTries = rectsData.maxTries,
        noiseAffectSizing = noiseAffectSizing,
      ),
    )


    if (linesData.showLines) {
      onNextLayer(
        LayerSVGConfig(
          nextLayerName = "lines",
          style = Pen.GellyColorDarkPeach.style,
        ),
      )

      boundRect.parallelLinesInBound(
        linesData.lineAngle,
        linesData.lineSpacing,
        linesData.lineOffset,
      ).clip(allRects, if (linesData.showLinesOuter) ClipType.DIFFERENCE else INTERSECTION)
        .drawAsSegments()
    }

    onNextLayer(
      LayerSVGConfig(
        nextLayerName = "rects",
        style = Pen.GellyColorBlue.style,
      ),
    )

    allRects.map { it.draw(boundRect) }

    debugLog("Placed ${(allRects.size.toDouble() / rectsData.targetNumber).asPercentString()}")
  }
}

data class GetRectsData(
  val seed: Int,
  val sizes: List<Double>,
  val bounds: BoundRect,
  val noise: Noise,
  val padding: Double,
  val minRatio: Double,
  val minSize: Double,
  val maxTries: Int,
  val noiseAffectSizing: Double
)

@Serializable
data class FillLinesData(
  var lineAngle: Deg = Deg(0),
  var lineSpacing: Double = 5.0,
  var lineOffset: Double = 0.0,
  var showLines: Boolean = true,
  var showLinesOuter: Boolean = true
)

@Serializable
data class SquarePackingRectsData(
  var padding: Double = 0.0,
  var maxTries: Int = 10,
  var positionSeed: Int = 0,
  var minRatio: Double = 0.0,
  var largest: Double = 100.0,
  var smallest: Double = 10.0,
  var minSize: Double = 3.0,
  var targetNumber: Int = 500,
)

@Serializable
data class SquarePackingData(
  var directionNoise: Noise = Noise.DEFAULT,
  var linesData: FillLinesData = FillLinesData(),
  var rectsData: SquarePackingRectsData = SquarePackingRectsData(),
  var clipToBounds: Boolean = false,
  var noiseAffectSizing: Double = 0.0,
) : PropData<SquarePackingData> {
  override fun bind() = tabs {
    tab("Global") {
      row {
        heightRatio = 3
        noisePanel(::directionNoise, showStrengthSliders = false)
      }
      slider(rectsData::positionSeed, 0..1000)
      row {
        style = ControlStyle.Green
        slider(rectsData::padding, range = 0..10)
        slider(rectsData::maxTries, 1..1000)
      }
      row {
        style = ControlStyle.Blue
        slider(rectsData::minRatio, 0..1)
        slider(rectsData::minSize, 0..20)
      }
      row {
        style = ControlStyle.Blue
        slider(rectsData::largest, 1..100)
        slider(rectsData::smallest, 1..50)
      }
      row {
        slider(rectsData::targetNumber, 1..20_000)
        toggle(::clipToBounds)
      }
      slider(::noiseAffectSizing, 0..1)
    }

    tab("lines") {
      style = ControlStyle.Red
      slider(linesData::lineAngle)
      row {
        toggle(linesData::showLines)
        toggle(linesData::showLinesOuter)
      }
      slider(linesData::lineSpacing, 1.0..15.0)
      slider(linesData::lineOffset)
    }
  }

  override fun clone() = copy()

  override fun toSerializer() = serializer()
}

fun main() = SquarePacking().run()
