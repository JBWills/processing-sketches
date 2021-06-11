package util.image

import appletExtensions.editAsMat
import arrow.core.memoize
import controlP5.ControlP5Constants.GRAY
import processing.core.PConstants.INVERT
import processing.core.PImage
import util.luminance
import java.awt.Color

private val _luminance = { p: PImage ->
  p.copy().apply { filter(GRAY) }
}.memoize()

fun PImage.luminance(): PImage = _luminance(this)

private val _threshold = { p: PImage, min: Int, max: Int ->
  p.copy().apply {
    loadPixels()

    pixels = pixels.map {
      val lum = Color(it).luminance().toInt()

      val clipped = when {
        lum < min -> 0
        lum > max -> 255
        else -> (((lum - min).toDouble() / (max - min)) * 255).toInt()
      }

      Color(clipped, clipped, clipped).rgb
    }.toIntArray()
  }
}.memoize()

fun PImage.threshold(min: Int, max: Int): PImage = _threshold(this, min, max)

fun PImage.blurred(radius: Number, sigma: Double = radius.toDouble() / 2.0): PImage {
  if (radius.toDouble() < 1) return this
  return editAsMat { gaussianBlur(radius.toInt(), sigma) }
}

/**
 * Blur just the the alpha channel and set the color of the blurred shape to fillColor.
 *
 * This will modify the PImage in place.
 *
 * @param radius
 * @param fillColor
 */
fun PImage.blurAlpha(radius: Double, fillColor: Color, sigma: Double = radius / 2.0) {
  if (radius < 1) return
  editAsMat { gaussianBlurAlpha(radius.toInt(), fillColor, sigma) }
}

private val _invert = { image: PImage ->
  image.copy().apply { filter(INVERT) }
}.memoize()

fun PImage.inverted(): PImage = _invert(this)
