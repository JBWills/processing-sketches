package util.base

import java.awt.Color

const val MinLuminanceForBlackText = 183

fun getTextColorForBackground(backgroundColor: Color): Color =
  if (backgroundColor.luminance > MinLuminanceForBlackText) Color.BLACK else Color.WHITE

