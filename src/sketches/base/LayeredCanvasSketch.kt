package sketches.base

import LayerConfig
import controls.ControlTab
import controls.Props
import controls.intProp
import controls.nullableEnumProp
import geomerativefork.src.util.mapArray
import interfaces.Bindable
import interfaces.Copyable
import util.iterators.flattenArray
import util.iterators.timesArray
import util.letWith
import util.limit
import util.print.Orientation
import util.print.Paper
import util.print.Pen
import util.print.StrokeWeight
import util.print.StrokeWeight.Thick
import util.print.Style
import java.awt.Color
import java.awt.Color.BLUE
import java.awt.Color.CYAN
import java.awt.Color.DARK_GRAY
import java.awt.Color.GREEN
import java.awt.Color.LIGHT_GRAY
import java.awt.Color.MAGENTA
import java.awt.Color.ORANGE
import java.awt.Color.RED
import java.awt.Color.YELLOW


abstract class LayeredCanvasSketch<TabValues, GlobalValues>(
  svgBaseFilename: String,
  defaultGlobal: GlobalValues,
  layerToDefaultTab: (Int) -> TabValues,
  canvas: Paper = Paper.SquareBlack,
  orientation: Orientation = Orientation.Landscape,
  val maxLayers: Int = MAX_LAYERS,
) : CanvasSketch(
  svgBaseFilename,
  canvas,
  orientation,
) where
TabValues : Bindable, TabValues : Copyable<TabValues>,
GlobalValues : Bindable, GlobalValues : Copyable<GlobalValues> {

  private val props: Props<TabValues, GlobalValues> by lazy {
    Props(
      this,
      maxLayers,
      defaultGlobal,
      layerToDefaultTab
    )
  }

  private var weightOverride: StrokeWeight? = null

  private var frozenValues: DrawInfo? = null

  var numLayers: Int = maxLayers

  private fun coloredLayer(c: Color) = LayerConfig(Style(Thick, c))

  override fun getLayers(): List<LayerConfig> = listOf(
    coloredLayer(BLUE),
    coloredLayer(RED),
    coloredLayer(GREEN),
    coloredLayer(LIGHT_GRAY),
    coloredLayer(DARK_GRAY),
    coloredLayer(ORANGE),
    coloredLayer(YELLOW),
    coloredLayer(CYAN),
    coloredLayer(Color(GRAY)),
    coloredLayer(MAGENTA),
  )
    .limit(numLayers)
    .map { LayerConfig(it.style.applyOverrides(Style(weightOverride))) }
    .plus(LayerConfig(Pen.ThickGellyWhite.style))

  override fun getControlTabs(): Array<ControlTab> = arrayOf(
    ControlTab(
      CANVAS_TAB_NAME,
      *super.getControls().toTypedArray(),
      intProp(::numLayers, range = 0..maxLayers),
      nullableEnumProp(::weightOverride, StrokeWeight.values()),
    ),
    *props.globalControlTabs,
    *timesArray(maxLayers) { index ->
      props.layerControlTabs[index]
        .mapArray { tab ->
          ControlTab(
            name = "${tab.name}-${index + 1}",
            tab.controlSections
          )
        }
    }.flattenArray(),
  )

  abstract fun drawOnce(values: LayerInfo)

  open fun drawSetup(layerInfo: DrawInfo) {}

  final override fun drawSetup() {
    super.drawSetup()

    val (global, tab) = props.letWith { globalValues.clone() to tabValues.map { it.clone() } }
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
