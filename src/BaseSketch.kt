import RecordMode.NoRecord
import RecordMode.RecordSVG
import appletExtensions.PAppletExt
import appletExtensions.createGraphics
import appletExtensions.withStyle
import controls.panels.ControlList.Companion.col
import controls.panels.ControlTab
import controls.panels.ControlTab.Companion.tab
import controls.panels.Panelable
import controls.panels.TabsBuilder.Companion.singleTab
import controls.panels.panelext.button
import controls.panels.panelext.toggle
import coordinate.Point
import geomerativefork.src.RG
import interfaces.listeners.MouseListener
import nu.pattern.OpenCV
import processing.core.PGraphics
import processing.core.PImage
import processing.event.MouseEvent
import util.drawLayeredSvg
import util.image.ImageFormat
import util.iterators.iterate
import util.lineLimit
import util.print.StrokeJoin
import util.print.Style
import util.print.TextAlign.CenterVertical
import util.print.Thick
import util.window.BaseSketchWindow
import java.awt.Color
import java.io.File

class LayerConfig(val style: Style)

enum class RecordMode {
  NoRecord,
  RecordSVG,
}

val CONTROL_PANEL_SIZE = Point(400, 800)

typealias StartNewLayerFn = () -> Unit

abstract class BaseSketch(
  var backgroundColor: Color = Color.white,
  var strokeColor: Color = Color.BLACK,
  val svgBaseFileName: String = "output",
  var size: Point = Point(1000, 1000),
) : PAppletExt() {
  init {
    // This has to happen before setup() so native methods work for deserialization.
    OpenCV.loadLocally()
  }

  private val window by lazy { BaseSketchWindow(svgBaseFileName, surface) }
  val sketchSize get() = Point(size.x, size.y)

  var isDebugMode: Boolean = false

  private var lastDrawImage: PImage? = null

  val interactiveGraphicsLayer: PGraphics by lazy { createGraphics(size, ImageFormat.ARGB) }

  private val mouseListeners: MutableSet<MouseListener> = mutableSetOf()

  fun fileSelected(block: (File?) -> Unit, f: File?) {
    println("file selected: $f")
    block(f)
  }

  fun updateSize(newSize: Point) {
    size = newSize
    interactiveGraphicsLayer.setSize(size.xi, size.yi)
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

  open fun getLayers(): List<LayerConfig> = listOf(LayerConfig(Style(Thick(), strokeColor)))

  abstract suspend fun SequenceScope<Unit>.drawOnce(layer: Int, layerConfig: LayerConfig)

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

  /**
   * This function is for drawing UI elements on top of the image. This is good for showing your
   * cursor if you're using a tool or something.
   */
  open fun drawInteractive() {}

  override fun draw() {
    fun getLayerSequence() = sequence {
      val layers = getLayers()
      layers.forEachIndexed { index, layer ->
        drawOnce(index, layer)
        yield(Unit)
      }
    }

    onlyRunIfDirty {
      background(backgroundColor.rgb)
      window.setLoadingStarted()
      try {
        drawSetup()
        if (recordMode == RecordSVG) {
          drawLayeredSvg(
            svgBaseFileName,
            getFilenameSuffix(),
            getLayerSequence(),
          )

          recordMode = NoRecord
        } else {
          getLayerSequence().iterate()
        }
      } catch (e: Exception) {
        e.printStackTrace()
        background(Color.white)
        withStyle(Style(textAlign = CenterVertical, fillColor = Color.black, textSize = 20)) {
          text(e.stackTraceToString().lineLimit(5), 10f, center.yf)
        }
      }

      window.setLoadingEnded()
      lastDrawImage = get()
    }

    // TODO: large canvases can't have ui elements on small screens because of this
    // Processing shrinks the screen size if it can't fit on your screen which is what causes the
    // discrepancy between the graphics layer and the surface size.
//    if (lastDrawImage?.size == interactiveGraphicsLayer.size) {
//      interactiveGraphicsLayer.withDraw {
//        interactiveGraphicsLayer.background(lastDrawImage)
//      }
//      drawInteractive()
//      interactiveGraphicsLayer.displayOnParent()
//    }
  }

  open fun markDirty() {
    dirty = true
  }

  private fun MouseEvent?.run(f: (Point) -> Unit) = this?.let { f(Point(it.x, it.y)) } ?: Unit
  override fun mouseClicked(event: MouseEvent?) = event.run { p -> mouseClicked(p) }
  override fun mousePressed(event: MouseEvent?) = event.run { p -> mousePressed(p) }
  override fun handleMouseEvent(event: MouseEvent?) {
    super.handleMouseEvent(event)
    event ?: return
    mouseListeners.forEach { listener -> listener.onEvent(event) }
  }

  fun addMouseEventListener(listener: MouseListener) = mouseListeners.add(listener)
  fun removeMouseEventListener(listener: MouseListener) = mouseListeners.remove(listener)

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
    frameRate(30f)
    strokeJoin(StrokeJoin.Round.joinInt)

    surface.setResizable(true)
    updateControls()

    randomSeed(0)

    colorMode(ARGB, 255f, 255f, 255f, 255f)
    markDirty()
  }
}

