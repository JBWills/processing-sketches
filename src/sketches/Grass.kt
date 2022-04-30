package sketches

import controls.controlsealedclasses.Slider.Companion.slider
import controls.controlsealedclasses.Slider2D.Companion.slider2D
import controls.panels.ControlStyle
import controls.panels.TabsBuilder.Companion.tabs
import controls.panels.panelext.noisePanel
import controls.panels.panelext.sliderPair
import controls.props.PropData
import coordinate.Deg
import coordinate.Point
import coordinate.util.mapPoints
import fastnoise.Noise
import kotlinx.serialization.Serializable
import sketches.base.SimpleCanvasSketch
import util.base.step
import util.concurrency.pmap
import util.layers.LayerSVGConfig
import util.numbers.remap
import kotlin.math.max

/**
 * Starter sketch that uses all the latest bells and whistles.
 *
 * Copy and paste this to create a new sketch.
 */
class GrassSketch : SimpleCanvasSketch<GrassData>("Grass", GrassData()) {

  override fun drawLayers(drawInfo: DrawInfo, onNextLayer: (LayerSVGConfig) -> Unit) {
    val (density, spawnNoise, directionNoise, directionVariability, waveStep, size1, center1, lengthMinMax, threshold) = drawInfo.dataValues

    val spawnBounds =
      boundRect
        .resizeCentered(boundRect.size * size1)
        .translated(boundRect.size * center1 * size1)

    val numSpawns = density * spawnBounds.size

    val points = spawnBounds.mapPoints(numSpawns.x, numSpawns.y) { it }.flatten()

    points.pmap { p ->
      val noiseValue = spawnNoise.getPositive(p.x, p.y)
      val noiseValueThresholded = max(0.0, noiseValue - threshold) * (1 - threshold)
      if (noiseValueThresholded == 0.0) return@pmap listOf<Point>()
      val length = noiseValueThresholded.remap(0.0..1.0, lengthMinMax.first..lengthMinMax.second)

      var lastPoint: Point = p

      (0.0..length step waveStep).map {
        val percent = it / length

        val dirNoise = directionNoise.getPositive(p.x, p.y, percent * directionVariability)
        val direction =
          Deg(dirNoise * 720)

        val newPoint = lastPoint + direction.unitVector * waveStep
        lastPoint = newPoint
        newPoint

        //p + direction.unitVector * it
      }
    }.draw(boundRect)
  }
}

@Serializable
data class GrassData(
  var density: Point = Point.Half,
  var spawnNoise: Noise = Noise.DEFAULT,
  var directionNoise: Noise = Noise.DEFAULT,
  var directionVariability: Double = 0.2,
  var waveStep: Double = 5.0,
  var size: Point = Point(1, 1),
  var center: Point = Point(0, 0),
  var lengthMinMax: Pair<Double, Double> = 0.0 to 10.0,
  var threshold: Double = 0.0,
) : PropData<GrassData> {
  override fun bind() = tabs {
    tab("Global") {
      sliderPair(::lengthMinMax, 0.0..2_000.0, 1.0..2_000.0)
      slider(::threshold, 0..1)
      sliderPair(::density, 0.0..1.0, withLockToggle = true, defaultLocked = true)
      sliderPair(::size, 0.0..2.0, withLockToggle = true, defaultLocked = true)
      slider2D(::center, -1..1)
    }
    tab("spawnNoise") {
      noisePanel(::spawnNoise)
    }
    tab("directionNoise") {
      row {
        style = ControlStyle.Gray
        noisePanel(::directionNoise)
      }
      slider(::directionVariability, 0..1_000)
      slider(::waveStep, 0.2..10.0)
    }
  }

  override fun clone() = copy()

  override fun toSerializer() = serializer()
}

fun main() = GrassSketch().run()
