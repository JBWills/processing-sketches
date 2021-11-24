package sketches

import controls.panels.TabsBuilder.Companion.layerTab
import controls.panels.TabsBuilder.Companion.singleTab
import controls.controlsealedclasses.Slider.Companion.slider
import controls.props.PropData
import kotlinx.serialization.Serializable
import sketches.base.LayeredCanvasSketch

/**
 * Starter sketch that uses all of the latest bells and whistles.
 *
 * Copy and paste this to create a new sketch.
 */
class RippleLines : LayeredCanvasSketch<RippleLinesData, RippleLinesLayerData>(
  "RippleLines",
  defaultGlobal = RippleLinesData(),
  layerToDefaultTab = { RippleLinesLayerData() },
) {
  override fun drawSetup(layerInfo: DrawInfo) {}

  override fun drawOnce(layerInfo: LayerInfo) {
    val (exampleGlobalField) = layerInfo.globalValues
    val (exampleTabField) = layerInfo.tabValues
  }
}

@Serializable
data class RippleLinesLayerData(
  var exampleTabField: Int = 1,
) : PropData<RippleLinesLayerData> {
  override fun bind() = layerTab {
    slider(::exampleTabField, 0..10)
  }

  override fun clone() = copy()
  override fun toSerializer() = serializer()
}

@Serializable
data class RippleLinesData(
  var exampleGlobalField: Int = 1,
) : PropData<RippleLinesData> {
  override fun bind() = singleTab("Global") {
    slider(::exampleGlobalField, 0..10)
  }

  override fun clone() = copy()

  override fun toSerializer() = serializer()
}

fun main() = RippleLines().run()
