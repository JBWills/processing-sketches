package controls

import BaseSketch
import controls.Control.EnumDropdown
import controls.Control.Slider
import controls.Control.Slider2d
import controls.ControlGroup.Companion.group
import controls.ControlGroup.Companion.groupIf
import coordinate.Point
import fastnoise.Noise
import kotlin.reflect.KMutableProperty0

fun BaseSketch.noiseControls(
  noiseProp: KMutableProperty0<Noise>,
  showStrengthSliders: Boolean = true,
): Array<ControlGroupable> {
  fun updateNoiseField(fn: Noise.() -> (Noise)) {
    noiseProp.set(noiseProp.get().fn())
    markDirty()
  }

  return controls(
    group(
      EnumDropdown("${noiseProp.name} quality", noiseProp.get().quality) {
        updateNoiseField { with(quality = it) }
      },
      EnumDropdown("${noiseProp.name} Type", noiseProp.get().noiseType) {
        updateNoiseField { with(noiseType = it) }
      },
      heightRatio = 4
    ),
    groupIf(
      showStrengthSliders,
      Slider(
        "${noiseProp.name} Strength X",
        0.0..2000.0,
        noiseProp.get().strength.x
      ) {
        updateNoiseField {
          with(strength = Point(it, noiseProp.get().strength.y))
        }
      },
      Slider(
        "${noiseProp.name} Strength Y",
        0.0..2000.0,
        noiseProp.get().strength.y
      ) {
        updateNoiseField {
          with(strength = Point(noiseProp.get().strength.x, it))
        }
      }),
    Slider(
      "${noiseProp.name} Seed", 0.0..2000.0,
      noiseProp.get().seed.toDouble()
    ) { updateNoiseField { with(seed = it.toInt()) } },
    Slider("${noiseProp.name} scale", 0.0..2.0, noiseProp.get().scale) {
      updateNoiseField { with(scale = it) }
    },
    group(
      Slider2d(
        "${noiseProp.name} offset",
        Point.One..Point(1000, 1000),
        noiseProp.get().offset
      ) { updateNoiseField { with(offset = it) } },
      heightRatio = 5
    ),
  )
}
