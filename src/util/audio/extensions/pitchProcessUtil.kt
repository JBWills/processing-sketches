package util.audio.extensions

import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.pitch.PitchProcessor
import be.tarsos.dsp.pitch.PitchProcessor.PitchEstimationAlgorithm
import util.audio.DefaultSampleSize

fun AudioDispatcher.processPitch(
  algo: PitchEstimationAlgorithm = PitchEstimationAlgorithm.FFT_YIN,
  sampleSize: Int = DefaultSampleSize,
): List<Double> {
  val result = mutableListOf<Double>()

  addAudioProcessor(
    PitchProcessor(algo, format.sampleRate, sampleSize) { pitch, event ->
      result.add(pitch.pitch.toDouble())
    },
  )

  return result
}
