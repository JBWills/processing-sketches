import controls.Control
import controls.Control.Button
import controls.ControlFrame
import processing.core.PConstants
import util.PAppletExt
import java.awt.Color
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

abstract class SketchConfig

abstract class BaseSketch<TConfig : SketchConfig>(
  private val backgroundColor: Color = Color.white,
  private val svgBaseFileName: String = "output",
  private val controls: List<Control> = listOf(),
  private var sketchConfig: TConfig? = null,
) : PAppletExt() {

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
    .withZone(ZoneId.systemDefault());

  companion object Factory {
    const val SIZE_X: Int = 1000
    const val SIZE_Y: Int = 1000

    fun <T : SketchConfig> run(app: BaseSketch<T>) {
      app.setSize(SIZE_X, SIZE_Y)
      app.runSketch()
    }
  }

  abstract fun drawOnce(config: TConfig)

  abstract fun getRandomizedConfig(): TConfig

  private fun getOutputFileName(): String {
    val curTime = dateTimeFormatter.format(Instant.now())
    val root = System.getProperty("user.dir")
    return "$root/sketches/$svgBaseFileName-$curTime.svg"
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
      wrapRecord {
        drawOnce(sketchConfigNonNull)
      }
    }
  }

  fun markDirty() {
    dirty = true
  }

  override fun setup() {
    val baseControls = listOf(
      Button("Randomize values") { randomize = true },
      Button("Save frame") { recordSvg = true }
    )

    ControlFrame(400, 800, baseControls + controls)

    randomSeed(0)

    colorMode(PConstants.RGB, 255f, 255f, 255f, 255f)
  }
}