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
import util.darkened
import util.print.DPI
import util.print.Orientation
import util.print.Paper

class EmptyConfig : SketchConfig()
abstract class CanvasSketch(
  svgBaseFilename: String,
  canvas: Paper = Paper.SquareBlack,
  orientation: Orientation = Orientation.Landscape,
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
    updateSize(paper.horizontalPx(), paper.verticalPx())

    backgroundColor = paper.defaultBackgroundColor
    strokeColor = paper.defaultStrokeColor
  }

  override fun getControls(): List<ControlGroupable> = listOf(
    ControlGroup(paperField, heightRatio = 2.0),
    ControlGroup(orientationField, heightRatio = 1.0),
    ControlGroup(drawBoundRectField, heightRatio = 0.5),
    boundBoxCenterField,
    boundBoxScaleField,
  )

  abstract fun drawOnce(layer: Int)

  override fun drawOnce(config: EmptyConfig, layer: Int, layerConfig: LayerConfig) {
    noFill()
    stroke(if (isRecording) layerConfig.pen.color.darkened(0.5f).rgb else layerConfig.pen.color.rgb)
    strokeWeight(DPI.InkScape.toPixelsFromMm(layerConfig.pen.mm))
    if (layer == getLayers().size - 1) {
      boundRect = calcBoundRect()

      if (drawBoundRect) rect(boundRect)
    } else {
      drawOnce(layer)
    }
  }

  private fun calcBoundRect() = paper
    .toBoundRect()
    .scale(
      boundBoxScale,
      newCenter = boundBoxCenter * Point(sizeX, sizeY)
    )

  override fun getRandomizedConfig() = EmptyConfig()
}
