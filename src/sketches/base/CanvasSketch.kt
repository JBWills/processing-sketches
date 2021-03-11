package sketches.base

import BaseSketch
import LayerConfig
import appletExtensions.withStyle
import controls.ControlGroup.Companion.group
import controls.ControlGroupable
import controls.booleanProp
import controls.controls
import controls.doublePairProp
import controls.enumProp
import coordinate.Point
import util.darkened
import util.print.Orientation
import util.print.Paper
import util.print.StrokeWeight.Thick
import util.print.Style
import java.awt.Color

abstract class CanvasSketch(
  svgBaseFilename: String,
  canvas: Paper = Paper.SquareBlack,
  var orientation: Orientation = Orientation.Landscape,
) : BaseSketch(
  canvas.defaultBackgroundColor,
  canvas.defaultStrokeColor,
  svgBaseFilename,
  canvas.horizontalPx(orientation),
  canvas.verticalPx(orientation),
) {

  var paper: Paper = canvas
  var boundBoxCenter: Point = Point.Half
  var boundBoxScale: Point = Point(0.8, 0.8)
  var drawBoundRect: Boolean = true

  var boundRect = calcBoundRect()

  init {
    markDirty()
  }

  private fun markCanvasDirty() {
    updateSize(paper.horizontalPx(orientation), paper.verticalPx(orientation))

    backgroundColor = paper.defaultBackgroundColor
    strokeColor = paper.defaultStrokeColor
  }

  override fun getFilenameSuffix(): String = paper.name

  override fun getControls(): Array<ControlGroupable> = controls(
    group(enumProp(::paper) { markCanvasDirty() }, heightRatio = 2.0),
    group(enumProp(::orientation) { markCanvasDirty() }, heightRatio = 1.0),
    group(booleanProp(::drawBoundRect), heightRatio = 0.5),
    group(booleanProp(::isDebugMode), heightRatio = 0.5),
    doublePairProp(::boundBoxCenter),
    doublePairProp(::boundBoxScale),
  )

  abstract fun drawOnce(layer: Int)

  override fun drawOnce(layer: Int, layerConfig: LayerConfig) {
    noFill()

    val needsDarkStroke: Boolean = isRecording || paper.defaultBackgroundColor != Color.black
    val style = paper.defaultStyle
      .applyOverrides(layerConfig.style)
      .applyOverrides(
        Style(
          weight = if (isRecording) Thick else null,
          color = if (needsDarkStroke) layerConfig.style.color?.darkened(0.5f) else null
        )
      )

    withStyle(style) {
      if (layer == getLayers().size - 1) {
        boundRect = calcBoundRect()

        if (drawBoundRect) rect(boundRect)
      } else {
        drawOnce(layer)
      }
    }
  }

  private fun calcBoundRect() = paper
    .toBoundRect(orientation)
    .scale(
      boundBoxScale,
      newCenter = boundBoxCenter * Point(sizeX, sizeY)
    )

}
