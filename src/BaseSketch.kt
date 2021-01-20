import controls.Control.Button
import controls.ControlFrame
import controls.ControlGroup
import controls.ControlGroupable
import controls.ControlTab
import controls.toControlGroups
import coordinate.Point
import processing.core.PConstants
import processing.event.MouseEvent
import util.PAppletExt
import util.print.Pen
import java.awt.Color
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

abstract class SketchConfig

class LayerConfig(val pen: Pen)

abstract class BaseSketch<TConfig : SketchConfig>(
  protected var backgroundColor: Color = Color.white,
  protected var strokeColor: Color = Color.BLACK,
  private val svgBaseFileName: String = "output",
  private var sketchConfig: TConfig? = null,
  protected var sizeX: Int = 1000,
  protected var sizeY: Int = 1000,
  var isDebugMode: Boolean = false,
) : PAppletExt() {

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
  private var recordSvg = false
    set(value) {
      if (field != value) markDirty()
      field = value
    }

  private var randomize = false
    set(value) {
      if (field != value) markDirty()
      field = value
    }

  private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyMMdd--hh-mm-ss")
    .withLocale(Locale.US)
    .withZone(ZoneId.systemDefault())

  companion object Factory {
    fun <T : SketchConfig> run(app: BaseSketch<T>) {
      app.setSize(app.sizeX, app.sizeY)
      app.runSketch()
    }
  }

  open fun getLayers(): List<LayerConfig> = listOf(LayerConfig(Pen(1.0, strokeColor)))

  abstract fun drawOnce(config: TConfig, layer: Int, layerConfig: LayerConfig)

  abstract fun getRandomizedConfig(): TConfig

  private fun getOutputFileName(layer: Int): String {
    val curTime = dateTimeFormatter.format(Instant.now())
    val root = System.getProperty("user.dir")
    val theDir = File("$root/svgs/$svgBaseFileName")
    if (!theDir.exists()) {
      theDir.mkdirs()
    }
    return "$theDir/$curTime-layer-$layer.svg"
  }

  private fun drawHelper(
    handleSetup: () -> Unit, runDraw: () -> Unit, cleanUp: () -> Unit,
  ): () -> Unit = {
    handleSetup()
    runDraw()
    cleanUp()
  }

  private fun wrapRecord(layer: Int, isLastLayer: Boolean = false, f: () -> Unit) {
    if (!recordSvg) {
      f()
      return
    }

    // if we're recording this to svgs, need to set
    // the background before recording each layer.
    background(backgroundColor.rgb)

    beginRecord(SVG, getOutputFileName(layer))
    f()
    endRecord()
    if (isLastLayer) recordSvg = false
  }

  private fun onlyRunIfDirty(f: () -> Unit) {
    if (!dirty) return

    f()
    dirty = false
  }

  private fun maybeRandomizeSketchConfig(): TConfig {
    val sketchConfigNonNull: TConfig =
      if (randomize) getRandomizedConfig() else sketchConfig ?: getRandomizedConfig()
    sketchConfig = sketchConfigNonNull
    randomize = false
    return sketchConfigNonNull
  }

  open fun drawSetup(sketchConfig: TConfig) {}

  override fun draw() {
    onlyRunIfDirty {
      background(backgroundColor.rgb)
      val sketchConfigNonNull = maybeRandomizeSketchConfig()
      val layers = getLayers()
      drawSetup(sketchConfigNonNull)
      layers.forEachIndexed { index, layer ->
        wrapRecord(index + 1, index == layers.size - 1) {
          drawOnce(sketchConfigNonNull, index, layer)
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

  open fun getControlTabs(): Array<ControlTab> =
    arrayOf(ControlTab("controls", *getControls().toTypedArray()))

  open fun getControls(): List<ControlGroupable> = listOf<ControlGroup>()

  fun updateControls() = controlFrame.value.updateControls(getAllControls())

  fun getAllControls() = listOf(
    ControlTab("default", listOf(
      Button("Randomize values") { randomize = true },
      Button("Save frame") { recordSvg = true }
    ).toControlGroups()),
    *getControlTabs(),
  )

  override fun setup() {
    surface.setResizable(true)
    controlFrame.value

    randomSeed(0)

    colorMode(PConstants.RGB, 255f, 255f, 255f, 255f)
  }
}