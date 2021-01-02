package sketches.base

import BaseSketch
import LayerConfig
import SketchConfig
import controls.ControlField.Companion.enumField
import controls.ControlGroupable
import util.print.Paper

class EmptyConfig : SketchConfig()
abstract class CanvasSketch(
  svgBaseFilename: String,
  canvas: Paper = Paper.SquareBlack,
  isDebugMode: Boolean = false,
) : BaseSketch<EmptyConfig>(
  canvas.defaultBackgroundColor,
  canvas.defaultStrokeColor,
  svgBaseFilename,
  EmptyConfig(),
  canvas.horizontalPx(),
  canvas.verticalPx(),
  isDebugMode,
) {

  private var paperField = enumField("paper", canvas) { markCanvasDirty() }

  val paper get() = paperField.get()

  private fun markCanvasDirty() {
    updateSize(paper.horizontalPx(), paper.verticalPx())

    backgroundColor = paper.defaultBackgroundColor
    strokeColor = paper.defaultStrokeColor
  }

  override fun getControls(): List<ControlGroupable> = listOf(paperField)

  abstract fun drawOnce(layer: Int)

  override fun drawOnce(config: EmptyConfig, layer: Int, layerConfig: LayerConfig) {
    stroke(layerConfig.pen.color.rgb)
    strokeWeight(paper.dpi.toPixelsFromMm(layerConfig.pen.mm))
    drawOnce(layer)
  }

  override fun getRandomizedConfig() = EmptyConfig()
}