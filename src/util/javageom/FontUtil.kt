package util.javageom

import util.iterators.mapOf
import java.awt.Font
import java.awt.font.TextAttribute
import java.awt.font.TextAttribute.FAMILY
import java.awt.font.TextAttribute.KERNING
import java.awt.font.TextAttribute.KERNING_ON
import java.awt.font.TextAttribute.SIZE

enum class FontFamily(val family: String) {
  Arial("Arial"),
  Helvetica("Helvetica"),
}

fun getFont(family: FontFamily, size: Int, kerning: Boolean = true): Font {
  val attrs: Map<TextAttribute, Any> = mapOf(
    FAMILY to family.family,
    SIZE to size,
    if (kerning) (KERNING to KERNING_ON) else null,
  )

  return Font.getFont(attrs)
}
