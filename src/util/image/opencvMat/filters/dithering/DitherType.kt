package util.image.opencvMat.filters.dithering

@Suppress("unused")
enum class DitherType(
  val divisor: Double,
  val errorArray: List<List<Int>>
) {
  // improvement on stucki
  Burkes(
    divisor = 1 / 32.0,
    errorArray = listOf(
      listOf(0, 0, 0, 8, 4),
      listOf(2, 4, 8, 4, 2),
    ),
  ),

  // Causes washed out light or dark sections of image
  Atkinson(
    divisor = 1 / 8.0,
    errorArray = listOf(
      listOf(0, 0, 1, 1),
      listOf(1, 1, 1, 0),
      listOf(0, 1, 0, 0),
    ),
  ),

  // Improvement on Jarvis + Steinberg
  Stucki(
    divisor = 1 / 42.0,
    errorArray = listOf(
      listOf(0, 0, 0, 8, 4),
      listOf(2, 4, 8, 4, 2),
      listOf(1, 2, 4, 2, 1),
    ),
  ),

  // improvement on Steinberg
  JarvisJudiceNinke(
    divisor = 1 / 48.0,
    errorArray = listOf(
      listOf(0, 0, 0, 7, 5),
      listOf(3, 5, 7, 5, 3),
      listOf(1, 3, 5, 3, 1),
    ),
  ),

  // Oldest, simplest
  FloydSteinberg(
    divisor = 1 / 16.0,
    errorArray = listOf(
      listOf(0, 0, 7),
      listOf(3, 5, 1),
    ),
  ),
  ;

  val errorColIndex = errorArray.first().lastIndexOf(0)
  val errorRowIndex = 0
}


