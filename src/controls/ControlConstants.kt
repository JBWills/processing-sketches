package controls

import BaseSketch
import controls.Control.EnumDropdown
import controls.Control.Slider
import controls.Control.Slider2d
import controls.panels.ControlList.Companion.col
import controls.panels.ControlList.Companion.row
import controls.panels.ControlList.Companion.rowIf
import controls.panels.ControlPanel
import coordinate.Point
import fastnoise.Noise
import kotlin.reflect.KMutableProperty0

fun BaseSketch.noiseControls(
  noiseProp: KMutableProperty0<Noise>,
  showStrengthSliders: Boolean = true,
): ControlPanel {
  fun updateNoiseField(fn: Noise.() -> (Noise)) {
    noiseProp.set(noiseProp.get().fn())
    markDirty()
  }

  val name = noiseProp.name
  val noise = noiseProp.get()

  fun text(fieldName: String) = "$name $fieldName"

  return col(
    row(
      EnumDropdown(text("Quality"), noise.quality) {
        updateNoiseField { with(quality = it) }
      }.withHeight(4),
      EnumDropdown(text("Type"), noise.noiseType) {
        updateNoiseField { with(noiseType = it) }
      }.withHeight(4),
    ),
    rowIf(
      showStrengthSliders,
      Slider(
        text("Strength X"),
        0.0..2000.0,
        noise.strength.x,
      ) {
        updateNoiseField {
          with(strength = noise.strength.withX(it))
        }
      },
      Slider(
        text("Strength Y"),
        0.0..2000.0,
        noise.strength.y,
      ) { updateNoiseField { with(strength = noise.strength.withY(it)) } },
    ),
    Slider(
      text("Seed"),
      0.0..2000.0,
      noise.seed.toDouble(),
    ) { updateNoiseField { with(seed = it.toInt()) } },
    Slider(text("Scale"), 0.0..2.0, noise.scale) {
      updateNoiseField { with(scale = it) }
    },
    Slider2d(
      text("Offset"),
      Point.One..Point(1000, 1000),
      noise.offset,
    ) { updateNoiseField { with(offset = it) } }.withHeight(5),
  )
}
