package sketches.legacy

import BaseSketch
import FastNoiseLite.NoiseType.Perlin
import LayerConfig
import controls.panels.ControlList.Companion.col
import controls.panels.ControlList.Companion.row
import controls.panels.Panelable
import coordinate.BoundRect
import coordinate.Point
import fastnoise.Noise
import fastnoise.NoiseQuality.High
import util.print.DPI
import util.print.Orientation.Landscape
import util.print.Paper
import java.awt.Color

open class DebugMeasureSketch(
  backgroundColor: Color = Color.WHITE,
  sizeX: Int = DPI.InkScape.toPixels(20),
  sizeY: Int = DPI.InkScape.toPixels(20),
) : BaseSketch(
  backgroundColor = backgroundColor,
  svgBaseFileName = "DebugMeasureSketch",
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
    strength = Point(0, 0),
  )

  override fun getControls(): Panelable = col {
    row {
      intSlider(::numCircles, range = 1..1000)
      slider(::circleSpacing, range = 0.001..50.0)
    }

    row {
      slider(::moveAmountX, range = 0.0..5.0)
      slider(::moveAmountY, range = 0.0..2000.0)
    }

    row {
      slider(::spiralRotations, range = 0.0..10.0)
      slider(::spiralSpacing, range = 0.0..50.0)
    }

    row {
      slider(::spiralStartAngle, range = 0.0..2.0)
      slider(::interiorSpiralStartAngle, range = 0.0..2.0)
    }

    noisePanel(::noise)

    row(heightOverride = 3) {
      slider2D(::centerOrigin, Point.Zero..Point.One)
    }
  }

  override fun drawOnce(layer: Int, layerConfig: LayerConfig) {
    noStroke()

    stroke(Color.BLACK.rgb)
    strokeWeight(2f)
    noFill()
    for (paper in Paper.values()) {
      textAlign(RIGHT, BOTTOM)
      textSize(32f)
      fill(Color.BLACK.rgb)
      val bound = paper.toBoundRect(Landscape)
      text(paper.name, bound.bottomRight.xf, bound.bottomRight.yf)

      noFill()
      rect(bound)
    }

  }
}

fun main() = DebugMeasureSketch().run()
