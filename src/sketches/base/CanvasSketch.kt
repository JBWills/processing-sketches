package sketches.base

import BaseSketch
import LayerConfig
import controls.ControlField.Companion.booleanField
import controls.ControlField.Companion.doublePairField
import controls.ControlField.Companion.enumField
import controls.ControlGroup
import controls.ControlGroupable
import controls.controls
import coordinate.Point
import util.darkened
import util.print.DPI
import util.print.Orientation
import util.print.Paper
import java.awt.Color

abstract class CanvasSketch(
  svgBaseFilename: String,
  canvas: Paper = Paper.SquareBlack,
  orientation: Orientation = Orientation.Landscape,
  isDebugMode: Boolean = false,
) : BaseSketch(
  canvas.defaultBackgroundColor,
  canvas.defaultStrokeColor,
  svgBaseFilename,
  canvas.horizontalPx(orientation),
  canvas.verticalPx(orientation),
  isDebugMode,
) {

  private var paperField = enumField("paper", canvas) { markCanvasDirty() }
  val paper get() = paperField.get()

  private var orientationField = enumField("orientation", orientation) { markCanvasDirty() }
  val orientation get() = orientationField.get()

  var boundBoxCenterField = doublePairField("boundBoxCenter", Point.Half)
  var boundBoxCenter
    get() = boundBoxCenterField.get()
    set(value) = boundBoxCenterField.set(value)

  var boundBoxScaleField = doublePairField("boundBoxScale", Point(0.8, 0.8))
  var boundBoxScale
    get() = boundBoxScaleField.get()
    set(value) = boundBoxScaleField.set(value)

  var drawBoundRectField = booleanField("drawBoundRect", true)
  var drawBoundRect
    get() = drawBoundRectField.get()
    set(value) = drawBoundRectField.set(value)

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
    ControlGroup(paperField, heightRatio = 2.0),
    ControlGroup(orientationField, heightRatio = 1.0),
    ControlGroup(drawBoundRectField, heightRatio = 0.5),
    boundBoxCenterField,
    boundBoxScaleField,
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
