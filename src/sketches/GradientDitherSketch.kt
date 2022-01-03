package sketches

import controls.controlsealedclasses.Button.Companion.button
import controls.controlsealedclasses.Dropdown.Companion.dropdown
import controls.controlsealedclasses.Slider.Companion.slider
import controls.controlsealedclasses.Slider2D.Companion.slider2D
import controls.controlsealedclasses.Toggle.Companion.toggle
import controls.panels.TabStyle.Companion.toTabStyle
import controls.panels.TabsBuilder.Companion.tabs
import controls.props.PropData
import controls.props.types.PenProp
import coordinate.BoundRect
import coordinate.Point
import coordinate.util.mapPoints
import kotlinx.serialization.Serializable
import sketches.RandomizePositionType2.RandomDistances
import sketches.base.SimpleCanvasSketch
import util.iterators.deepForEach
import util.layers.LayerSVGConfig
import util.numbers.squared
import util.print.Pen
import util.randItem
import util.random.randomWeightedIndex
import util.randomDouble
import util.translatedRandomDirection
import java.awt.Color

/**
 * Starter sketch that uses all the latest bells and whistles.
 *
 * Copy and paste this to create a new sketch.
 */
class GradientDitherSketch : SimpleCanvasSketch<GradientDitherData>(
  "GradientDither",
  GradientDitherData(),
) {

  private fun distanceToProbability(distanceToAnchor: Double, anchorIntensity: Double): Double =
    anchorIntensity.squared() / distanceToAnchor.squared()

  override suspend fun SequenceScope<LayerSVGConfig>.drawLayers(drawInfo: DrawInfo) {
    val (gradientAnchors, radius, drawAsScribbles, numPoints, pointsSeed, randomizePosition, randomizePositionType) = drawInfo.dataValues.pointData
    if (gradientAnchors.size == 0) return

    fun getAnchor(p: Point): GradientPoint {
      val distances: List<Pair<GradientPoint, Double>> =
        gradientAnchors.map { it to it.centerInRect(boundRect).dist(p) }
      val probabilities =
        distances.map { (anchor, dist) -> distanceToProbability(dist, anchor.intensity) }

      return gradientAnchors[probabilities.randomWeightedIndex(pointsSeed)]
    }

    val anchorToPoints: MutableMap<GradientPoint, MutableList<Point>> = mutableMapOf()

    boundRect.mapPoints(numPoints) {
      val dist = when (randomizePositionType) {
        RandomizePositionType2.RandomDistances -> randomDouble(
          0.0..randomizePosition,
          pointsSeed,
        )
        RandomizePositionType2.EqualDistances -> randomizePosition
      }
      it.translatedRandomDirection(dist, pointsSeed)
    }.deepForEach { point ->
      if (boundRect.contains(point)) {
        anchorToPoints
          .getOrPut(getAnchor(point)) { mutableListOf() }
          .add(point)
      }
    }

    anchorToPoints.forEach { (anchor, points) ->
      newLayerStyled(
        anchor.penProp.style,
        LayerSVGConfig(layerName = anchor.penProp.pen.name),
      ) {
        points.drawPoints(radius)
      }
    }

    if (drawInfo.dataValues.debugMode) {
      gradientAnchors.forEach { it.centerInRect(boundRect).draw(5, Color.WHITE) }
    }
  }
}

enum class RandomizePositionType2 { EqualDistances, RandomDistances }

@Serializable
data class GradientPoint(
  var center: Point = Point(0.5, 0.5),
  var penProp: PenProp = PenProp(pen = Pen.GellyColors.randItem(), filterByWeight = true),
  var intensity: Double = 1.0,
  // consider adding intensity field here
) {
  constructor(p: GradientPoint) : this(p.center.copy(), p.penProp.clone())

  fun centerInRect(rect: BoundRect) = rect.pointAt(center)
}


@Serializable
data class PointData(
  val points: MutableList<GradientPoint> = mutableListOf(),
  var radius: Double = 1.0,
  var drawAsScribbles: Boolean = false,
  var numPoints: Int = 10_000,
  var pointsSeed: Int = 0,
  var randomizePosition: Double = 0.0,
  var randomizePositionType: RandomizePositionType2 = RandomDistances,
)

private const val GlobalTabName = "Global"

@Serializable
data class GradientDitherData(
  var debugMode: Boolean = false,
  val pointData: PointData = PointData(),
) : PropData<GradientDitherData> {
  private val gradientAnchors get() = pointData.points
  override fun bind() = tabs {
    fun getGradientAnchorName(index: Int) = "p$index"
    tab(GlobalTabName) {
      row {
        button("Add New Point") {
          gradientAnchors.add(GradientPoint())
          updateControls(newTabName = getGradientAnchorName(gradientAnchors.size - 1))
          markDirty()
        }
        button("Clear Points") {
          gradientAnchors.clear()
          updateControls()
          markDirty()
        }
        toggle(::debugMode)
      }

      row {
        toggle(pointData::drawAsScribbles)
        slider(pointData::radius, 0..10)
      }

      row {
        slider(pointData::numPoints, 0..100_000)
        slider(pointData::pointsSeed, 0..1_000)
      }
      row {
        dropdown(pointData::randomizePositionType)
        slider(pointData::randomizePosition, 0.0..100.0)
      }
    }

    tabs(
      gradientAnchors,
      getTabName = { index, _ -> getGradientAnchorName(index) },
      getTabStyle = { index, gradientPoint -> gradientPoint.penProp.pen.toTabStyle() },
    ) { index, gradientPoint ->
      row {
        heightRatio = 0.5
        button("Refresh Controls Panel") { updateControls() }
      }
      row {
        button("Clone") {
          gradientAnchors.add(GradientPoint(gradientPoint))
          updateControls(newTabName = getGradientAnchorName(gradientAnchors.size - 1))
          markDirty()
        }

        button("Delete") {
          gradientAnchors.removeAt(index)
          val newIndex = if (gradientAnchors.size > index) index else index - 1
          val newTabName =
            if (gradientAnchors.indices.contains(newIndex)) getGradientAnchorName(newIndex)
            else GlobalTabName
          updateControls(newTabName = newTabName)
          markDirty()
        }
      }

      row {
        panel(gradientPoint::penProp)
      }

      slider(gradientPoint::intensity, 0..10)
      slider2D(gradientPoint::center, 0..1).withHeight(2)
    }
  }

  override fun clone() = copy()

  override fun toSerializer() = serializer()
}

fun main() = GradientDitherSketch().run()
