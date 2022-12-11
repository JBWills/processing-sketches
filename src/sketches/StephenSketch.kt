package sketches

import controls.controlsealedclasses.Button.Companion.button
import controls.controlsealedclasses.Slider.Companion.slider
import controls.controlsealedclasses.Slider2D.Companion.slider2D
import controls.controlsealedclasses.Slider2DArgs
import controls.controlsealedclasses.Toggle.Companion.toggle
import controls.panels.ControlStyle
import controls.panels.TabStyle
import controls.panels.TabsBuilder.Companion.tabs
import controls.panels.panelext.SliderPairArgs
import controls.panels.panelext.sliderPair
import controls.props.PropData
import controls.props.types.DitherProps
import controls.props.types.ImageTransformProps
import controls.props.types.PhotoMatProp
import coordinate.Circ
import coordinate.Point
import kotlinx.serialization.Serializable
import sketches.base.SimpleCanvasSketch
import util.debugLog
import util.image.opencvMat.getOr
import util.layers.LayerSVGConfig
import util.print.Pen

/**
 * This is a sketch for Stephen to use to create SVGs for me to draw.
 *
 * Example sketches that are good to check out for specific techniques:
 * Packing
 *    - truly generative, no images or whatever, just using math to generate a thing
 * TextSketch
 *    - Drawing text as vectors
 * SpiralPhotoSketch
 *    - Image filtering
 *    - using OpenCV mats
 *    - loading an image file
 *    - transforming points between screenspace and image space
 * MapSketchLines
 *    - layers
 *    - geo data
 *    - simplifying lines for performance
 *    - opencv masking an stuff
 *
 */
class StephenSketch : SimpleCanvasSketch<StephenData>("Stephen", StephenData()) {
  override fun drawLayers(drawInfo: DrawInfo, onNextLayer: (LayerSVGConfig) -> Unit) {
    val (drawCircle, radius, unusedPoint, centerPoint, photo, numPoints) = drawInfo.dataValues

    // Create a new layer. This is how you would use separate pens for a single drawing
    // This behavior is a little weird, but layerName names the _previous_ layer, and next layername
    // names the upcoming layer. And Style styles the upcoming layer. Sorry, this needs to be made
    // way simpler
    onNextLayer(
      LayerSVGConfig(
        nextLayerName = "image",
        style = Pen.GellyColorDarkPeach.style,
      ),
    )

    // Use OpenCV mats to read pixel data and manipulate images
    val mat = photo.loadMatMemoized()

    mat?.let {
      val screenToMatTransform = photo.getScreenToMatTransform(mat, boundRect)

      // Just draw a bunch of random points at locations where the image is darker than mid-gray.
      // It's not going to be the most interesting image

      (0 until numPoints).forEach {
        val screenPoint = boundRect.pointAt(random.nextDouble(), random.nextDouble())
        val matPoint = screenToMatTransform.transform(screenPoint)
        val pxValue = mat.getOr(matPoint, 0.0, band = 0)

        if (pxValue > 125) {
          point(screenPoint)
        }
      }
    }

    if (drawCircle) {
      onNextLayer(
        LayerSVGConfig(
          nextLayerName = "circle",
          style = Pen.GellyColorBlue.style,
        ),
      )

      // Convert a percent-based point into a point within the boundrect
      // ex: if centerpoint is Point(0.5, 0.5), and boundrect is a 100x100 square with topleft at 0,0,
      //     centerPx will be in the center of boundrect, at Point(50,50)
      val centerPx = boundRect.pointAt(centerPoint)
      val c = Circ(centerPx, radius = radius)

      c.draw(boundRect)
    }
  }
}

/**
 * This data class is what will be passed to your drawlayers function. When you hit "Save preset" these will be serialized
 * into json that can be re-loaded later.
 */
@Serializable
data class StephenData(
  var drawCircle: Boolean = true,
  // example double field
  var radius: Double = 50.0,
  // example point
  var unusedPoint: Point = Point.Half,
  // example point
  var centerPoint: Point = Point.Half,
  // example image file prop
  var photo: PhotoMatProp = PhotoMatProp(
    transformProps = ImageTransformProps(
      dither = DitherProps(
        shouldDither = true,
      ),
    ),
  ),
  var numPoints: Int = 1_000,
) : PropData<StephenData> {
  /**
   * <Notes>
   * This bind function is how you can create a control panel that's bound to your sketch state
   *
   */
  override fun bind() = tabs {
    tab("Global") {
      // you can use row and col to group panels
      row {
        col {       // A regular slider from 0 to 10
          slider(::radius, 0.0..400.0)
          // A 2d draggable grid to change a point value
          slider2D(::centerPoint, Slider2DArgs(0..1))
        }
        col {
          // width and heightRatios kind of work like flexbox
          // For container elements, the default to the sum of widths or heights of their children
          // for rows and columns, respectively
          // For leaf elements (toggle, slider, button, etc), they default to 1.
          widthRatio = 0.3
          style = ControlStyle.Gray
          toggle(::drawCircle).withHeight(2)
        }
      }

      // A slider pair is just a way to manipulate 2 double values, in this case their packaged
      // into a Point, but you could also use DoubleRange, Pair<Double, Double>, or two separate
      // Double references
      sliderPair(::unusedPoint, SliderPairArgs(withLockToggle = true))

      button("Click me to log a string") {
        debugLog("Logging button click")
      }
    }

    tabs {
      photo.asControlTabs()
    }

    // This is a little different than the other props, since it actually contains its own bind()
    // function inside it. Check out PhotoMatProp for the bing definition.
    // Any data that extends PropData can just call panel or panelTabs with a reference and it'll call the inner
    // bind
    panelTabs(::photo, style = TabStyle.Red)
  }

  override fun clone() = copy()

  override fun toSerializer() = serializer()
}

fun main() = StephenSketch().run()
