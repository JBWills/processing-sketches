package util.print

import java.awt.Color

enum class Pen(val style: Style) {
  GellyWhite(Style(weight = Thick(), color = Color.WHITE)),
  GellyBlack(Style(weight = Thick(), color = Color.BLACK)),
  MicronBlack(Style(weight = Thin(), color = Color.BLACK)),
  MicronBrown(Style(weight = Thin(), color = Color(128, 38, 32))),
  MicronGreen(Style(weight = Thin(), color = Color(128, 38, 32))),
}
