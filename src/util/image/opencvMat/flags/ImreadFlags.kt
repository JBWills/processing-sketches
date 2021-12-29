package util.image.opencvMat.flags

@Suppress("unused")
enum class ImreadFlags(val value: Int) {
  ImreadUnchanged(-1),
  ImreadGrayscale(0),
  ImreadColor(1),
  ImreadAnyDepth(2),
  ImreadAnyColor(4),
  ImreadLoadGDal(8),
  ImreadReducedGrayscale2(16),
  ImreadReducedColor2(17),
  ImreadReducedGrayscale4(32),
  ImreadReducedColor4(33),
  ImreadReducedGrayscale8(64),
  ImreadReducedColor8(65),
  ImreadIgnoreOrientation(128),
}
