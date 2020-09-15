package coordinate

data class PaddingRect(
  val base: Float = 0f,
  val vertical: Float = base,
  val horizontal: Float = base,
  val top: Float = vertical,
  val bottom: Float = vertical,
  val left: Float = horizontal,
  val right: Float = horizontal,
) {
  fun totalHorizontal() = left + right
  fun totalVertical() = top + bottom
}