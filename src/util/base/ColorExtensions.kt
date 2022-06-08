@file:Suppress("unused")

package util.base

import com.dajudge.colordiff.ColorDiff
import com.dajudge.colordiff.LabColor
import com.github.ajalt.colormath.model.RGB
import org.opencv.core.Scalar
import util.iterators.endPointPair
import util.iterators.getLerpIndices
import util.iterators.mapPercentToIndex
import util.iterators.zip
import util.numbers.bound
import util.numbers.boundInt
import util.numbers.mean
import java.awt.Color

val Color.r get() = red
val Color.g get() = green
val Color.b get() = blue
val Color.a get() = alpha
val Color.rd get() = r.toDouble()
val Color.gd get() = g.toDouble()
val Color.bd get() = b.toDouble()
val Color.ad get() = a.toDouble()
val Color.rf get() = r.toFloat()
val Color.gf get() = g.toFloat()
val Color.bf get() = b.toFloat()
val Color.af get() = a.toFloat()

fun Number.toRgbInt() = (bound(0f, 1f) * 255).toInt()

fun Color.toRgbScalar() = Scalar(rd, gd, bd)
fun Color.toRgbaScalar() = Scalar(rd, gd, bd, ad)
fun Color.toArgbScalar() = Scalar(ad, rd, gd, bd)
fun Color.toBgrScalar() = Scalar(bd, gd, rd)
fun Color.toBgraScalar() = Scalar(bd, gd, rd, ad)

fun Color.toFloatArray() = floatArrayOf(rf, gf, bf)

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

val LuminanceCoeffsRgb: List<Double> = listOf(0.3, 0.59, 0.11)

/**
 * @return Luminance value from 0 to 255
 */
fun Color.luminance(): Double =
  (LuminanceCoeffsRgb[0] * red + LuminanceCoeffsRgb[1] * green + LuminanceCoeffsRgb[2] * blue) * alphaDouble

fun Pair<Color, Color>.lerp(amt: Double): Color = (first.rgbList() to second.rgbList())
  .zip { rgb1, rgb2 -> (rgb1 + ((rgb2 - rgb1) * amt)).toInt() }
  .toColor()

fun String.toColor(): Color? = Color.decode(this)
fun String.toRgbInt(): Int = toColor()?.rgb ?: 0

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
fun Color.withAlphaFloat(v: Float) = withAlpha(v.toRgbInt())
fun Color.withAlphaDouble(v: Double) = withAlpha(v.toRgbInt())

/**
 * create new Color with red value as double
 * @param v between 0 and 1
 */
fun Color.withRed(v: Double) = withRed((v.toRgbInt()))

/**
 * create new Color with green value as double
 * @param v between 0 and 1
 */
fun Color.withGreen(v: Double) = withGreen((v.toRgbInt()))

/**
 * create new Color with blue value as double
 * @param v between 0 and 1
 */
fun Color.withBlue(v: Double) = withBlue((v.toRgbInt()))

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

fun Color.toColorMathRGB() = RGB(this.rf, this.gf, this.bf)

fun Color.toLab(): LabColor = ColorDiff.rgb_to_lab(this)

fun Color.deltaE2000(other: Color): Double =
  ColorDiff.diff(toLab(), other.toLab())
