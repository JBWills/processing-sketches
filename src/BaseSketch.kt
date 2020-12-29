import controls.Control.Button
import controls.ControlFrame
import controls.ControlGroup
import controls.toControlGroups
import coordinate.Point
import processing.core.PConstants
import processing.event.MouseEvent
import util.PAppletExt
import java.awt.Color
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

abstract class SketchConfig

abstract class BaseSketch<TConfig : SketchConfig>(
  protected var backgroundColor: Color = Color.white,
  protected var strokeColor: Color = Color.BLACK,
  private val svgBaseFileName: String = "output",
  private var sketchConfig: TConfig? = null,
  protected var sizeX: Int = 1000,
  protected var sizeY: Int = 1000,
  var isDebugMode: Boolean = false,
) : PAppletExt() {

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

  abstract fun drawOnce(config: TConfig)

  abstract fun getRandomizedConfig(): TConfig

  private fun getOutputFileName(): String {
    val curTime = dateTimeFormatter.format(Instant.now())
    val root = System.getProperty("user.dir")
    return "$root/svgs/$svgBaseFileName-$curTime.svg"
  }

  private fun drawHelper(handleSetup: () -> Unit, runDraw: () -> Unit, cleanUp: () -> Unit): () -> Unit = {
    handleSetup()
    runDraw()
    cleanUp()
  }

  private fun wrapRecord(f: () -> Unit) {
    if (!recordSvg) {
      f()
      return
    }

    beginRecord(SVG, getOutputFileName())
    f()
    endRecord()
    recordSvg = false
  }

  private fun onlyRunIfDirty(f: () -> Unit) {
    if (!dirty) return

    f()
    dirty = false
  }

  private fun maybeRandomizeSketchConfig(): TConfig {
    val sketchConfigNonNull: TConfig = if (randomize) getRandomizedConfig() else sketchConfig ?: getRandomizedConfig()
    sketchConfig = sketchConfigNonNull
    randomize = false
    return sketchConfigNonNull
  }

  override fun draw() {
    onlyRunIfDirty {
      val sketchConfigNonNull = maybeRandomizeSketchConfig()
      background(backgroundColor.rgb)
      stroke(strokeColor.rgb)
      wrapRecord {
        drawOnce(sketchConfigNonNull)
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

  open fun getControls() = listOf<ControlGroup>()

  fun updateControls(additionalControls: List<ControlGroup>) {
    val baseControls = listOf(
      Button("Randomize values") { randomize = true },
      Button("Save frame") { recordSvg = true }
    ).toControlGroups()

    ControlFrame(400, 800, (baseControls + additionalControls))
  }

  override fun setup() {
    surface.setResizable(true)
    updateControls(getControls())

    randomSeed(0)

    colorMode(PConstants.RGB, 255f, 255f, 255f, 255f)
  }
}