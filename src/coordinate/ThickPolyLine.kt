package coordinate

import coordinate.InterpolationType.EaseInOut
import coordinate.InterpolationType.Linear
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import util.interpolation.interpolate
import util.iterators.uniqByInPlace
import util.percentAlong
import util.polylines.MutablePolyLine
import util.polylines.PolyLine
import util.polylines.iterators.WalkCursor
import util.polylines.iterators.walkWithCursor
import util.polylines.length
import util.tuple.map
import util.tuple.plus

const val MinPxDistToDrawDistinctLines = 0.01
const val MaxPxDistToDrawDistinctLines = 2.0

@Suppress("unused")
enum class InterpolationType {
  Linear,
  EaseInOut,
}


@Serializable
data class Thickness(
  val amount: Double,
  /**
   * 0 = centered around original line
   * 1 = all subsequent lines above line
   * -1 = all lines drawn below line
   */
  val centerAmount: Double
)

@Serializable
data class ThicknessKey(
  val distance: Double,
  val amount: Double,
  /**
   * 0 = centered around original line
   * 1 = all subsequent lines above line
   * -1 = all lines drawn below line
   */
  val centerAmount: Double
)

@Serializable
data class ThickPolyLine(
  val line: PolyLine,
  val keys: List<ThicknessKey>,
  val maxPxDistBetweenLines: Double = MaxPxDistToDrawDistinctLines,
  val minPxDistToDrawDistinctLines: Double = MinPxDistToDrawDistinctLines,
  val interpolationType: InterpolationType = Linear,
  val drawOriginal: Boolean = true
) {
  @Suppress("UNNECESSARY_LATEINIT")
  @Transient
  lateinit var processedKeys: List<ThicknessKey>

  private val lineLength = line.length

  init {
    processedKeys = preProcessKeys(keys, lineLength)
  }

  private fun getInterpolatedKeyframe(keyframeIndex: Int, length: Double): ThicknessKey {
    if (keyframeIndex >= processedKeys.size - 1) {
      return processedKeys.last()
    }

    val curr = processedKeys[keyframeIndex]
    val next = processedKeys[keyframeIndex + 1]


    val percent = length.percentAlong(curr.distance..next.distance)

    return when (interpolationType) {
      Linear -> ThicknessKey(
        distance = length,
        amount = (curr.amount..next.amount).interpolate(percent),
        centerAmount = (curr.centerAmount..next.centerAmount).interpolate(percent),
      )
      EaseInOut -> TODO()
    }
  }

  fun toLines(): List<List<PolyLine>> {
    val length = lineLength
    val result: MutableList<MutablePolyLine> =
      mutableListOf(
        if (drawOriginal) line.toMutableList() else mutableListOf(),
        mutableListOf(),
        mutableListOf(),
      )
    var keyframeIndex = 0

    fun getNewKeyframeIndex(currLen: Double): Int {
      if (keyframeIndex >= processedKeys.size - 2) {
        return keyframeIndex
      }
      var currIndex = keyframeIndex
      var nextIndex = keyframeIndex + 1

      while (nextIndex <= processedKeys.size - 1 && processedKeys[nextIndex].distance <= currLen) {
        currIndex++
        nextIndex++
      }

      return currIndex
    }

    line.walkWithCursor(
      step = 2,

      ) { cursor ->
      val lenSoFar = length * cursor.percent
      val normal = cursor.normal
      keyframeIndex = getNewKeyframeIndex(lenSoFar)

      val interpolatedKey = getInterpolatedKeyframe(keyframeIndex, lenSoFar)
      val minMaxMoveAmount = (-1.0 to 1.0) + interpolatedKey.centerAmount
      val minMax =
        minMaxMoveAmount.map { (cursor.point + it * (normal.slope.unitVector * interpolatedKey.amount)) }

      result[1].add(minMax.first)
      result[2].add(minMax.second)
    }
    return result.map { mutableListOf(it) }
  }


  companion object {
    private fun preProcessKeys(
      keys: List<ThicknessKey>,
      maxDist: Double
    ): List<ThicknessKey> {
      val mutableKeys = keys.toMutableList()
      mutableKeys.uniqByInPlace { it.distance }
      sortKeysInPlace(mutableKeys)
      addEndpointsInPlaceIfNecessary(mutableKeys, maxDist)
      return mutableKeys
    }

    private fun sortKeysInPlace(keys: MutableList<ThicknessKey>) {
      keys.sortBy { it.distance }
    }

    private fun addEndpointsInPlaceIfNecessary(
      sortedKeys: MutableList<ThicknessKey>,
      maxDist: Double
    ) {
      if (sortedKeys.isEmpty()) {
        return
      }

      if (sortedKeys.first().distance > 0) {
        sortedKeys.add(
          0,
          ThicknessKey(0.0, 0.0, sortedKeys.first().centerAmount),
        )
      }

      if (sortedKeys.last().distance < maxDist) {
        sortedKeys.add(
          ThicknessKey(0.0, 0.0, sortedKeys.last().centerAmount),
        )
      }
    }


    fun PolyLine.toThickLine(
      numKeyFrames: Int,
      maxPxDistBetweenLines: Double = MaxPxDistToDrawDistinctLines,
      minPxDistToDrawDistinctLines: Double = MinPxDistToDrawDistinctLines,
      interpolationType: InterpolationType = Linear,
      drawOriginal: Boolean = true,
      walk: (WalkCursor) -> Thickness,
    ): ThickPolyLine {
      val len = length
      if (len == 0.0) {
        return ThickPolyLine(this, listOf())
      }

      val step = len / numKeyFrames
      val thicknessKeys = walkWithCursor(step) {
        val thickness = walk(it)

        ThicknessKey(
          distance = it.distance,
          amount = thickness.amount,
          centerAmount = thickness.centerAmount,
        )
      }

      return ThickPolyLine(
        this,
        thicknessKeys,
        maxPxDistBetweenLines,
        minPxDistToDrawDistinctLines,
        interpolationType,
        drawOriginal,
      )
    }

    fun PolyLine.toThickLine(
      step: Double,
      walk: (WalkCursor) -> Thickness,
      maxPxDistBetweenLines: Double = MaxPxDistToDrawDistinctLines,
      minPxDistToDrawDistinctLines: Double = MinPxDistToDrawDistinctLines,
      interpolationType: InterpolationType = Linear,
      drawOriginal: Boolean = true
    ): ThickPolyLine {
      val len = length
      if (len == 0.0) {
        return ThickPolyLine(this, listOf())
      }

      val thicknessKeys = walkWithCursor(step) {
        val thickness = walk(it)

        ThicknessKey(
          distance = it.distance,
          amount = thickness.amount,
          centerAmount = thickness.centerAmount,
        )
      }

      return ThickPolyLine(
        this,
        thicknessKeys,
        maxPxDistBetweenLines,
        minPxDistToDrawDistinctLines,
        interpolationType,
        drawOriginal,
      )
    }
  }
}
