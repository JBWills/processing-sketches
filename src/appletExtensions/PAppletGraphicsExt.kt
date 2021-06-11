package appletExtensions

import coordinate.BoundRect
import coordinate.Point
import org.opencv.core.Mat
import processing.core.PConstants.JAVA2D
import processing.core.PGraphics
import processing.core.PImage
import util.image.ImageFormat
import util.image.copyTo
import util.image.toMat
import util.image.toPImage
import util.print.Style

fun PGraphics.image(image: PImage, topLeft: Point = Point(0, 0)) =
  image(image, topLeft.xf, topLeft.yf)

fun PGraphics.image(image: Mat, topLeft: Point = Point(0, 0)) =
  image(image.toPImage(), topLeft.xf, topLeft.yf)

fun Mat.draw(g: PGraphics, topLeft: Point = Point(0, 0)) = g.image(this, topLeft)
fun PImage.draw(g: PGraphics, topLeft: Point = Point(0, 0)) = g.image(this, topLeft)

fun PAppletExt.createGraphics(
  size: Point,
  format: ImageFormat = ImageFormat.ARGB,
  renderer: String = JAVA2D,
): PGraphics = createGraphics(size.xi, size.yi, renderer)
  .also { it.init(size.xi, size.yi, format.pImageFormat) }

fun PAppletExt.createGraphicsAndDraw(
  size: Point,
  format: ImageFormat = ImageFormat.ARGB,
  renderer: String = JAVA2D,
  block: PGraphics.() -> Unit
): PGraphics = createGraphics(size, format, renderer).withDraw {
  block()
  this
}

fun PAppletExt.createImage(
  size: Point = Point(width, height),
  format: ImageFormat = ImageFormat.ARGB,
  renderer: String = JAVA2D,
  style: Style? = null,
  block: PGraphics.() -> Unit
): PImage = createGraphics(size, format, renderer).withDraw(style) {
  block()
  get()
}

private var isDrawing: Boolean = false
fun <R> PGraphics.withDraw(style: Style? = null, block: PGraphics.() -> R): R {
  if (isDrawing) {
    throw Exception("Tried to call beginDraw when a draw call already in effect.")
  }
  beginDraw()
  isDrawing = true
  val result = withStyle(style) { block() }
  endDraw()
  isDrawing = false
  return result
}

fun PGraphics.withDrawToImage(style: Style? = null, block: PGraphics.() -> Unit): PImage =
  withDraw(style) {
    block()
    get()
  }


fun PGraphics.displayOnParent(offset: Point = Point.Zero) = parent.image(this, offset.xf, offset.yf)

fun PImage.getBounds() = BoundRect(Point.Zero, width - 1, height - 1)

fun PImage.editAsMat(block: Mat.() -> Mat): PImage {
  toMat()
    .block()
    .copyTo(this)

  return this
}

fun PImage.copyAndEditAsMat(block: Mat.() -> Mat): PImage = toMat()
  .block()
  .toPImage()

