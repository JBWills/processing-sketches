package util.image

import arrow.core.memoize
import controlP5.ControlP5Constants.GRAY
import processing.core.PConstants.BLUR
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

private val _blur = { image: PImage, radius: Number ->
  image.copy().apply { filter(BLUR, radius.toFloat()) }
}.memoize()

fun PImage.blurred(radius: Number): PImage = _blur(this, radius)

private val _invert = { image: PImage ->
  image.copy().apply { filter(INVERT) }
}.memoize()

fun PImage.inverted(): PImage = _invert(this)
