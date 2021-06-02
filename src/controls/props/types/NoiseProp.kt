package controls.props.types

import BaseSketch
import controls.Control.EnumDropdown
import controls.Control.Slider
import controls.Control.Slider2d
import controls.panels.ControlList.Companion.col
import controls.panels.ControlPanel
import coordinate.Point
import fastnoise.Noise
import kotlin.reflect.KMutableProperty0

private fun noiseControls(
  noiseProp: KMutableProperty0<Noise>,
  showStrengthSliders: Boolean = true,
): ControlPanel {
  fun BaseSketch.updateNoiseField(fn: Noise.() -> (Noise)) {
    noiseProp.set(noiseProp.get().fn())
    markDirty()
  }

  val noise = noiseProp.get()

  return col(noiseProp.name) {
    row {
      +EnumDropdown("Quality", noise.quality) {
        updateNoiseField { with(quality = it) }
      }.withHeight(4)

      +EnumDropdown("Type", noise.noiseType) {
        updateNoiseField { with(noiseType = it) }
      }.withHeight(4)
    }
    if (showStrengthSliders) row {
      +Slider(
        "Strength X",
        0.0..2000.0,
        noise.strength.x,
      ) {
        updateNoiseField {
          with(strength = noise.strength.withX(it))
        }
      }

      +Slider(
        "Strength Y",
        0.0..2000.0,
        noise.strength.y,
      ) { updateNoiseField { with(strength = noise.strength.withY(it)) } }
    }

    +Slider(
      "Seed",
      0.0..2000.0,
      noise.seed.toDouble(),
    ) { updateNoiseField { with(seed = it.toInt()) } }
    +Slider("Scale", 0.0..2.0, noise.scale) {
      updateNoiseField { with(scale = it) }
    }
    +Slider2d(
      "Offset",
      Point.One..Point(1000, 1000),
      noise.offset,
    ) { updateNoiseField { with(offset = it) } }.withHeight(5)
  }
}
