package sketches

import appletExtensions.getParallelLinesInBoundMemo
import controls.panels.ControlStyle
import controls.panels.TabsBuilder.Companion.layerTab
import controls.panels.TabsBuilder.Companion.singleTab
import controls.panels.panelext.intSlider
import controls.panels.panelext.slider
import controls.props.PropData
import controls.props.types.BrushProp
import coordinate.Deg
import kotlinx.serialization.Serializable
import sketches.base.LayeredCanvasSketch
import util.image.getValue

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
    layerInfo.globalValues.brush.drawInteractive(this)
  }

  override fun drawOnce(values: LayerInfo) {
    val (distanceBetweenLines, brush) = values.globalValues
    val (exampleTabField) = values.tabValues


    getParallelLinesInBoundMemo(boundRect, Deg.HORIZONTAL, distanceBetweenLines)
      .forEach { segment ->
        segment.walk(1.0) {
          it.addY(brush.latestAlphaMat?.getValue(it) ?: 0)
        }.draw(boundRect)
      }
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
  var distanceBetweenLines: Double = 5.0,
  var brush: BrushProp = BrushProp(),
) : PropData<InteractiveLinesData> {
  override fun bind() = singleTab("Global") {
    slider(::distanceBetweenLines, 1..500)
    panel(::brush, style = ControlStyle.Red)
  }

  override fun clone() = copy()

  override fun toSerializer() = serializer()
}

fun main() = InteractiveLines().run()
