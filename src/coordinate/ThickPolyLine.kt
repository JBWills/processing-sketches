package coordinate

import coordinate.InterpolationType.EaseInOut
import coordinate.InterpolationType.Linear
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import util.interpolation.interpolate
import util.iterators.uniqByInPlace
import util.numbers.ceilInt
import util.numbers.times
import util.percentAlong
import util.polylines.MutablePolyLine
import util.polylines.PolyLine
import util.polylines.iterators.WalkCursor
import util.polylines.iterators.walkWithCursor
import util.polylines.length
import util.tuple.plus
import util.tuple.sort
import util.tuple.times

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
  val centerAmount: Double = 0.0
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

    val maxAmount = processedKeys.maxByOrNull { it.amount }?.amount ?: 0.0

    val maxPolylines = (maxAmount * 2 / maxPxDistBetweenLines).ceilInt()

    val result: List<MutableList<MutablePolyLine>> =
      Array(maxPolylines) { mutableListOf(mutableListOf<Point>()) }.toList()

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

    line.walkWithCursor(step = 2) { cursor ->
      val lenSoFar = length * cursor.percent
      val normal = cursor.normal
      val point = cursor.point
      keyframeIndex = getNewKeyframeIndex(lenSoFar)

      val interpolatedKey = getInterpolatedKeyframe(keyframeIndex, lenSoFar)
      val (min, max) = (((-1.0 to 1.0) + interpolatedKey.centerAmount) * interpolatedKey.amount).sort()


      val shouldDrawMaxLine = max != 0.0
      val shouldDrawMinLine = min != 0.0

      maxPolylines.times { index ->
        if (index == 0 && shouldDrawMaxLine) {
          result.first().last().add(point + max * (normal.slope.unitVector))
          return@times
        } else if (index == maxPolylines - 1 && shouldDrawMinLine) {
          result.last().last().add(point + min * (normal.slope.unitVector))
          return@times
        }

        val curr = max - maxPxDistBetweenLines * index

        if ((min..max).contains(curr)) {
          result[index].last()
            .add((point + curr * (normal.slope.unitVector)))
        } else if (result[index].last().isNotEmpty()) {
          result[index].add(mutableListOf())
        }
      }
    }

    return if (drawOriginal) result.plusElement(listOf(line)) else result
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

    fun PolyLine.toThickLines(
      step: Double,
      maxPxDistBetweenLines: Double = MaxPxDistToDrawDistinctLines,
      minPxDistToDrawDistinctLines: Double = MinPxDistToDrawDistinctLines,
      interpolationType: InterpolationType = Linear,
      drawOriginal: Boolean = true,
      walk: (WalkCursor) -> Thickness,
    ): List<List<PolyLine>> = toThickLine(
      step,
      maxPxDistBetweenLines,
      minPxDistToDrawDistinctLines,
      interpolationType,
      drawOriginal,
      walk,
    ).toLines()

    @JvmName("toThickLinesList")
    fun List<PolyLine>.toThickLines(
      step: Double,
      maxPxDistBetweenLines: Double = MaxPxDistToDrawDistinctLines,
      minPxDistToDrawDistinctLines: Double = MinPxDistToDrawDistinctLines,
      interpolationType: InterpolationType = Linear,
      drawOriginal: Boolean = true,
      walk: (WalkCursor) -> Thickness,
    ): List<List<PolyLine>> = flatMap {
      it.toThickLines(
        step,
        maxPxDistBetweenLines,
        minPxDistToDrawDistinctLines,
        interpolationType,
        drawOriginal,
        walk,
      )
    }
  }
}
