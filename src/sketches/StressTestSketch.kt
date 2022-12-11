package sketches

import FastNoiseLite.NoiseType.Value
import FastNoiseLite.NoiseType.ValueCubic
import controls.controlsealedclasses.Button.Companion.button
import controls.controlsealedclasses.Slider.Companion.slider
import controls.controlsealedclasses.Slider2D.Companion.slider2D
import controls.controlsealedclasses.Slider2DArgs
import controls.panels.ControlStyle
import controls.panels.TabsBuilder.Companion.tabs
import controls.panels.panelext.noisePanel
import controls.props.PropData
import controls.props.types.ShapeProp
import coordinate.Point
import fastnoise.Noise
import kotlinx.serialization.Serializable
import sketches.base.LayeredCanvasSketch
import util.base.ZeroToOne

/**
 * Starter sketch that uses all of the latest bells and whistles.
 *
 * Copy and paste this to create a new sketch.
 */
class StressTest : LayeredCanvasSketch<StressTestData, StressTestLayerData>(
  "StressTest",
  defaultGlobal = StressTestData(),
  layerToDefaultTab = { StressTestLayerData() },
) {
  override fun drawSetup(layerInfo: DrawInfo) {}

  override fun drawOnce(layerInfo: LayerInfo) {}
}

@Serializable
data class StressTestLayerData(
  var noise: Noise = Noise.DEFAULT.with(
    noiseType = ValueCubic,
    scale = 1.07,
  ),
  var noise2: Noise = Noise.DEFAULT.with(
    noiseType = Value,
    scale = 0.5,
  ),
  var point: Point = Point.One,
  var double: Double = 1.0,
  var int: Int = 2,
  var shape: ShapeProp = ShapeProp(),
  var shape2: ShapeProp = ShapeProp(),
  var shape3: ShapeProp = ShapeProp(),
  var shape4: ShapeProp = ShapeProp(),
) : PropData<StressTestLayerData> {
  override fun bind() = tabs {
    tab("L") {
      row {
        noisePanel(::noise)
        noisePanel(::noise2)
      }

      row {
        col {
          style = ControlStyle.Orange
          heightRatio = 5

          slider2D(::point, Slider2DArgs(0..1))
          slider(::double, ZeroToOne)
        }

        col {
          style = ControlStyle.Gray

          button("click me!") {
            println("Button clicked")
          }

          slider(::int, 0..1)

          panel(::shape)
          panel(::shape2, style = ControlStyle.Green)
        }
      }
    }

    tab("L2") {
      style = ControlStyle.Gray
      panel(::shape3, style = ControlStyle.Red)
      panel(::shape4, style = ControlStyle.Green)
    }
  }

  override fun clone() = copy()
  override fun toSerializer() = serializer()
}

@Serializable
data class StressTestData(
  var noiseGlobal: Noise = Noise.DEFAULT.with(
    noiseType = ValueCubic,
    scale = 1.07,
  ),
  var noiseGlobal2: Noise = Noise.DEFAULT.with(
    noiseType = Value,
    scale = 0.5,
  ),
  var pointGlobal: Point = Point.One,
  var doubleGlobal: Double = 1.0,
  var intGlobal: Int = 2,
  var shapeGlobal: ShapeProp = ShapeProp(),
  var shapeGlobal2: ShapeProp = ShapeProp(),
  var shapeGlobal3: ShapeProp = ShapeProp(),
  var shapeGlobal4: ShapeProp = ShapeProp(),
) : PropData<StressTestData> {
  override fun bind() = tabs {
    tab("Global") {
      row {
        noisePanel(::noiseGlobal)
        noisePanel(::noiseGlobal2)
      }

      row {
        col {
          style = ControlStyle.Orange
          heightRatio = 5

          slider2D(::pointGlobal, Slider2DArgs(ZeroToOne))
          slider(::doubleGlobal, ZeroToOne)
        }

        col {
          style = ControlStyle.Gray

          button("click me!") {
            println("Button clicked")
          }

          slider(::intGlobal, 0..1)

          panel(::shapeGlobal)
          panel(::shapeGlobal2, style = ControlStyle.Green)
        }
      }
    }

    tab("baby") {
      style = ControlStyle.Gray
      panel(::shapeGlobal3, style = ControlStyle.Red)
      panel(::shapeGlobal4, style = ControlStyle.Green)
    }
  }

  override fun clone() = copy()

  override fun toSerializer() = serializer()
}

fun main() = StressTest().run()
