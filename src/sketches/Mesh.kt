package sketches

import BaseSketch
import FastNoiseLite.NoiseType.ValueCubic
import appletExtensions.withStroke
import controls.*
import controls.ControlTab.Companion.tab
import coordinate.Point
import fastnoise.Noise
import fastnoise.NoiseQuality.High
import interfaces.Copyable
import interfaces.TabBindable
import sketches.base.LayeredCanvasSketch
import util.*
import util.print.Paper.A4Black
import util.tuple.map
import java.awt.Color

/**
 * Starter sketch that uses all of the latest bells and whistles.
 *
 * Copy and paste this to create a new sketch.
 */
class Mesh : LayeredCanvasSketch<MeshLayerData, MeshData>(
  "Mesh",
  MeshData(),
  { MeshLayerData() },
  canvas = A4Black
) {
  init {
    numLayers = 1
  }

  override fun drawSetup(layerInfo: DrawInfo) {
  }

  override fun drawOnce(values: LayerInfo) {
    val (
      noise,
      numDots,
      size,
      dotRectCenter,
      showDiagonalsDown,
      showDiagonalsUp,
      showVerticals,
      showHorizontals,
    ) = values.globalValues
    val (numDotsX, numDotsY) = (numDots.x to numDots.y).map { it.toInt() }
    val (MeshTabField) = values.tabValues

    val dotBound = boundRect.scale(size, boundRect.pointAt(dotRectCenter.x, dotRectCenter.y))

    val pointRows = numDotsX.map { xIndex ->
      val amountAlongX = (0 until numDotsX).percentAlong(xIndex)

      return@map numDotsY.map { yIndex ->
        val amountAlongY = (0 until numDotsY).percentAlong(yIndex)

        dotBound.pointAt(amountAlongX, amountAlongY)
      }
    }

    val warpedPoints = pointRows.map {
      noise.move(it)
    }

    if (isDebugMode) withStroke(Color.GREEN) { pointRows.forEach { it.drawPoints() } }


    if (showVerticals) {
      warpedPoints.forEach { row ->
        row.draw(boundRect)
      }
    }

    if (showHorizontals) {
      val flippedArray = numDotsY.map { warpedPoints.map { row -> row[it] } }

      flippedArray.forEach { it.draw(boundRect) }
    }

    if (showDiagonalsDown) {
      fun getDiagonal(startX: Int, startY: Int): List<Point> =
        min(numDotsX - startX, numDotsY - startY)
          .map {
            warpedPoints[startX + it][startY + it]
          }

      val diagonals =
        numDotsX.map { startXIndex -> getDiagonal(startXIndex, 0) } +
          (1 until numDotsY).map { startYIndex -> getDiagonal(0, startYIndex) }

      diagonals.draw(boundRect)
    }

    if (showDiagonalsUp) {
      fun getDiagonal(startX: Int, startY: Int): List<Point> =
        min(numDotsX - startX, numDotsY - startY)
          .map {
            warpedPoints[startX + it][(numDotsY - 1) - (startY + it)]
          }

      val diagonals =
        numDotsX.map { startXIndex ->
          getDiagonal(startXIndex, 0)
        } +
          (1 until numDotsY).map { startYIndex -> getDiagonal(0, startYIndex) }

      diagonals.draw(boundRect)
    }
  }
}

data class MeshLayerData(
  var MeshTabField: Int = 1,
) : TabBindable, Copyable<MeshLayerData> {
  override fun BaseSketch.bindTab() = tab(
    "T",
    intProp(::MeshTabField, 0..10)
  )

  override fun clone() = copy()
}

data class MeshData(
  var noise: Noise = Noise(
    seed = 100,
    noiseType = ValueCubic,
    quality = High,
    scale = 1.0,
    offset = Point.Zero,
    strength = Point(10, 0)
  ),
  var numDots: Point = Point(10, 10),
  var size: Point = Point(0.5, 0.5),
  var dotRectCenter: Point = Point(0.5, 0.5),
  var showDiagonalsDown: Boolean = true,
  var showDiagonalsUp: Boolean = true,
  var showVerticals: Boolean = true,
  var showHorizontals: Boolean = true,
) : TabBindable, Copyable<MeshData> {
  override fun BaseSketch.bindTab() = tab(
    "Global",
    noiseProp(::noise),
    ControlGroup(
      booleanProp(::showDiagonalsDown),
      booleanProp(::showDiagonalsUp),
      booleanProp(::showVerticals),
      booleanProp(::showHorizontals)
    ),
    doublePairProp(::numDots, zeroTo(1000) + 2),
    doublePairProp(::size, zeroTo(1.5)),
    doublePairProp(::dotRectCenter, ZeroToOne),
  )

  override fun clone() = copy(
    numDots = numDots.copy(),
    noise = noise.copy(),
    size = size.copy(),
    dotRectCenter = dotRectCenter.copy()
  )
}

fun main() = Mesh().run()
