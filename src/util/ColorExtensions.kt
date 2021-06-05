package util

import geomerativefork.src.util.boundInt
import geomerativefork.src.util.toRGBInt
import util.iterators.endPointPair
import util.iterators.getLerpIndices
import util.iterators.mapPercentToIndex
import util.iterators.zip
import java.awt.Color

fun List<Color>.lerp(amt: Double): Color {
  val lerpIndices = getLerpIndices(amt)

  return if (lerpIndices.isEmpty()) Color.PINK
  else lerpIndices.map { get(it) }
    .endPointPair()
    .lerp(mapPercentToIndex(amt) - lerpIndices.first())
}


fun Color.luminance(): Double = 0.3 * red + 0.59 * green + 0.11 * blue

fun Pair<Color, Color>.lerp(amt: Double) = (first.rgbList() to second.rgbList())
  .zip { rgb1, rgb2 -> (rgb1 + rgb2) / 2 }
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
fun Color.withAlphaFloat(v: Float) = Color(red, green, blue, v.toRGBInt())
fun Color.withAlphaDouble(v: Double) = Color(red, green, blue, v.toRGBInt())

/**
 * Redval must be between 0 and 1
 */
fun Color.withRed(v: Float) = withRed((v * 255).toInt())
fun Color.withGreen(v: Float) = withGreen((v * 255).toInt())
fun Color.withBlue(v: Float) = withBlue((v * 255).toInt())

fun Color.map(block: (Int) -> Int) = rgbList()
  .map { block(it) }
  .toColor()

/**
 * @param percent is between 0 and 1
 */
fun Color.darkened(percent: Float) =
  map { (it * (1 - percent)).boundInt(0, (255 * (1 - percent)).toInt()) }

/**
 * @param percent is between 0 and 1
 */
fun Color.lightened(percent: Float) =
  map { (it * (1 + percent)).boundInt((percent * 255).toInt(), 255) }
