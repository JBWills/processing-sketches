/**
 * Copyright 2004-2008 Ricard Marxer  <email></email>@ricardmarxer.com>
 *
 *
 * This file is part of Geomerative.
 *
 *
 * Geomerative is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *
 * Geomerative is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *
 * You should have received a copy of the GNU General Public License
 * along with Geomerative.  If not, see <http:></http:>//www.gnu.org/licenses/>.
 */
@file:Suppress("unused")

package geomerativefork.src

import geomerativefork.src.RCommand.Companion.setSegmentAngle
import geomerativefork.src.RCommand.Companion.setSegmentLength
import geomerativefork.src.RCommand.Companion.setSegmentStep
import geomerativefork.src.RCommand.Companion.setSegmentator
import geomerativefork.src.RShape.Companion.createEllipse
import geomerativefork.src.RShape.Companion.createLine
import geomerativefork.src.RShape.Companion.createRectangle
import geomerativefork.src.RShape.Companion.createRing
import geomerativefork.src.RShape.Companion.createStar
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PGraphics
import util.with

/**
 * RG is a static class containing all the states, modes, etc..
 * Geomerative is mostly used by calling RG methods. e.g.  RShape s = RG.getEllipse(30, 40, 80, 80)
 */
object RG : PConstants {

  const val ROUND = 0
  const val PROJECT = 1
  const val SQUARE = 2
  const val MITER = 3
  const val BEVEL = 4

  /**
   * The adaptor adapts the shape to a particular shape by adapting each of the groups points.  This can cause deformations of the individual elements in the group.
   */
  const val BYPOINT = 0

  /**
   * The adaptor adapts the shape to a particular shape by adapting each of the groups elements positions.  This mantains the proportions of the shapes.
   */
  const val BYELEMENTPOSITION = 1

  /**
   * The adaptor adapts the shape to a particular shape by adapting each of the groups elements indices.  This mantains the proportions of the shapes.
   */
  const val BYELEMENTINDEX = 2

  /**
   * @invisible
   */
  var ignoreStyles = false

  /**
   * @invisible
   */
  var useFastClip = true

  /**
   * ADAPTATIVE segmentator minimizes the number of segments avoiding perceptual artifacts like angles or cusps.  Use this in order to have polygons and meshes with the fewest possible vertices.
   */
  var ADAPTATIVE = RCommand.ADAPTATIVE

  /**
   * UNIFORMLENGTH segmentator is the slowest segmentator and it segments the curve on segments of equal length.  This can be useful for very specific applications when for example drawing incrementally a shape with a uniform speed.
   */
  var UNIFORMLENGTH = RCommand.UNIFORMLENGTH

  /**
   * UNIFORMSTEP segmentator is the fastest segmentator and it segments the curve based on a constant value of the step of the curve parameter, or on the number of segments wanted.  This can be useful when segmentating very often a Shape or when we know the amount of segments necessary for our specific application.
   */
  var UNIFORMSTEP = RCommand.UNIFORMSTEP

  /**
   * @invisible
   * can be RG.BYPOINT, RG.BYELEMENTPOSITION or RG.BYELEMENTINDEX
   */
  var adaptorType = BYELEMENTPOSITION

  /**
   * @invisible
   */
  var adaptorScale = 1f

  /**
   * @invisible
   * This specifies where to start adapting the group to the shape.
   *
   */
  var adaptorLengthOffset = 0f
    set(value) {
      if (value in 0f..1f) field = value
      else throw RuntimeException("The adaptor length offset must take a value between 0 and 1.")
    }
  var dpi = 72

  var shape: RShape? = null

  var fntLoader: RFont? = null

  /**
   * @invisible
   */
  private var initialized = false

  /**
   * @invisible
   */
  private var parent: PApplet? = null

  /**
   * Load and get the font object that can be used in the textFont method.
   *
   * @param fontFile the filename of the font to be loaded
   * @return RFont, the font object
   * @eexample loadFont
   */
  fun loadFont(fontFile: String?): RFont {
    val newFntLoader = RFont(fontFile)
    fntLoader = fntLoader ?: newFntLoader
    return newFntLoader
  }

  /**
   * Draw text to the screen using the font set using the textFont method.
   *
   * @param text the string to be drawn on the screen
   * @eexample text
   */
  fun text(text: String) = getText(text).draw()

  /**
   * Set the font object to be used in all text calls.
   *
   * @param font the font object to be set
   * @param size the size of the font
   * @eexample textFont
   */
  fun textFont(font: RFont, size: Int) {
    font.size = size
    fntLoader = font
  }

  // Font methods
  /**
   * Get the shape corresponding to a text.  Use the textFont method to select the font and size.
   *
   * @param font  the filename of the font to be loaded
   * @param text  the string to be created
   * @param size  the size of the font to be used
   * @param align the alignment. Use RG.CENTER, RG.LEFT or RG.RIGHT
   * @return RShape, the shape created
   * @eexample getText
   */
  fun getText(text: String, font: String, size: Int, align: Int): RShape {
    val tempFntLoader = RFont(font, size, align)
    return tempFntLoader.toShape(text)
  }

  fun getText(text: String): RShape =
    fntLoader?.toShape(text) ?: throw FontNotLoadedException()

  /**
   * Draw a shape to a given position on the screen.
   *
   * @param shp the shape to be drawn
   * @param x   the horizontal coordinate
   * @param y   the vertical coordinate
   * @param w   the width with which we draw the shape
   * @param h   the height with which we draw the shape
   * @eexample shape
   */
  fun shape(shp: RShape, x: Float, y: Float, w: Float, h: Float) =
    RShape(shp).with {
      transform(
        RMatrix().also { matrix ->
          matrix.translate(x, y)
          matrix.scale(w / getOrigWidth(), h / getOrigHeight())
        }
      )
      draw()
    }

  fun shape(shp: RShape, x: Float, y: Float) =
    RShape(shp).with {
      transform(
        RMatrix().also { matrix -> matrix.translate(x, y) }
      )
      draw()
    }

  fun shape(shp: RShape) = shp.draw()

  // Shape methods
  /**
   * Create a shape from an array of point arrays.
   *
   * @eexample createShape
   */
  fun createShape(points: Array<Array<RPoint>>): RShape = RShape(points)

  /**
   * Load a shape object from a file.
   *
   * @param filename the SVG file to be loaded.  Must be in the data directory
   * @eexample loadShape
   */
  fun loadShape(filename: String): RShape = RSVG().toShape(filename)

  /**
   * Save a shape object to a file.
   *
   * @param filename the SVG file to be saved.
   * @param shape    the shape to be saved.
   * @eexample saveShape
   */
  fun saveShape(filename: String, shape: RShape) = parent().saveStrings(
    filename,
    RSVG().fromShape(shape).split("\n").toTypedArray()
  )

  /**
   * Begin to create a shape.
   *
   * @eexample createShape
   */
  fun beginShape() {
    shape = RShape()
  }

  /**
   * Begin a new path in the current shape.  Can only be called inside beginShape() and endShape().
   *
   * @param endMode if called with RG.CLOSE it closes the current path before starting the new one.
   * @eexample createShape
   */
  fun breakShape(endMode: Int) {
    val shape = shape!!
    if (endMode == PConstants.CLOSE) shape.addClose()
    shape.updateOrigParams()
    breakShape()
  }

  private fun breakShape() = shape?.addPath()

  // Methods to create shapes
  /**
   * Add a vertex to the shape.  Can only be called inside beginShape() and endShape().
   *
   * @param x the x coordinate of the vertex
   * @param y the y coordinate of the vertex
   * @eexample createShape
   */
  fun vertex(x: Float, y: Float) {
    val shape = shape!!
    if (shape.paths.isEmpty()) {
      shape.addMoveTo(x, y)
    } else {
      shape.addLineTo(x, y)
    }
  }

  /**
   * Add a bezierVertex to the shape.  Can only be called inside beginShape() and endShape().
   *
   * @param cx1 the x coordinate of the first control point
   * @param cy1 the y coordinate of the first control point
   * @param cx2 the x coordinate of the second control point
   * @param cy2 the y coordinate of the second control point
   * @param x   the x coordinate of the end point
   * @param y   the y coordinate of the end point
   * @eexample createShape
   */
  fun bezierVertex(cx1: Float, cy1: Float, cx2: Float, cy2: Float, x: Float, y: Float) {
    val shape = shape ?: throw NoPathInitializedException()
    if (shape.paths.isEmpty()) {
      throw NoPathInitializedException()
    } else {
      shape.addBezierTo(cx1, cy1, cx2, cy2, x, y)
    }
  }

  /**
   * End the shape being created and draw it to the screen or the PGraphics passed as parameter.
   *
   * @param g the canvas on which to draw.  By default it draws on the screen
   * @eexample createShape
   */
  fun endShape(g: PGraphics) {
    shape?.draw(g)
    shape = null
  }

  fun endShape() {
    shape?.draw()
    shape = null
  }

  /**
   * Get an ellipse as a shape object.
   *
   * @param x x coordinate of the center of the shape
   * @param y y coordinate of the center of the shape
   * @param w width of the ellipse
   * @param h height of the ellipse
   * @return RShape, the shape created
   * @eexample getEllipse
   */
  fun getEllipse(x: Float, y: Float, w: Float, h: Float): RShape =
    createEllipse(x, y, w, h)

  fun getEllipse(x: Float, y: Float, w: Float): RShape =
    getEllipse(x, y, w, w)

  /**
   * Get a line as a shape object.
   *
   * @param x1 x coordinate of the first point of the line
   * @param y1 y coordinate of the first point of the line
   * @param x2 x coordinate of the last point of the line
   * @param y2 y coordinate of the last point of the line
   * @return RShape, the shape created
   * @eexample getLine
   */
  fun getLine(x1: Float, y1: Float, x2: Float, y2: Float): RShape =
    createLine(x1, y1, x2, y2)

  /**
   * Get an rectangle as a shape object.
   *
   * @param x x coordinate of the top left corner of the shape
   * @param y y coordinate of the top left of the shape
   * @param w width of the rectangle
   * @param h height of the rectangle
   * @return RShape, the shape created
   * @eexample getRect
   */
  fun getRect(x: Float, y: Float, w: Float, h: Float): RShape =
    createRectangle(x, y, w, h)

  fun getRect(x: Float, y: Float, w: Float): RShape =
    getRect(x, y, w, w)

  /**
   * Get a star as a shape object.
   *
   * @param x          x coordinate of the center of the shape
   * @param y          y coordinate of the center of the shape
   * @param widthBig   the outter width of the star polygon
   * @param widthSmall the inner width of the star polygon
   * @param spikes     the amount of spikes on the star polygon
   * @return RShape, the shape created
   * @eexample getStar
   */
  fun getStar(x: Float, y: Float, widthBig: Float, widthSmall: Float, spikes: Int): RShape =
    createStar(x, y, widthBig, widthSmall, spikes)

  /**
   * Get a ring as a shape object.
   *
   * @param x          x coordinate of the center of the shape
   * @param y          y coordinate of the center of the shape
   * @param widthBig   the outter width of the ring polygon
   * @param widthSmall the inner width of the ring polygon
   * @return RShape, the shape created
   * @eexample getRing
   */
  fun getRing(x: Float, y: Float, widthBig: Float, widthSmall: Float): RShape =
    createRing(x, y, widthBig, widthSmall)

  // Transformation methods
  @JvmOverloads
  fun centerIn(grp: RShape, g: PGraphics, margin: Float = 0f): RShape =
    RShape(grp).also {
      it.centerIn(g, margin)
    }

  /**
   * Split a shape along the curve length in two parts.
   *
   * @param shp the shape to be splited
   * @param t   the proportion (a value from 0 to 1) along the curve where to split
   * @return RShape[], an array of shapes with two elements, one for each side of the split
   * @eexample split
   */
  fun split(shp: RShape, t: Float): Array<RShape> = shp.split(t)

  /**
   * Adapt a shape along the curve of another shape.
   *
   * @param shp  the shape to be adapted
   * @param path the shape which curve will be followed
   * @return RShape  the adapted shape
   * @eexample split
   * @related setAdaptor ( )
   */
  fun adapt(shp: RShape, path: RShape): RShape =
    RShape(shp).also {
      it.adapt(path)
    }


  /**
   * Polygonize a shape.
   *
   * @param shp the shape to be polygonized
   * @return RShape, the polygonized shape
   * @eexample split
   * @related setPolygonizer ( )
   */
  fun polygonize(shp: RShape): RShape =
    RShape(shp).also {
      it.polygonize()
    }

  /**
   * Initialize the library.  Must be called before any call to Geomerative methods.  Must be called by passing the PApplet.  e.g. RG.init(this)
   */
  fun init(_parent: PApplet?) {
    parent = _parent
    initialized = true
  }

  /**
   * @invisible
   */
  fun initialized(): Boolean = initialized

  /**
   * @invisible
   */
  internal fun parent(): PApplet = parent ?: throw LibraryNotInitializedException()

  /**
   * Binary difference between two shapes.
   *
   * @param a first shape to operate on
   * @param b second shape to operate on
   * @return RShape, the result of the operation
   * @eexample binaryOps
   * @related diff ( )
   * @related union ( )
   * @related intersection ( )
   * @related xor ( )
   */
  fun diff(a: RShape, b: RShape): RShape = a.diff(b)

  /**
   * Binary union between two shapes.
   *
   * @param a first shape to operate on
   * @param b second shape to operate on
   * @return RShape, the result of the operation
   * @eexample binaryOps
   * @related diff ( )
   * @related union ( )
   * @related intersection ( )
   * @related xor ( )
   */
  fun union(a: RShape, b: RShape): RShape = a.union(b)

  /**
   * Binary intersection between two shapes.
   *
   * @param a first shape to operate on
   * @param b second shape to operate on
   * @return RShape, the result of the operation
   * @eexample binaryOps
   * @related diff ( )
   * @related union ( )
   * @related intersection ( )
   * @related xor ( )
   */
  fun intersection(a: RShape, b: RShape): RShape = a.intersection(b)

  /**
   * Binary xor between two shapes.
   *
   * @param a first shape to operate on
   * @param b second shape to operate on
   * @return RShape, the result of the operation
   * @eexample binaryOps
   * @related diff ( )
   * @related union ( )
   * @related intersection ( )
   * @related xor ( )
   */
  fun xor(a: RShape, b: RShape): RShape = a.xor(b)

  /**
   * Ignore the styles of the shapes when drawing and use the Processing style methods.
   *
   * @param value value to which the ignoreStyles state should be set
   * @eexample ignoreStyles
   */
  fun ignoreStyles(value: Boolean) {
    ignoreStyles = value
  }

  fun ignoreStyles() {
    ignoreStyles = true
  }

  /**
   * Use this to set the polygonizer type.
   *
   * @param segmenterMethod can be RG.ADAPTATIVE, RG.UNIFORMLENGTH or RG.UNIFORMSTEP.
   * @eexample setPolygonizer
   * @related ADAPTATIVE
   * @related UNIFORMLENGTH
   * @related UNIFORMSTEP
   */
  fun setPolygonizer(segmenterMethod: Int) = setSegmentator(segmenterMethod)

  /**
   * Use this to set the segmentator angle tolerance for the ADAPTATIVE segmentator and set the segmentator to ADAPTATIVE.
   *
   * @param angle an angle from 0 to PI/2 it defines the maximum angle between segments.
   * @eexample setPolygonizerAngle
   * @related ADAPTATIVE
   */
  fun setPolygonizerAngle(angle: Float) = setSegmentAngle(angle)

  /**
   * Use this to set the segmentator length for the UNIFORMLENGTH segmentator and set the segmentator to UNIFORMLENGTH.
   *
   * @param length the length of each resulting segment.
   * @eexample setPolygonizerLength
   * @related UNIFORMLENGTH
   * @related polygonize ( )
   */
  fun setPolygonizerLength(length: Float) = setSegmentLength(length)

  /**
   * Use this to set the segmentator step for the UNIFORMSTEP segmentator and set the segmentator to UNIFORMSTEP.
   *
   * @param step if a float from +0.0 to 1.0 is passed it's considered as the step, else it's considered as the number of steps.  When a value of 0.0 is used the steps will be calculated automatically depending on an estimation of the length of the curve.  The special value -1 is the same as 0.0 but also turning of the segmentation of lines (faster segmentation).
   * @eexample setSegmentStep
   * @related UNIFORMSTEP
   * @related polygonize ( )
   */
  fun setPolygonizerStep(step: Float) = setSegmentStep(step)

  /**
   * @invisible
   */
  class LibraryNotInitializedException : NullPointerException() {
    private val serialVersionUID = -3710605630786298671L
  }

  /**
   * @invisible
   */
  class FontNotLoadedException : NullPointerException() {
    private val serialVersionUID = -3710605630786298672L
  }

  /**
   * @invisible
   */
  class NoPathInitializedException : NullPointerException() {
    private val serialVersionUID = -3710605630786298673L
  }
}
