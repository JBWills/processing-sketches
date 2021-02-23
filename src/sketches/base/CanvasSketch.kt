package sketches.base

import BaseSketch
import LayerConfig
import controls.ControlGroup
import controls.ControlGroupable
import controls.booleanProp
import controls.controls
import controls.doublePairProp
import controls.enumProp
import coordinate.Point
import util.darkened
import util.print.DPI
import util.print.Orientation
import util.print.Paper
import java.awt.Color

abstract class CanvasSketch(
  svgBaseFilename: String,
  canvas: Paper = Paper.SquareBlack,
  var orientation: Orientation = Orientation.Landscape,
  isDebugMode: Boolean = false,
) : BaseSketch(
  canvas.defaultBackgroundColor,
  canvas.defaultStrokeColor,
  svgBaseFilename,
  canvas.horizontalPx(orientation),
  canvas.verticalPx(orientation),
  isDebugMode,
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

  override fun getControls(): List<ControlGroupable> = controls(
    ControlGroup(enumProp(::paper) { markCanvasDirty() }, heightRatio = 2.0),
    ControlGroup(enumProp(::orientation) { markCanvasDirty() }, heightRatio = 1.0),
    ControlGroup(booleanProp(::drawBoundRect), heightRatio = 0.5),
    doublePairProp(::boundBoxCenter),
    doublePairProp(::boundBoxScale),
  )

  abstract fun drawOnce(layer: Int)

  override fun drawOnce(layer: Int, layerConfig: LayerConfig) {
    noFill()

    val needsDarkStroke: Boolean = isRecording || paper.defaultBackgroundColor != Color.black
    stroke(
      if (needsDarkStroke) layerConfig.pen.color.darkened(0.5f).rgb else layerConfig.pen.color.rgb)
    strokeWeight(DPI.InkScape.toPixelsFromMm(layerConfig.pen.mm))
    if (layer == getLayers().size - 1) {
      boundRect = calcBoundRect()

      if (drawBoundRect) rect(boundRect)
    } else {
      drawOnce(layer)
    }
  }

  private fun calcBoundRect() = paper
    .toBoundRect(orientation)
    .scale(
      boundBoxScale,
      newCenter = boundBoxCenter * Point(sizeX, sizeY)
    )

}
