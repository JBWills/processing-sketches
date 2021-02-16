package sketches.base

import LayerConfig
import controls.ControlField.Companion.intField
import controls.ControlGroupable
import controls.ControlTab
import util.limit
import util.print.Orientation
import util.print.Paper
import util.print.Pen
import util.times
import java.awt.Color

abstract class LayeredCanvasSketch(
  svgBaseFilename: String,
  canvas: Paper = Paper.SquareBlack,
  orientation: Orientation = Orientation.Landscape,
  val maxLayers: Int = MAX_LAYERS,
  isDebugMode: Boolean = false,
) : CanvasSketch(
  svgBaseFilename,
  canvas,
  orientation,
  isDebugMode,
) {
  var numLayers = intField("numLayersToDisplay", startVal = 1, range = 0..maxLayers)

  abstract fun getControlsForLayer(index: Int): Array<ControlGroupable>
  open fun getGlobalControls(): Array<ControlGroupable> = arrayOf()

  override fun getLayers(): List<LayerConfig> = listOf(
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
  ).limit(numLayers.get()) + LayerConfig(Pen.WhiteGellyThick)

  override fun getControlTabs(): Array<ControlTab> = arrayOf(
    ControlTab(
      CANVAS_TAB_NAME,
      *super.getControls().toTypedArray(),
      numLayers,
    ),
    ControlTab(GLOBAL_CONFIG_TAB_NAME, *getGlobalControls()),
    *times(maxLayers) { index ->
      ControlTab("L-${index + 1}", *getControlsForLayer(index))
    }.toTypedArray(),
  )

  override fun setup() {
    super.setup()

    setActiveTab(GLOBAL_CONFIG_TAB_NAME)
  }

  companion object {
    const val GLOBAL_CONFIG_TAB_NAME = "global config"
    const val CANVAS_TAB_NAME = "canvas"
    const val MAX_LAYERS = 10
  }
}
