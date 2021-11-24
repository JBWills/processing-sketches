package sketches

import BaseSketch
import FastNoiseLite.NoiseType.Perlin
import LayerConfig
import appletExtensions.draw.rect
import controls.controlsealedclasses.Slider.Companion.slider
import controls.controlsealedclasses.Slider2D.Companion.slider2D
import controls.panels.ControlList.Companion.col
import controls.panels.Panelable
import controls.panels.panelext.noisePanel
import coordinate.BoundRect
import coordinate.BoundRect.Companion.mappedOnto
import coordinate.Circ
import coordinate.Point
import fastnoise.Noise
import fastnoise.Noise.Companion.warpedRadially
import fastnoise.NoiseQuality.High
import fastnoise.mapNoiseToPositiveValues
import java.awt.Color

open class CircleWarpSketch(
  backgroundColor: Color = Color.WHITE,
  size: Point = Point(9 * 72, 12 * 72),
) : BaseSketch(
  backgroundColor = backgroundColor,
  svgBaseFileName = "CircleWarpSketch",
  size = size,
) {

  private val outerPaddingX: Double = size.x * 0.02
  private val outerPaddingY: Double = size.y * 0.02
  var drawBound: BoundRect = BoundRect(
    Point(outerPaddingX, outerPaddingY),
    size.x - 2 * outerPaddingX,
    size.y - 2 * outerPaddingY,
  )

  private val points: MutableList<Point> = mutableListOf()
  private var numCircles: Int = 12
  private var circleSpacing: Double = 30.0
  private var moveAmountX: Int = 1
  private var moveAmountY: Int = 1
  private var centerOrigin: Point = Point(0.5, 0.5)

  private var noise: Noise = Noise(
    seed = 100,
    noiseType = Perlin,
    quality = High,
    scale = 0.15,
    offset = Point.Zero,
    strength = Point(0, 0),
  )

  override fun getControls(): Panelable = col {
    row {
      slider(::numCircles, range = 1..1000)
      slider(::circleSpacing, range = 0.001..50.0)
    }

    row {
      slider(::moveAmountX, range = 0..2000)
      slider(::moveAmountY, range = 0..2000)
    }

    noisePanel(::noise)

    row {
      heightRatio = 3
      slider2D(::centerOrigin, Point.Zero..Point.One)
    }
  }

  override fun mousePressed(p: Point) {
    points.add(p)
    markDirty()
  }

  override suspend fun SequenceScope<Unit>.drawOnce(
    layer: Int,
    layerConfig: LayerConfig,
  ) {
    noStroke()

    stroke(Color.BLACK.rgb)
    strokeWeight(2f)
    noFill()

    val origin = centerOrigin.mappedOnto(drawBound.expand(200.0))

    (1..numCircles).forEach { i ->
      val radius = i * circleSpacing

      val multiplier = (i.toDouble() - 1) / numCircles

      val radialMoveStrength = Point(moveAmountX, moveAmountY).magnitude

      val warpedCircle = Circ(origin, radius).warpedRadially(noise, origin) {
        val positiveNoise = it.mapNoiseToPositiveValues()
        positiveNoise * multiplier * radialMoveStrength
      }

      warpedCircle.draw(drawBound)
    }

    rect(drawBound)
  }
}

fun main() = CircleWarpSketch().run()
