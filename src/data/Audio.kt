package data

import arrow.core.memoize
import kotlinx.serialization.Serializable
import util.audio.getFeatures
import java.io.File

val getFeaturesMemo: (String, Int) -> AudioFeatures? = { filename: String, sampleSize: Int ->
  try {
    getFeatures(File(filename), sampleSize = sampleSize)
  } catch (e: Exception) {
    println("Error getting audio features: ${e.message}")
    null
  }
}.memoize()

@Serializable
data class Audio(
  var filename: String? = null,
  var startPercent: Double = 0.0,
  var endPercent: Double = 1.0
) {
  constructor(startPercent: Number, endPercent: Number) : this(
    null,
    startPercent.toDouble(),
    endPercent.toDouble(),
  )

  fun getFeatures(sampleSize: Int): AudioFeatures? =
    filename?.let { getFeaturesMemo(it, sampleSize) }
}
