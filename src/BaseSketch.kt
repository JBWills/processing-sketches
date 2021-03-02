import RecordMode.NoRecord
import RecordMode.RecordSVG
import appletExtensions.PAppletExt
import controls.*
import controls.Control.Button
import controls.ControlTab.Companion.tab
import coordinate.Point
import geomerativefork.src.RG
import processing.core.PConstants
import processing.event.MouseEvent
import util.combineDrawLayersIntoSVG
import util.print.StrokeWeight.Thick
import util.print.Style
import java.awt.Color

class LayerConfig(val style: Style)

enum class RecordMode {
  NoRecord,
  RecordSVG
}

abstract class BaseSketch(
  protected var backgroundColor: Color = Color.white,
  protected var strokeColor: Color = Color.BLACK,
  val svgBaseFileName: String = "output",
  var sizeX: Int = 1000,
  var sizeY: Int = 1000,
) : PAppletExt() {
  val sizeXD get() = sizeX.toDouble()
  val sizeYD get() = sizeX.toDouble()
  val sketchSize get() = Point(sizeX, sizeY)

  var isDebugMode: Boolean = false

  private val controlFrame: Lazy<ControlFrame> = lazy {
    ControlFrame(400, 800, getAllControls())
  }

  fun updateSize(newSizeX: Int, newSizeY: Int) {
    sizeX = newSizeX
    sizeY = newSizeY
    surface.setSize(newSizeX, newSizeY)
  }

  val center get() = Point(sizeX / 2, sizeY / 2)

  private var dirty = true
  private var recordMode: RecordMode = NoRecord
    set(value) {
      if (field != value) markDirty()
      field = value
    }

  val isRecording get() = recordMode != NoRecord

  fun run() {
    setSize(sizeX, sizeY)
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
      drawSetup()
      if (recordMode == RecordSVG) {
        combineDrawLayersIntoSVG(svgBaseFileName, getFilenameSuffix(), layers.size) { layerIndex ->
          drawOnce(layerIndex, layers[layerIndex])
        }

        recordMode = NoRecord
      } else {
        layers.forEachIndexed { index, layer ->
          drawOnce(index, layer)
        }
      }
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
  open fun getControlTabs(): Array<ControlTab> =
    arrayOf(tab("controls", *getControls()))

  /**
   * Override this to add controls to your sketch.
   */
  open fun getControls(): Array<ControlGroupable> = arrayOf()

  fun updateControls() = controlFrame.value.updateControls(getAllControls())

  fun setActiveTab(tabName: String) = controlFrame.value.setActiveTab(tabName)

  private fun getAllControls() = listOf(
    tab(
      "file",
      ControlGroup(booleanProp(::isDebugMode), heightRatio = 0.3),
      Button("Save frame") { recordMode = RecordSVG },
    ),
    *getControlTabs(),
  )

  override fun setup() {
    RG.init(this)
    RG.setPolygonizer(RG.ADAPTATIVE)

    surface.setResizable(true)
    controlFrame.value

    randomSeed(0)

    colorMode(PConstants.RGB, 255f, 255f, 255f, 255f)
  }
}
