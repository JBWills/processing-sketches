package util

import java.awt.Color

fun String.toColor(): Color? = Color.decode(this)
fun String.toRGBInt(): Int = toColor()?.rgb ?: 0