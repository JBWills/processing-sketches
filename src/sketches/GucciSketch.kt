package sketches

import controls.controlsealedclasses.Button.Companion.button
import controls.controlsealedclasses.Slider.Companion.slider
import controls.controlsealedclasses.Slider2D.Companion.slider2D
import controls.controlsealedclasses.Toggle.Companion.toggle
import controls.panels.ControlStyle
import controls.panels.TabStyle
import controls.panels.TabStyle.Companion.toTabStyle
import controls.panels.TabsBuilder.Companion.tabs
import controls.props.PropData
import controls.props.types.PenProp
import controls.props.types.PhotoMatProp
import coordinate.Deg
import coordinate.Line
import coordinate.Point
import coordinate.Segment
import kotlinx.serialization.Serializable
import org.opencv.core.Mat
import sketches.base.SimpleCanvasSketch
import util.algorithms.contouring.walkThreshold
import util.base.doIf
import util.concurrency.pmap
import util.debugLog
import util.image.ImageFormat.Gray
import util.image.converted
import util.image.opencvMat.getOr
import util.iterators.deepMap
import util.layers.LayerSVGConfig
import util.numbers.map
import util.numbers.roundedString
import util.polylines.PolyLine
import util.polylines.clipping.simplify.removeSmallGaps
import util.polylines.walk

/**
 * Sketch of Gucci for Scot.
 */
class GucciSketch :
  SimpleCanvasSketch<GucciData>("Gucci", GucciData()) {

  private fun getLines(
    numLines: Int,
    center: Point,
    spacing: Double,
    angle: Deg,
    curveScale: Double,
    curveAspect: Double,
    curveOffsetPercent: Double
  ): List<PolyLine> {
    val totalDist = spacing * numLines
    val walkLine = Line(center, angle.perpendicular).let { line ->
      Segment(
        line.getPointAtDist(-totalDist / 2),
        line.getPointAtDist(totalDist / 2),
      )
    }

    val lines =
      if (numLines == 1) {
        listOf(Line(walkLine.getPointAtPercent(0.5), angle))
      } else {
        numLines.map { i ->
          Line(
            walkLine.getPointAtPercent(i / (numLines - 1).toDouble()),
            angle,
          )
        }
      }

    return lines.mapNotNull { line ->
      val boundLine = boundRect.expand(curveScale)
        .intersection(line, false)
        .firstOrNull()
        ?.toPolyLine()
        ?: return@mapNotNull null

      val moveDir = line.slope.perpendicular.unitVector

      boundLine.doIf(curveScale > 0 && curveAspect > 0) {
        it.walk(2.0) { p ->
          val x = p.perpendicularDistanceTo(walkLine) / (curveScale * curveAspect)
          val offset = curveOffsetPercent * PI
          val moveAmount = kotlin.math.sin(offset + x) * curveScale

          p + moveDir * moveAmount
        }
      }
    }
  }

  override fun drawLayers(drawInfo: DrawInfo, onNextLayer: (LayerSVGConfig) -> Unit) {
    val (layers, lineGapMin, curveScale, curveAspect, photo) = drawInfo.dataValues

    layers.forEachIndexed { index, (show, pen, lineData, ditherData) ->
      if (!show) {
        return@forEachIndexed
      }

      val centerPoint = boundRect.pointAt(lineData.lineCenter)
      val mat =
        photo.copy(
          transformProps = photo.transformProps.copy(
            imageWhitePoint = (255 * ditherData.otherDitherWhitePoint).toInt(),
            imageBlackPoint = (255 * ditherData.otherDitherBlackPoint).toInt(),
          ),
        )
          .loadMatMemoized() ?: return

      if (photo.drawImage && index == 0) {
        mat.draw(photo.getMatBounds(mat, boundRect))
      }

      newLayerStyled(
        pen.style,
        LayerSVGConfig(layerName = pen.pen.name),
        onNextLayer,
      ) {
        val lines = getLines(
          lineData.numLines,
          centerPoint,
          lineData.lineSpacing,
          lineData.angle,
          curveScale,
          curveAspect,
          lineData.curveOffsetPercent,
        )

        val screenToMatTransform = photo.getScreenToMatTransform(mat, boundRect)
        val matToScreenTransform = photo.getMatToScreenTransform(mat, boundRect)

        val luminanceMat = mat.converted(to = Gray)

        var linesBefore = 0
        var linesAfter = 0

        fun traverseLine(line: PolyLine, m: Mat): List<PolyLine> = boundRect.intersection(line)
          .flatMap { path ->
            path.map { p -> screenToMatTransform.transform(p) }
              .walk(ditherData.step)
              .walkThreshold { p -> m.getOr(p, 0.0) < 128 }
              .also { linesBefore += it.size }
              .removeSmallGaps(lineGapMin)
              .also { linesAfter += it.size }
              .deepMap(matToScreenTransform::transform)
          }

        lines.pmap { line -> traverseLine(line, luminanceMat) }.draw()

        val numLinesSaved = linesBefore - linesAfter
        val percentSaved = (numLinesSaved.toDouble() * 100) / linesBefore.toDouble()

        debugLog(
          "Saved $numLinesSaved lines (${percentSaved.roundedString(2)}%)! LinesBefore: $linesBefore, linesAfter:$linesAfter",
        )
      }
    }
  }
}


@Serializable
data class GucciLinesData(
  var numLines: Int = 100,
  var lineCenter: Point = Point(0.5, 0.5),
  var lineSpacing: Double = 10.0,
  var angle: Deg = Deg.HORIZONTAL,
  var curveOffsetPercent: Double = 0.0,
)

@Serializable
data class GucciDitherData(
  var step: Double = 5.0,
  var otherDitherWhitePoint: Double = 1.0,
  var otherDitherBlackPoint: Double = 0.0
)

@Serializable
data class GucciLayer(
  var show: Boolean = true,
  var pen: PenProp = PenProp(),
  var lineData: GucciLinesData = GucciLinesData(),
  var ditherData: GucciDitherData = GucciDitherData(),
) {

  constructor(p: GucciLayer) : this(
    p.show,
    p.pen.clone(),
    p.lineData.copy(),
    p.ditherData.copy(),
  )
}

private const val GlobalTabName = "Global"

@Serializable
data class GucciData(
  var layers: MutableList<GucciLayer> = mutableListOf(),
  var lineGapMin: Double = 0.0,
  var curveScale: Double = 0.0,
  var curveAspect: Double = 0.5,
  var photo: PhotoMatProp = PhotoMatProp(),
) : PropData<GucciData> {
  override fun bind() = tabs {
    fun getLayerName(index: Int) = "l$index"

    panelTabs(::photo, style = TabStyle.Red)

    tab("Base lines") {
      row {
        button("Add layer") {
          layers.add(GucciLayer())
          updateControls(newTabName = getLayerName(layers.size - 1))
          markDirty()
        }

        button("Clear layers") {
          layers.clear()
          updateControls()
          markDirty()
        }
      }

      row {
        slider(::lineGapMin, 0.0..10.0)
      }

      row {
        style = ControlStyle.Yellow
        slider(::curveScale, 0.0..1000.0)
        slider(::curveAspect, 0..10)
      }
    }

    layers.forEachIndexed { index, layer ->
      val name = getLayerName(index)
      val tabStyle = layer.pen.pen.toTabStyle()
      tab(name, tabStyle) {
        row {
          heightRatio = 0.5
          toggle(layer::show)
        }

        row {
          button("Clone") {
            layers.add(GucciLayer(layer))
            updateControls(newTabName = getLayerName(layers.size - 1))
            markDirty()
          }

          button("Delete") {
            layers.removeAt(index)
            val newIndex = if (layers.size > index) index else index - 1
            val newTabName =
              if (layers.indices.contains(newIndex)) getLayerName(newIndex)
              else GlobalTabName
            updateControls(newTabName = newTabName)
            markDirty()
          }
        }

        layer.pen.penPanel(
          this,
          updateControlsOnPenChange = true,
          filterByWeight = true,
        )

        row {
          style = ControlStyle.Gray
          slider(layer.ditherData::otherDitherBlackPoint)
          slider(layer.ditherData::otherDitherWhitePoint)
          slider(layer.ditherData::step, 0.5..50.0)
        }
      }

      tab("${name}_lines", tabStyle) {
        slider2D(layer.lineData::lineCenter, 0..2 to 0..2)
        slider(layer.lineData::angle, range = 0.0..90.0)
        slider(layer.lineData::curveOffsetPercent, range = 0.0..1.0)
        slider(layer.lineData::numLines, 0..1000)

        slider(layer.lineData::lineSpacing, 0..10)
      }
    }
  }

  override fun clone() = copy()

  override fun toSerializer() = serializer()
}

fun main() = GucciSketch().run()
