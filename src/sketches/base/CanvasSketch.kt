package sketches.base

import BaseSketch
import SketchConfig
import controls.ControlGroup
import util.print.Paper
import util.propertyEnumDropdown

class EmptyConfig : SketchConfig()
abstract class CanvasSketch(
  svgBaseFilename: String,
  canvas: Paper = Paper.A4Black,
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

  var paper: Paper = Paper.ColoredPaper
  fun setCanvas(newCanvas: Paper) {
    updateSize(newCanvas.horizontalPx(), newCanvas.verticalPx())

    backgroundColor = newCanvas.defaultBackgroundColor
    strokeColor = newCanvas.defaultStrokeColor
  }

  override fun getControls(): List<ControlGroup> = listOf(
    ControlGroup(
      propertyEnumDropdown(::paper) {
        setCanvas(paper)
      }
    )
  )

  abstract fun drawOnce()

  override fun drawOnce(config: EmptyConfig) {
    stroke(strokeColor.rgb)
    strokeWeight(3)
    drawOnce()
  }

  override fun getRandomizedConfig() = EmptyConfig()
}