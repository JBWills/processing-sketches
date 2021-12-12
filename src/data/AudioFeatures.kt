package data

import kotlinx.serialization.Serializable
import util.audio.DefaultSampleSize

@Serializable
data class AudioFeatures(
  val pressures: DoubleArray,
  val pitches: Array<DoubleArray>,
  val sampleSize: Int = DefaultSampleSize
) {

  fun pressuresFrom(indices: IntRange): DoubleArray {
    val safeLast = indices.last.coerceAtMost(pressures.size - 1)
    return pressures.sliceArray(indices.first..safeLast)
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as AudioFeatures

    if (!pressures.contentEquals(other.pressures)) return false
    if (!pitches.contentEquals(other.pitches)) return false
    if (sampleSize != other.sampleSize) return false

    return true
  }

  override fun hashCode(): Int {
    var result = pressures.contentHashCode()
    result = 31 * result + pitches.contentHashCode()
    result = 31 * result + sampleSize
    return result
  }

  override fun toString(): String {
    return "AudioFeatures(\n    pressures=Array(${(pressures.size)}),\n    pitches=Array(${(pitches.size)}),\n    sampleSize=$sampleSize\n)"
  }
}
