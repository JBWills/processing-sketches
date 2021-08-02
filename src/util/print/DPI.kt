package util.print

@Suppress("unused")
enum class DPI(private val dpiVal: Int) {
  Reg(72),
  InkScape(96),
  High(300);

  fun toPixels(inches: Number): Double = (dpiVal * inches.toDouble())
  fun toPixelsFromMm(mm: Number): Double = toPixels(mm.toDouble() / 25.4)
}
