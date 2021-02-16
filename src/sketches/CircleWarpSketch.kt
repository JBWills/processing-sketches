package sketches

import BaseSketch
import LayerConfig
import controls.ControlGroup
import controls.ControlGroupable
import controls.noiseControls
import coordinate.BoundRect
import coordinate.BoundRect.Companion.mappedOnto
import coordinate.Circ
import coordinate.Point
import fastnoise.FastNoise.NoiseType.Perlin
import fastnoise.Noise
import fastnoise.Noise.Companion.warpedRadially
import fastnoise.NoiseQuality.High
import fastnoise.mapNoiseToPositiveValues
import util.property2DSlider
import util.propertySlider
import java.awt.Color

open class CircleWarpSketch(
  isDebugMode: Boolean = false,
  backgroundColor: Color = Color.WHITE,
  sizeX: Int = 9 * 72,
  sizeY: Int = 12 * 72,
) : BaseSketch(
  backgroundColor = backgroundColor,
  svgBaseFileName = "CircleWarpSketch",
  sizeX = sizeX,
  sizeY = sizeY,
  isDebugMode = isDebugMode
) {

  private val outerPaddingX: Double = sizeX * 0.02
  private val outerPaddingY: Double = sizeY * 0.02
  var drawBound: BoundRect = BoundRect(
    Point(outerPaddingX, outerPaddingY),
    sizeY - 2 * outerPaddingY,
    sizeX - 2 * outerPaddingX
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
    strength = Point(0, 0)
  )

  override fun getControls(): List<ControlGroupable> = listOf(
    ControlGroup(
      propertySlider(::numCircles, r = 1..1000),
      propertySlider(::circleSpacing, r = 0.001..50.0)
    ),
    ControlGroup(propertySlider(::moveAmountX, r = 0..2000),
      propertySlider(::moveAmountY, r = 0..2000)),
    *noiseControls(::noise),
    ControlGroup(property2DSlider(::centerOrigin, Point.Zero..Point(1, 1)), heightRatio = 5)
  )

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
