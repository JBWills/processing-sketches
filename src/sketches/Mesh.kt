package sketches

import BaseSketch
import appletExtensions.withStroke
import controls.ControlSection.Companion.section
import controls.doublePairProp
import controls.intProp
import controls.noiseProp
import coordinate.Point
import fastnoise.FastNoise.NoiseType.Cubic
import fastnoise.Noise
import fastnoise.NoiseQuality.High
import interfaces.Bindable
import sketches.base.LayeredCanvasSketch
import util.ZeroToOne
import util.atAmountAlong
import util.map
import util.percentAlong
import util.plus
import util.zeroTo
import java.awt.Color

/**
 * Starter sketch that uses all of the latest bells and whistles.
 *
 * Copy and paste this to create a new sketch.
 */
class Mesh : LayeredCanvasSketch<MeshTabValues, MeshGlobalValues>(
  "Mesh",
  MeshGlobalValues(),
  { MeshTabValues() }
) {
  init {
    numLayers = 1
  }

  override fun drawSetup(layerInfo: DrawInfo) {
  }

  override fun drawOnce(values: LayerInfo) {
    val (noise, numDots, size, dotRectCenter) = values.globalValues
    val (MeshTabField) = values.tabValues

    val dotBound = boundRect.scale(size, boundRect.pointAt(dotRectCenter.x, dotRectCenter.y))

    val pointRows = numDots.x.toInt().map { xIndex ->
      val amountAlongX = (0 until numDots.x.toInt()).percentAlong(xIndex)
      return@map numDots.y.toInt().map { yIndex ->
        val amountAlongY = (0 until numDots.y.toInt()).percentAlong(yIndex)

        Point(
          (dotBound.left..dotBound.right).atAmountAlong(amountAlongX),
          (dotBound.top..dotBound.bottom).atAmountAlong(amountAlongY),
        )
      }
    }

    val warpedPoints = pointRows.map {
      noise.move(it)
    }

    if (isDebugMode) withStroke(Color.GREEN) { pointRows.forEach { it.drawPoints() } }

    warpedPoints.forEach { row ->
      row.draw(boundRect)
    }

    val flippedArray = warpedPoints[0].size.map { warpedPoints.map { row -> row[it] } }

    flippedArray.forEach { it.draw(boundRect) }

//    warpedPoints.forEachWithNext { currRow, nextRow ->
//      currRow.forEachWithNextIndexed { point, nextPoint, index ->
//        if (nextPoint != null) {
//          line(Segment(point, nextPoint))
//        }
//
//        if (nextRow != null) {
//          val downPoint = nextRow[index]
//          line(Segment(point, downPoint))
//        }
//
//        if (nextRow != null && nextPoint != null) {
//          val diagonalPoint = nextRow[index + 1]
//          line(Segment(point, diagonalPoint))
//        }
//      }
//    }
  }
}


data class MeshTabValues(
  var MeshTabField: Int = 1
) : Bindable {
  override fun BaseSketch.bind() = section(
    intProp(::MeshTabField, 0..10)
  )
}

data class MeshGlobalValues(
  var noise: Noise = Noise(
    seed = 100,
    noiseType = Cubic,
    quality = High,
    scale = 1.0,
    offset = Point.Zero,
    strength = Point(10, 0)
  ),
  var numDots: Point = Point(10, 10),
  var size: Point = Point(0.5, 0.5),
  var dotRectCenter: Point = Point(0.5, 0.5)
) : Bindable {
  override fun BaseSketch.bind() = section(
    noiseProp(::noise),
    doublePairProp(::numDots, zeroTo(100) + 2),
    doublePairProp(::size, zeroTo(1)),
    doublePairProp(::dotRectCenter, ZeroToOne),
  )
}

fun main() = Mesh().run()
