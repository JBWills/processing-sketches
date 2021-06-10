package sketches

import controls.panels.TabsBuilder.Companion.layerTab
import controls.panels.TabsBuilder.Companion.singleTab
import controls.panels.panelext.intSlider
import controls.props.PropData
import controls.props.types.BrushProp
import kotlinx.serialization.Serializable
import sketches.base.LayeredCanvasSketch

/**
 * Starter sketch that uses all of the latest bells and whistles.
 *
 * Copy and paste this to create a new sketch.
 */
class InteractiveLines : LayeredCanvasSketch<InteractiveLinesData, InteractiveLinesLayerData>(
  "InteractiveLines",
  defaultGlobal = InteractiveLinesData(),
  layerToDefaultTab = { InteractiveLinesLayerData() },
) {
  override fun drawSetup(layerInfo: DrawInfo) {}

  override fun drawInteractive(layerInfo: DrawInfo) {
    val (brush) = layerInfo.globalValues
    brush.drawInteractive(this)
  }

  override fun drawOnce(values: LayerInfo) {
    val (brush) = values.globalValues
    val (exampleTabField) = values.tabValues

  }
}

@Serializable
data class InteractiveLinesLayerData(
  var layerInt: Int = 0,
) : PropData<InteractiveLinesLayerData> {
  override fun bind() = layerTab {
    intSlider(::layerInt, 0..10)
  }

  override fun clone() = copy()
  override fun toSerializer() = serializer()
}

@Serializable
data class InteractiveLinesData(
  var brush: BrushProp = BrushProp(),
) : PropData<InteractiveLinesData> {
  override fun bind() = singleTab("Global") {
    panel(::brush)
  }

  override fun clone() = copy()

  override fun toSerializer() = serializer()
}

fun main() = InteractiveLines().run()
