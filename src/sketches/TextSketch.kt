package sketches

import controls.panels.ControlStyle
import controls.panels.TabsBuilder.Companion.layerTab
import controls.panels.TabsBuilder.Companion.tabs
import controls.panels.panelext.fontSelect
import controls.panels.panelext.slider
import controls.panels.panelext.slider2D
import controls.panels.panelext.textInput
import controls.panels.panelext.toggle
import controls.props.PropData
import coordinate.Deg
import coordinate.Point
import coordinate.transforms.TransformBuilder.Companion.buildTransform
import de.lighti.clipper.Clipper.EndType.CLOSED_POLYGON
import de.lighti.clipper.Clipper.JoinType
import kotlinx.serialization.Serializable
import sketches.base.LayeredCanvasSketch
import util.atAmountAlong
import util.fonts.FontData
import util.iterators.skipFirst
import util.javageom.DefaultFlatness
import util.javageom.boundRect
import util.javageom.toPolyLine
import util.polylines.PolyLine
import util.polylines.clipping.offsetBy
import util.step
import java.awt.Color
import java.awt.Shape
import java.awt.geom.AffineTransform

/**
 * Draw some text as a path
 */
class TextSketch : LayeredCanvasSketch<TextData, TextLayerData>(
  "Text",
  defaultGlobal = TextData(),
  layerToDefaultTab = { TextLayerData() },
) {
  override fun drawSetup(layerInfo: DrawInfo) {}
  override fun drawOnce(layerInfo: LayerInfo) {}

  override suspend fun SequenceScope<Unit>.drawLayers(layerInfo: DrawInfo) {
    val (lineWidth, lineHeight, centerPoint, rotation, flatness, font, text, interiorOffsets, exteriorOffsets, lockOffsetsDistance) = layerInfo.globalValues

    fun List<PolyLine>.offset(amounts: Iterable<Double>) =
      offsetBy(amounts, JoinType.ROUND, CLOSED_POLYGON)
        .values.flatten()

    fun getLineTransform(s: Shape, heightOffset: Double): AffineTransform {
      val bounds = s.boundRect()

      return buildTransform {
        translate(-bounds.center)
        rotate(rotation)
        scale(lineWidth.toDouble() / bounds.width)
        translate(boundRect.pointAt(centerPoint).addY(heightOffset))
      }
    }

    val textOutLine = text.toPolyLine(
      font.toFont(), flatness,
      getTransforms = { shapesAndLayouts ->
        val totalHeight = (shapesAndLayouts.size - 1) * lineHeight
        val offsetRange = (-totalHeight / 2)..(totalHeight / 2)
        shapesAndLayouts.mapIndexed { index, (_, shape) ->
          val offsetPercent = index.toDouble() / (shapesAndLayouts.size - 1)
          val offset = offsetRange.atAmountAlong(offsetPercent)
          getLineTransform(shape, offset)
        }
      },
    ).flatten().also { it.draw() }

    val (interiorOffsetsStep, interiorOffsetsList) =
      interiorOffsets.let { o ->
        val step = (o.offsetEnd - o.offsetStart) / o.numOffsets
        val offsets = (o.offsetStart..o.offsetEnd step step).map { -it }
        Pair(step, offsets)
      }

    textOutLine
      .offset(interiorOffsetsList)
      .draw(boundRect)

    yield(Unit)
    stroke(Color.red)

    val exteriorOffsetsIterator = exteriorOffsets.let {
      val step = (it.offsetEnd - it.offsetStart) / it.numOffsets
      val range = it.offsetStart..it.offsetEnd
      range step if (lockOffsetsDistance) interiorOffsetsStep else step
    }

    textOutLine
      .offset(exteriorOffsetsIterator.toList().skipFirst())
      .draw(boundRect)
  }
}

@Serializable
data class TextLayerData(
  var exampleTabField: Int = 1,
) : PropData<TextLayerData> {
  override fun bind() = layerTab {
    slider(::exampleTabField, 0..10)
  }

  override fun clone() = copy()
  override fun toSerializer() = serializer()
}

@Serializable
data class OffsetData(
  var offsetStart: Double = 0.0,
  var offsetEnd: Double = 100.0,
  var numOffsets: Int = 4,
)

@Serializable
data class TextData(
  var lineWidth: Int = 25,
  var lineHeight: Int = 50,
  var centerPoint: Point = Point.Half,
  var rotation: Deg = Deg.HORIZONTAL,
  var flatness: Double = DefaultFlatness,
  var font: FontData = FontData("Arvo", "Bold"),
  var text: String = "Test",
  var interiorOffsets: OffsetData = OffsetData(),
  var exteriorOffsets: OffsetData = OffsetData(),
  var lockOffsetsDistance: Boolean = false,
) : PropData<TextData> {
  override fun bind() = tabs {
    tab("Global") {
      fontSelect(::font)
      row {
        slider(::lineWidth, 5..2000)
        slider(::lineHeight, 5..2000)
      }
      slider(::rotation)
      slider2D(::centerPoint, Point.Zero..Point.One)
      slider(::flatness, 0.1..10.0)
      textInput(::text)
    }

    tab("Offsets") {
      col {
        style = ControlStyle.Blue
        slider(interiorOffsets::offsetStart, 0..100)
        slider(interiorOffsets::offsetEnd, 0..500)
        slider(interiorOffsets::numOffsets, 1..100)
      }
      col {
        style = ControlStyle.Red
        slider(exteriorOffsets::offsetStart, 0..100)
        slider(exteriorOffsets::offsetEnd, 0..500)
        slider(exteriorOffsets::numOffsets, 1..100)
        toggle(::lockOffsetsDistance)
      }
    }
  }

  override fun clone() = copy()

  override fun toSerializer() = serializer()
}

fun main() = TextSketch().run()
