package util.audio.extensions

import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.pitch.PitchProcessor
import be.tarsos.dsp.pitch.PitchProcessor.PitchEstimationAlgorithm
import util.audio.AudioProcessGetter
import util.audio.DefaultSampleSize

fun AudioDispatcher.processPitch(
  algo: PitchEstimationAlgorithm = PitchEstimationAlgorithm.FFT_YIN,
  sampleSize: Int = DefaultSampleSize,
): AudioProcessGetter<Double> {
  val result = AudioProcessGetter<Double>()

  addAudioProcessor(
    PitchProcessor(algo, format.sampleRate, sampleSize) { pitch, _ ->
      result.addProcessedResult(pitch.pitch.toDouble())
    },
  )

  return result
}
