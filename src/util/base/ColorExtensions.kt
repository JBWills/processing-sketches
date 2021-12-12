package util.base

import org.opencv.core.Scalar
import util.iterators.endPointPair
import util.iterators.getLerpIndices
import util.iterators.mapPercentToIndex
import util.iterators.zip
import util.numbers.bound
import util.numbers.boundInt
import util.numbers.mean
import java.awt.Color

fun Number.toRGBInt() = (bound(0f, 1f) * 255).toInt()

fun Color.toRGBScalar() = Scalar(red.toDouble(), blue.toDouble(), green.toDouble())
fun Color.toARGBScalar() =
  Scalar(alpha.toDouble(), red.toDouble(), blue.toDouble(), green.toDouble())

val Color.luminance get() = luminance()
val Color.alphaDouble get() = alpha / 255.0
val Color.gray get() = mean(red, green, blue) * alphaDouble

fun List<Color>.lerp(amt: Double): Color {
  val lerpIndices = getLerpIndices(amt)

  return if (lerpIndices.isEmpty()) Color.PINK
  else lerpIndices.map { get(it) }
    .endPointPair()
    .lerp(mapPercentToIndex(amt) - lerpIndices.first())
}

fun Color.luminance(): Double = (0.3 * red + 0.59 * green + 0.11 * blue) * alphaDouble

fun Pair<Color, Color>.lerp(amt: Double): Color = (first.rgbList() to second.rgbList())
  .zip { rgb1, rgb2 -> (rgb1 + ((rgb2 - rgb1) * amt)).toInt() }
  .toColor()

fun String.toColor(): Color? = Color.decode(this)
fun String.toRGBInt(): Int = toColor()?.rgb ?: 0

fun asColor(rgb: List<Int>) = Color(rgb[0], rgb[1], rgb[2])
fun List<Int>.toColor() = asColor(this)
fun Color.rgbList() = mutableListOf(red, green, blue)

fun Color.withColorSet(colorVal: Int, rgbIndex: Int) =
  asColor(rgbList().also { it[rgbIndex] = colorVal })

/**
 * v must be between 0 and 255
 */
fun Color.withRed(v: Int) = withColorSet(v, 0)
fun Color.withGreen(v: Int) = withColorSet(v, 1)
fun Color.withBlue(v: Int) = withColorSet(v, 2)
fun Color.withAlpha(v: Int) = Color(red, green, blue, v)
fun Color.withAlpha(v: Int?) = v?.let { withAlpha(it) } ?: this
fun Color.withAlphaFloat(v: Float) = withAlpha(v.toRGBInt())
fun Color.withAlphaDouble(v: Double) = withAlpha(v.toRGBInt())

/**
 * create new Color with red value as double
 * @param v between 0 and 1
 */
fun Color.withRed(v: Double) = withRed((v.toRGBInt()))

/**
 * create new Color with green value as double
 * @param v between 0 and 1
 */
fun Color.withGreen(v: Double) = withGreen((v.toRGBInt()))

/**
 * create new Color with blue value as double
 * @param v between 0 and 1
 */
fun Color.withBlue(v: Double) = withBlue((v.toRGBInt()))

fun Color.map(block: (Int) -> Int) = rgbList()
  .map { block(it) }
  .toColor()

/**
 * @param percent is between 0 and 1
 */
fun Color.darkened(percent: Double) =
  map { (it * (1 - percent)).boundInt(0, (255 * (1 - percent)).toInt()) }

/**
 * @param percent is between 0 and 1
 */
fun Color.lightened(percent: Double) =
  map { (it * (1 + percent)).boundInt((percent * 255).toInt(), 255) }
