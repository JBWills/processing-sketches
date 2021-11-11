package util.audio

import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.AudioEvent
import be.tarsos.dsp.AudioProcessor

fun <T> AudioDispatcher.processAudioEvents(
  block: (a: AudioEvent) -> T
): List<T> {
  val result = mutableListOf<T>()

  addAudioProcessor(
    object : AudioProcessor {
      override fun processingFinished() {}
      override fun process(audioEvent: AudioEvent): Boolean {
        result.add(block(audioEvent))
        return true
      }
    },
  )

  return result
}

