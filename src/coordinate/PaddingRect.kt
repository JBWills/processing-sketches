package coordinate

data class PaddingRect(
  val base: Double = 0.0,
  val vertical: Double = base,
  val horizontal: Double = base,
  val top: Double = vertical,
  val bottom: Double = vertical,
  val left: Double = horizontal,
  val right: Double = horizontal,
) {
  constructor(
    base: Number = 0.0,
    vertical: Number = base,
    horizontal: Number = base,
    top: Number = vertical,
    bottom: Number = vertical,
    left: Number = horizontal,
    right: Number = horizontal,
  ) : this(
    base.toDouble(),
    vertical.toDouble(),
    horizontal.toDouble(),
    top.toDouble(),
    bottom.toDouble(),
    left.toDouble(),
    right.toDouble()
  )

  fun totalHorizontal() = left + right
  fun totalVertical() = top + bottom
  fun totalPadding() = Point(totalHorizontal(), totalVertical())

  fun leftOnly() = PaddingRect(left = left)
  fun topOnly() = PaddingRect(top = top)
  fun rightOnly() = PaddingRect(right = right)
  fun bottomOnly() = PaddingRect(bottom = bottom)
  fun verticalOnly() = PaddingRect(vertical = vertical)
  fun horizontalOnly() = PaddingRect(horizontal = horizontal)
}
