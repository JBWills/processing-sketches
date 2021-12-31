package controls.panels.panelext

import BaseSketch
import controls.controlsealedclasses.Dropdown.Companion.dropdown
import controls.controlsealedclasses.Slider
import controls.controlsealedclasses.Slider2D
import controls.panels.ControlList.Companion.col
import controls.panels.ControlPanel
import controls.panels.ControlStyle
import controls.panels.PanelBuilder
import controls.panels.Panelable
import controls.props.GenericProp
import coordinate.Point
import fastnoise.Noise
import util.generics.getValues
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

  fun <E : Enum<E>> PanelBuilder.noiseDropdown(
    name: String,
    value: E,
    getNewField: ((oldNoise: Noise, newField: E) -> Noise)
  ): Panelable = dropdown(
    name,
    options = value.getValues(),
    initialValue = value,
    getName = { it.name },
    onSetValue = { updateNoiseField { getNewField(this, it) } },
    shouldMarkDirty = false,
  )

  return col(noiseProp.name) {
    row {
      heightRatio = 4
      noiseDropdown(name = "Quality", value = noise.quality) { oldNoise, newField ->
        oldNoise.with(quality = newField)
      }

      noiseDropdown(name = "Type", value = noise.noiseType) { oldNoise, newField ->
        oldNoise.with(noiseType = newField)
      }
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

    +Slider2D(
      "Offset",
      Point.One..Point(1000, 1000),
      noise.offset,
    ) { updateNoiseField { with(offset = it) } }.withHeight(5)
  }
}
