package util.audio.extensions

import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.util.fft.FFT
import util.audio.AudioProcessGetter
import util.audio.processAudioEvents
import util.iterators.filterMap

fun AudioDispatcher.processFFT(): AudioProcessGetter<DoubleArray> {
  val result = AudioProcessGetter<DoubleArray>()

  var fft: FFT? = null

  processAudioEvents { audioEvent ->
    fft = fft ?: FFT(audioEvent.bufferSize)
    val amplitudes = FloatArray(audioEvent.bufferSize / 2)
    audioEvent.floatBuffer.let {
      fft?.forwardTransform(it)
      fft?.modulus(it, amplitudes)
    }

    result.addProcessedResult(
      amplitudes
        .filterMap(Float::isFinite, Float::toDouble)
        .toDoubleArray(),
    )
  }

  return result
}
