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
class ThreeD : LayeredCanvasSketch<ThreeDData, ThreeDLayerData>(
  "ThreeD",
  defaultGlobal = ThreeDData(),
  layerToDefaultTab = { ThreeDLayerData() },
) {
  override fun drawSetup(layerInfo: DrawInfo) {}

  override fun drawOnce(layerInfo: LayerInfo) {
    val (exampleGlobalField) = layerInfo.globalValues
    val (exampleTabField) = layerInfo.tabValues


  }
}

@Serializable
data class ThreeDLayerData(
  var exampleTabField: Int = 1,
) : PropData<ThreeDLayerData> {
  override fun bind() = layerTab {
    slider(::exampleTabField, 0..10)
  }

  override fun clone() = copy()
  override fun toSerializer() = serializer()
}

@Serializable
data class ThreeDData(
  var exampleGlobalField: Int = 1,
) : PropData<ThreeDData> {
  override fun bind() = singleTab("Global") {
    slider(::exampleGlobalField, 0..10)
  }

  override fun clone() = copy()

  override fun toSerializer() = serializer()
}

fun main() = ThreeD().run()
