package util.audio

import be.tarsos.dsp.SilenceDetector
import data.AudioFeatures
import getPressure
import util.audio.extensions.processPitch
import java.io.File

/**
 * Most of this code comes from the example in https://github.com/JorenSix/TarsosDSP/blob/master/src/examples/be/tarsos/dsp/example/FeatureExtractor.java
 *
 * @param f
 * @param sampleSize if the entire file has 100 samples, and your sampleSize is 2, the result
 *    will have length 50, because we calculate the soundpressure once at each samplesize
 *    chunk.
 * @return an AudioFeatures object with pitches and pressures
 */
fun getFeatures(f: File, sampleSize: Int = DefaultSampleSize): AudioFeatures {
  val silenceDetector = SilenceDetector()
  return f.toAudioDispatcher(sampleSize = sampleSize).let {
    val pressures = it.processAudioEvents(silenceDetector::getPressure)
    val pitches = it.processPitch(sampleSize = sampleSize)
    it.run()

    AudioFeatures(
      pressures = pressures.toDoubleArray(),
      pitches = pitches.toDoubleArray(),
      sampleSize,
    )
  }
}
