package util.print

import coordinate.BoundRect
import coordinate.Point
import util.doIf
import util.lightened
import util.print.DPI.InkScape
import util.print.Orientation.Portrait
import java.awt.Color
import java.awt.Color.BLACK
import java.awt.Color.RED
import java.awt.Color.WHITE

const val MAX_VERTICAL_IN = 13.75

// not so sure about max horizontal in
const val MAX_HORIZONTAL_IN = 17.0

@Suppress("unused")
enum class DPI(private val dpiVal: Int) {
  Reg(72),
  InkScape(96),
  High(300);

  fun toPixels(inches: Number): Double = (dpiVal * inches.toDouble())
  fun toPixelsFromMm(mm: Number): Double = toPixels(mm.toDouble() / 25.4)
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
  ColoredPaper(12.5, 9.5, RED.lightened(1.0), WHITE);

  val defaultStyle: Style = Style(color = defaultStrokeColor)

  private fun sidePx(sideInches: Double, dpi: DPI = InkScape): Int =
    dpi.toPixels(sideInches).toInt()

  fun px(orientation: Orientation, dpi: DPI = InkScape) = Point(
    sidePx(longSideInches, dpi),
    sidePx(shortSideInches, dpi),
  ).doIf(orientation == Portrait) { it.swapXY() }

  fun horizontalPx(orientation: Orientation, dpi: DPI = InkScape) =
    px(orientation, dpi).x

  fun verticalPx(orientation: Orientation, dpi: DPI = InkScape) =
    px(orientation, dpi).y

  fun toBoundRect(orientation: Orientation, dpi: DPI = InkScape) =
    BoundRect(Point.Zero, horizontalPx(orientation, dpi), verticalPx(orientation, dpi))

  constructor(
    longSideInches: Number,
    shortSideInches: Number,
    defaultBackgroundColor: Color,
    defaultStrokeColor: Color,
  ) : this(
    longSideInches.toDouble(),
    shortSideInches.toDouble(),
    defaultBackgroundColor,
    defaultStrokeColor,
  )
}
