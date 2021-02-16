package util.print

import coordinate.BoundRect
import coordinate.Point
import util.print.Orientation.Landscape
import java.awt.Color

val MAX_VERTICAL_IN = 13.75

// not so sure about max horizontal in
val MAX_HORIZONTAL_IN = 17.0

enum class DPI(val dpiVal: Int) {
  Reg(72),
  InkScape(96),
  High(300);

  fun toPixels(inches: Number) = (dpiVal * inches.toDouble()).toInt()
  fun toPixelsFromMm(mm: Number) = toPixels(mm.toDouble() / 25.4)
}

enum class Orientation {
  Portrait,
  Landscape
}

enum class Paper(
  val longSideInches: Double,
  val shortSideInches: Double,
  val defaultBackgroundColor: Color,
  val defaultStrokeColor: Color,
) {

  LargeWhite(14, 11, Color.WHITE, Color.BLACK),
  A4White(11.69, 8.27, Color.WHITE, Color.BLACK),
  A4Black(12, 9, Color.BLACK, Color.WHITE),
  SquareBlack(7.87, 7.87, Color.BLACK, Color.WHITE),
  ColoredPaper(12.5, 9.5, Color.RED, Color.WHITE);
  
  private fun sidePx(sideInches: Double, dpi: DPI = DPI.InkScape) = dpi.toPixels(sideInches)

  fun horizontalPx(orientation: Orientation = Landscape, dpi: DPI = DPI.InkScape) =
    sidePx(if (orientation == Landscape) longSideInches else shortSideInches, dpi)

  fun verticalPx(orientation: Orientation = Landscape, dpi: DPI = DPI.InkScape) =
    sidePx(if (orientation == Landscape) shortSideInches else longSideInches, dpi)

  fun toBoundRect() = BoundRect(Point.Zero, verticalPx(), horizontalPx())

  constructor(
    longSideInches: Number,
    shortSideInches: Number,
    defaultBackgroundColor: Color,
    defaultStrokeColor: Color,
  ) :
    this(longSideInches.toDouble(),
      shortSideInches.toDouble(),
      defaultBackgroundColor,
      defaultStrokeColor)

}
