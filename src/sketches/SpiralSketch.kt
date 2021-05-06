package sketches

import BaseSketch
import FastNoiseLite.NoiseType.Perlin
import LayerConfig
import controls.panels.TabsBuilder.Companion.tabs
import coordinate.BoundRect
import coordinate.BoundRect.Companion.mappedOnto
import coordinate.Circ
import coordinate.Deg
import coordinate.Point
import coordinate.Spiral
import fastnoise.Noise
import fastnoise.NoiseQuality.High
import util.pointsAndLines.polyLine.normalizeDistances
import util.squared
import java.awt.Color
import kotlin.math.sin

open class SpiralSketch(
  backgroundColor: Color = Color.WHITE,
  size: Point = Point(9 * 72, 9 * 72),
) : BaseSketch(
  backgroundColor = backgroundColor,
  svgBaseFileName = "SpiralSketch",
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
  private var moveAmountX: Double = 1.0
  private var moveAmountY: Double = 0.0
  private var withNormalization: Boolean = false
  private var normalizeMin: Double = 1.0
  private var normalizeMax: Double = 5.0
  private var normalizeAngleCutoff: Double = 45.0
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

  override fun getControlTabs() = tabs {
    tab("Spiral") {
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

      row {
        col {
          toggle(::withNormalization)
          slider(::normalizeAngleCutoff, range = 0.0..360.0)
        }
        col {
          slider(::normalizeMin, range = 0.0..100.0)
          slider(::normalizeMax, range = 0.0..100.0)
        }
      }

      noisePanel(::noise)

      slider2D(::centerOrigin, Point.Zero..Point.One)
    }
  }.toTypedArray()

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
      spiralStartAngle..(spiralRotations + spiralStartAngle),
    )

    var finalSpiral = Spiral(
      { t, percentAlong, deg ->
        outerSpiral.pointAt(percentAlong)
      },
      { t, percentAlong, deg ->
        moveAmountX * (-(-sin(2 * PI * percentAlong * 14) - 1) / 2) + 5
        moveAmountX * (percentAlong * 10).squared()
      },
      interiorSpiralStartAngle..(numCircles.toDouble() + interiorSpiralStartAngle),
    )
      .walk(noise.quality.step / 50)

    if (withNormalization) {
      finalSpiral = finalSpiral.normalizeDistances(normalizeMin..normalizeMax, normalizeAngleCutoff)
    }

    finalSpiral.draw(drawBound)

    rect(drawBound)
  }
}

fun main() = SpiralSketch().run()
