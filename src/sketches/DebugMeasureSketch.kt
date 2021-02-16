package sketches

import BaseSketch
import LayerConfig
import SketchConfig
import controls.ControlGroup
import controls.ControlGroupable
import controls.noiseControls
import coordinate.BoundRect
import coordinate.Point
import fastnoise.FastNoise.NoiseType.Perlin
import fastnoise.Noise
import fastnoise.NoiseQuality.High
import util.print.DPI
import util.print.Paper
import util.property2DSlider
import util.propertySlider
import java.awt.Color

class DebugMeasureConfig : SketchConfig()

open class DebugMeasureSketch(
  isDebugMode: Boolean = false,
  backgroundColor: Color = Color.WHITE,
  sizeX: Int = DPI.InkScape.toPixels(20),
  sizeY: Int = DPI.InkScape.toPixels(20),
) : BaseSketch<DebugMeasureConfig>(
  backgroundColor = backgroundColor,
  svgBaseFileName = "DebugMeasureSketch",
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

  override fun getRandomizedConfig() = DebugMeasureConfig()

  override fun drawOnce(config: DebugMeasureConfig, layer: Int, layerConfig: LayerConfig) {
    noStroke()

    stroke(Color.BLACK.rgb)
    strokeWeight(2f)
    noFill()
    for (paper in Paper.values()) {
      textAlign(RIGHT, BOTTOM)
      textSize(32f)
      fill(Color.BLACK.rgb)
      val bound = BoundRect(Point.Zero, paper.verticalPx(), paper.horizontalPx())
      text(paper.name, bound.bottomRight.xf, bound.bottomRight.yf)

      noFill()
      rect(bound)
    }

  }
}

fun main() = BaseSketch.run(DebugMeasureSketch())
