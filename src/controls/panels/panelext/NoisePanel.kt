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
import controls.panels.panelext.util.RefGetter
import controls.props.GenericProp
import coordinate.Point
import fastnoise.Noise
import util.base.getValues
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
      fineSliderPairPanel(
        object : RefGetter<Point> {
          override val name: String
            get() = "Strength"

          override fun get(): Point = noise.strength

          override fun set(item: Point) = noiseProp.set(noise.with(strength = item))
        },
        FineSliderPairArgs(
          coarseRange = 0.0..2000.0,
          fineRange = -15.0..15.0,
          shouldMarkDirty = shouldMarkDirty,
          withLockToggle = true,
        ),
      )
    }

    +Slider(
      "Seed",
      0.0..2000.0,
      noise.seed.toDouble(),
    ) { updateNoiseField { with(seed = it.toInt()) } }

    row {
      fineSliderPanel(
        object : RefGetter<Double> {
          override val name: String get() = "Scale"
          override fun get(): Double = noise.scale
          override fun set(item: Double) = noiseProp.set(noise.with(scale = item))
        },
        FineSliderArgs(range = 0.0..20.0, fineRange = -1.0..1.0),
      )
    }

//    +Slider("Scale", 0.0..2.0, noise.scale) {
//      updateNoiseField { with(scale = it) }
//    }

    +Slider2D(
      "Offset",
      1.0..1000.0,
      1.0..1000.0,
      noise.offset,
    ) { updateNoiseField { with(offset = it) } }.withHeight(5)
  }
}
