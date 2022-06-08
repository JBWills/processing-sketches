package sketches.debug

import controls.panels.TabStyle
import controls.panels.TabsBuilder.Companion.tabs
import controls.props.PropData
import controls.props.types.PhotoMatProp
import kotlinx.serialization.Serializable
import org.opencv.core.Mat
import processing.event.MouseEvent
import sketches.base.SimpleCanvasSketch
import util.base.deltaE2000
import util.debugLog
import util.image.opencvMat.getColor
import util.io.input.point
import util.layers.LayerSVGConfig

/**
 * Testing some new dither techniques that will only capture certain ranges of colors.
 */
class DitherSketch : SimpleCanvasSketch<DitherData>("Dither", DitherData()) {

  var mat: Mat? = null

  override fun mouseClicked(event: MouseEvent?, drawInfo: DrawInfo?) {
    super.mouseClicked(event, drawInfo)

    event ?: return
    drawInfo ?: return
    val nonNullMat = mat ?: return

    val transform = drawInfo.dataValues.photo.getScreenToMatTransform(nonNullMat, boundRect)

    val pointOnMat = transform.transform(event.point)

    val clickedColor = nonNullMat.getColor(pointOnMat)
    debugLog("Clicked color: $clickedColor")
    debugLog("Chosen color: ${drawInfo.dataValues.photo.transformProps.dither.color}")
    debugLog("Diff: ${clickedColor?.deltaE2000(drawInfo.dataValues.photo.transformProps.dither.color)}")
  }

  override fun drawLayers(drawInfo: DrawInfo, onNextLayer: (LayerSVGConfig) -> Unit) {
    val (photo) = drawInfo.dataValues

    val nonNullMat = photo.loadMatMemoized() ?: return

    if (photo.drawImage) {
      nonNullMat.draw(photo.getMatBounds(nonNullMat, boundRect))
    }
    mat = nonNullMat
  }
}

@Serializable
data class DitherData(
  var photo: PhotoMatProp = PhotoMatProp(),
) : PropData<DitherData> {
  override fun bind() = tabs {
    panelTabs(::photo, style = TabStyle.Red)
  }

  override fun clone() = copy()

  override fun toSerializer() = serializer()
}

fun main() = DitherSketch().run()
