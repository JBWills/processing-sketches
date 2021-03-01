package util.print

import util.print.StrokeWeight.Thick
import util.print.StrokeWeight.Thin
import java.awt.Color

enum class Pen(val style: Style) {
  ThickGellyWhite(Style(weight = Thick, color = Color.WHITE)),
  ThinGellyWhite(Style(weight = Thin, color = Color.WHITE)),
  ThickGellyBlack(Style(weight = Thick, color = Color.BLACK)),
}
