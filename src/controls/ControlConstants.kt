package controls

import BaseSketch
import SketchConfig
import controls.Control.Dropdown
import controls.Control.Slider
import controls.Control.Slider2d
import coordinate.Point
import fastnoise.FastNoise.NoiseType
import fastnoise.Noise
import util.property2DSlider
import util.propertyEnumDropdown
import util.propertySlider
import kotlin.reflect.KMutableProperty0

fun <TConfig : SketchConfig> BaseSketch<TConfig>.noiseControls(
  noiseTypeProp: KMutableProperty0<NoiseType>,
  seedProp: KMutableProperty0<Int>,
  noiseOffsetProp: KMutableProperty0<Point>,
  centerOriginProp: KMutableProperty0<Point>,
  noiseScaleProp: KMutableProperty0<Int>,
) = arrayOf(
  ControlGroup(propertyEnumDropdown(noiseTypeProp), heightRatio = 5),
  ControlGroup(propertySlider(seedProp, r = 0..2000)),
  ControlGroup(propertySlider(noiseScaleProp, r = 1..100)),
  ControlGroup(property2DSlider(noiseOffsetProp, Point.One..Point(1000, 1000)), heightRatio = 5),
  ControlGroup(property2DSlider(centerOriginProp, Point.Zero..Point(1, 1)), heightRatio = 5),
)

fun <TConfig : SketchConfig> BaseSketch<TConfig>.noiseControls(
  noiseProp: KMutableProperty0<Noise>,
): Array<ControlGroup> {
  fun setNoiseFieldAndMarkDirty(fn: Noise.() -> (Noise)) {
    noiseProp.set(noiseProp.get().fn())
    markDirty()
  }

  return arrayOf(
    ControlGroup(
      Dropdown("quality", noiseProp.get().quality) {
        setNoiseFieldAndMarkDirty {
          with(quality = it)
        }
      },
      Dropdown("Noise Type", noiseProp.get().noiseType,
        { setNoiseFieldAndMarkDirty { with(noiseType = it) } }),
      heightRatio = 4
    ),
    ControlGroup(
      Slider(
        "Strength X",
        0.0..2000.0,
        noiseProp.get().strength.x
      ) { setNoiseFieldAndMarkDirty { with(strength = Point(it, noiseProp.get().strength.y)) } },
      Slider(
        "Strength Y",
        0.0..2000.0,
        noiseProp.get().strength.y
      ) { setNoiseFieldAndMarkDirty { with(strength = Point(noiseProp.get().strength.x, it)) } }),
    ControlGroup(Slider("Seed", 0.0..2000.0,
      noiseProp.get().seed.toDouble()) { setNoiseFieldAndMarkDirty { with(seed = it.toInt()) } }),
    ControlGroup(Slider("noise scale", 0.0..2.0, noiseProp.get().scale) {
      setNoiseFieldAndMarkDirty {
        with(scale = it)
      }
    }),
    ControlGroup(
      Slider2d("noise offset", Point.One..Point(1000, 1000),
        noiseProp.get().offset) { setNoiseFieldAndMarkDirty { with(offset = it) } },
      heightRatio = 5
    ),
  )
}
