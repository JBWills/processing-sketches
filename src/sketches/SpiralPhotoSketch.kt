package sketches

import controls.controlsealedclasses.Slider.Companion.slider
import controls.panels.ControlStyle
import controls.panels.TabStyle
import controls.panels.TabsBuilder.Companion.tabs
import controls.props.PropData
import controls.props.types.PhotoMatProp
import controls.props.types.SpiralProp
import coordinate.Point
import coordinate.ThickPolyLine.Companion.toThickLines
import coordinate.Thickness
import kotlinx.serialization.Serializable
import processing.event.MouseEvent
import sketches.base.SimpleCanvasSketch
import util.debugLog
import util.image.opencvMat.get
import util.io.input.point
import util.layers.LayerSVGConfig
import util.polylines.bound

/**
 * Draw a spiralling line that is thicker or thinner based on the luminance of an image. Creates
 * an optical illusion-like effect that results in a grayscale image.
 */
class SpiralPhoto : SimpleCanvasSketch<SpiralPhotoData>("SpiralPhoto", SpiralPhotoData()) {

  var lastMouseClickLocation: Point? = null

  override fun mouseClicked(event: MouseEvent?, drawInfo: DrawInfo?) {
    super.mouseClicked(event, drawInfo)

    lastMouseClickLocation = event?.point?.minus(boundRect.topLeft)
    debugLog(lastMouseClickLocation)
    markDirty()
  }

  override fun drawLayers(drawInfo: DrawInfo, onNextLayer: (LayerSVGConfig) -> Unit) {
    val (photo, spiral, maxThickness, lineDensity) = drawInfo.dataValues

    val mat = photo.loadMatMemoized() ?: return
    val matToScreenTransform = photo.getMatToScreenTransform(mat, boundRect)
    val screenToMatTransform = photo.getScreenToMatTransform(mat, boundRect)

    fun getPxValue(screenPoint: Point): Double =
      (mat.get(screenToMatTransform.transform(screenPoint)) ?: 0.0) * maxThickness / 255.0

    SpiralProp(
      spiral,
      origin = lastMouseClickLocation?.div(boundRect.size)
        ?: spiral.origin,
    )
      .spiral(boundRect) { t, percent, deg, p -> p }
      .bound(boundRect.expand(Point(10, 10)))
      .toThickLines(
        step = 2.0,
        drawOriginal = false,
        maxPxDistBetweenLines = lineDensity,
      ) { Thickness(getPxValue(it.point)) }
      .draw(boundRect)
  }
}

@Serializable
data class SpiralPhotoData(
  var photo: PhotoMatProp = PhotoMatProp(),
  var spiral: SpiralProp = SpiralProp(),
  var maxThickness: Double = 5.0,
  var lineDensity: Double = 4.0
) : PropData<SpiralPhotoData> {
  override fun bind() = tabs {
    tab("Photo") {
      panel(::photo)
    }

    tab("Spiral") {
      tabStyle = TabStyle.Green
      style = ControlStyle.Blue
      panel(::spiral)
      slider(::maxThickness, 0.0..20.0)
      slider(::lineDensity, 0.1..50.0)
    }
  }

  override fun clone() = copy()

  override fun toSerializer() = serializer()
}

fun main() = SpiralPhoto().run()
