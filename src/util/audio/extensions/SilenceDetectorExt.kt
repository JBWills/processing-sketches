import be.tarsos.dsp.AudioEvent
import be.tarsos.dsp.SilenceDetector

fun SilenceDetector.getPressure(audioEvent: AudioEvent): Double {
  process(audioEvent)
  return currentSPL()
}
