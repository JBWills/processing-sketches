package util.constants

import util.iterators.extendCyclical
import java.awt.Color.BLUE
import java.awt.Color.CYAN
import java.awt.Color.DARK_GRAY
import java.awt.Color.GRAY
import java.awt.Color.GREEN
import java.awt.Color.LIGHT_GRAY
import java.awt.Color.MAGENTA
import java.awt.Color.ORANGE
import java.awt.Color.RED
import java.awt.Color.YELLOW

val LAYER_COLORS = listOf(
  BLUE,
  RED,
  GREEN,
  LIGHT_GRAY,
  DARK_GRAY,
  ORANGE,
  YELLOW,
  CYAN,
  GRAY,
  MAGENTA,
)

fun getLayerColors(n: Int) = LAYER_COLORS.extendCyclical(n)
