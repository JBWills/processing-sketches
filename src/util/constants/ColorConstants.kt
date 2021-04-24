package util.constants

import util.iterators.extendCyclical
import java.awt.Color
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

val Purple0 = Color(30, 5, 61)
val Purple1 = Color(50, 5, 131)
val Purple2 = Color(90, 0, 220)
val Purple3 = Color(108, 0, 238)

val Red0 = Color(41, 5, 20)
val Red1 = Color(81, 5, 30)
val Red2 = Color(121, 15, 70)
val Red3 = Color(141, 5, 80)

val Green0 = Color(8, 25, 5)
val Green1 = Color(8, 51, 5)
val Green2 = Color(8, 81, 5)
val Green3 = Color(8, 101, 5)

val Blue0 = Color(8, 25, 50)
val Blue1 = Color(8, 25, 101)
val Blue2 = Color(8, 45, 131)
val Blue3 = Color(8, 45, 161)

val Orange0 = Color(41, 15, 8)
val Orange1 = Color(71, 37, 8)
val Orange2 = Color(121, 47, 8)
val Orange3 = Color(111, 42, 8)

val Yellow0 = Color(41, 25, 8)
val Yellow1 = Color(71, 65, 8)
val Yellow2 = Color(121, 100, 8)
val Yellow3 = Color(111, 90, 8)

val Gray0 = Color(29, 27, 35)
val Gray1 = Color(67, 67, 87)
val Gray2 = Color(97, 97, 107)
val Gray3 = Color(87, 87, 97)


val Black0 = Color(0, 0, 0)
val Black1 = Color(23, 24, 20)
val Black2 = Color(40, 40, 40)
val Black3 = Color(40, 40, 40)
