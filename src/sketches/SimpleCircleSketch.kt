package sketches

import appletExtensions.draw.rect
import appletExtensions.withStroke
import controls.controlsealedclasses.Slider.Companion.slider
import controls.panels.TabsBuilder.Companion.tabs
import controls.props.PropData
import coordinate.BoundRect
import kotlinx.serialization.Serializable
import sketches.base.SimpleCanvasSketch
import util.layers.LayerSVGConfig
import util.print.Style
import java.awt.Color

/**
 * Simplest sketch possible to show off the layer capabilities of Inkscape SVGs
 */
class SimpleCircleSketch :
  SimpleCanvasSketch<SimpleCircleData>("SimpleCircle", SimpleCircleData()) {
  override fun drawLayers(drawInfo: DrawInfo, onNextLayer: (LayerSVGConfig) -> Unit) {
    val (exampleGlobalField) = drawInfo.dataValues
    
    withStroke(Color.red) {
      rect(
        BoundRect(
          topLeft = boundRect.pointAt(0.30, 0.30),
          bottomRight = boundRect.pointAt(0.60, 0.60),
        ),
      )
    }
    onNextLayer(
      LayerSVGConfig(
        layerName = "RedRect",
        nextLayerName = "BlueRect",
        style = Style(color = Color.blue, noFill = true),
      ),
    )
    rect(
      BoundRect(
        topLeft = boundRect.pointAt(0.40, 0.40),
        bottomRight = boundRect.pointAt(0.70, 0.70),
      ),
    )

  }
}

@Serializable
data class SimpleCircleData(
  var exampleField: Int = 1,
) : PropData<SimpleCircleData> {
  override fun bind() = tabs {
    tab("Global") {
      slider(::exampleField, 0..10)
    }
  }

  override fun clone() = copy()

  override fun toSerializer() = serializer()
}

fun main() = SimpleCircleSketch().run()
