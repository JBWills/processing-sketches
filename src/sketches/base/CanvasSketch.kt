package sketches.base

import BaseSketch
import LayerConfig
import appletExtensions.draw.rect
import appletExtensions.withStyle
import controls.panels.Panelable
import controls.props.types.CanvasProp
import util.base.alsoIf
import util.base.darkened
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

  private fun drawBoundRect(): Unit =
    mutableListOf(boundRect)
      .alsoIf(canvasProps.boundRectExtraWide) {
        it.add(boundRect.expand(0.2))
        it.add(boundRect.shrink(0.2))
      }.forEach { rect(it) }

  abstract suspend fun SequenceScope<Unit>.drawOnce(layer: Int)

  override suspend fun SequenceScope<Unit>.drawOnce(layer: Int, layerConfig: LayerConfig) {
    noFill()

    val needsDarkStroke: Boolean =
      isRecording || canvasProps.paper.defaultBackgroundColor != Color.black
    val style = canvasProps.paper.defaultStyle
      .applyOverrides(Style(weight = canvasProps.strokeWeight))
      .applyOverrides(layerConfig.style)
      .applyOverrides(
        Style(color = if (needsDarkStroke) layerConfig.style.color?.darkened(0.5) else null),
      )



    withStyle(style) {
      val isFirstLayer = layer == 0
      if (isFirstLayer && canvasProps.drawBoundRect) {
        drawBoundRect()
        nextLayer()
      }

      drawOnce(layer)
    }
  }
}
