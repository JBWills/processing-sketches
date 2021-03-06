package sketches

import controls.panels.TabsBuilder.Companion.layerTab
import controls.panels.TabsBuilder.Companion.singleTab
import controls.panels.panelext.slider
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
  defaultGlobal = ExampleData(),
  layerToDefaultTab = { ExampleLayerData() },
) {
  override fun drawSetup(layerInfo: DrawInfo) {}

  override fun drawOnce(layerInfo: LayerInfo) {
    val (exampleGlobalField) = layerInfo.globalValues
    val (exampleTabField) = layerInfo.tabValues
  }
}

@Serializable
data class ExampleLayerData(
  var exampleTabField: Int = 1,
) : PropData<ExampleLayerData> {
  override fun bind() = layerTab {
    slider(::exampleTabField, 0..10)
  }

  override fun clone() = copy()
  override fun toSerializer() = serializer()
}

@Serializable
data class ExampleData(
  var exampleGlobalField: Int = 1,
) : PropData<ExampleData> {
  override fun bind() = singleTab("Global") {
    slider(::exampleGlobalField, 0..10)
  }

  override fun clone() = copy()

  override fun toSerializer() = serializer()
}

fun main() = Example().run()
