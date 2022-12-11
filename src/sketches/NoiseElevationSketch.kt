package sketches

import controls.controlsealedclasses.Slider.Companion.slider
import controls.controlsealedclasses.Toggle.Companion.toggle
import controls.panels.TabsBuilder.Companion.layerTab
import controls.panels.TabsBuilder.Companion.tabs
import controls.panels.panelext.SliderPairArgs
import controls.panels.panelext.noisePanel
import controls.panels.panelext.sliderPair
import controls.props.PropData
import controls.props.types.VectorProp
import coordinate.Mesh
import coordinate.Point
import fastnoise.Noise
import kotlinx.serialization.Serializable
import sketches.base.LayeredCanvasSketch

/**
 * Draws a map with topology that can be offset to create a 3d effect.
 */
class NoiseElevationSketch : LayeredCanvasSketch<NoiseElevationData, NoiseElevationLayerData>(
  "NoiseElevationSketch",
  defaultGlobal = NoiseElevationData(),
  layerToDefaultTab = { NoiseElevationLayerData() },
) {

  val MaxMoveAmount = 300

  init {
    numLayers = 1
  }

  override fun drawSetup(layerInfo: DrawInfo) {
  }

  override fun drawOnce(layerInfo: LayerInfo) {
    val (noise, minThreshold, maxThreshold, elevationMoveVector, samplePointsXY, showHorizontalLines, showVerticalLines) = layerInfo.globalValues

    val elevationRange = minThreshold..maxThreshold
    val elevationMoveAmount = elevationMoveVector.scaledVector(MaxMoveAmount)


    fun elevationPercent(p: Point) = noise.get(p.x, p.y) + 0.5

    fun isPointVisible(p: Point): Boolean = elevationRange.contains(noise.get(p.x, p.y) + 0.5)

    val (horizontalLines, verticalLines) = Mesh(
      boundRect.expand(elevationMoveAmount.abs() * 2),
      samplePointsXY.xi,
      samplePointsXY.yi,
      pointTransformFunc = { pointLocation, _, _ ->
        pointLocation + (elevationMoveAmount * elevationPercent(pointLocation))
      },
      pointVisibleFunc = { pointLocation, _, _, _ ->
        isPointVisible(pointLocation)
      },
    ).toLines()

    if (showHorizontalLines) horizontalLines.draw(boundRect)
    if (showVerticalLines) verticalLines.draw(boundRect)
  }
}

@Serializable
data class NoiseElevationLayerData(
  var exampleTabField: Int = 1,
) : PropData<NoiseElevationLayerData> {
  override fun bind() = layerTab {
    slider(::exampleTabField, 0..10)
  }

  override fun clone() = copy()
  override fun toSerializer() = serializer()
}

@Serializable
data class NoiseElevationData(
  var noise: Noise = Noise.DEFAULT,
  var minThreshold: Double = 0.0,
  var maxThreshold: Double = 1.0,
  var elevationMoveVector: VectorProp = VectorProp(Point.Zero, 0.0),
  var samplePointsXY: Point = Point(2, 2),
  var showHorizontalLines: Boolean = true,
  var showVerticalLines: Boolean = true,
) : PropData<NoiseElevationData> {
  override fun bind() = tabs {
    tab("Noise") {
      noisePanel(::noise)
    }

    tab("Lines") {
      row {
        slider(::minThreshold, 0..1)
        slider(::maxThreshold, 0..1)
      }
      panel(::elevationMoveVector)
      sliderPair(::samplePointsXY, SliderPairArgs(1.0..500.0))
      row {
        toggle(::showHorizontalLines)
        toggle(::showVerticalLines)
      }
    }
  }

  override fun clone() = copy()

  override fun toSerializer() = serializer()
}

fun main() = NoiseElevationSketch().run()
