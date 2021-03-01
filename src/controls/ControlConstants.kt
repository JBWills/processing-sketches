package controls

import BaseSketch
import controls.Control.EnumDropdown
import controls.Control.Slider
import controls.Control.Slider2d
import coordinate.Point
import fastnoise.Noise
import kotlin.reflect.KMutableProperty0

fun BaseSketch.noiseControls(
  noiseProp: KMutableProperty0<Noise>,
): Array<ControlGroupable> {
  fun setNoiseFieldAndMarkDirty(fn: Noise.() -> (Noise)) {
    noiseProp.set(noiseProp.get().fn())
    markDirty()
  }

  return arrayOf(
    ControlGroup(
      EnumDropdown("${noiseProp.name} quality", noiseProp.get().quality) {
        setNoiseFieldAndMarkDirty {
          with(quality = it)
        }
      },
      EnumDropdown("${noiseProp.name} Noise Type", noiseProp.get().noiseType,
        { setNoiseFieldAndMarkDirty { with(noiseType = it) } }),
      heightRatio = 4
    ),
    ControlGroup(
      Slider(
        "${noiseProp.name} Strength X",
        0.0..2000.0,
        noiseProp.get().strength.x
      ) { setNoiseFieldAndMarkDirty { with(strength = Point(it, noiseProp.get().strength.y)) } },
      Slider(
        "${noiseProp.name} Strength Y",
        0.0..2000.0,
        noiseProp.get().strength.y
      ) { setNoiseFieldAndMarkDirty { with(strength = Point(noiseProp.get().strength.x, it)) } }),
    ControlGroup(Slider("${noiseProp.name} Seed", 0.0..2000.0,
      noiseProp.get().seed.toDouble()) { setNoiseFieldAndMarkDirty { with(seed = it.toInt()) } }),
    ControlGroup(Slider("${noiseProp.name} noise scale", 0.0..2.0, noiseProp.get().scale) {
      setNoiseFieldAndMarkDirty {
        with(scale = it)
      }
    }),
    ControlGroup(
      Slider2d("${noiseProp.name} noise offset", Point.One..Point(1000, 1000),
        noiseProp.get().offset) { setNoiseFieldAndMarkDirty { with(offset = it) } },
      heightRatio = 5
    ),
  )
}
