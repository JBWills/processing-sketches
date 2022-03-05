package sketches

import controls.controlsealedclasses.Slider.Companion.slider
import controls.controlsealedclasses.Slider2D.Companion.slider2D
import controls.panels.ControlStyle
import controls.panels.TabStyle
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
    val (linesData, ditherData, photo) = drawInfo.dataValues

    val centerPoint = boundRect.pointAt(linesData.lineCenter)

    val mat = photo.loadMatMemoized() ?: return
    val mat2 =
      photo.copy(
        transformProps = photo.transformProps.copy(
          imageWhitePoint = (255 * ditherData.otherDitherWhitePoint).toInt(),
          imageBlackPoint = (255 * ditherData.otherDitherBlackPoint).toInt(),
        ),
      )
        .loadMatMemoized() ?: return

    if (photo.drawImage) {
      mat.draw(photo.getMatBounds(mat, boundRect))
    }

    val xLines = getLines(
      linesData.numLines.xi,
      centerPoint,
      linesData.lineSpacing.x,
      linesData.angle1,
      linesData.curveScale,
      linesData.curveAspect,
      linesData.curveOffsetPercent,
    )

    val yLines = getLines(
      linesData.numLines.yi,
      centerPoint,
      linesData.lineSpacing.y,
      linesData.angle2,
      linesData.curveScale,
      linesData.curveAspect,
      linesData.curveOffsetPercent,
    )

    val screenToMatTransform = photo.getScreenToMatTransform(mat, boundRect)
    val matToScreenTransform = photo.getMatToScreenTransform(mat, boundRect)

    val luminanceMat = mat.converted(to = Gray)
    val luminanceMat2 = mat2.converted(to = Gray)

    var linesBefore = 0
    var linesAfter = 0

    fun traverseLine(line: PolyLine, m: Mat): List<PolyLine> = boundRect.intersection(line)
      .flatMap { path ->
        path.map { p -> screenToMatTransform.transform(p) }
          .walk(ditherData.step)
          .walkThreshold { p -> m.getOr(p, 0.0) < 128 }
          .also { linesBefore += it.size }
          .removeSmallGaps(linesData.lineGapMin)
          .also { linesAfter += it.size }
          .deepMap(matToScreenTransform::transform)
      }

    xLines.pmap { line -> traverseLine(line, luminanceMat) }.draw()
    onNextLayer(LayerSVGConfig())
    yLines.pmap { line -> traverseLine(line, luminanceMat2) }.draw()

    val numLinesSaved = linesBefore - linesAfter
    val percentSaved = (numLinesSaved.toDouble() * 100) / linesBefore.toDouble()

    debugLog(
      "Saved $numLinesSaved lines (${percentSaved.roundedString(2)}%)! LinesBefore: $linesBefore, linesAfter:$linesAfter",
    )
  }
}

@Serializable
data class GucciLinesData(
  var numLines: Point = Point(100, 100),
  var lineCenter: Point = Point(0.5, 0.5),
  var lineSpacing: Point = Point(10, 10),
  var lineGapMin: Double = 0.0,
  var angle1: Deg = Deg.HORIZONTAL,
  var angle2: Deg = Deg(90),
  var curveScale: Double = 0.0,
  var curveAspect: Double = 0.5,
  var curveOffsetPercent: Double = 0.0
)

@Serializable
data class GucciDitherData(
  var step: Double = 5.0,
  var otherDitherWhitePoint: Double = 1.0,
  var otherDitherBlackPoint: Double = 0.0
)

@Serializable
data class GucciData(
  var linesData: GucciLinesData = GucciLinesData(),
  var ditherData: GucciDitherData = GucciDitherData(),
  var photo: PhotoMatProp = PhotoMatProp(),
) : PropData<GucciData> {
  override fun bind() = tabs {
    panelTabs(::photo, style = TabStyle.Red)

    tab("Lines") {
      row {
        style = ControlStyle.Gray
        slider(ditherData::otherDitherBlackPoint)
        slider(ditherData::otherDitherWhitePoint)
      }
      slider2D(linesData::lineCenter, 0..1 to 0..1)
      sliderPair(
        linesData::numLines,
        0.0..1000.0,
        withLockToggle = true,
        defaultLocked = true,
      )
      sliderPair(
        linesData::lineSpacing,
        0.0..10.0,
        withLockToggle = true,
        defaultLocked = true,
      )
      sliderPair(
        linesData::angle1.doubleWrapped(),
        linesData::angle2.doubleWrapped(),
        0.0..180.0,
      )

      row {
        slider(linesData::lineGapMin, 0.0..10.0)
        slider(ditherData::step, 0.5..50.0)
      }

      row {
        style = ControlStyle.Yellow
        slider(linesData::curveScale, 0.0..1000.0)
        slider(linesData::curveAspect, 0..10)
        slider(linesData::curveOffsetPercent, 0..1)
      }
    }
  }

  override fun clone() = copy()

  override fun toSerializer() = serializer()
}

fun main() = GucciSketch().run()
