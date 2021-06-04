package sketches.base

import BaseSketch
import LayerConfig
import appletExtensions.draw.rect
import appletExtensions.withStyle
import controls.panels.Panelable
import controls.props.types.CanvasProp
import util.darkened
import util.print.StrokeWeight.Thick
import util.print.Style
import java.awt.Color

abstract class CanvasSketch(
  svgBaseFilename: String,
  open var canvasProps: CanvasProp,
) : BaseSketch(
  canvasProps.paper.defaultBackgroundColor,
  canvasProps.paper.defaultStrokeColor,
  svgBaseFilename,
  canvasProps.pagePx,
) {

  val boundRect get() = canvasProps.boundRect

  override fun getFilenameSuffix(): String = canvasProps.paper.name

  override fun getControls(): Panelable = canvasProps.asControlPanel()

  abstract fun drawOnce(layer: Int)

  override fun drawOnce(layer: Int, layerConfig: LayerConfig) {
    noFill()

    val needsDarkStroke: Boolean =
      isRecording || canvasProps.paper.defaultBackgroundColor != Color.black
    val style = canvasProps.paper.defaultStyle
      .applyOverrides(layerConfig.style)
      .applyOverrides(
        Style(
          weight = if (isRecording) Thick else null,
          color = if (needsDarkStroke) layerConfig.style.color?.darkened(0.5f) else null,
        ),
      )

    withStyle(style) {
      if (layer == getLayers().size - 1) {
        if (canvasProps.drawBoundRect) rect(boundRect)
      } else {
        drawOnce(layer)
      }
    }
  }
}
