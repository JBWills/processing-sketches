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
class Example : LayeredCanvasSketch<ExampleData, ExampleLayerData>(
  "Example",
  ExampleData(),
  { ExampleLayerData() }
) {
  override fun drawSetup(layerInfo: DrawInfo) {}

  override fun drawOnce(values: LayerInfo) {
    val (exampleGlobalField) = values.globalValues
    val (exampleTabField) = values.tabValues
  }
}

@Serializable
data class ExampleLayerData(
  var exampleTabField: Int = 1
) : PropData<ExampleLayerData> {
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
data class ExampleData(
  var exampleGlobalField: Int = 1,
) : PropData<ExampleData> {
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
