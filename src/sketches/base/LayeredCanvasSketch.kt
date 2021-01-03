package sketches.base

import LayerConfig
import controls.ControlField.Companion.intField
import controls.ControlGroupable
import controls.ControlTab
import util.limit
import util.print.Paper
import util.print.Pen
import util.times
import java.awt.Color

abstract class LayeredCanvasSketch(
  svgBaseFilename: String,
  canvas: Paper = Paper.SquareBlack,
  isDebugMode: Boolean = false,
) : CanvasSketch(
  svgBaseFilename,
  canvas,
  isDebugMode,
) {
  val MAX_LAYERS = 10
  var numLayers = intField("numLayersToDisplay", startVal = 1, range = 0..10)

  abstract fun getControlsForLayer(index: Int): Array<ControlGroupable>
  open fun getGlobalControls(): Array<ControlGroupable> = arrayOf()

  override fun getLayers(): List<LayerConfig> = listOf(
    LayerConfig(Pen.WhiteGellyThick),
    LayerConfig(Pen(Color.BLUE)),
    LayerConfig(Pen(Color.RED)),
    LayerConfig(Pen(Color.GREEN)),
    LayerConfig(Pen(Color.LIGHT_GRAY)),
    LayerConfig(Pen(Color.DARK_GRAY)),
    LayerConfig(Pen(Color.ORANGE)),
    LayerConfig(Pen(Color.YELLOW)),
    LayerConfig(Pen(Color.CYAN)),
    LayerConfig(Pen(Color.GRAY)),
    LayerConfig(Pen(Color.MAGENTA)),
  ).limit(numLayers.get() + 1)

  override fun getControlTabs(): Array<ControlTab> = arrayOf(
    ControlTab(
      "page setup",
      *super.getControls().toTypedArray(),
      numLayers,
      *getGlobalControls()
    ),
    *times(MAX_LAYERS) { index ->
      ControlTab("L-${index + 1}", *getControlsForLayer(index))
    }.toTypedArray(),
  )
}