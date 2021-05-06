import RecordMode.NoRecord
import RecordMode.RecordSVG
import appletExtensions.PAppletExt
import appletExtensions.withStyle
import controls.panels.ControlList.Companion.col
import controls.panels.ControlTab
import controls.panels.ControlTab.Companion.tab
import controls.panels.Panelable
import controls.panels.TabsBuilder.Companion.singleTab
import coordinate.Point
import geomerativefork.src.RG
import processing.event.MouseEvent
import util.combineDrawLayersIntoSVG
import util.lineLimit
import util.print.StrokeWeight.Thick
import util.print.Style
import util.print.TextAlign.CenterVertical
import util.window.BaseSketchWindow
import java.awt.Color
import java.io.File

class LayerConfig(val style: Style)

enum class RecordMode {
  NoRecord,
  RecordSVG,
}

val CONTROL_PANEL_SIZE = Point(400, 800)

abstract class BaseSketch(
  var backgroundColor: Color = Color.white,
  var strokeColor: Color = Color.BLACK,
  val svgBaseFileName: String = "output",
  var size: Point = Point(1000, 1000),
) : PAppletExt() {
  private val window by lazy { BaseSketchWindow(svgBaseFileName, surface) }
  val sketchSize get() = Point(size.x, size.y)

  var isDebugMode: Boolean = false

  fun fileSelected(block: (File?) -> Unit, f: File?) {
    kotlin.io.println("file selected: $f")
    block(f)
  }

  fun updateSize(newSize: Point) {
    size = newSize
    setSurfaceSize(newSize)
  }

  val center get() = Point(size.x / 2, size.y / 2)

  private var dirty = true
  private var recordMode: RecordMode = NoRecord
    set(value) {
      if (field != value) markDirty()
      field = value
    }

  val isRecording get() = recordMode != NoRecord

  fun run() {
    setSize(size.xi, size.yi)
    runSketch()
  }

  open fun getLayers(): List<LayerConfig> = listOf(LayerConfig(Style(Thick, strokeColor)))

  abstract fun drawOnce(layer: Int, layerConfig: LayerConfig)

  open fun getFilenameSuffix(): String = ""

  private fun onlyRunIfDirty(f: () -> Unit) {
    if (dirty) {
      f()
      dirty = false
    }
  }

  /**
   * This function is run once before each draw call. Use it to set up
   * your drawing before actually drawing anything.
   */
  open fun drawSetup() {}

  override fun draw() {
    onlyRunIfDirty {
      val layers = getLayers()
      background(backgroundColor.rgb)
      window.setLoadingStarted()
      try {
        drawSetup()
        if (recordMode == RecordSVG) {
          combineDrawLayersIntoSVG(
            svgBaseFileName,
            getFilenameSuffix(),
            layers.size,
          ) { layerIndex ->
            drawOnce(layerIndex, layers[layerIndex])
          }

          recordMode = NoRecord
        } else {
          layers.forEachIndexed { index, layer ->
            drawOnce(index, layer)
          }
        }
      } catch (e: Exception) {
        e.printStackTrace()
        background(Color.white)
        withStyle(Style(textAlign = CenterVertical, fillColor = Color.black, textSize = 20)) {
          text(e.stackTraceToString().lineLimit(5), 10f, center.yf)
        }
      }

      window.setLoadingEnded()
    }
  }

  fun markDirty() {
    dirty = true
  }

  private fun MouseEvent?.run(f: (Point) -> Unit) = this?.let { f(Point(it.x, it.y)) } ?: Unit
  override fun mouseClicked(event: MouseEvent?) = event.run { p -> mouseClicked(p) }
  override fun mousePressed(event: MouseEvent?) = event.run { p -> mousePressed(p) }
  open fun mousePressed(p: Point) {}
  open fun mouseClicked(p: Point) {}

  /**
   * Override this to add multiple controlTabs to your sketch.
   */
  open fun getControlTabs(): Array<ControlTab> = singleTab("test") {}.toTypedArray()

  /**
   * Override this to add controls to your sketch.
   */
  open fun getControls(): Panelable = col {}

  private fun getAllControls(): List<ControlTab> = listOf(
    tab("file") {
      toggle(::isDebugMode)
      button("Save frame") { recordMode = RecordSVG }
    },
    *getControlTabs(),
  )

  fun updateControls() = window.updateControls(this, CONTROL_PANEL_SIZE, getAllControls())

  fun setActiveTab(tabName: String) = window.controlFrame?.setActiveTab(tabName)

  override fun setup() {
    RG.init(this)
    RG.setPolygonizer(RG.ADAPTATIVE)

    surface.setResizable(true)
    updateControls()

    randomSeed(0)

    colorMode(RGB, 255f, 255f, 255f, 255f)
  }
}

