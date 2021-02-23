package sketches

import BaseSketch
import controls.controls
import controls.intProp
import interfaces.Bindable
import sketches.base.LayeredCanvasSketch

/**
 * Starter sketch that uses all of the latest bells and whistles.
 *
 * Copy and paste this to create a new sketch.
 */
class Example : LayeredCanvasSketch<ExampleTabValues, ExampleGlobalValues>(
  "Example",
  ExampleGlobalValues(),
  { ExampleTabValues() }
) {
  override fun drawSetup(layerInfo: DrawInfo) {
  }

  override fun drawOnce(values: LayerInfo) {
    val (exampleGlobalField) = values.globalValues
    val (exampleTabField) = values.tabValues
  }
}


data class ExampleTabValues(
  var exampleTabField: Int = 1
) : Bindable {
  override fun bind(s: BaseSketch) = controls(
    s.intProp(::exampleTabField, 0..10)
  )
}

data class ExampleGlobalValues(
  var exampleGlobalField: Int = 1,
) : Bindable {
  override fun bind(s: BaseSketch) = controls(
    s.intProp(::exampleGlobalField, 0..10)
  )
}

fun main() = Example().run()
