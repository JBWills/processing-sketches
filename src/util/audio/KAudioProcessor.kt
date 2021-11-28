package util.audio

import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.AudioEvent
import be.tarsos.dsp.AudioProcessor

fun <T> AudioDispatcher.processAudioEvents(
  block: (a: AudioEvent) -> T
): AudioProcessGetter<T> {
  val result = AudioProcessGetter<T>()

  addAudioProcessor(
    object : AudioProcessor {
      override fun processingFinished() {}
      override fun process(audioEvent: AudioEvent): Boolean {
        result.addProcessedResult(block(audioEvent))
        return true
      }
    },
  )

  return result
}

