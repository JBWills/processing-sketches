package util

import java.awt.Color

fun String.toColor(): Color? = Color.decode(this)
fun String.toRGBInt(): Int = toColor()?.rgb ?: 0

fun asColor(rgb: List<Int>) = Color(rgb[0], rgb[1], rgb[2])
fun Color.rgbList() = mutableListOf(red, green, blue)

fun Color.withColorSet(colorVal: Int, rgbIndex: Int) =
  asColor(rgbList().also { it[rgbIndex] = colorVal })

/**
 * v must be between 0 and 255
 */
fun Color.withRed(v: Int) = withColorSet(v, 0)
fun Color.withGreen(v: Int) = withColorSet(v, 1)
fun Color.withBlue(v: Int) = withColorSet(v, 2)

/**
 * Redval must be between 0 and 1
 */
fun Color.withRed(v: Float) = withRed((v * 255).toInt())
fun Color.withGreen(v: Float) = withGreen((v * 255).toInt())
fun Color.withBlue(v: Float) = withBlue((v * 255).toInt())
