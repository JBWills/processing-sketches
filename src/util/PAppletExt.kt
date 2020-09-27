package util

import processing.core.PApplet

open class PAppletExt : PApplet() {
  fun random(low: Int, high: Int) = random(low.toFloat(), high.toFloat())
  fun random(low: Float, high: Int) = random(low, high.toFloat())
  fun random(low: Int, high: Float) = random(low.toFloat(), high)
}