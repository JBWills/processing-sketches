package util.print

import java.awt.Color

class Pen(val mm: Double, val color: Color) {
  constructor(color: Color) : this(1.0, color)

  companion object {
    val WhiteGellyThick = Pen(1.0, Color.WHITE)
    val WhiteGellyThin = Pen(0.5, Color.WHITE)
    val BlackGellyThick = Pen(1.0, Color.BLACK)
  }
}