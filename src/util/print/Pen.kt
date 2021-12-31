package util.print

import arrow.core.memoize
import util.print.PenBrand.Gelly
import util.print.PenBrand.Micron
import util.print.PenType.GellyColor
import util.print.PenType.GellyGold
import util.print.PenType.GellyMetallic
import util.print.PenType.GellySilver
import util.print.PenType.GellySparkle
import util.print.PenType.MicronBrush
import util.print.PenType.MicronPen
import java.awt.Color

@Suppress("unused")
enum class Pen(
  val brand: PenBrand,
  val type: PenType,
  val colorName: String,
  val color: Color,
  vararg val weights: StrokeWeight
) {
  GellyMetallicCopper(Gelly, GellyMetallic, "copper", Color(0xD9AB8C), Gelly1()),
  GellyMetallicSilver(Gelly, GellyMetallic, "silver", Color(0xD5D0D3), Gelly1()),
  GellyMetallicGold(Gelly, GellyMetallic, "gold", Color(0xE0BC7F), Gelly1()),
  GellySparkleBlue(Gelly, GellySparkle, "blue", Color(0x81BDD9), Gelly1()),
  GellySparkleGreen(Gelly, GellySparkle, "green", Color(0x90C47D), Gelly1()),
  GellySparkleYellow(Gelly, GellySparkle, "yellow", Color(0xE3CF63), Gelly1()),
  GellySparkleRed(Gelly, GellySparkle, "red", Color(0xF07F67), Gelly1()),
  GellySparklePurple(Gelly, GellySparkle, "purple", Color(0xA886A1), Gelly1()),
  GellySparkleMagenta(Gelly, GellySparkle, "magenta", Color(0xE4788A), Gelly1()),
  GellyGoldBrownTint(Gelly, GellyGold, "brown tint", Color(0xCCAE73), Gelly1()),
  GellyGoldRedTint(Gelly, GellyGold, "red tint", Color(0xE2A475), Gelly1()),
  GellyGoldBlueTint(Gelly, GellyGold, "blue tint", Color(0xB4A28C), Gelly1()),
  GellyGoldPurpleTint(Gelly, GellyGold, "purple tint", Color(0xCC9669), Gelly1()),
  GellySilverGreenTint(Gelly, GellySilver, "green tint", Color(0xD4CAB5), Gelly1()),
  GellySilverBlueTint(Gelly, GellySilver, "blue tint", Color(0xC6B6B0), Gelly1()),
  GellySilverPurpleTint(Gelly, GellySilver, "purple tint", Color(0xE0CFB8), Gelly1()),
  GellySilverRedTint(Gelly, GellySilver, "red tint", Color(0xD8B59E), Gelly1()),
  GellyColorBlack(Gelly, GellyColor, "black", Color(0x433F3E), Gelly08()),
  GellyColorWhite(Gelly, GellyColor, "white", Color(0xEEEEEE), Gelly08()),
  GellyColorBlue(Gelly, GellyColor, "blue", Color(0x3A5BAA), Gelly06()),
  GellyColorTurquoise(Gelly, GellyColor, "turquoise", Color(0x0090DA), Gelly06(), Gelly1()),
  GellyColorGreenBlue(Gelly, GellyColor, "green blue", Color(0x29969E), Gelly06(), Gelly1()),
  GellyColorSalmon(
    Gelly,
    GellyColor,
    "salmon",
    Color(0xFF5844),
    Gelly06(),
    Gelly1(),
  ),
  GellyColorPink(
    Gelly,
    GellyColor,
    "pink",
    Color(0xFF7D90),
    Gelly06(),
    Gelly1(),
  ),
  GellyColorLimeGreen(Gelly, GellyColor, "lime green", Color(0xC6BC46), Gelly06()),
  GellyColorNeonOrange(
    Gelly,
    GellyColor,
    "neon orange",
    Color(0xFF8572),
    Gelly06(),
    Gelly1(),
  ),
  GellyColorPeach(
    Gelly,
    GellyColor,
    "peach",
    Color(0xFFA360),
    Gelly06(),
    Gelly1(),
  ),
  GellyColorLightBlue(Gelly, GellyColor, "light blue", Color(0x95C9E6), Gelly06()),
  GellyColorDarkGray(Gelly, GellyColor, "dark gray", Color(0x777376), Gelly06()),
  GellyColorMedGray(Gelly, GellyColor, "med gray", Color(0xAA9A92), Gelly06()),
  GellyColorDarkBlue(Gelly, GellyColor, "dark blue", Color(0x2B54B9), Gelly06()),
  GellyColorDarkerGray(Gelly, GellyColor, "darker gray", Color(0x796654), Gelly06()),
  GellyColorMedDarkGray(Gelly, GellyColor, "med dark gray", Color(0x918C84), Gelly06()),
  GellyColorPurple(
    Gelly,
    GellyColor,
    "purple",
    Color(0x9C6EB3),
    Gelly06(),
    Gelly1(),
  ),
  GellyColorLightGray(Gelly, GellyColor, "light gray", Color(0xD2C0AF), Gelly06()),
  GellyColorLightPurple(Gelly, GellyColor, "light purple", Color(0xC398BC), Gelly06()),
  GellyColorDarkPeach(Gelly, GellyColor, "dark peach", Color(0xD6764F), Gelly06()),
  GellyColorNeonYellow(
    Gelly,
    GellyColor,
    "neon yellow",
    Color(0xFCF375),
    Gelly06(),
    Gelly1(),
  ),
  GellyColorNeonGreen(
    Gelly,
    GellyColor,
    "neon green",
    Color(0xA7D281),
    Gelly06(),
    Gelly1(),
  ),
  GellyColorMedLightGray(Gelly, GellyColor, "med light gray", Color(0xBFB3A0), Gelly06()),
  GellyColorTaupe(Gelly, GellyColor, "taupe", Color(0x8A564D), Gelly06()),
  GellyColorDarkPink(Gelly, GellyColor, "dark pink", Color(0xE46E93), Gelly06()),
  GellyColorMustard(Gelly, GellyColor, "mustard", Color(0xBC8847), Gelly06()),
  GellyColorNeonTurquoise(Gelly, GellyColor, "neon turquoise", Color(0x00B3A9), Gelly06()),
  GellyColorSeaGreen(Gelly, GellyColor, "sea green", Color(0x7AAA63), Gelly06()),
  GellyColorMagenta(Gelly, GellyColor, "magenta", Color(0xDF4B73), Gelly1()),

  MicronMicronBlack(
    Micron,
    MicronPen,
    "black",
    Color(0x474039),
    Micron003(),
    Micron005(),
    Micron01(),
    Micron02(),
    Micron03(),
    Micron05(),
    Micron08(),
    Micron1(),
    Micron2(),
    Micron3(),
  ),
  MicronMicronOrange(
    Micron,
    MicronPen,
    "orange",
    Color(0xFF9105),
    Micron01(),
    Micron005(),
  ),
  MicronMicronGreen(
    Micron,
    MicronPen,
    "green",
    Color(0x00A6A1),
    Micron005(),
    Micron01(),
  ),
  MicronMicronPink(Micron, MicronPen, "pink", Color(0xE9608C), Micron01()),
  MicronMicronBrown(
    Micron,
    MicronPen,
    "brown",
    Color(0xCD6D35),
    Micron005(),
    Micron01(),
  ),
  MicronMicronRed(
    Micron,
    MicronPen,
    "red",
    Color(0xFF6E47),
    Micron005(),
    Micron01(),
  ),
  MicronMicronPurple(Micron, MicronPen, "purple", Color(0x6D4674), Micron01()),
  MicronMicronDarkBrown(
    Micron,
    MicronPen,
    "dark brown",
    Color(0x79583B),
    Micron005(),
    Micron01(),
  ),

  MicronMicronBrushBlack(Micron, MicronBrush, "black", Color(0x474039), MicronBR()),
  MicronMicronBrushGreen(Micron, MicronBrush, "green", Color(0x00A6A1), MicronBR()),
  MicronMicronBrushBrown(Micron, MicronBrush, "brown", Color(0xCD6D35), MicronBR()),
  MicronMicronBrushRed(Micron, MicronBrush, "red", Color(0xFF6E47), MicronBR()),
  MicronMicronBrushDarkBrown(Micron, MicronBrush, "dark brown", Color(0x79583B), MicronBR()),
  ;

  val style get() = Style(color = color)

  companion object {
    private val withTypeMemo: (PenType) -> List<Pen> = { penType: PenType ->
      values().filter { it.type === penType }
    }.memoize()

    fun withType(penType: PenType): List<Pen> = withTypeMemo(penType)

    private val withThicknessMemo: (thickness: StrokeWeight) -> List<Pen> =
      { thickness: StrokeWeight -> values().filter { it.weights.contains(thickness) } }.memoize()

    fun withThickness(thickness: StrokeWeight): List<Pen> = withThicknessMemo(thickness)
  }
}
