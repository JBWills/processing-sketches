package sketches.base

import BaseSketch
import LayerConfig
import SketchConfig
import controls.ControlField.Companion.booleanField
import controls.ControlField.Companion.doublePairField
import controls.ControlField.Companion.enumField
import controls.ControlGroup
import controls.ControlGroupable
import coordinate.Point
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
  var boundBoxCenter = doublePairField("boundBoxCenter", Point.Half)
  var boundBoxScale = doublePairField("boundBoxScale", Point(0.8, 0.8))
  var drawBoundRect = booleanField("drawBoundRect", true)

  val paper get() = paperField.get()

  private fun markCanvasDirty() {
    updateSize(paper.horizontalPx(), paper.verticalPx())

    backgroundColor = paper.defaultBackgroundColor
    strokeColor = paper.defaultStrokeColor
  }

  override fun getControls(): List<ControlGroupable> = listOf(
    paperField,
    ControlGroup(drawBoundRect, heightRatio = 0.5),
    boundBoxCenter,
    boundBoxScale,
  )

  abstract fun drawOnce(layer: Int)

  override fun drawOnce(config: EmptyConfig, layer: Int, layerConfig: LayerConfig) {
    stroke(layerConfig.pen.color.rgb)
    strokeWeight(paper.dpi.toPixelsFromMm(layerConfig.pen.mm))
    drawOnce(layer)
  }

  override fun getRandomizedConfig() = EmptyConfig()
}