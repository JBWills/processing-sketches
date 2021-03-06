package sketches

import BaseSketch
import controls.ControlSection.Companion.section
import controls.ControlTab.Companion.tab
import controls.intProp
import controls.props.PropData
import kotlinx.serialization.Serializable
import sketches.base.LayeredCanvasSketch

/**
 * Starter sketch that uses all of the latest bells and whistles.
 *
 * Copy and paste this to create a new sketch.
 */
class Example : LayeredCanvasSketch<ExampleGlobalValues, ExampleTabValues>(
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

@Serializable
data class ExampleTabValues(
  var exampleTabField: Int = 1
) : PropData<ExampleTabValues> {
  override fun BaseSketch.bind() = listOf(
    tab(
      "L",
      intProp(::exampleTabField, 0..10)
    )
  )

  override fun clone() = copy()
  override fun toSerializer() = serializer()
}

@Serializable
data class ExampleGlobalValues(
  var exampleGlobalField: Int = 1,
) : PropData<ExampleGlobalValues> {
  override fun BaseSketch.bind() = listOf(
    tab(
      "Global",
      section(
        intProp(::exampleGlobalField, 0..10)
      )
    )
  )

  override fun clone() = copy()

  override fun toSerializer() = serializer()
}

fun main() = Example().run()
