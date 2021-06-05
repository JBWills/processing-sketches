package appletExtensions

import coordinate.BoundRect
import coordinate.Point
import processing.core.PConstants.ARGB
import processing.core.PConstants.JAVA2D
import processing.core.PGraphics
import processing.core.PImage
import util.print.Style
import util.withAlpha
import java.awt.Color

fun PAppletExt.createGraphics(
  size: Point,
  colorMode: Int = ARGB,
  renderer: String = JAVA2D,
): PGraphics =
  createGraphics(size.xi, size.yi, renderer)
    .also { it.init(size.xi, size.yi, colorMode) }

private var isDrawing: Boolean = false
fun PGraphics.withDraw(style: Style? = null, block: () -> Unit): PGraphics {
  if (isDrawing) {
    throw Exception("Tried to call beginDraw when a draw call already in effect.")
  }
  beginDraw()
  isDrawing = true
  style?.let { withStyle(style) { block() } } ?: block()
  endDraw()
  isDrawing = false
  return this
}


fun PGraphics.displayOnParent(offset: Point = Point.Zero) = parent.image(this, offset.xf, offset.yf)

fun PImage.getBounds() = BoundRect(Point.Zero, width - 1, height - 1)

fun PGraphics.blend(
  mode: Int,
  other: PGraphics,
  offsetOnDest: Point,
  blendFunc: (srcValue: Int, destValue: Int) -> Int
) {
  other.loadPixels()

  filterPixels(BoundRect(offsetOnDest, other.width, other.height)) { x, y, value ->
    val otherX = x - offsetOnDest.xi
    val otherY = y - offsetOnDest.yi

    val otherIndex = otherY * other.width + otherX
    val otherValue = other.pixels[otherIndex]

    blendFunc(otherValue, value)
  }
}

//fun PImage.add(other: PImage, offset: Point) =
//  filterPixels(BoundRect(offset, other.width, other.height)) { x, y, value ->
//    val otherValue = other.pixels[]
//  }

//fun PImage.subtract(other: PImage, offset: Point) = blend(SUBTRACT, other, offset)
//fun PImage.multiply(other: PImage, offset: Point) = blend(MULTIPLY, other, offset)
//fun PImage.lightest(other: PImage, offset: Point) = blend(LIGHTEST, other, offset)
//fun PImage.darkest(other: PImage, offset: Point) = blend(DARKEST, other, offset)


fun PImage.filterByRow(block: (rowIndex: Int, values: IntArray) -> IntArray) {
  (0 until height).forEach { rowNum ->
    block(rowNum, pixels.sliceArray(0 until width))
      .copyInto(pixels, rowNum * width)
  }

  setModified()
}

fun PImage.setPixels(c: Color, alpha: Int = 255) {
  val cRGB = c.withAlpha(alpha).rgb
  pixels = IntArray(pixels.size) { cRGB }
}

fun PImage.filterPixels(
  bound: BoundRect = getBounds(),
  block: (x: Int, y: Int, value: Int) -> Int
) {
  bound.boundsIntersection(getBounds())?.forEachGrid { p ->
    val index = p.yi * width + p.xi
    pixels[index] = block(p.xi, p.yi, pixels[index])
  }

  setModified()
}

fun PGraphics.mapPixels(
  bound: BoundRect = getBounds(),
  newColorMode: Int = colorMode,
  block: (x: Int, y: Int, value: Int) -> Int
): PGraphics {
  loadPixels()
  val newGraphics = PGraphics().also {
    it.setSize(width, height)
    it.pixels = pixels
    it.filterPixels(bound, block)
    it.colorMode = newColorMode
  }

  return newGraphics
}

fun PGraphics.mapPixelValues(
  bound: BoundRect = getBounds(),
  newColorMode: Int = colorMode,
  block: (value: Int) -> Int
): PGraphics {
  loadPixels()
  val newGraphics = PGraphics().also {
    it.setSize(width, height)
    it.pixels = pixels
    it.filterPixels(bound) { _, _, v -> block(v) }
    it.colorMode = newColorMode
  }

  return newGraphics
}

