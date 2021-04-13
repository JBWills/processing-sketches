package sketches

import BaseSketch
import LayerConfig
import appletExtensions.drawParallelLinesInBound
import controls.Control.Slider
import controls.panels.ControlList.Companion.col
import controls.panels.Panelable
import coordinate.BoundRect
import coordinate.Deg
import coordinate.Point
import java.awt.Color

open class GradientLinesSketch(
  private var lineDegrees: Double = 0.0,
  backgroundColor: Color = Color.BLACK,
  sizeX: Int = 576,
  sizeY: Int = 864,
) : BaseSketch(
  backgroundColor = backgroundColor,
  svgBaseFileName = "svgs.GradientLines",
  sizeX = sizeX,
  sizeY = sizeY,
) {

  private val outerPaddingX: Double = sizeX * 0.05
  private val outerPaddingY: Double = sizeY * 0.05
  var drawBound: BoundRect = BoundRect(
    Point(outerPaddingX, outerPaddingY),
    sizeX - 2 * outerPaddingX,
    sizeY - 2 * outerPaddingY
  )

  override fun getControls(): Panelable = col(
    Slider(
      text = "Line angle (degrees)",
      range = 0.0..360.0,
      handleChange = {
        lineDegrees = it
        markDirty()
      }
    )
  )

  fun bounds(segmentHeight: Double, segIndexFromTop: Int, numSegs: Int = 1) = BoundRect(
    drawBound.topLeft + Point(0f, segIndexFromTop * segmentHeight),
    drawBound.width,
    segmentHeight * numSegs
  )

  override fun drawOnce(layer: Int, layerConfig: LayerConfig) {
    noStroke()

    stroke(Color.WHITE.rgb)
    strokeWeight(1f)
    noFill()

    val segmentHeight = drawBound.height / 7f

    fun bounds(segIndexFromTop: Int, numSegs: Int = 1) =
      bounds(segmentHeight, segIndexFromTop, numSegs)

    rect(drawBound)
    drawParallelLinesInBound(
      bounds(0, 1),
      Deg(lineDegrees),
      distanceBetween = 4
    )

    drawParallelLinesInBound(
      bounds(1, 1),
      Deg(lineDegrees),
      distanceBetween = 8
    )

    drawParallelLinesInBound(
      bounds(2, 1),
      Deg(lineDegrees),
      distanceBetween = 16
    )

    drawParallelLinesInBound(
      bounds(3, 1),
      Deg(lineDegrees),
      distanceBetween = 32
    )

    drawParallelLinesInBound(
      bounds(4, 1),
      Deg(lineDegrees),
      distanceBetween = 64
    )

    drawParallelLinesInBound(
      bounds(5, 1),
      Deg(lineDegrees),
      distanceBetween = 128,
      offset = 32
    )

    drawParallelLinesInBound(
      bounds(6, 1),
      Deg(lineDegrees),
      distanceBetween = 256f
    )
  }
}

fun main() = GradientLinesSketch().run()
