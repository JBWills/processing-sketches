package sketches

import BaseSketch
import FastNoiseLite.NoiseType.Perlin
import LayerConfig
import controls.panels.ControlList.Companion.col
import controls.panels.ControlList.Companion.row
import controls.panels.Panelable
import controls.props.types.doubleProp
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
  sizeX: Int = 9 * 72,
  sizeY: Int = 12 * 72,
) : BaseSketch(
  backgroundColor = backgroundColor,
  svgBaseFileName = "CircleWarpSketch",
  sizeX = sizeX,
  sizeY = sizeY,
) {

  private val outerPaddingX: Double = sizeX * 0.02
  private val outerPaddingY: Double = sizeY * 0.02
  var drawBound: BoundRect = BoundRect(
    Point(outerPaddingX, outerPaddingY),
    sizeX - 2 * outerPaddingX,
    sizeY - 2 * outerPaddingY,
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
      intSlider(::numCircles, range = 1..1000)
      doubleProp(::circleSpacing, range = 0.001..50.0)
    }

    row {
      intSlider(::moveAmountX, range = 0..2000)
      intSlider(::moveAmountY, range = 0..2000)
    }

    noisePanel(::noise)

    row(heightOverride = 3) {
      slider2D(::centerOrigin, Point.Zero..Point.One)
    }
  }

  override fun mousePressed(p: Point) {
    points.add(p)
    markDirty()
  }

  override fun drawOnce(layer: Int, layerConfig: LayerConfig) {
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

      shape(warpedCircle, bound = drawBound)
    }

    rect(drawBound)
  }
}

fun main() = CircleWarpSketch().run()
