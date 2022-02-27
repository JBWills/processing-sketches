package sketches

import controls.controlsealedclasses.Slider.Companion.slider
import controls.controlsealedclasses.Slider2D.Companion.slider2D
import controls.panels.ControlStyle
import controls.panels.TabsBuilder.Companion.tabs
import controls.panels.panelext.sliderPair
import controls.panels.panelext.util.doubleWrapped
import controls.props.PropData
import controls.props.types.PhotoMatProp
import coordinate.Deg
import coordinate.Line
import coordinate.Point
import coordinate.Segment
import kotlinx.serialization.Serializable
import sketches.base.SimpleCanvasSketch
import util.algorithms.contouring.walkThreshold
import util.concurrency.pmap
import util.image.ImageFormat.Gray
import util.image.converted
import util.image.opencvMat.getOr
import util.iterators.deepMap
import util.layers.LayerSVGConfig
import util.numbers.map
import util.polylines.PolyLine
import util.polylines.walk

/**
 * Sketch of Gucci for Scot.
 */
class GucciSketch :
  SimpleCanvasSketch<GucciData>("Gucci", GucciData()) {

  private fun getLines(numLines: Int, center: Point, spacing: Double, angle: Deg): List<Line> {
    val totalDist = spacing * numLines
    val walkLine = Line(center, angle.perpendicular).let { line ->
      Segment(
        line.getPointAtDist(-totalDist / 2),
        line.getPointAtDist(totalDist / 2),
      )
    }

    if (numLines == 1) {
      return listOf(Line(walkLine.getPointAtPercent(0.5), angle))
    }

    return numLines.map { i ->
      Line(
        walkLine.getPointAtPercent(i / (numLines - 1).toDouble()),
        angle,
      )
    }
  }

//  private fun Line.curveLine(linesData: GucciLinesData, ditherData: GucciDitherData): PolyLine {
//
//    if (linesData.curveScale == 0.0) {
//      return boundRect.intersection(this).toPolyLine().walk(ditherData.step)
//    }
//
//    val segment = boundRect.expand(linesData.curveScale).intersection(this)
//    val totalDist = segment.len
//      .toPolyLine()
//      .walkWithPercentAndSegment(step) {
//
//      }
//  }

  override fun drawLayers(drawInfo: DrawInfo, onNextLayer: (LayerSVGConfig) -> Unit) {
    val (linesData, ditherData, photo) = drawInfo.dataValues

    val centerPoint = boundRect.pointAt(linesData.lineCenter)

    val mat = photo.loadMatMemoized() ?: return

    if (photo.drawImage) {
      mat.draw(photo.getMatBounds(mat, boundRect))
    }

    val xLines = getLines(
      linesData.numLines.xi,
      centerPoint,
      linesData.lineSpacing.x,
      linesData.lineAnglesBase,
    )

    val yLines = getLines(
      linesData.numLines.yi,
      centerPoint,
      linesData.lineSpacing.y,
      linesData.lineAnglesBase + linesData.lineAnglesDifference,
    )

    val screenToMatTransform = photo.getScreenToMatTransform(mat, boundRect)
    val matToScreenTransform = photo.getMatToScreenTransform(mat, boundRect)

    val luminanceMat = mat.converted(to = Gray)

    fun traverseLine(line: Line): List<List<PolyLine>> = boundRect.intersection(line)
      .map { segment -> segment.toPolyLine() }
      .map { path ->
        val pathThreshold = 128
        path.map { p -> screenToMatTransform.transform(p) }
          .walk(ditherData.step)
          .walkThreshold { p -> luminanceMat.getOr(p, 0.0) < pathThreshold }
          .deepMap { p -> matToScreenTransform.transform(p) }
      }

    (xLines + yLines).pmap(::traverseLine).draw()
  }
}

@Serializable
data class GucciLinesData(
  var numLines: Point = Point(100, 100),
  var lineCenter: Point = Point(0.5, 0.5),
  var lineSpacing: Point = Point(10, 10),
  var lineAnglesBase: Deg = Deg.HORIZONTAL,
  var lineAnglesDifference: Deg = Deg(90),
  var curveScale: Double = 0.0,
  var curveAspect: Double = 0.5,
)

@Serializable
data class GucciDitherData(
  var step: Double = 5.0,
  var chunkSize: Double = 50.0,
  var percentInked: Double = 0.5
)

@Serializable
data class GucciData(
  var linesData: GucciLinesData = GucciLinesData(),
  var ditherData: GucciDitherData = GucciDitherData(),
  var photo: PhotoMatProp = PhotoMatProp(),
) : PropData<GucciData> {
  override fun bind() = tabs {
    tab("Lines") {
      slider2D(linesData::lineCenter, 0..1 to 0..1)
      sliderPair(
        linesData::numLines,
        0.0..1000.0,
        withLockToggle = true,
        defaultLocked = false,
      )
      sliderPair(
        linesData::lineSpacing,
        0.0..10.0,
        withLockToggle = true,
        defaultLocked = false,
      )
      sliderPair(
        linesData::lineAnglesBase.doubleWrapped(),
        linesData::lineAnglesDifference.doubleWrapped(),
        0.0..180.0,
      )

      row {
        style = ControlStyle.Yellow
        slider(linesData::curveScale, 0.0..1000.0)
        slider(linesData::curveAspect, 0..1)
      }
    }
    tab("Photo") {
      panel(::photo)
    }
    tab("Dither") {
      slider(ditherData::step, 0.5..50.0)
      slider(ditherData::chunkSize, 5.0..500.0)
      slider(ditherData::percentInked, 0.0..1.0)
    }
  }

  override fun clone() = copy()

  override fun toSerializer() = serializer()
}

fun main() = GucciSketch().run()
