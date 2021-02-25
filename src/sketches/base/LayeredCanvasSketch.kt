package sketches.base

import LayerConfig
import controls.ControlTab
import controls.Props
import controls.intProp
import interfaces.Bindable
import util.letWith
import util.limit
import util.print.Orientation
import util.print.Paper
import util.print.Pen
import util.times
import java.awt.Color


abstract class LayeredCanvasSketch<TabValues : Bindable, GlobalValues : Bindable>(
  svgBaseFilename: String,
  val defaultGlobal: GlobalValues,
  val layerToDefaultTab: (Int) -> TabValues,
  canvas: Paper = Paper.SquareBlack,
  orientation: Orientation = Orientation.Landscape,
  val maxLayers: Int = MAX_LAYERS,
) : CanvasSketch(
  svgBaseFilename,
  canvas,
  orientation,
) {

  private val props: Props<TabValues, GlobalValues> = object : Props<TabValues, GlobalValues>(this, maxLayers, defaultGlobal, layerToDefaultTab) {}

  private var frozenValues: DrawInfo? = null

  var numLayers: Int = maxLayers

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
  ).limit(numLayers) + LayerConfig(Pen.WhiteGellyThick)

  override fun getControlTabs(): Array<ControlTab> = arrayOf(
    ControlTab(
      CANVAS_TAB_NAME,
      *super.getControls().toTypedArray(),
      intProp(::numLayers, range = 0..maxLayers),
    ),
    ControlTab(GLOBAL_CONFIG_TAB_NAME, *props.globalControls),
    *times(maxLayers) { index ->
      ControlTab("L-${index + 1}", *props.tabControls[index])
    }.toTypedArray(),
  )

  abstract fun drawOnce(values: LayerInfo)

  open fun drawSetup(layerInfo: DrawInfo) {}

  final override fun drawSetup() {
    super.drawSetup()

    val (global, tab) = props.letWith { globalValues to tabValues }
    frozenValues = DrawInfo(global, tab).also { drawSetup(it) }
  }

  final override fun drawOnce(layer: Int) {
    val frozenValues = frozenValues ?: return
    drawOnce(LayerInfo(layer, frozenValues.globalValues, frozenValues.allTabValues))
  }

  final override fun setup() {
    super.setup()

    setActiveTab(GLOBAL_CONFIG_TAB_NAME)
  }

  open inner class DrawInfo(
    val globalValues: GlobalValues,
    val allTabValues: List<TabValues>,
  )

  inner class LayerInfo(
    val layerIndex: Int,
    globalValues: GlobalValues,
    allTabValues: List<TabValues>,
  ) : DrawInfo(globalValues, allTabValues) {
    val tabValues = allTabValues[layerIndex]
  }

  companion object {
    const val GLOBAL_CONFIG_TAB_NAME = "global config"
    const val CANVAS_TAB_NAME = "canvas"
    const val MAX_LAYERS = 10
  }
}
