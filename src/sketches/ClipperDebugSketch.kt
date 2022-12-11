package sketches

import appletExtensions.withStroke
import controls.controlsealedclasses.Dropdown.Companion.dropdown
import controls.controlsealedclasses.Slider.Companion.slider
import controls.controlsealedclasses.Slider2D.Companion.slider2D
import controls.controlsealedclasses.Slider2DArgs
import controls.controlsealedclasses.Toggle.Companion.toggle
import controls.panels.TabsBuilder.Companion.layerTab
import controls.panels.TabsBuilder.Companion.singleTab
import controls.props.PropData
import coordinate.Arc
import coordinate.Circ
import coordinate.Deg
import coordinate.Point
import de.lighti.clipper.Clipper.ClipType
import kotlinx.serialization.Serializable
import sketches.base.LayeredCanvasSketch
import util.iterators.listWrapped
import util.polylines.PolyLine
import util.polylines.clipping.ForceClosedOption
import util.polylines.clipping.ForceClosedOption.Close
import util.polylines.clipping.clip
import util.polylines.isClosed
import util.polylines.simplify
import java.awt.Color

/**
 * Starter sketch that uses all of the latest bells and whistles.
 *
 * Copy and paste this to create a new sketch.
 */
class ClipperDebug :
  LayeredCanvasSketch<ClipperDebugData, ClipperDebugLayerData>(
    "ClipperDebug",
    defaultGlobal = ClipperDebugData(),
    layerToDefaultTab = { ClipperDebugLayerData() },
  ) {

  init {
    numLayers = 1
  }

  override fun drawSetup(layerInfo: DrawInfo) {}

  override fun drawOnce(layerInfo: LayerInfo) {
    val (arcLocation, arcRadius, arcDeg, arcDegStart, simplifyAmt, boundByRect, doDiffCircle, doDiffSquare, intersectPinkSquare, forceClosed) = layerInfo.globalValues

    var arc: List<PolyLine> =
      Arc(
        arcDegStart,
        arcDeg.unboundValue,
        Circ(boundRect.size * arcLocation, arcRadius),
      )
        .walk(1.0)
        .simplify(simplifyAmt)
        .listWrapped()

    fun diffArc(other: List<PolyLine>, forceClosed: ForceClosedOption) {
      arc = arc.clip(other, ClipType.DIFFERENCE, forceClosed)
    }

    fun intersectArc(other: List<PolyLine>, forceClosed: ForceClosedOption) {
      arc = arc.clip(other, ClipType.INTERSECTION, forceClosed)
    }

    val staticSquare: PolyLine = boundRect
      .scale(Point(0.3), boundRect.size / 3)
      .toPolyLine()
      .also {
        withStroke(Color.RED) {
          it.draw()
        }
      }

    val intersectSquare: PolyLine = boundRect
      .scale(Point(0.3), boundRect.size / 4)
      .toPolyLine()
      .also {
        withStroke(Color.PINK) {
          it.draw()
        }
      }

    val staticCircle: PolyLine = Circ(Point(0.7, 0.7) * boundRect.size, 50)
      .walk(1.0)
      .also { withStroke(Color.BLUE) { it.draw() } }

    if (doDiffCircle) diffArc(staticCircle.listWrapped(), forceClosed)
    if (doDiffSquare) diffArc(staticSquare.listWrapped(), forceClosed)
    if (intersectPinkSquare) intersectArc(
      intersectSquare.listWrapped(),
      forceClosed,
    )

    arc.forEach { arcLine ->
      withStroke(if (arcLine.isClosed()) Color.GRAY else Color.WHITE) {

        arcLine.draw(if (boundByRect) boundRect else null)
      }
    }
  }
}

@Serializable
data class ClipperDebugLayerData(
  var exampleTabField: Int = 1,
) : PropData<ClipperDebugLayerData> {
  override fun bind() = layerTab {
    slider(::exampleTabField, 0..10)
  }

  override fun clone() = copy()
  override fun toSerializer() = serializer()
}

@Serializable
data class ClipperDebugData(
  var arcLocation: Point = Point.Half,
  var arcRadius: Double = 50.0,
  var arcDeg: Deg = Deg(180),
  var arcDegStart: Deg = Deg(0),
  var simplifyAmt: Double = 0.0,
  var boundByRect: Boolean = true,
  var doDiffCircle: Boolean = true,
  var doDiffSquare: Boolean = true,
  var intersectPinkSquare: Boolean = true,
  var forceClosed: ForceClosedOption = Close,
) : PropData<ClipperDebugData> {
  override fun bind() = singleTab("Global") {
    row {
      slider2D(::arcLocation, Slider2DArgs(0..1))
      slider(::arcRadius, 0..500)
    }
    row {
      slider(::arcDegStart)
      slider(::arcDeg)
    }

    slider(::simplifyAmt)

    row {
      toggle(::boundByRect)
      toggle(::doDiffCircle)
      toggle(::doDiffSquare)
      toggle(::intersectPinkSquare)
    }

    dropdown(::forceClosed)
  }

  override fun clone() = copy()

  override fun toSerializer() = serializer()
}

fun main() = ClipperDebug().run()
