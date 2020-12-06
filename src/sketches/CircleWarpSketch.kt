package sketches

import BaseSketch
import SketchConfig
import controls.Control.Button
import controls.ControlGroup
import coordinate.BoundRect
import coordinate.BoundRect.Companion.mappedOnto
import coordinate.Circ
import coordinate.Point
import fastnoise.FastNoise
import fastnoise.FastNoise.NoiseType
import fastnoise.FastNoise.NoiseType.PerlinFractal
import fastnoise.Noise
import fastnoise.Noise.Companion.warpedRadially
import fastnoise.NoiseQuality.Extreme
import fastnoise.NoiseQuality.High
import fastnoise.NoiseQuality.Low
import fastnoise.NoiseQuality.Medium
import fastnoise.NoiseQuality.VeryLow
import fastnoise.mapNoiseToPositiveValues
import util.property2DSlider
import util.propertyEnumDropdown
import util.propertySlider
import java.awt.Color

class CircleWarpConfig : SketchConfig()

open class CircleWarpSketch(
  isDebugMode: Boolean = false,
  backgroundColor: Color = Color.WHITE,
  sizeX: Int = 9 * 72,
  sizeY: Int = 12 * 72,
) : BaseSketch<CircleWarpConfig>(
  backgroundColor = backgroundColor,
  svgBaseFileName = "CircleWarpSketch",
  sketchConfig = null,
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
  private var xMidpointVal: Double = 0.5
  private var numCircles: Int = 1
  private var circleSpacing: Double = 30.0
  private var noiseScale: Int = 15
  private var moveAmountX: Int = 1
  private var moveAmountY: Int = 1
  private var centerOrigin: Point = Point(0.5, 0.5)
  private var noiseOffset: Point = Point(0, 0)
  private var quality: Double = 0.5
  private var seed: Int = 1000
  private var noiseType: NoiseType = PerlinFractal

  override fun getControls() = listOf(
    ControlGroup(
      Button("clear") {
        points.clear()
        markDirty()
      }
    ),
    ControlGroup(
      propertySlider(::xMidpointVal)
    ),
    ControlGroup(propertyEnumDropdown(::noiseType), heightRatio = 5),
    ControlGroup(propertySlider(::quality)),
    ControlGroup(
      propertySlider(::numCircles, r = 1..100),
      propertySlider(::circleSpacing, r = 0.001..200.0)
    ),
    ControlGroup(propertySlider(::noiseScale, r = 1..100)),
    ControlGroup(propertySlider(::moveAmountX, r = 0..2000), propertySlider(::moveAmountY, r = 0..2000)),
    ControlGroup(propertySlider(::seed, r = 0..2000)),
    ControlGroup(property2DSlider(::noiseOffset, Point.One..Point(1000, 1000)), heightRatio = 5),
    ControlGroup(property2DSlider(::centerOrigin, Point.Zero..Point(1, 1)), heightRatio = 5)
  )

  override fun mousePressed(p: Point) {
    points.add(p)
    markDirty()
  }

  override fun getRandomizedConfig() = CircleWarpConfig()

  private fun getQuality() = when {
    quality < 0.3 -> VeryLow
    quality < 0.5 -> Low
    quality < 0.7 -> Medium
    quality < 0.9 -> High
    else -> Extreme
  }

  override fun drawOnce(config: CircleWarpConfig) {
    noStroke()

    stroke(Color.BLACK.rgb)
    strokeWeight(2f)
    noFill()

    NOISE.SetSeed(seed)
    NOISE.SetNoiseType(noiseType)

    val fastNoise = FastNoise(seed)
    fastNoise.SetNoiseType(noiseType)
    val noise = Noise(fastNoise, getQuality(), noiseScale / 100.0, noiseOffset, Point(moveAmountX, moveAmountY))

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

fun main() = BaseSketch.run(CircleWarpSketch())
