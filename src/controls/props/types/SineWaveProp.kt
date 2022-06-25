package controls.props.types

import controls.controlsealedclasses.Slider.Companion.slider
import controls.panels.ControlTab
import controls.panels.TabsBuilder.Companion.singleTab
import controls.props.PropData
import kotlinx.serialization.Serializable
import kotlin.math.PI
import kotlin.math.sin

const val TwoPi = 2 * PI

const val DefaultAmplitude = 100.0
const val DefaultOffset = 0.0
const val DefaultYOffset = 0.0
const val DefaultFreq = 1.0

@Serializable
data class SineWaveProp(
  var amplitude: Double,
  var offset: Double,
  var yOffset: Double,
  var freq: Double,
) : PropData<SineWaveProp> {

  constructor(
    amplitude: Number = DefaultAmplitude,
    offset: Number = DefaultOffset,
    yOffset: Number = DefaultYOffset,
    freq: Number = DefaultFreq,
  ) : this(
    amplitude = amplitude.toDouble(),
    offset = offset.toDouble(),
    yOffset = yOffset.toDouble(),
    freq = freq.toDouble(),
  )

  constructor(
    s: SineWaveProp,
    amplitude: Number? = null,
    offset: Number? = null,
    yOffset: Number? = null,
    freq: Number? = null,
  ) : this(
    amplitude = amplitude?.toDouble() ?: s.amplitude,
    offset = offset?.toDouble() ?: s.offset,
    yOffset = yOffset?.toDouble() ?: s.yOffset,
    freq = freq?.toDouble() ?: s.freq,
  )

  override fun toSerializer() = serializer()

  override fun clone() = SineWaveProp(this)

  fun f(t: Double) = sin(freq * TwoPi * t + (offset * TwoPi)) * amplitude

  override fun bind(): List<ControlTab> = singleTab("SinWaveProp") {
    row {
      heightRatio = 0.7
      slider(::amplitude, 0.0..1000.0)
      slider(::offset, 0.0..1.0)
      slider(::freq, 0.0..20.0)
    }
  }
}
