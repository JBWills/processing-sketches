package util.image

import arrow.core.memoize
import controlP5.ControlP5Constants.GRAY
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import processing.core.PConstants.BLUR
import processing.core.PConstants.INVERT
import processing.core.PGraphics
import processing.core.PImage
import util.equalsZero
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

fun PGraphics.blur(radius: Number) {
  if (radius.equalsZero()) return else filter(BLUR, radius.toFloat())
}

fun Mat.gaussianBlur(radius: Double): Mat = Mat(height(), width(), type())
  .also { destMat ->
    Imgproc.GaussianBlur(this, destMat, Size(radius, radius), radius / 2)
  }

fun Mat.copyTo(pImage: PImage) =
  get(0, 0, pImage.pixels)

fun PImage.quickBlur(radius: Double) {
  if (radius.equalsZero()) return
  toOpenCV()
    .gaussianBlur(radius)
    .copyTo(this)
}

private val _invert = { image: PImage ->
  image.copy().apply { filter(INVERT) }
}.memoize()

fun PImage.inverted(): PImage = _invert(this)
