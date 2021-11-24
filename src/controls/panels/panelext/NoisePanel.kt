package controls.panels.panelext

import BaseSketch
import controls.controlsealedclasses.Control.EnumDropdown
import controls.controlsealedclasses.Control.Slider
import controls.controlsealedclasses.Control.Slider2d
import controls.panels.ControlList
import controls.panels.ControlPanel
import controls.panels.ControlStyle
import controls.panels.PanelBuilder
import controls.props.GenericProp
import coordinate.Point
import fastnoise.Noise
import java.awt.Color
import kotlin.reflect.KMutableProperty0

fun PanelBuilder.noisePanel(
  ref: KMutableProperty0<Noise>,
  showStrengthSliders: Boolean = true,
  style: ControlStyle = ControlStyle.Orange.withColor(
    frameBackground = Color(50, 20, 0),
  ),
  shouldMarkDirty: Boolean = true,
) = addNewPanel(style) {
  GenericProp(ref) {
    noiseControls(ref, showStrengthSliders, shouldMarkDirty)
  }
}

private fun noiseControls(
  noiseProp: KMutableProperty0<Noise>,
  showStrengthSliders: Boolean = true,
  shouldMarkDirty: Boolean = true,
): ControlPanel {
  fun BaseSketch.updateNoiseField(fn: Noise.() -> (Noise)) {
    noiseProp.set(noiseProp.get().fn())
    markDirtyIf(shouldMarkDirty)
  }

  val noise = noiseProp.get()

  return ControlList.col(noiseProp.name) {
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
