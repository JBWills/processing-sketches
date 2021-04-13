package util.print

import coordinate.BoundRect
import coordinate.Point
import util.lightened
import util.print.DPI.InkScape
import util.print.Orientation.Landscape
import java.awt.Color
import java.awt.Color.BLACK
import java.awt.Color.RED
import java.awt.Color.WHITE

val MAX_VERTICAL_IN = 13.75

// not so sure about max horizontal in
val MAX_HORIZONTAL_IN = 17.0

@Suppress("unused")
enum class DPI(private val dpiVal: Int) {
  Reg(72),
  InkScape(96),
  High(300);

  fun toPixels(inches: Number) = (dpiVal * inches.toDouble()).toInt()
  fun toPixelsFromMm(mm: Number) = toPixels(mm.toDouble() / 25.4)
}

@Suppress("unused")
enum class Orientation {
  Portrait,
  Landscape
}

enum class Paper(
  private val longSideInches: Double,
  private val shortSideInches: Double,
  val defaultBackgroundColor: Color,
  val defaultStrokeColor: Color,
) {

  LargeWhite(14, 11, WHITE, BLACK),
  LargeBlack(18, 12, BLACK, WHITE),
  A4White(11.69, 8.27, WHITE, BLACK),
  A4Black(12, 9, BLACK, WHITE),
  SquareBlack(7.87, 7.87, BLACK, WHITE),
  ColoredPaper(12.5, 9.5, RED.lightened(1f), WHITE);

  val defaultStyle: Style = Style(color = defaultStrokeColor)

  private fun sidePx(sideInches: Double, dpi: DPI = InkScape) = dpi.toPixels(sideInches)

  fun horizontalPx(orientation: Orientation, dpi: DPI = InkScape) =
    sidePx(if (orientation == Landscape) longSideInches else shortSideInches, dpi)

  fun verticalPx(orientation: Orientation, dpi: DPI = InkScape) =
    sidePx(if (orientation == Landscape) shortSideInches else longSideInches, dpi)

  fun toBoundRect(orientation: Orientation) =
    BoundRect(Point.Zero, horizontalPx(orientation), verticalPx(orientation))

  constructor(
    longSideInches: Number,
    shortSideInches: Number,
    defaultBackgroundColor: Color,
    defaultStrokeColor: Color,
  ) :
    this(
      longSideInches.toDouble(),
      shortSideInches.toDouble(),
      defaultBackgroundColor,
      defaultStrokeColor
    )
}
