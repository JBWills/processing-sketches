package util.easing

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

@Suppress("unused")
enum class Ease(val f: (Double) -> Double) {
  Linear({ it }),
  EaseOutSine({ sin((it * PI) / 2) }),
  EaseInSine({ 1 - cos((it * PI) / 2) }),
  EaseInOutSine({ -(cos(PI * it) - 1) / 2; }),
  EaseOutQuad({ it * it }),
  EaseInQuad({ 1 - (1 - it) * (1 - it) }),
  EaseInOutQuad({ if (it < 0.5) 2 * it * it else 1 - (-2 * it + 2).pow(2) / 2 }),
  ;
}
