package sketches

import BaseSketch
import controls.ControlSection.Companion.section
import controls.ControlTab.Companion.tab
import controls.intProp
import interfaces.Bindable
import interfaces.Copyable
import interfaces.TabBindable
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
  override fun drawSetup(layerInfo: DrawInfo) {}

  override fun drawOnce(values: LayerInfo) {
    val (exampleGlobalField) = values.globalValues
    val (exampleTabField) = values.tabValues
  }
}


data class ExampleTabValues(
  var exampleTabField: Int = 1
) : TabBindable, Copyable<ExampleTabValues> {
  override fun BaseSketch.bindTab() = tab(
    "L",
    intProp(::exampleTabField, 0..10)
  )

  override fun clone() = copy()
}

data class ExampleGlobalValues(
  var exampleGlobalField: Int = 1,
) : Bindable, Copyable<ExampleGlobalValues> {
  override fun BaseSketch.bind() = listOf(
    tab("Global",
      section(
        intProp(::exampleGlobalField, 0..10)
      )
    )
  )

  override fun clone() = copy()
}

fun main() = Example().run()
