package util.image.pimage

import arrow.core.memoize
import coordinate.BoundRect
import coordinate.Point
import coordinate.Point.Companion.minXY
import processing.core.PConstants.RGB
import processing.core.PGraphics
import processing.core.PImage
import util.base.luminance
import util.image.ImageCrop
import util.image.ImageCrop.Crop
import java.awt.Color

val PImage.aspect: Double get() = width.toDouble() / height
val PImage.bounds: BoundRect get() = BoundRect(width, height)
val PImage.center: Point get() = Point(width / 2, height / 2)
val PImage.size: Point get() = Point(width, height)

fun PImage.get(p: Point): Color = Color(get(p.xi, p.yi))

private val _resized = { image: PImage, p: Point ->
  image.copy().apply { resize(p.xi, p.yi) }
}.memoize()

fun PImage.resized(p: Point): PImage = _resized(this, p)

private val _get = { image: PImage, topLeft: Point, size: Point ->
  image.get(topLeft.xi, topLeft.yi, size.xi, size.yi)
}.memoize()

fun PImage.get(topLeft: Point, size: Point): PImage = _get(this, topLeft, size)

fun PImage.get(bounds: BoundRect): PImage = get(bounds.topLeft, bounds.size)

fun PImage.gradientAt(point: Point, filterSizePx: Int = 1) = Point(
  get(point.addX(-filterSizePx)).luminance() + get(point.addX(filterSizePx)).luminance(),
  get(point.addY(-filterSizePx)).luminance() + get(point.addY(filterSizePx)).luminance(),
)

fun PImage.cropHeightCentered(newHeight: Int): PImage = cropCentered(Point(width, newHeight))

fun PImage.cropWidthCentered(newWidth: Int): PImage = cropCentered(Point(newWidth, height))

fun PImage.cropCentered(newSize: Point): PImage {
  val actualNewSize = minXY(newSize, size)
  return get(bounds.resizeCentered(actualNewSize))
}

fun PImage.scaleByLargestDimension(newSize: Number): PImage =
  if (width > height) scaleByWidth(newSize) else scaleByHeight(newSize)

fun PImage.scaleByWidth(newWidth: Number): PImage = resized(Point(newWidth, 0))

fun PImage.scaleByHeight(newHeight: Number): PImage = resized(Point(0, newHeight))
fun PImage.scale(newSize: Point): PImage = resized(newSize)

private val _scaleAndCrop = { image: PImage, cropType: ImageCrop, container: BoundRect ->
  cropType.cropped(image, container)
}.memoize()

fun PImage.scaleAndCrop(cropType: ImageCrop, container: BoundRect) =
  _scaleAndCrop(this, cropType, container)


fun PImage.pasteOnTopCentered(other: PImage): PImage {
  val toPaste = get()
  val containerImage = other.get()
  if (toPaste.width > containerImage.width || toPaste.height > containerImage.height) {
    toPaste.scaleAndCrop(Crop, containerImage.bounds)
  }

  val newImageBounds = toPaste.bounds.recentered(containerImage.bounds.center)
  containerImage.set(newImageBounds.topLeft.xi, newImageBounds.topLeft.yi, toPaste)

  return containerImage
}

fun PGraphics.overlay(src: PImage) = image(src, 0f, 0f)

fun solidColorPImage(size: Point, c: Color) = PImage(size.xi, size.yi, RGB).apply {
  val colorRgb = c.rgb
  loadPixels()
  pixels = (0 until height).flatMap { (0 until width).map { colorRgb } }.toIntArray()
}

