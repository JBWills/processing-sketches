package sketches

import BaseSketch
import LayerConfig
import controls.ControlGroup
import controls.ControlGroupable
import controls.noiseControls
import coordinate.BoundRect
import coordinate.BoundRect.Companion.mappedOnto
import coordinate.Circ
import coordinate.Deg
import coordinate.Point
import coordinate.Spiral
import fastnoise.FastNoise.NoiseType.Perlin
import fastnoise.Noise
import fastnoise.NoiseQuality.High
import util.property2DSlider
import util.propertySlider
import util.squared
import java.awt.Color
import kotlin.math.sin

open class SpiralSketch(
  isDebugMode: Boolean = false,
  backgroundColor: Color = Color.WHITE,
  sizeX: Int = 9 * 72,
  sizeY: Int = 9 * 72,
) : BaseSketch(
  backgroundColor = backgroundColor,
  svgBaseFileName = "SpiralSketch",
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
  private var moveAmountX: Double = 1.0
  private var moveAmountY: Double = 0.0
  private var spiralRotations: Double = 1.0
  private var spiralSpacing: Double = 200.0
  private var interiorSpiralStartAngle: Double = 0.0
  private var spiralStartAngle: Double = 0.0
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
    ControlGroup(propertySlider(::moveAmountX, r = 0.0..5.0),
      propertySlider(::moveAmountY, r = 0.0..2000.0)),
    ControlGroup(propertySlider(::spiralRotations, r = 0.0..10.0),
      propertySlider(::spiralSpacing, r = 0.0..50.0)),
    ControlGroup(propertySlider(::spiralStartAngle, r = 0.0..2.0),
      propertySlider(::interiorSpiralStartAngle, r = 0.0..2.0)),
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

    val c = Circ(origin, moveAmountY)

    val outerSpiral = Spiral(
      { t, percentAlong, deg ->
        origin + c.pointAtRad(Deg(percentAlong * 360).rad)
      },
      { t, percentAlong, deg ->
        spiralSpacing * t.squared()
      },
      spiralStartAngle..(spiralRotations + spiralStartAngle)
    )

    Spiral(
      { t, percentAlong, deg ->
        outerSpiral.pointAt(percentAlong)
      },
      { t, percentAlong, deg ->
        moveAmountX * (-(-sin(2 * PI * percentAlong * 14) - 1) / 2) + 5
        moveAmountX * (percentAlong * 10).squared()
      },
      interiorSpiralStartAngle..(numCircles.toDouble() + interiorSpiralStartAngle)
    )
      .walk(noise.quality.step / 50)
      .draw(drawBound)

    rect(drawBound)
  }
}

fun main() = SpiralSketch().run()
